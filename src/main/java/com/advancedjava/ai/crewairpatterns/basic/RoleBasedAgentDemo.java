package com.advancedjava.ai.crewairpatterns.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * CrewAI 角色基智能体模式演示
 * 
 * CrewAI 是一个协作智能体框架，角色(Role)、目标(Goal)、背景故事(Backstory)是其核心概念
 * 这些元素定义了智能体的行为特征和执行上下文
 * 
 * 本示例为 Java 等效实现，原始框架为 CrewAI Python
 */
public class RoleBasedAgentDemo {

    /**
     * CrewAgent 类代表一个具有特定角色的智能体
     * 包含角色定义、目标、背景故事和可用工具
     */
    public static class CrewAgent {
        private String role;
        private String goal;
        private String backstory;
        private List<String> tools;

        public CrewAgent(String role, String goal, String backstory) {
            this.role = role;
            this.goal = goal;
            this.backstory = backstory;
            this.tools = new ArrayList<>();
        }

        /**
         * 添加工具到智能体
         * @param toolName 工具名称
         * @return 当前智能体实例以支持链式调用
         */
        public CrewAgent addTool(String toolName) {
            this.tools.add(toolName);
            return this;
        }

        /**
         * 执行智能体的主要功能
         */
        public void executeRole() {
            System.out.printf("[%s] %s %s%n", this.role, this.backstory, this.goal);
            if (!tools.isEmpty()) {
                System.out.println("  可用工具: " + String.join(", ", this.tools));
            }
            performJob();
            System.out.println();
        }

        /**
         * 根据角色执行具体工作
         */
        private void performJob() {
            switch (role.toLowerCase()) {
                case "research analyst":
                    System.out.println("  -> 执行数据分析: 收集、清洗、分析市场数据");
                    System.out.println("  -> 生成报告: 准备详细的研究分析报告");
                    break;
                case "senior writer":
                    System.out.println("  -> 内容创作: 将分析结果转换为读者友好的文章");
                    System.out.println("  -> 质量检查: 确保内容准确性并优化可读性");
                    break;
                case "marketing specialist":
                    System.out.println("  -> 市场研究: 分析目标受众和竞争格局");
                    System.out.println("  -> 策略规划: 制定有效的内容传播策略");
                    break;
                case "quality reviewer":
                    System.out.println("  -> 内容审查: 检查准确性、一致性和质量");
                    System.out.println("  -> 反馈改进: 提供具体的改进建议");
                    break;
                default:
                    System.out.println("  -> 默认操作: 根据角色描述执行相关任务");
                    break;
            }
        }

        // Getters
        public String getRole() { return role; }
        public String getGoal() { return goal; }
        public String getBackstory() { return backstory; }
        public List<String> getTools() { return tools; }
    }

    public static void main(String[] args) {
        System.out.println("=== CrewAI 角色基智能体模式演示 ===");
        System.out.println("展示不同角色的智能体以及它们的职责定义\n");

        // 创建分析师智能体
        CrewAgent analyst = new CrewAgent(
                "Research Analyst",
                "根据最新的科技趋势提供深度市场分析和见解",
                "作为专业研究分析师，你具备敏锐的市场洞察力和丰富的行业知识。"
            )
            .addTool("Data Analysis Software")
            .addTool("Market Research Database")
            .addTool("Statistical Modeling Tool");

        // 创建作家智能体
        CrewAgent writer = new CrewAgent(
                "Senior Writer",
                "为技术主题的受众撰写清晰、有趣、信息丰富的内容",
                "你是具有多年经验的资深记者和技术作家，擅长将复杂的概念转换为易懂的叙述。"
            )
            .addTool("Content Management System")
            .addTool("SEO Optimization Tool")
            .addTool("Grammar Checker");

        // 创建营销专家智能体
        CrewAgent marketingSpecialist = new CrewAgent(
                "Marketing Specialist", 
                "制定并执行有效的营销策略以扩大内容传播范围",
                "作为营销专家，你深刻理解数字营销渠道和用户行为分析。"
            )
            .addTool("Social Media Management")
            .addTool("Analytics Platform")
            .addTool("Email Marketing Tool");

        // 创建质检员智能体
        CrewAgent qualityReviewer = new CrewAgent(
                "Quality Reviewer",
                "确保最终输出符合高质量和准确性标准",
                "作为质量评审员，你有严格的质量控制标准和细致的关注力。"
            )
            .addTool("Automated Testing Suite")
            .addTool("Spell Checker")
            .addTool("Style Guide Compliance Tool");

        // 执行智能体任务演示
        System.out.println("=== 智能体执行演示 ===");
        System.out.println("各智能体根据其角色和工具执行特定任务");

        System.out.println("\n--- 分析师智能体 ---");
        analyst.executeRole();

        System.out.println("--- 作家智能体 ---");
        writer.executeRole();

        System.out.println("--- 营销专家智能体 ---");
        marketingSpecialist.executeRole();

        System.out.println("--- 质检员智能体 ---");
        qualityReviewer.executeRole();

        // 展示智能体角色的多样性
        System.out.println("=== 角色多样性总结 ===");
        System.out.println("角色定义决定了智能体的行为模式和专注领域:");
        System.out.printf("  - %s: %s [目标: %s]%n", 
            analyst.getRole(), analyst.getBackstory(), analyst.getGoal());
        System.out.printf("  - %s: %s [目标: %s]%n", 
            writer.getRole(), writer.getBackstory(), writer.getGoal());
        System.out.printf("  - %s: %s [目标: %s]%n", 
            marketingSpecialist.getRole(), marketingSpecialist.getBackstory(), marketingSpecialist.getGoal());
        System.out.printf("  - %s: %s [目标: %s]%n", 
            qualityReviewer.getRole(), qualityReviewer.getBackstory(), qualityReviewer.getGoal());
    }
}