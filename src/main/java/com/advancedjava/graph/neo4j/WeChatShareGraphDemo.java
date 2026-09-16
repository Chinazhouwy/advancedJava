package com.advancedjava.graph.neo4j;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;

import java.util.List;
import java.util.Map;

/**
 * 微信分享关系图 Demo：查询业务员到潜在客户的最短人脉路径。
 *
 * <p>关系方向表示“谁把页面分享给了谁”，停留时长表示被分享人的兴趣信号。
 * 最短路径按分享关系的跳数计算，停留时长只用于筛选和排序，不混入图距离。</p>
 */
public final class WeChatShareGraphDemo {

    // Neo4j 默认数据库名称。单机 Demo 使用 neo4j，生产环境可能使用其他数据库名。
    private static final String DATABASE = "neo4j";

    // Bolt 是 Neo4j Java Driver 使用的连接协议，7687 是 Neo4j 默认 Bolt 端口。
    private static final String DEFAULT_URI = "bolt://localhost:7687";
    private static final String DEFAULT_USER = "neo4j";

    // 页面停留至少 60 秒，才把这次分享链路当作“有一定意向”的候选结果。
    // 这个阈值只是 Demo 参数，真实项目应该通过历史数据分析得到。
    private static final long INTEREST_THRESHOLD_SECONDS = 60L;

    // 工具类不需要创建对象，因此把构造方法设为 private。
    private WeChatShareGraphDemo() {
    }

    public static void main(String[] args) {
        // URI 和用户名有本地默认值，密码必须从环境变量读取，避免写进源码。
        String uri = envOrDefault("NEO4J_URI", DEFAULT_URI);
        String user = envOrDefault("NEO4J_USER", DEFAULT_USER);
        String password = requiredEnv("NEO4J_PASSWORD");

        // Driver 负责连接池和底层网络连接，通常整个应用只创建一个 Driver。
        // try-with-resources 会在程序结束时自动关闭 Driver，避免连接泄漏。
        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password))) {
            // 先验证连接，连接失败时尽早给出错误，而不是执行到 Cypher 时才失败。
            driver.verifyConnectivity();

            // Session 表示一次数据库会话。这里明确连接 neo4j 数据库。
            try (Session session = driver.session(SessionConfig.forDatabase(DATABASE))) {
                // 1. 创建约束；2. 清理旧 Demo 数据；3. 写入本次演示数据。
                createSchema(session);
                resetDemoData(session);
                seedDemoData(session);

                // 从业务员 sales-001 出发，只保留停留时间达到阈值的潜在客户。
                List<PathResult> results = findShortestPaths(
                        session,
                        "sales-001",
                        INTEREST_THRESHOLD_SECONDS
                );
                printResults(results);
            }
        } catch (Exception exception) {
            System.err.println("Neo4j Demo 运行失败：" + exception.getMessage());
            System.err.println("请先启动 dev/neo4j/compose.yml 中的 Neo4j，再重新运行本类。");
            throw exception;
        }
    }

    private static void createSchema(Session session) {
        // 节点的 label 是 Person，id 是业务主键。
        // 约束保证同一个人不会因为重复导入而产生多个相同 id 的节点。
        // IF NOT EXISTS 让程序可以重复运行，不会因为约束已经存在而报错。
        session.run("""
                CREATE CONSTRAINT person_id IF NOT EXISTS
                FOR (person:Person) REQUIRE person.id IS UNIQUE
                """).consume();
    }

    /** Demo 可重复运行，所以只清理本 Demo 使用的节点。生产环境不能照搬。 */
    private static void resetDemoData(Session session) {
        // DETACH DELETE 会先删除关系，再删除节点，否则有关系连接的节点不能直接删除。
        // 这里按 Person label 清理，是为了让 Demo 每次运行都得到固定结果。
        session.run("MATCH (person:Person) DETACH DELETE person").consume();
    }

    private static void seedDemoData(Session session) {
        // 图数据库中的节点：
        // SALES = 业务员，SHARER = 中间转发人，PROSPECT = 潜在客户。
        List<Map<String, Object>> people = List.of(
                person("sales-001", "业务员小周", "SALES"),
                person("user-001", "转发用户甲", "SHARER"),
                person("user-002", "转发用户乙", "SHARER"),
                person("user-003", "转发用户丙", "SHARER"),
                person("prospect-001", "潜在客户甲", "PROSPECT"),
                person("prospect-002", "潜在客户乙", "PROSPECT"),
                person("prospect-003", "低停留客户", "PROSPECT")
        );

        // UNWIND 把 Java 传入的 people 列表逐条展开为 row。
        // MERGE 表示“存在就匹配，不存在就创建”，适合做幂等写入。
        // SET 更新节点属性。
        session.run("""
                UNWIND $people AS row
                MERGE (person:Person {id: row.id})
                SET person.name = row.name, person.role = row.role
                """, Map.of("people", people)).consume();

        // 每条 SHARED_TO 关系表示一次分享：from 分享给 to。
        // dwellSeconds 是被分享人在页面上的停留时间，用来估计兴趣程度。
        List<Map<String, Object>> shares = List.of(
                share("share-001", "sales-001", "user-001", 180L),
                share("share-002", "sales-001", "user-002", 45L),
                share("share-003", "user-001", "prospect-001", 300L),
                share("share-004", "user-002", "user-003", 600L),
                share("share-005", "user-003", "prospect-002", 420L),
                share("share-006", "user-001", "prospect-003", 20L)
        );

        // 先 MATCH 两端节点，再 MERGE 有向关系。
        // shareId 用于区分不同分享事件，避免同一分享事件重复写入。
        session.run("""
                UNWIND $shares AS row
                MATCH (from:Person {id: row.fromId})
                MATCH (to:Person {id: row.toId})
                MERGE (from)-[share:SHARED_TO {shareId: row.shareId}]->(to)
                SET share.dwellSeconds = row.dwellSeconds
                """, Map.of("shares", shares)).consume();
    }

    /**
     * 查询每个潜在客户的一条最短路径，同跳数时优先停留更久的客户。
     *
     * <p>这里的“最短”指分享关系的跳数最少，而不是页面停留时间最短。
     * 例如：业务员 -> 用户甲 -> 客户，只需要 2 跳；
     * 业务员 -> 用户乙 -> 用户丙 -> 客户，则需要 3 跳。</p>
     */
    static List<PathResult> findShortestPaths(
            Session session,
            String salesId,
            long minimumDwellSeconds
    ) {
        // Cypher 是 Neo4j 的查询语言，写法和 SQL 不同：
        // MATCH 用来匹配节点/关系，path 保存整条路径，nodes(path) 可取出路径上的节点。
        // shortestPath 只按关系跳数找最短路径；停留时间在后面的 WHERE 中单独筛选。
        String cypher = """
                MATCH (sales:Person {id: $salesId}), (prospect:Person {role: 'PROSPECT'})
                WHERE sales <> prospect
                MATCH path = shortestPath((sales)-[:SHARED_TO*1..6]->(prospect))
                WITH prospect, path,
                     length(path) AS hops,
                     last(relationships(path)).dwellSeconds AS finalDwellSeconds,
                     reduce(total = 0, relation IN relationships(path) |
                         total + relation.dwellSeconds) AS totalDwellSeconds
                WHERE finalDwellSeconds >= $minimumDwellSeconds
                RETURN prospect.id AS prospectId,
                       prospect.name AS prospectName,
                       hops,
                       finalDwellSeconds,
                       totalDwellSeconds,
                       [node IN nodes(path) | node.name] AS pathNames
                ORDER BY hops ASC, finalDwellSeconds DESC, totalDwellSeconds DESC
                """;

        // $salesId 和 $minimumDwellSeconds 是参数，不拼接到字符串里，避免注入并便于复用查询计划。
        return session.run(cypher, Map.of(
                        "salesId", salesId,
                        "minimumDwellSeconds", minimumDwellSeconds
                ))
                .list(WeChatShareGraphDemo::toPathResult);
    }

    private static PathResult toPathResult(Record record) {
        // Neo4j 返回 Record，先按列名取值，再转换成 Java record，方便后续打印或交给接口层。
        Value pathNames = record.get("pathNames");
        return new PathResult(
                record.get("prospectId").asString(),
                record.get("prospectName").asString(),
                record.get("hops").asInt(),
                record.get("finalDwellSeconds").asLong(),
                record.get("totalDwellSeconds").asLong(),
                pathNames.asList(Value::asString)
        );
    }

    private static void printResults(List<PathResult> results) {
        // 这里只做控制台输出，实际项目可以把结果返回给服务层，再提供给前端或推荐任务。
        System.out.println("业务员到潜在客户的最短人脉路径：");
        results.forEach(result -> System.out.printf(
                "- %s：%d 跳，最后一次停留 %d 秒，总停留 %d 秒，路径：%s%n",
                result.prospectName(),
                result.hops(),
                result.finalDwellSeconds(),
                result.totalDwellSeconds(),
                String.join(" -> ", result.pathNames())
        ));
    }

    private static Map<String, Object> person(String id, String name, String role) {
        // Java Map 对应 Cypher 中一行 row，最终作为参数传给 UNWIND。
        return Map.of("id", id, "name", name, "role", role);
    }

    private static Map<String, Object> share(
            String shareId,
            String fromId,
            String toId,
            long dwellSeconds
    ) {
        // 分享关系是有方向的：fromId -> toId。
        return Map.of(
                "shareId", shareId,
                "fromId", fromId,
                "toId", toId,
                "dwellSeconds", dwellSeconds
        );
    }

    private static String envOrDefault(String name, String defaultValue) {
        // 非敏感配置没有设置时使用默认值，方便 IDE 直接运行。
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "缺少环境变量 " + name + "。请先执行：set -a; source .env; set +a"
            );
        }
        return value;
    }

    /** Java 17 record：用于承载一条最短路径查询结果的不可变数据。 */
    record PathResult(
            String prospectId,
            String prospectName,
            int hops,
            long finalDwellSeconds,
            long totalDwellSeconds,
            List<String> pathNames
    ) {
    }
}
