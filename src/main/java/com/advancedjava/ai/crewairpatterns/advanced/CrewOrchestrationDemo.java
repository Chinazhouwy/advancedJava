package com.advancedjava.ai.crewairpatterns.advanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CrewAI 团队协调模式演示
 * 
 * 展示如何组织多个角色智能体形成一个团队(Crew)
 * 支持两种工作流程：顺序执行(Sequential) 和 层级执行(Hierarchical)
 * 
 * 本示例为 Java 等效实现，原始框架为 CrewAI Python
 */
public class CrewOrchestrationDemo {

    /**
     * Process 枚举定义工作流模式
     */
    public enum Process {
        SEQUENTIAL,    // 顺序执行：智能体按顺序依次执行任务
        HIERARCHICAL   // 层级执行：主智能体协调其他智能体的工作
    }

    /**
     * CrewAgent 复制基本的智能体定义（也可以从基础包导入如果设计更成熟）
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

        public CrewAgent addTool(String toolName) {
            this.tools.add(toolName);
            return this;
        }

        public String getRole() { return role; }

        /**
         * 执行代理任务
         */
        public void executeTask(int taskId, String taskDescription) {
            System.out.printf("  [%s] #ID%d - 任务: %s%n", this.role, taskId, taskDescription);
            System.out.printf("  -> 执行目标: %s%n", this.goal);
            System.out.printf("  -> 执行细节: %s (工具: %s)%n", this.backstory, 
                String.join(", ", this.tools));
            
            // 模拟任务执行
            switch (role.toLowerCase()) {
                case "project coordinator":
                    System.out.printf("  -> [Project Coordinator] 正在制定项目计划...%n");
                    System.out.printf("  -> [Project Coordinator] 分配资源配置...%n");
                    break;
                case "research analyst":
                    System.out.printf("  -> [Research Analyst] 正在进行数据分析...%n");
                    System.out.printf("  -> [Research Analyst] 生成统计报告...%n");
                    break;
                case "report writer":
                    System.out.printf("  -> [Report Writer] 整合研究成果...%n");
                    System.out.printf("  -> [Report Writer] 生成项目报告...%n");
                    break;
                case "quality auditor":
                    System.out.printf("  -> [Quality Auditor] 检查报告质量...%n");
                    System.out.printf("  -> [Quality Auditor] 提供修正建议...%n");
                    break;
            }
        }
    }

    /**
     * Crew 类代表一个协作团队
     */
    public static class Crew {
        private String name;
        private List<CrewAgent> agents;
        private Process process;
        private int currentTaskId = 0;

        public Crew(String name, Process process) {
            this.name = name;
            this.process = process;
            this.agents = new ArrayList<>();
        }

        public Crew addAgent(CrewAgent agent) {
            this.agents.add(agent);
            return this;
        }

        /**
         * 执行团队任务
         */
        public void execute() {
            System.out.println("=== 开始执行团队 '" + name + "' ===");
            System.out.println("工作流模式: " + this.process.toString());

            if (this.process == Process.SEQUENTIAL) {
                executeSequential();
            } else if (this.process == Process.HIERARCHICAL) {
                executeHierarchical();
            }

            System.out.println("=== 团队 '" + name + "' 执行完成 ===\n");
        }

        /**
         * 顺序执行模式：每个智能体依次执行自己的任务
         */
        private void executeSequential() {
            System.out.println("执行顺序: 智能体按注册顺序依次执行任务");
            
            for (int i = 0; i < agents.size(); i++) {
                CrewAgent agent = agents.get(i);
                currentTaskId++;
                String taskDescription = getSequentialTaskForAgent(agent, i);
                
                agent.executeTask(currentTaskId, taskDescription);
            }
        }

        /**
         * 层级执行模式：第一个智能体管理并分派任务给其他智能体
         */
        private void executeHierarchical() {
            if (agents.isEmpty()) return;

            CrewAgent manager = agents.get(0);  // 第一个智能体作为管理者
            System.out.println("执行顺序: " + manager.getRole() + " 作为管理者分派任务给其他成员");

            System.out.printf("  [%s] (管理者) 接收整体任务...%n", manager.getRole());
            System.out.printf("  -> 分析任务并制定执行方案...%n");

            // 管理者向团队分派具体的子任务
            for (int i = 1; i < agents.size(); i++) {
                CrewAgent subordinate = agents.get(i);
                currentTaskId++;
                String assignmentDescription = getHierarchicalTaskForAgent(subordinate, i - 1);
                
                System.out.printf("  -> 向 [%s] 分派子任务: %s%n", 
                    subordinate.getRole(), assignmentDescription);
                    
                subordinate.executeTask(currentTaskId, assignmentDescription);
            }

            System.out.printf("  [%s] (管理者) 汇总所有结果并输出最终报告%n", manager.getRole());
        }

        /**
         * 获取顺序执行模式下智能体的具体任务
         */
        private String getSequentialTaskForAgent(CrewAgent agent, int index) {
            switch (index) {
                case 0: return "制定整体项目计划";
                case 1: return "开展市场需求分析";  
                case 2: return "编写项目报告初稿";
                case 3: return "对交付成果进行质量审核";
                default: return "执行通用分析任务";
            }
        }

        /**
         * 获取层级执行模式下管理者分派的子任务
         */
        private String getHierarchicalTaskForAgent(CrewAgent agent, int index) {
            switch (index) {
                case 0: return "执行数据挖掘分析任务";
                case 1: return "撰写详细的技术报告";
                case 2: return "验证报告的准确性";
                default: return "提供专业意见";
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CrewAI 团队协调模式演示 ===");
        System.out.println("展示不同工作流下智能体团队的协作方式\n");

        // 创建通用智能体实例 - 这些会在后面的团队中复用
        CrewAgent projectCoordinator = new CrewAgent(
                "Project Coordinator",
                "管理整个项目时间线、资源配置和任务协调", 
                "作为项目经理，负责确保所有任务按时完成"
            )
            .addTool("Project Management Tool")
            .addTool("Resource Allocator");

        CrewAgent dataAnalyst = new CrewAgent(
                "Research Analyst",
                "基于数据提供深刻的业务洞察", 
                "专注于分析数据集并提供可行的建议"
            )  
            .addTool("Data Analyzer")
            .addTool("Statistical Software");

        CrewAgent reportWriter = new CrewAgent(
                "Report Writer",
                "创作专业且易于理解的分析报告", 
                "整合信息并转化为有价值的内容"
            )
            .addTool("Document Editor")
            .addTool("Content Formatter");
            
        CrewAgent qualityAuditor = new CrewAgent(
                "Quality Auditor", 
                "确保所有提交内容达到最高标准",
                "仔细审查每份文档以确保质量和准确性"
            )
            .addTool("Quality Checker")
            .addTool("Style Guide");

        // 演示顺序执行流程
        System.out.println("--- 顺序执行 (Sequential) 演示 ---");
        Crew sequentialCrew = new Crew("Sequential Marketing Campaign Crew", Process.SEQUENTIAL)
            .addAgent(projectCoordinator)
            .addAgent(dataAnalyst)
            .addAgent(reportWriter)
            .addAgent(qualityAuditor);
        
        sequentialCrew.execute();

        // 演示层级执行流程 
        System.out.println("--- 层级执行 (Hierarchical) 演示 ---");
        Crew hierarchicalCrew = new Crew("Hierarchical Report Development Crew", Process.HIERARCHICAL)
            .addAgent(projectCoordinator)  // 管理者
            .addAgent(dataAnalyst)         // 下属
            .addAgent(reportWriter)        // 下属
            .addAgent(qualityAuditor);     // 下属
            
        hierarchicalCrew.execute();

        // 总结两种执行模式的区别
        System.out.println("=== 执行模式对比总结 ===");
        System.out.println("1. 顺序执行 (Sequential):");
        System.out.println("   - 智能体按注册顺序各自独立处理任务");
        System.out.println("   - 适合任务可分解且各智能体相对自治的场景");
        System.out.println("   - 执行流程清晰明了，易于跟踪");
        System.out.println();
        System.out.println("2. 层级执行 (Hierarchical):");  
        System.out.println("   - 第一个智能体作为管理者分派和协调其他智能体");
        System.out.println("   - 适合需要中央协调和集中决策的复杂任务");
        System.out.println("   - 管理者可以基于全局视角进行任务分配");
    }
}