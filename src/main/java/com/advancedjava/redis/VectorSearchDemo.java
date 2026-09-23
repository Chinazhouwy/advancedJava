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
 * 演示 8：Redis 向量库基本用法（RediSearch KNN + RedisJSON）。
 *
 * 流程只有三步：
 * 1. JSON.SET 写入带向量的文档；
 * 2. FT.CREATE 在 JSON 上建 HNSW 向量索引；
 * 3. FT.SEARCH 用 KNN 语句做相似度检索（可叠加元数据过滤）。
 *
 * 运行环境：Redis 8 官方镜像已内置 search / ReJSON 模块：
 *   docker run -d --name redis-demo -p 6379:6379 redis:8-alpine
 *
 * 两个必踩的坑：
 * 1. KNN 的 {@code =>} 语法属于 DIALECT 2，Jedis 必须显式 .dialect(2)，否则语法报错；
 * 2. 查询向量按 FLOAT32 小端字节序传（见 toFloatBytes），JSON 里存的则是数字数组。
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.VectorSearchDemo
 */
public class VectorSearchDemo {

    private static final String INDEX = "demo-vector-index";
    private static final String KEY_PREFIX = "demo:doc:";

    public static void main(String[] args) throws Exception {
        try (JedisPooled jedis = new JedisPooled("127.0.0.1", 6379)) {

            // ---------- 1) 写入 JSON 文档（title + category + 向量） ----------
            // 玩具语义空间：4 维依次代表 [动物性, 食物性, 运动感, 科技感]
            // 真实场景是 1536+ 维、由 embedding 模型输出
            writeDoc(jedis, 1, "金毛犬在公园奔跑", "pet", new float[]{0.9f, 0.1f, 0.8f, 0.0f});
            writeDoc(jedis, 2, "红烧肉的做法", "food", new float[]{0.1f, 0.9f, 0.1f, 0.0f});
            writeDoc(jedis, 3, "机器学习的向量检索", "tech", new float[]{0.0f, 0.1f, 0.2f, 0.9f});
            writeDoc(jedis, 4, "宠物猫和狗粮", "pet", new float[]{0.8f, 0.3f, 0.2f, 0.0f});
            System.out.println("已写入 4 篇文档");

            // ---------- 2) 建索引（重复运行先删旧索引） ----------
            dropIndexQuietly(jedis);
            jedis.ftCreate(INDEX,
                    new FTCreateParams().on(IndexDataType.JSON).addPrefix(KEY_PREFIX),
                    List.of(
                            TextField.of("$.title").as("title"),
                            TagField.of("$.category").as("category"),
                            VectorField.builder()
                                    .fieldName("$.vector").as("vector")
                                    .algorithm(VectorAlgorithm.HNSW)
                                    .attributes(Map.of(
                                            "TYPE", "FLOAT32",
                                            "DIM", 4,
                                            "DISTANCE_METRIC", "COSINE"))
                                    .build()));
            Thread.sleep(800);   // 等模块回填索引；生产中轮询 FT.INFO 的 indexing 状态
            System.out.println("FT.CREATE 完成");

            // ---------- 3) 纯向量 KNN：「狗在奔跑」找 top2 ----------
            SearchResult knn = jedis.ftSearch(INDEX,
                    new Query("*=>[KNN 2 @vector $vec AS score]")
                            .addParam("vec", toFloatBytes(new float[]{0.85f, 0.15f, 0.7f, 0.05f}))
                            .dialect(2)
                            .returnFields("title", "category", "score"));
            System.out.println("\n① KNN 查询「狗在奔跑」top2（score 是余弦距离，越小越像）:");
            printHits(knn);

            // ---------- 4) 混合检索：先按标签过滤，再算近邻（RAG 常用形态） ----------
            SearchResult hybrid = jedis.ftSearch(INDEX,
                    new Query("@category:{tech}=>[KNN 2 @vector $vec AS score]")
                            .addParam("vec", toFloatBytes(new float[]{0.0f, 0.1f, 0.2f, 0.9f}))
                            .dialect(2)
                            .returnFields("title", "category", "score"));
            System.out.println("\n② 只在 category=tech 里做 KNN:");
            printHits(hybrid);
        }
    }

    /** 写一个 JSON 文档：title + category + 向量数组。 */
    private static void writeDoc(JedisPooled jedis, int id, String title, String category, float[] vec) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", String.valueOf(id));
        doc.put("title", title);
        doc.put("category", category);
        doc.put("vector", vec);
        // jsonSetWithEscape：新版对 JSON.SET 的 path 校验更严，转义版兼容性更好
        jedis.jsonSetWithEscape(KEY_PREFIX + id, Path2.ROOT_PATH, doc);
    }

    /** 重复运行时索引已存在会报错，静默删掉即可（文档 key 不删，直接覆盖写）。 */
    private static void dropIndexQuietly(JedisPooled jedis) {
        try {
            jedis.ftDropIndex(INDEX);
        } catch (RuntimeException ignored) {
            // 首次运行索引不存在
        }
    }

    /** KNN 查询向量必须按 FLOAT32 小端字节序传。 */
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

    private static void printHits(SearchResult result) {
        System.out.println("  命中 " + result.getTotalResults() + " 条");
        result.getDocuments().forEach(d ->
                System.out.println("  - " + d.getId() + " " + d.getProperties()));
    }
}
