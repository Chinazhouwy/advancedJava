package com.advancedjava.redis;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import redis.clients.jedis.JedisPooled;

import java.util.List;
import java.util.Map;

/**
 * 向量检索的「封装后」写法：Spring AI VectorStore。
 *
 * 对比 {@link VectorSearchDemo}（裸 Jedis + FT.SEARCH），这里看不到任何：
 * FT.CREATE / DIALECT 2 / FLOAT32 字节序 / DIM / HNSW 参数——全部由封装层处理。
 *
 * 分工：
 * - EmbeddingModel：文本 → 向量（本 demo 用本地 Ollama + all-minilm，384 维）；
 * - VectorStore：存文档、建索引、KNN + 过滤检索，一行一个动作。
 *
 * 本地准备（只需一次）：
 *   ollama pull all-minilm && ollama serve
 *   docker 容器 redis-demo 已在 6379 运行
 *
 * 运行：mvn exec:java -Dexec.mainClass=com.advancedjava.redis.VectorStoreSimpleDemo
 */
public class VectorStoreSimpleDemo {

    public static void main(String[] args) throws Exception {
        // ---------- 1) embedding 模型：本地 Ollama（OpenAI 兼容协议） ----------
        OpenAiApi ollamaApi = OpenAiApi.builder()
                .baseUrl("http://localhost:11434")     // 会自动拼 /v1/embeddings
                .apiKey("ollama")                      // Ollama 不校验，占位即可
                .build();
        EmbeddingModel embeddingModel = new OpenAiEmbeddingModel(
                ollamaApi,
                MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder()
                        .model("all-minilm")           // 384 维，约 44MB
                        .build());

        // ---------- 2) 向量库：FT.CREATE / HNSW / DIM 全部自动 ----------
        try (JedisPooled jedis = new JedisPooled("127.0.0.1", 6379)) {
            RedisVectorStore vectorStore = RedisVectorStore.builder(jedis, embeddingModel)
                    .indexName("demo-ai-vector")
                    .metadataFields(List.of(
                            RedisVectorStore.MetadataField.tag("category")))  // 过滤字段要声明
                    .initializeSchema(true)             // 首次自动建索引
                    .build();
            vectorStore.afterPropertiesSet();

            // ---------- 3) 写入：只给「文本 + 元数据」，向量是模型算的 ----------
            vectorStore.add(List.of(
                    Document.builder().text("金毛犬在公园奔跑")
                            .metadata(Map.of("category", "pet")).build(),
                    Document.builder().text("红烧肉的做法")
                            .metadata(Map.of("category", "food")).build(),
                    Document.builder().text("机器学习的向量检索")
                            .metadata(Map.of("category", "tech")).build(),
                    Document.builder().text("宠物猫和狗粮")
                            .metadata(Map.of("category", "pet")).build()));
            System.out.println("已写入 4 篇文档（embedding 由本地模型计算）");

            // ---------- 4) 语义检索：没有 => / KNN / dialect ----------
            System.out.println("\n① 语义检索「狗在奔跑」top2:");
            print(vectorStore.similaritySearch(
                    SearchRequest.builder().query("狗在奔跑").topK(2).build()));

            // ---------- 5) 混合检索：filterExpression 代替 @category:{tech}=>[KNN...] ----------
            System.out.println("\n② 只在 category=tech 里语义检索:");
            print(vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query("向量数据库怎么用")
                            .topK(2)
                            .filterExpression("category == 'tech'")
                            .build()));
        }
    }

    private static void print(List<Document> hits) {
        if (hits.isEmpty()) {
            System.out.println("  (无命中)");
            return;
        }
        hits.forEach(d -> System.out.printf("  - %s  score=%.4f  category=%s%n",
                d.getText(), d.getScore(), d.getMetadata().get("category")));
    }
}
