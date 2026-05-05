package com.advancedjava.ai.mcp.advanced;

/**
 * MCP (Model Context Protocol) 与 Spring 集成概念演示
 * 
 * 此演示展示 Spring AI 如何与 MCP 集成的概念。
 * 
 * 注意：此项目使用的 Spring AI 版本为 1.1.2，而 MCP 支持需要 2.0.0+ 版本，
 * 因此这是一个概念演示，不是实际实现。
 */
public class McpSpringIntegrationDemo {

    public static void main(String[] args) {
        System.out.println("=== MCP 与 Spring 集成概念演示（仅概念级）===");
        System.out.println("(注意：当前项目 Spring AI 版本低于支持 MCP 的版本 2.0.0+，因此仅为概念演示)");
        
        // Spring + MCP 集成演示，概念级别
        demonstrateSpringMcpConcepts();
    }

    /**
     * 演示 Spring AI 中 MCP 集成的关键概念
     */
    private static void demonstrateSpringMcpConcepts() {
        System.out.println("\n1. 自动配置概念:");
        System.out.println("   在 Spring Boot 应用中，启用 MCP 通常通过以下方式:");
        System.out.println("   @EnableMcpServer 注解或自动配置");
        System.out.println("   这将自动发现和注册带 @Tool 注解的方法");

        System.out.println("\n2. Spring 配置示例 (概念级):");
        showExampleConfiguration();

        System.out.println("\n3. 工具注册概念:");
        showToolRegistrationConcept();

        System.out.println("\n4. 依赖注入示例 (概念级):");
        showDependencyInjectionConcept();

        System.out.println("\n5. Spring Boot 自动配置流程 (概念级):");
        showAutoConfigurationProcess();
    }

    /**
     * 显示 MCP 相关的 Spring 配置示例
     */
    private static void showExampleConfiguration() {
        String configExample = """
            // application.properties 或 application.yml 示例
            // spring.ai.mcp.server.enabled=true
            // spring.ai.mcp.server.port=8080
            // spring.ai.mcp.client.servers.weather-server.url=ws://localhost:8081
            
            // 在配置类中
            @EnableMcpServer
            @Configuration
            public class McpConfig {
                // MCP 服务器配置
            }
            """;

        System.out.println(configExample);
    }

    /**
     * 展示如何使用 @Tool 注解标记工具方法（概念级）
     */
    private static void showToolRegistrationConcept() {
        System.out.println("   在概念中，带有 @Tool 注解的方法会被自动注册为工具：");

        String toolExample = """
            @Component
            public class CalculatorTools {
                
                @Tool("Perform addition calculation")
                public int add(int a, int b) {
                    return a + b;
                }
                
                @Tool("Multiply two numbers")
                public int multiply(int a, int b) {
                    return a * b;
                }
            }
            
            // Spring AI 会自动:
            // 1. 检测所有带有 @Tool 注解的方法
            // 2. 将它们注册为可用的 MCP 工具
            // 3. 根据注解值生成工具描述
            """;

        System.out.println(toolExample);
    }

    /**
     * 展示依赖注入在 MCP 上下文中的使用（概念级）
     */
    private static void showDependencyInjectionConcept() {
        String dependencyInjectionExample = """
            @Component
            public class WeatherTools {
                
                @Autowired
                private WeatherService weatherService;
                
                @Tool("Get current weather for a city")
                public WeatherInfo getCurrentWeather(String city, String country) {
                    return weatherService.getCurrentWeather(city, country);
                }
            }
            
            // Spring IoC 容器负责:
            // 1. 注入相关依赖 (WeatherService)
            // 2. 管理组件生命周期
            // 3. 将带 @Tool 注解的 Bean 方法注册为工具
            """;

        System.out.println(dependencyInjectionExample);
    }

    /**
     * 概念级展示 Spring Boot 自动配置如何处理 MCP
     */
    private static void showAutoConfigurationProcess() {
        System.out.println("   Spring Boot 自动配置概念流程：");
        System.out.println("\n1. 启动时，Spring 启动 MVC 自动配置");
        System.out.println("2. 检测 classpath 上是否存在 MCP 相关依赖");
        System.out.println("3. 如启用，则注册 MCP 服务");
        System.out.println("4. 扫描并注册所有带 @Tool 注解的方法");
        System.out.println("5. 设置 MCP 服务器监听端口并开始接受连接");

        String autoConfigExample = """
            // 概念级自动配置示例
            @Configuration
            @ConditionalOnProperty("spring.ai.mcp.server.enabled")
            @EnableWebSocket
            public class McpServerAutoConfiguration {
                
                @Bean
                @ConditionalOnMissingBean
                public McpToolRegistry mcpToolRegistry(List<Tool> tools) {
                    return new AutoConfiguredMcpToolRegistry(tools);
                }
                
                @Bean
                public McpServer mcpServer(McpToolRegistry registry, 
                                         @Value("${spring.ai.mcp.server.port}") int port) {
                    return new EmbeddedMcpServer(registry, port);
                }
            }
            """;

        System.out.println(autoConfigExample);

        System.out.println("\n总结：在更高版本的 Spring AI 中，MCP 集成将通过自动配置");
        System.out.println("实现无缝整合。开发者只需要使用 @Tool 注解标记工具方法，");
        System.out.println("Spring 就会自动处理注册、服务器设置和其他基础设施任务。");
    }
}