package com.advancedjava.ai.a2a.basic;

/**
 * A2A 客户端概念演示
 * 展示 A2A 协议的客户端部分概念：
 * - 代理发现机制
 * - 向其他代理发送任务
 * - 轮询结果
 * - 处理响应
 */
public class A2AClientDemo {
    
    /**
     * 模拟从 /.well-known/agent.json 发现代理信息
     * @param agentDomain 代理域名
     * @return 模拟返回的代理卡片信息
     */
    public static String discoverAgent(String agentDomain) {
        System.out.println("正在通过 /.well-known/agent.json 协议发现代理: " + agentDomain);
        
        // 返回模拟的代理卡片信息
        String mockAgentCard = """  
            {
                "did": "did:a2a:client-agent-001",
                "name": "Demo Client Agent",
                "description": "用于演示目的的A2A客户端代理",
                "version": "1.0.0",
                "capabilities": [
                    "task-receipt",
                    "result-return",
                    "heartbeat"
                ],
                "endpoints": {
                    "submit-task": "https://client-agent.example.com/api/tasks",
                    "poll-result": "https://client-agent.example.com/api/results/{taskId}",
                    "status": "https://client-agent.example.com/api/status"
                },
                "supported-task-types": ["demo-task"],
                "authentication": {
                    "protocol": "bearer-token",
                    "issuer": "client-agent.example.com"
                }
            }
            """;
        return mockAgentCard;
    }
    
    /**
     * 模拟向指定代理发送任务
     * @param targetAgentDomain 目标代理域
     * @param taskDescription 任务描述
     * @return 任务ID
     */
    public static String sendTask(String targetAgentDomain, String taskDescription) {
        System.out.println("向代理 " + targetAgentDomain + " 发送任务: " + taskDescription);
        
        // 生成模拟的任务ID
        String taskId = "task-" + System.currentTimeMillis();
        System.out.println("任务已分配ID: " + taskId);
        
        return taskId;
    }
    
    /**
     * 模拟轮询特定任务的结果
     * @param taskId 任务ID
     * @param maxAttempts 最大轮询次数
     * @return 任务执行结果
     */
    public static String pollForResult(String taskId, int maxAttempts) {
        System.out.println("开始轮询任务 " + taskId + " 的结果...");
        
        for (int i = 1; i <= maxAttempts; i++) {
            System.out.println("第 " + i + " 次轮询 (" + taskId + ")");
            
            // 模拟延迟
            try {
                Thread.sleep(500); // 延迟0.5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            // 模拟结果状态变化
            if (i >= 3) { // 模拟处理3次后完成
                System.out.println("任务 " + taskId + " 已完成!");
                return "任务 " + taskId + " 执行成功，结果: 这是一个演示代理执行任务的模拟响应";
            }
        }
        
        System.out.println("任务 " + taskId + " 轮询超时");
        return "任务 " + taskId + " 执行超时或失败";
    }
    
    /**
     * 验证代理身份和能力
     * @param agentInfo 代理信息
     * @return 是否支持所需能力
     */
    public static boolean validateAgentCapabilities(String agentInfo) {
        System.out.println("验证代理功能...");
        boolean supportsTasks = agentInfo.contains("\"demo-task\"");
        
        if (supportsTasks) {
            System.out.println("代理支持必要功能");
        } else {
            System.out.println("代理不支持所需的'处理任务'功能");
        }
        
        return supportsTasks;
    }
    
    /**
     * 主方法 - 演示完整的 A2A 客户端工作流程
     */
    public static void main(String[] args) {
        System.out.println("=== A2A 客户端概念演示 ===");
        System.out.println();
        
        // 1. 代理发现
        String targetAgentDomain = "target-agent.example.com";
        String discoveredAgentInfo = discoverAgent(targetAgentDomain);
        System.out.println("代理发现完成");
        System.out.println(discoveredAgentInfo.substring(0, Math.min(discoveredAgentInfo.length(), 100)) + "..."); // 显示开头
        System.out.println();
        
        // 2. 验证代理能力
        boolean isValid = validateAgentCapabilities(discoveredAgentInfo);
        if (!isValid) {
            System.out.println("代理不符合要求，停止执行");
            return;
        }
        System.out.println();
        
        // 3. 发送任务到发现的代理
        String task = "执行计算密集型数据分析，并返回摘要结果";
        String taskId = sendTask(targetAgentDomain, task);
        System.out.println();
        
        // 4. 轮询结果
        String result = pollForResult(taskId, 5);
        System.out.println("接收到结果: " + result);
        System.out.println();
        
        System.out.println("=== A2A 客户端演示结束 ===");
    }
}