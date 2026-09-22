package com.advancedjava.redis;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;
import redis.clients.jedis.search.schemafields.VectorField.VectorAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 演示 8：Redis 集成的向量库能力（RediSearch + RedisJSON）。
 *
 * "Redis 当向量库"不是比喻，是官方模块栈：
 * - RedisJSON：文档型存储（JSON.SET / JSON.GET），向量的宿主数据结构；
 * - RediSearch：FT.CREATE 建索引、FT.SEARCH 查询；VECTOR 字段类型支持 HNSW / FLAT
 *   两种算法与 COSINE / L2 / IP 距离——这就是语义检索的底座。
 *
 * 运行环境需要 redis-stack 镜像（普通 redis 镜像不带 search/json 模块）：
 *   docker run -d --name redis-demo -p 6379:6379 redis/redis-stack-server:latest
 *
 * KNN 检索流程（面试版一句话）：文本经 embedding 模型变成高维向量存进 VECTOR 字段，
 * 查询时把问题也变成向量，服务端按距离返回最近的 topK——匹配"意思"而非关键词。
 * 本 Demo 用手工构造的 4 维玩具向量演示全流程，生产里向量来自 OpenAI/DashScope 等
 * embedding API（工程已接 spring-ai-openai）。
 *
 * 两个必须知道的坑（本 Demo 踩过并修好）：
 * 1. KNN 语法 `*=>[KNN k @field $vec AS score]` 需要 DIALECT 2，
 *    Jedis 里必须显式 .dialect(2)，否则报 "Syntax error at offset 1 near >["；
 * 2. 查询向量要以 FLOAT32 小端字节序传（见 toFloatBytes），而 JSON 里存的是数字数组。
 *
 * 【部署形态提示】Cluster：FT.* 以索引名做路由，RediSearch 集群版自行维护分片内索引；
 * 主从上索引定义随 RDB/AOF 复制，replica 提升后 FT.SEARCH 可直接用。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.VectorSearchDemo
 */
public class VectorSearchDemo {

    private static final String INDEX = "demo-vector-index";
    private static final String KEY_PREFIX = "demo:doc:";

    public static void main(String[] args) throws Exception {
        // JedisPooled：线程安全的连接池客户端；jsonSet 的对象序列化依赖 gson（jedis 传递依赖）
        try (JedisPooled jedis = new JedisPooled("127.0.0.1", 6379)) {

            // ---------- 0) 前置检查：search/json 模块是否可用 ----------
            if (!modulesAvailable(jedis)) {
                System.out.println("当前 Redis 未加载 search/json 模块。请换容器：\n"
                        + "  docker rm -f redis-demo && docker run -d --name redis-demo -p 6379:6379 redis/redis-stack-server:latest");
                return;
            }

            // ---------- 1) 清理旧数据并写入 JSON 文档 ----------
            try {
                jedis.ftDropIndex(INDEX);
            } catch (RuntimeException ignored) {
                // 首次运行索引不存在
            }
            for (int i = 1; i <= 4; i++) {
                jedis.jsonDel(KEY_PREFIX + i);
            }

            // 玩具语义空间：4 维依次代表 [动物性, 食物性, 运动感, 科技感]
            // 真实场景是 1536+ 维、由 embedding 模型输出
            writeDoc(jedis, 1, "金毛犬在公园奔跑", "pet", new float[]{0.9f, 0.1f, 0.8f, 0.0f});
            writeDoc(jedis, 2, "红烧肉的做法", "food", new float[]{0.1f, 0.9f, 0.1f, 0.0f});
            writeDoc(jedis, 3, "机器学习的向量检索", "tech", new float[]{0.0f, 0.1f, 0.2f, 0.9f});
            writeDoc(jedis, 4, "宠物猫和狗粮", "pet", new float[]{0.8f, 0.3f, 0.2f, 0.0f});
            System.out.println("已写入 4 篇文档到 RedisJSON");

            // ---------- 2) FT.CREATE：JSON 文档上建 HNSW 向量索引 ----------
            // ON JSON + PREFIX 圈定哪些 key 归这个索引管；三种字段类型各来一个
            jedis.ftCreate(INDEX,
                    new FTCreateParams().on(IndexDataType.JSON).addPrefix(KEY_PREFIX),
                    List.of(
                            TextField.of("$.title").as("title"),          // 关键词检索用
                            TagField.of("$.category").as("category"),     // 元数据过滤用（不分词）
                            VectorField.builder()
                                    .fieldName("$.vector").as("vector")   // KNN 检索用
                                    .algorithm(VectorAlgorithm.HNSW)
                                    .attributes(vectorAttrs())
                                    .build()));
            System.out.println("FT.CREATE 完成，等待模块回填索引...");
            Thread.sleep(800);   // 简单起见睡一下；生产中轮询 FT.INFO 的 indexing 状态

            // ---------- 3) 纯向量 KNN：「狗在奔跑」找最近邻 ----------
            // 注意 dialect(2)：KNN 的 => 语法属于 dialect 2，缺了它直接语法报错
            SearchResult knn = jedis.ftSearch(INDEX,
                    new Query("*=>[KNN 2 @vector $vec AS score]")
                            .addParam("vec", toFloatBytes(new float[]{0.85f, 0.15f, 0.7f, 0.05f}))
                            .dialect(2)
                            .returnFields("title", "category", "score"));
            System.out.println("\n① 查询「狗在奔跑」top2（score 是余弦距离，越小越像）:");
            printHits(knn);

            // ---------- 4) 混合检索：先按标签过滤，再算近邻 ----------
            // 这种 "metadata filter + vector search" 是 RAG 里最常用的形态
            SearchResult hybrid = jedis.ftSearch(INDEX,
                    new Query("@category:{tech}=>[KNN 2 @vector $vec AS score]")
                            .addParam("vec", toFloatBytes(new float[]{0.0f, 0.1f, 0.2f, 0.9f}))
                            .dialect(2)
                            .returnFields("title", "category", "score"));
            System.out.println("\n② 只用 category=tech 的文档做 KNN（过滤掉宠物/美食）:");
            printHits(hybrid);

            // ---------- 5) 对照：纯关键词检索 ----------
            SearchResult keyword = jedis.ftSearch(INDEX,
                    new Query("@title:狗").returnFields("title", "category"));
            System.out.println("\n③ 关键词检索 @title:狗 —— 中文默认不分词，整句是一个 token，命中 "
                    + keyword.getTotalResults() + " 条:");
            printHits(keyword);

            SearchResult prefix = jedis.ftSearch(INDEX,
                    new Query("@title:金毛犬*").returnFields("title", "category"));
            System.out.println("\n④ 关键词前缀检索 @title:金毛犬* —— 必须字面命中开头才行:");
            printHits(prefix);

            System.out.println("\n结论：向量检索能把语义相近的「宠物猫和狗粮」一起召回，关键词检索做不到"
                    + "——这就是语义检索的价值，也是 RAG 选它的原因。");
        }
    }

    /** 写一个 JSON 文档：title + category + 向量数组。 */
    private static void writeDoc(JedisPooled jedis, int id, String title, String category, float[] vec) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", String.valueOf(id));
        doc.put("title", title);
        doc.put("category", category);
        doc.put("vector", vec);
        // jsonSetWithEscape：新版 stack 对 JSON.SET 的 path 校验更严，转义版兼容性更好
        jedis.jsonSetWithEscape(KEY_PREFIX + id, Path2.ROOT_PATH, doc);
    }

    /** HNSW 参数：FLOAT32 / DIM=4（玩具维度）/ COSINE；M 与 EF_CONSTRUCTION 调"精度 vs 内存/构建耗时"。 */
    private static Map<String, Object> vectorAttrs() {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("TYPE", "FLOAT32");
        attrs.put("DIM", 4);
        attrs.put("DISTANCE_METRIC", "COSINE");
        attrs.put("M", 16);
        attrs.put("EF_CONSTRUCTION", 100);
        return attrs;
    }

    /** KNN 的查询向量必须按 FLOAT32 小端字节序传（JSON 里则是普通数字数组）。 */
    private static byte[] toFloatBytes(float[] vec) {
        byte[] bytes = new byte[vec.length * 4];
        for (int i = 0; i < vec.length; i++) {
            int bits = Float.floatToIntBits(vec[i]);
            bytes[i * 4] = (byte) (bits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((bits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((bits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((bits >> 24) & 0xFF);
        }
        return bytes;
    }

    /** 探测 search 模块是否加载：命令不存在才是"没模块"。 */
    private static boolean modulesAvailable(JedisPooled jedis) {
        try {
            jedis.ftDropIndex("demo-probe-nonexistent");
            return true;
        } catch (RuntimeException e) {
            String msg = String.valueOf(e.getMessage());
            return !msg.contains("unknown command");
        }
    }

    private static void printHits(SearchResult result) {
        System.out.println("  命中 " + result.getTotalResults() + " 条");
        result.getDocuments().forEach(d ->
                System.out.println("  - " + d.getId() + " " + d.getProperties()));
    }
}
