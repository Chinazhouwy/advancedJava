package com.advancedjava.ai.adkpatterns.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Google ADK Agent Definition Pattern 演示
 * 
 * ADK (Agent Development Kit) 是 Google 开发的框架，用于定义和协调智能体
 * AgentDef 模式展示了如何定义一个智能体的结构：名字、指令描述、工具集合、子智能体集合
 * 
 * 本示例为 Java 等效实现，原始框架为 Google ADK Python
 */
public class AgentDefPatternDemo {

    /**
     * AgentDef 类模拟 Google ADK 中的 Agent 定义模式
     * 包含智能体的核心要素：名称、描述、工具和子智能体
     */
    public static class AgentDef {
        private String name;
        private String instruction;
        private Map<String, Runnable> tools;
        private Map<String, AgentDef> subAgents;

        public AgentDef(String name, String instruction) {
            this.name = name;
            this.instruction = instruction;
            this.tools = new HashMap<>();
            this.subAgents = new HashMap<>();
        }

        /**
         * 注册工具到当前 AgentDef
         * @param name 工具名称
         * @param function 工具执行函数
         * @return 当前 AgentDef 对象以支持链式调用
         */
        public AgentDef registerTool(String name, Runnable function) {
            this.tools.put(name, function);
            return this;
        }

        /**
         * 注册子智能体到当前 AgentDef
         * @param agent 子智能体对象
         * @return 当前 AgentDef 对象以支持链式调用
         */
        public AgentDef registerSubAgent(AgentDef agent) {
            this.subAgents.put(agent.getName(), agent);
            return this;
        }

        // Getters
        public String getName() { return name; }
        public String getInstruction() { return instruction; }
        public Map<String, Runnable> getTools() { return tools; }
        public Map<String, AgentDef> getSubAgents() { return subAgents; }

        /**
         * 打印当前智能体的基本信息
         */
        public void printInfo() {
            System.out.println("=== Agent: " + name + " ===");
            System.out.println("Instruction: " + instruction);
            System.out.println("Tools (" + tools.size() + "): " + 
                tools.keySet().stream().collect(Collectors.joining(", ")));
            System.out.println("Sub-agents (" + subAgents.size() + "): " + 
                subAgents.keySet().stream().collect(Collectors.joining(", ")));
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Google ADK Agent Definition Pattern 演示 ===");
        System.out.println("本示例展示如何定义具有父子关系的智能体结构\n");

        // 创建子智能体 1: 计费处理智能体
        AgentDef billingAgent = new AgentDef("BillingAgent", 
            "负责计算客户账单金额和发票生成");
        billingAgent.registerTool("calculateBill", () -> 
            System.out.println("  BillingAgent: 正在计算账单..."));
        billingAgent.registerTool("generateInvoice", () -> 
            System.out.println("  BillingAgent: 正在生成发票..."));

        // 创建子智能体 2: 技术支持智能体
        AgentDef techSupportAgent = new AgentDef("TechSupportAgent", 
            "负责解答技术问题和故障排查");
        techSupportAgent.registerTool("troubleshootIssue", () -> 
            System.out.println("  TechSupportAgent: 正在排查技术问题..."));
        techSupportAgent.registerTool("sendResolution", () -> 
            System.out.println("  TechSupportAgent: 正在发送解决方案..."));

        // 创建主智能体，聚合子智能体
        AgentDef mainAgent = new AgentDef("MainAgent", 
            "主智能体，负责客户需求分析并将任务分派给相应的子智能体")
            .registerTool("routeRequest", () -> 
                System.out.println("  MainAgent: 正在路由请求..."))
            .registerTool("gatherFeedback", () -> 
                System.out.println("  MainAgent: 正在收集客户反馈..."))
            .registerSubAgent(billingAgent)
            .registerSubAgent(techSupportAgent);

        // 打印智能体层次结构
        System.out.println("打印智能体层次结构:");
        printAgentHierarchy(mainAgent, 0);

        System.out.println("\n=== 代理功能演示 ===");
        // 演示主智能体的路由功能
        System.out.println("主智能体正在路由请求...");
        mainAgent.getTools().get("routeRequest").run();
        
        // 演示子智能体的功能
        System.out.println("\n执行计费相关任务:");
        AgentDef billing = mainAgent.getSubAgents().get("BillingAgent");
        billing.getTools().get("calculateBill").run();
        billing.getTools().get("generateInvoice").run();
        
        System.out.println("\n执行技术支持相关任务:");
        AgentDef tech = mainAgent.getSubAgents().get("TechSupportAgent");
        tech.getTools().get("troubleshootIssue").run();
        tech.getTools().get("sendResolution").run();
    }

    /**
     * 递归打印智能体层级结构
     */
    private static void printAgentHierarchy(AgentDef agent, int level) {
        String indent = "  ".repeat(level);
        System.out.println(indent + "└── " + agent.getName() + 
            " (工具数: " + agent.getTools().size() + 
            ", 子智能体数: " + agent.getSubAgents().size() + ")");
        
        for (AgentDef subAgent : agent.getSubAgents().values()) {
            printAgentHierarchy(subAgent, level + 1);
        }
    }
}