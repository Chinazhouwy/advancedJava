package com.advancedjava.ai.mcp.basic;

/**
 * MCP (Model Context Protocol) 客户端概念演示
 * 
 * MCP 客户端用于连接到 MCP 服务器并使用其提供的工具和资源。
 * 
 * MCP 客户端的主要职责：
 * 1. 连接到 MCP 服务器
 * 2. 查询可用的工具列表
 * 3. 调用所需的服务/工具
 * 4. 处理服务器响应
 * 5. 正确管理连接生命周期
 */
public class McpClientDemo {

    public static void main(String[] args) {
        System.out.println("=== MCP 客户端概念演示 ===");
        
        // 模拟 MCP 客户端操作流程
        demonstrateMcpClientFlow();
    }

    /**
     * 展示 MCP 客户端的典型工作流程
     */
    private static void demonstrateMcpClientFlow() {
        System.out.println("\n1. 连接: 启动 MCP 客户端并连接到服务器...");
        connectToMcpServer();
        
        System.out.println("\n2. 发现服务: 查询服务器上的可用工具");
        discoverAvaialbleTools();
        
        System.out.println("\n3. 工具调用: 使用发现的工具进行功能调用");
        callSpecifiedTool();
        
        System.out.println("\n4. 处理结果: 解析并使用服务器响应");
        handleResult();
        
        System.out.println("\n5. 断开连接: 正常关闭客户端连接");
        disconnect();
    }
    
    /**
     * 模拟连接到 MCP 服务器的过程
     */
    private static void connectToMcpServer() {
        System.out.println("   └─ 客户端初始化: 配置服务器地址和认证信息");
        System.out.println("   └─ TCP 连接建立: 与 MCP 服务器建立通信通道");
        System.out.println("   └─ 协议握手: 确认双方支持的 MCP 版本");
        System.out.println("   └─ 会话建立: 创建通信会话上下文");
        
        // 在真实的 MCP 实现中，这里会有连接设置逻辑
        // 例如: client.connect(serverEndpoint, configuration);
    }
    
    /**
     * 查找服务器上可用的工具列表
     */
    private static void discoverAvaialbleTools() {
        System.out.println("   └─ 请求工具列表: 向服务器发送 'tools/list' 请求");
        System.out.println("   └─ 服务器响应: 接收工具详情和元数据");
        
        System.out.println("   可用工具列表:");
        System.out.println("   ├─ calculator.add: 数学加法运算工具");
        System.out.println("   │   说明: 对两个数值执行加法运算");
        System.out.println("   │   参数: {\"a\": number, \"b\": number}");
        
        System.out.println("   ├─ weather.get: 天气查询工具");
        System.out.println("   │   说明: 获取指定位置的天气信息");
        System.out.println("   │   参数: {\"city\": string, \"country\": string}");
        
        System.out.println("   └─ search.query: 搜索工具");
        System.out.println("       说明: 执行网络搜索");
        System.out.println("       参数: {\"query\": string, \"limit\": number}");
    }
    
    /**
     * 调用具体的服务器端工具/功能
     */
    private static void callSpecifiedTool() {
        System.out.println("   └─ 工具调用示例: 调用 calculator.add 工具");
        System.out.println("     -> 发送调用请求: tools/call");
        System.out.println("     -> 工具标识: \"calculator.add\"");
        System.out.println("     -> 执行参数: {\"a\": 25, \"b\": 17}");
        System.out.println("     -> 发送给服务器进行处理");
        
        System.out.println("   └─ 工具调用特征:");
        System.out.println("     -> 统一的参数格式 (JSON)");
        System.out.println("     -> 结构化的结果返回");
        System.out.println("     -> 明确定义的调用标识符");
    }
    
    /**
     * 处理从服务器得到的响应结果
     */
    private static void handleResult() {
        System.out.println("   └─ 接收响应: 从服务器获取调用结果");
        System.out.println("   └─ 解析数据: 将 JSON 响应解析为可用对象");
        System.out.println("     <- 收到结果: {\"sum\": 42, \"success\": true}");
        System.out.println("   └─ 验证结果: 检查结果的有效性和完整性");
        
        System.out.println("   错误处理示例:");
        System.out.println("   ├─ 错误码识别: \"INVALID_ARGUMENT\"");
        System.out.println("   ├─ 错误详情: 参数格式或类型不符");
        System.out.println("   └─ 重试策略: 如适用的重试逻辑");
    }
    
    /**
     * 模拟客户端正常断开连接过程
     */
    private static void disconnect() {
        System.out.println("   └─ 通知服务器: 发送连接关闭消息");
        System.out.println("   └─ 释放本地资源: 清理内存和关闭资源");
        System.out.println("   └─ 传输完成指示: 确认所有数据已传输完毕");
    }
}