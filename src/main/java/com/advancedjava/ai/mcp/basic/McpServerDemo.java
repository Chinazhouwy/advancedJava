package com.advancedjava.ai.mcp.basic;

/**
 * MCP (Model Context Protocol) 服务器概念演示
 * 
 * MCP 协议允许模型上下文平台（MCP Servers）提供工具和资源，
 * 并允许客户端查询和访问这些功能。
 * 
 * MCP 服务器的主要职责：
 * 1. 注册可用工具
 * 2. 响应初始化请求
 * 3. 提供工具列表
 * 4. 处理工具调用
 * 5. 处理关闭请求
 */
public class McpServerDemo {

    public static void main(String[] args) {
        System.out.println("=== MCP 服务器概念演示 ===");
        
        // 模拟 MCP 服务器初始化生命周期
        demonstrateMcpServerLifecycle();
    }

    /**
     * 展示 MCP 服务器的完整生命周期
     */
    private static void demonstrateMcpServerLifecycle() {
        System.out.println("\n1. 初始化: MCP 服务器启动，等待客户端连接...");
        System.out.println("   服务器注册工具并准备接收请求");
        
        System.out.println("\n2. 注册工具: 在启动时注册各种可用工具");
        registerTools();
        
        System.out.println("\n3. 工具列表: 客户端请求可用的工具列表");
        listAvailableTools();
        
        System.out.println("\n4. 工具调用: 客户端发起工具调用请求");
        simulateToolCall();
        
        System.out.println("\n5. 关闭: 服务器正常关闭过程");
        shutdown();
    }
    
    /**
     * 注册服务器上可用的各种工具
     */
    private static void registerTools() {
        System.out.println("   └─ 计算器工具 (calculator.add) - 执行基础数学运算");
        System.out.println("   └─ 天气工具 (weather.get) - 获取天气预报信息");
        System.out.println("   └─ 搜索工具 (search.query) - 执行网络搜索");
        
        // 在真正的 MCP 实现中，这会通过 SDK 注册实际的功能
        // 例如: server.registerTool("calculator.add", CalculatorTool::add);
    }
    
    /**
     * 列出所有可用的工具及其功能
     */
    private static void listAvailableTools() {
        System.out.println("   └─ 计算器工具 (calculator.add):");
        System.out.println("     参数: {\"a\": number, \"b\": number}");
        System.out.println("     描述: 执行两个数字的加法运算");
        
        System.out.println("   └─ 天气工具 (weather.get):");
        System.out.println("     参数: {\"city\": string, \"country\": string}");
        System.out.println("     描述: 获取指定城市的天气预报");
        
        System.out.println("   └─ 搜索工具 (search.query):");
        System.out.println("     参数: {\"query\": string, \"limit\": number}");
        System.out.println("     描述: 搜索网络上的特定信息");
    }
    
    /**
     * 模拟工具调用的处理流程
     */
    private static void simulateToolCall() {
        System.out.println("   └─ 客户端调用工具示例:");
        System.out.println("     -> 请求: calculator.add"); 
        System.out.println("     -> 参数: {\"a\": 10, \"b\": 5}");
        System.out.println("     -> 处理: 服务器验证参数，执行计算，返回结果");
        System.out.println("     <- 响应: {\"result\": 15}");
        
        System.out.println("   └─ 工具调用特点:");
        System.out.println("     -> 结构化的输入输出协议");
        System.out.println("     -> 统一的错误处理机制");
        System.out.println("     -> 异步或多线程支持（如需要）");
    }
    
    /**
     * 模拟服务器正常关闭过程
     */
    private static void shutdown() {
        System.out.println("   └─ 正常关闭流程启动");
        System.out.println("   └─ 释放资源、取消工具注册、断开连接");
        System.out.println("   └─ 发送最后的确认消息给客户端");
    }
}