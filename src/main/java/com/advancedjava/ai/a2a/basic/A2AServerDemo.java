package com.advancedjava.ai.a2a.basic;

/**
 * A2A 服务端概念演示
 * 展示 A2A 协议的服务端部分概念：
 * - 代理卡片创建与发布
 * - 任务接收与处理
 * - 结果返回
 * - 状态管理
 */
public class A2AServerDemo {
    
    /**
     * 创建代理卡片 (Agent Card)，这是 A2A 代理自我标识的方式
     * 存储在 /.well-known/agent.json 位置供其他代理发现
     * @return 代理卡片信息的 JSON 字符串
     */
    public static String createAgentCard() {
        System.out.println("正在创建代理卡片信息 (Agent Card)");
        
        String agentCard = """
            {
                "did": "did:a2a:server-agent-001",
                "name": "Demo Server Agent",
                "description": "用于演示目的的A2A服务器代理",
                "version": "1.0.0",
                "capabilities": [
                    "task-receipt",
                    "result-return",
                    "heartbeat",
                    "data-analysis"
                ],
                "endpoints": {
                    "receive-task": "https://server-agent.example.com/api/task-receive",
                    "results": "https://server-agent.example.com/api/results",
                    "status": "https://server-agent.example.com/api/status"
                },
                "supported-task-types": ["data-analysis", "text-processing", "image-analysis"],
                "authentication": {
                    "protocol": "bearer-token",
                    "issuers": ["trusted-party-1.example.com"]
                },
                "pubkeys": [
                    {
                        "alg": "RSA-OAEP-256",
                        "usage": "encrypt",
                        "value": "-----BEGIN PUBLIC KEY-----\\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA..."
                    }
                ]
            }
            """;
        
        return agentCard;
    }
    
    /**
     * 模拟发布代理卡片到标准位置 (/.well-known/agent.json)
     * 在实际实现中，这将作为 Web 服务器上的静态文件提供
     * @param agentCard 代理卡片信息
     */
    public static void publishAgentCard(String agentCard) {
        System.out.println("正在将代理卡片发布到标准位置: .well-known/agent.json");
        System.out.println("代理卡片已准备就绪，其他代理可通过标准协议访问此信息");
        System.out.println();
    }
    
    /**
     * 模拟从客户端接收到任务请求
     * @param clientId 发起请求的客户端ID
     * @param taskData 任务数据
     * @return 接收到的请求对象，包含任务ID
     */
    public static String receiveTask(String clientId, String taskData) {
        System.out.println("接收到来自客户端 " + clientId + " 的任务请求");
        System.out.println("任务详情: " + taskData);
        
        String taskId = "srv-task-" + System.currentTimeMillis();
        System.out.println("已为该任务分配ID: " + taskId);
        System.out.println("任务已添加到处理队列");
        
        return taskId;
    }
    
    /**
     * 模拟启动后台任务处理器，异步处理请求
     * @param taskId 任务ID
     */
    public static void startProcessingBackend(String taskId) {
        System.out.println("后台任务处理器已启动，开始处理任务: " + taskId);
        
        Runnable processingTask = () -> {
            System.out.println("任务 " + taskId + " 正在执行...");
            
            try {
                // 模拟耗时的任务处理
                Thread.sleep(2000); // 模拟2秒处理时间
            } catch (InterruptedException e) {
                System.out.println("任务 " + taskId + " 处理被中断");
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("任务 " + taskId + " 执行完毕");
            storeTaskResult(taskId, "这是任务 " + taskId + " 执行结果: 数据分析完成，关键指标: 平均值=85.2, 中位数=84.0");
        };
        
        Thread processingThread = new Thread(processingTask);
        processingThread.start();
    }
    
    /**
     * 存储任务结果以便客户端获取
     * @param taskId 任务ID
     * @param result 任务结果
     */
    public static void storeTaskResult(String taskId, String result) {
        System.out.println("正在保存任务 " + taskId + " 的执行结果到存储层");
        // 在实际实现中，这会真正存储任务结果
        System.out.println("结果已保存: " + result);
    }
    
    /**
     * 提供状态信息，可用于健康检查
     * @return 当前代理的运行状态信息
     */
    public static String getStatus() {
        String status = """
            {
                "status": "healthy",
                "uptime": "1420 seconds",
                "connected-clients": 4,
                "active-tasks": 2,
                "completed-tasks": 18,
                "last-status-check": "2026-05-05T10:30:45Z"
            }
            """;
        System.out.println("返回当前系统状态:");
        System.out.println(status);
        
        return status;
    }
    
    /**
     * 模拟返回处理结果给客户端
     * @param taskId 任务ID
     * @return 任务处理结果
     */
    public static String returnResultToClient(String taskId) {
        System.out.println("正在为客户端请求任务 " + taskId + " 的结果");
        
        // 在实际应用中，这里会从数据库或其他存储中查找结果
        String storedResult = "这是任务 " + taskId + " 执行结果: 数据分析完成，关键指标: 平均值=85.2, 中位数=84.0";
        
        System.out.println("找到结果并返回给客户端: " + storedResult);
        return storedResult;
    }
    
    /**
     * 主方法 - 演示完整的 A2A 服务器工作流程
     */
    public static void main(String[] args) {
        System.out.println("=== A2A 服务端概念演示 ===");
        System.out.println();
        
        // 1. 创建并发布代理卡片
        String agentCard = createAgentCard();
        System.out.println();
        publishAgentCard(agentCard);
        
        // 2. 服务健康状态
        System.out.println("--- 服务状态检查 ---");
        getStatus();
        System.out.println();
        
        // 3. 模拟接收到客户端请求
        System.out.println("--- 任务处理模拟开始 ---");
        String clientId = "client-agent-001";
        String taskRequest = "对提供的数据集进行复杂的统计分析";
        String receivedTaskId = receiveTask(clientId, taskRequest);
        
        // 4. 启动后台处理
        startProcessingBackend(receivedTaskId);
        System.out.println();
        
        // 等待处理完成
        System.out.println("等待任务处理完成...");
        try {
            Thread.sleep(2500); // 稍微比处理更长时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 5. 模拟客户端结果查询
        System.out.println("--- 模拟客户端查询任务结果 ---");
        String result = returnResultToClient(receivedTaskId);
        System.out.println("最终结果: " + result);
        System.out.println();
        
        System.out.println("=== A2A 服务端演示结束 ===");
    }
}