package com.advancedjava.ai.a2a.advanced;

import java.util.HashMap;
import java.util.Map;

/**
 * A2A 多代理任务协作演示
 * 展示多代理任务协调和分布式处理的概念：
 * - 代理注册中心
 * - 不同类型的特化代理 (Research, Analysis, Writing)
 * - 任务分解与分发
 * - 结果聚合
 */
public class A2AMultiAgentTaskDemo {
    
    /**
     * 模拟代理注册中心，存储已注册的 A2A 代理实例
     */
    static class AgentRegistry {
        private Map<String, Agent> agents = new HashMap<>();
        
        public void registerAgent(String agentId, Agent agent) {
            agents.put(agentId, agent);
            System.out.println("代理已注册: " + agentId + ", 类型: " + agent.getAgentType());
        }
        
        public Agent getAgent(String agentId) {
            return agents.get(agentId);
        }
        
        public Map<String, Agent> getAllAgents() {
            return new HashMap<>(agents);
        }
    }
    
    /**
     * 通用的 A2A 代理接口
     */
    interface Agent {
        String getAgentId();
        String getAgentType();
        String processTask(Map<String, Object> taskParameters);
        String getStatus();
    }
    
    /**
     * 研究代理：负责收集和整理相关资料
     */
    static class ResearchAgent implements Agent {
        private final String agentId;
        
        public ResearchAgent(String agentId) {
            this.agentId = agentId;
        }
        
        @Override
        public String getAgentId() {
            return agentId;
        }
        
        @Override
        public String getAgentType() {
            return "Research Agent";
        }
        
        @Override
        public String processTask(Map<String, Object> taskParameters) {
            System.out.println("研究代理 " + agentId + " 开始处理任务: " + taskParameters.get("topic"));
            
            // 模拟研究过程
            try {
                Thread.sleep(1000); // 模拟研究时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 模拟返回研究成果
            String researchResult = """
                研究结果: 
                1. 针对主题 '%s' 的相关研究文献已经搜集完毕
                2. 收集了 15 篇核心参考文献
                3. 识别出 3 个主要争议点: 
                   - 观点A: %s
                   - 观点B: %s
                4. 发现研究空白区域
                """.formatted(
                    taskParameters.get("topic"), 
                    taskParameters.getOrDefault("perspectiveA", "传统理论观点"),
                    taskParameters.getOrDefault("perspectiveB", "现代方法论")
                );
            
            System.out.println("研究代理 " + agentId + " 任务完成");
            return researchResult;
        }
        
        @Override
        public String getStatus() {
            return "Research Agent 正常运行，负载量中等";
        }
    }
    
    /**
     * 分析代理：负责数据分析和模型计算
     */
    static class AnalysisAgent implements Agent {
        private final String agentId;
        
        public AnalysisAgent(String agentId) {
            this.agentId = agentId;
        }
        
        @Override
        public String getAgentId() {
            return agentId;
        }
        
        @Override
        public String getAgentType() {
            return "Analysis Agent";
        }
        
        @Override
        public String processTask(Map<String, Object> taskParameters) {
            System.out.println("分析代理 " + agentId + " 开始处理任务: " + taskParameters.get("dataType"));
            
            // 模拟分析过程
            try {
                Thread.sleep(1500); // 模拟分析时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 模拟返回分析结果
            String analysisResult = """
                分析结果: 
                1. 数据集规模: %d 条记录
                2. 关键趋势: %s 
                3. 统计模型结果: R² = %.3f
                4. 异常检测: 发现 %d 个数据异常点
                """.formatted(
                    (Integer) taskParameters.getOrDefault("recordCount", 1000),
                    taskParameters.getOrDefault("keyTrend", "线性增长"),
                    (Double) taskParameters.getOrDefault("rSquared", 0.785),
                    (Integer) taskParameters.getOrDefault("anomalyCount", 12)
                );
            
            System.out.println("分析代理 " + agentId + " 任务完成");
            return analysisResult;
        }
        
        @Override
        public String getStatus() {
            return "Analysis Agent 正常运行，计算资源充足";
        }
    }
    
    /**
     * 写作代理：负责将结果整合成文档
     */
    static class WritingAgent implements Agent {
        private final String agentId;
        
        public WritingAgent(String agentId) {
            this.agentId = agentId;
        }
        
        @Override
        public String getAgentId() {
            return agentId;
        }
        
        @Override
        public String getAgentType() {
            return "Writing Agent";
        }
        
        @Override
        public String processTask(Map<String, Object> taskParameters) {
            System.out.println("写作代理 " + agentId + " 开始处理写作任务");
            
            // 模拟文档生成过程
            try {
                Thread.sleep(1200); // 模拟写作时间
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 模拟基于输入生成最终报告
            String researchPart = (String) taskParameters.get("researchResult");
            String analysisPart = (String) taskParameters.get("analysisResult");
            
            String report = """
                最终报告 [A2A多代理协作]
                
                %s
                
                %s
                
                结论: 
                根据综合研究和分析，针对主题"%s"形成了以下洞见...
                """.formatted(researchPart, analysisPart, taskParameters.get("topic"));
            
            System.out.println("写作代理 " + agentId + " 报告生成完成");
            return report;
        }
        
        @Override
        public String getStatus() {
            return "Writing Agent 正常运行，在线且准备接收任务";
        }
    }
    
    /**
     * 多代理协调器，负责整个任务的流程控制和结果聚合
     */
    static class MultiAgentCoordinator {
        private final AgentRegistry registry;
        
        public MultiAgentCoordinator(AgentRegistry registry) {
            this.registry = registry;
        }
        
        /**
         * 执行多代理任务流：分解、分发、聚合
         * @param initialTask 初始任务参数
         * @return 完整的结果报告
         */
        public String executeCoordinatedTask(Map<String, Object> initialTask) {
            System.out.println("\n--- 开始多代理协作任务执行 ---");
            
            // 步骤1: 从注册表获取所需代理
            Agent researchAgent = registry.getAgent("research-agent-01");
            Agent analysisAgent = registry.getAgent("analysis-agent-01");
            Agent writingAgent = registry.getAgent("writing-agent-01");
            
            if (researchAgent == null || analysisAgent == null || writingAgent == null) {
                return "错误: 无法找到必要的代理来协同处理任务";
            }
            
            // 将初始任务参数复制到每个子任务
            Map<String, Object> researchParams = new HashMap<>(initialTask);
            Map<String, Object> analysisParams = new HashMap<>(initialTask);
            Map<String, Object> writingParams = new HashMap<>(initialTask);
            
            // 步骤2: 并发执行研究和分析任务
            System.out.println("启动并行任务: 研究和分析");
            
            // 使用单独线程处理研究任务
            String researchResult = researchAgent.processTask(researchParams);
            
            // 使用单独线程处理分析任务
            String analysisResult = analysisAgent.processTask(analysisParams);
            
            // 步骤3: 更新写作参数包含研究和分析结果
            writingParams.put("researchResult", researchResult);
            writingParams.put("analysisResult", analysisResult);
            
            // 步骤4: 写作代理整合全部结果
            String finalReport = writingAgent.processTask(writingParams);
            
            System.out.println("\n--- 多代理协作任务执行完成 ---");
            
            return finalReport;
        }
        
        /**
         * 获取所有代理的联合状态
         * @return 代理网络的整体状态
         */
        public String getNetworkStatus() {
            StringBuilder status = new StringBuilder("A2A 代理网络整体状态:\n");
            for (Map.Entry<String, Agent> entry : registry.getAllAgents().entrySet()) {
                status.append("  ").append(entry.getKey())
                      .append(" (").append(entry.getValue().getAgentType()).append("): ")
                      .append(entry.getValue().getStatus()).append("\n");
            }
            return status.toString();
        }
    }
    
    /**
     * 主方法 - 演示完整的 A2A 多代理协同工作流程
     */
    public static void main(String[] args) {
        System.out.println("=== A2A 多代理任务协作演示 ===");
        
        // 1. 创建代理注册表
        AgentRegistry registry = new AgentRegistry();
        
        // 2. 注册不同类型的代理
        registry.registerAgent("research-agent-01", new ResearchAgent("research-agent-01"));
        registry.registerAgent("analysis-agent-01", new AnalysisAgent("analysis-agent-01"));
        registry.registerAgent("writing-agent-01", new WritingAgent("writing-agent-01"));
        
        System.out.println();
        
        // 3. 创建协调器
        MultiAgentCoordinator coordinator = new MultiAgentCoordinator(registry);
        
        // 4. 显示代理网络状态
        System.out.println(coordinator.getNetworkStatus());
        
        // 5. 准备初始任务参数
        Map<String, Object> taskParameters = new HashMap<>();
        taskParameters.put("topic", "基于机器学习的消费者行为预测");
        taskParameters.put("dataType", "消费者购买历史数据");
        taskParameters.put("recordCount", 12500);
        taskParameters.put("keyTrend", "季节性消费偏好变化");
        taskParameters.put("rSquared", 0.823);
        taskParameters.put("anomalyCount", 8);
        taskParameters.put("perspectiveA", "统计回归模型");
        taskParameters.put("perspectiveB", "深度神经网络方法");
        
        System.out.println("初始任务参数: " + taskParameters);
        
        // 6. 执行多代理协调任务
        String result = coordinator.executeCoordinatedTask(taskParameters);
        
        // 7. 输出最终结果
        System.out.println("\n=== 最终输出结果 ===");
        System.out.println(result);
        
        System.out.println("\n=== A2A 多代理协作演示完成 ===");
    }
}