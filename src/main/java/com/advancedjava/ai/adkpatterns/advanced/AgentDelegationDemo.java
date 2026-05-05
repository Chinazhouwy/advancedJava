package com.advancedjava.ai.adkpatterns.advanced;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Google ADK 任务委托模式演示
 * 
 * 展示如何实现任务分析和智能委派给合适的子智能体
 * Delegate pattern shows how a main agent can analyze and route tasks to appropriate sub-agents
 * 
 * 本示例为 Java 等效实现，原始框架为 Google ADK Python
 */
public class AgentDelegationDemo {

    /**
     * DelegationHandler 接口定义任务处理和委派逻辑
     */
    public interface DelegationHandler {
        void handleTask(String taskDescription);
    }

    /**
     * AgentDef 类模拟 Google ADK 中的 Agent 定义模式
     * 添加了任务分发功能
     */
    public static class AgentDef implements DelegationHandler {
        private String name;
        private String instruction;
        private Map<String, Runnable> tools;
        private Map<String, AgentDef> subAgents;
        private Map<Pattern, String> taskRoutingRules; // 路由规则：正则表达式匹配 -> 子智能体名

        public AgentDef(String name, String instruction) {
            this.name = name;
            this.instruction = instruction;
            this.tools = new HashMap<>();
            this.subAgents = new HashMap<>();
            this.taskRoutingRules = new HashMap<>();
        }

        public AgentDef registerTool(String name, Runnable function) {
            this.tools.put(name, function);
            return this;
        }

        public AgentDef registerSubAgent(AgentDef agent) {
            this.subAgents.put(agent.getName(), agent);
            return this;
        }

        /**
         * 添加基于关键词的任务路由规则
         */
        public AgentDef addRouteRule(String keyword, String subAgentName) {
            // 将关键词转化为不区分大小写的正则表达式匹配
            this.taskRoutingRules.put(Pattern.compile(".*" + keyword + ".*", Pattern.CASE_INSENSITIVE), subAgentName);
            return this;
        }

        @Override
        public void handleTask(String taskDescription) {
            System.out.printf("[%s] 收到任务: %s%n", this.name, taskDescription);
            
            // 分析任务内容并决定委派给哪个子智能体
            String routedSubAgent = null;
            for (Map.Entry<Pattern, String> rule : taskRoutingRules.entrySet()) {
                if (rule.getKey().matcher(taskDescription).matches()) {
                    routedSubAgent = rule.getValue();
                    break;
                }
            }

            if (routedSubAgent != null && subAgents.containsKey(routedSubAgent)) {
                System.out.printf("  -> 路由决策: 委派给 '%s'%n", routedSubAgent);
                AgentDef targetAgent = subAgents.get(routedSubAgent);
                
                // 执行委派的子任务
                System.out.printf("  [%s] 处理委派的任务: %s%n", targetAgent.getName(), taskDescription);
                targetAgent.performTask(taskDescription);
                
                // 反馈结果回主线
                System.out.printf("  [%s] 完成任务并反馈结果: 任务处理完毕%n", targetAgent.getName());
            } else {
                System.out.printf("  [%s] 无法找到合适的子智能体处理此任务，执行自身逻辑%n", this.name);
                performDefaultTask(taskDescription);
            }
        }

        /**
         * 子智能体执行分配的任务
         */
        public void performTask(String taskDescription) {
            System.out.printf("    [%s] 执行具体任务: %s%n", this.name, taskDescription);
            // 模拟执行任务，根据任务类型调用相应工具
            if (taskDescription.toLowerCase().contains("bill")) {
                if (tools.containsKey("calculateBill")) {
                    System.out.printf("      调用工具: calculateBill%n");
                    tools.get("calculateBill").run();
                }
            } else if (taskDescription.toLowerCase().contains("invoice")) {
                if (tools.containsKey("generateInvoice")) {
                    System.out.printf("      调用工具: generateInvoice%n");
                    tools.get("generateInvoice").run();
                }
            } else if (taskDescription.toLowerCase().contains("support")) {
                if (tools.containsKey("troubleshootIssue")) {
                    System.out.printf("      调用工具: troubleshootIssue%n");
                    tools.get("troubleshootIssue").run();
                }
            } else if (taskDescription.toLowerCase().contains("technical")) {
                if (tools.containsKey("sendResolution")) {
                    System.out.printf("      调用工具: sendResolution%n");
                    tools.get("sendResolution").run();
                }
            } else {
                performDefaultTask(taskDescription);
            }
        }

        private void performDefaultTask(String taskDescription) {
            System.out.printf("    [%s] 执行默认任务处理: %s%n", this.name, taskDescription);
        }

        // Getters
        public String getName() { return name; }
        public String getInstruction() { return instruction; }
        public Map<String, Runnable> getTools() { return tools; }
        public Map<String, AgentDef> getSubAgents() { return subAgents; }
    }

    public static void main(String[] args) {
        System.out.println("=== Google ADK 任务委托模式演示 ===");
        System.out.println("展示主智能体如何根据任务内容智能委派给合适的子智能体\n");

        // 创建子智能体 1: 计费智能体
        AgentDef billingAgent = new AgentDef("BillingAgent", 
            "负责处理与账单支付、收费计算、发票相关的问题");
        billingAgent
            .registerTool("calculateBill", () -> 
                System.out.println("      BillingAgent 正在计算账单..."))
            .registerTool("generateInvoice", () -> 
                System.out.println("      BillingAgent 正在生成发票..."));

        // 创建子智能体 2: 技术支持智能体
        AgentDef techSupportAgent = new AgentDef("TechSupportAgent", 
            "负责解答技术疑问、故障排查、发送解决方案");
        techSupportAgent
            .registerTool("troubleshootIssue", () -> 
                System.out.println("      TechSupportAgent 正在排查技术问题..."))
            .registerTool("sendResolution", () -> 
                System.out.println("      TechSupportAgent 正在发送解决方案..."));

        // 创建主智能体，配置路由规则
        AgentDef mainAgent = new AgentDef("MainAgent", 
            "主智能体，负责分析任务内容并分派给适当的子智能体")
            .registerSubAgent(billingAgent)
            .registerSubAgent(techSupportAgent)
            // 配置路由规则：关键词匹配 -> 委派目标智能体
            .addRouteRule("charge", "BillingAgent")
            .addRouteRule("bill", "BillingAgent")
            .addRouteRule("invoice", "BillingAgent")
            .addRouteRule("pay", "BillingAgent")
            .addRouteRule("technical", "TechSupportAgent")
            .addRouteRule("support", "TechSupportAgent")
            .addRouteRule("troubleshoot", "TechSupportAgent");

        // 模拟不同类型的任务，展示任务委派功能
        String[] tasks = {
            "我需要查看我的账单余额",
            "请帮我生成上个月的发票",
            "我的账户扣费有问题",
            "我遇到了登录的技术问题",
            "需要帮助进行故障排查",
            "我想申请退款处理"
        };

        System.out.println("开始处理任务列表:\n");
        for (int i = 0; i < tasks.length; i++) {
            System.out.printf("--- 任务 %d ---%n", i + 1);
            mainAgent.handleTask(tasks[i]);
            System.out.println();
        }

        // 演示无匹配规则的任务处理
        System.out.println("--- 演示未匹配任务的默认处理 ---");
        mainAgent.handleTask("我想申请退款，但这不是标准流程");
    }
}