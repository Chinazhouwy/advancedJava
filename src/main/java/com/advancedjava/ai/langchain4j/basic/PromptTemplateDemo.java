package com.advancedjava.ai.langchain4j.basic;

import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * LangChain4j PromptTemplate 使用演示
 *
 * <p>展示如何创建提示词模板、使用变量占位符、以及格式化消息。
 */
public class PromptTemplateDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j PromptTemplate 演示 ===\n");

        // 1. 创建基础模板（使用 {{variable}} 占位符）
        System.out.println("1. 创建基础提示词模板...");
        PromptTemplate basicTemplate = PromptTemplate.from("你好，{{name}}！今天感觉{{mood}}吗？");
        System.out.println("   模板: " + basicTemplate);

        String basicResult = basicTemplate.apply(Map.of("name", "张三", "mood", "怎么样")).text();
        System.out.println("   应用后: " + basicResult);
        System.out.println();

        // 2. 创建系统提示词模板
        System.out.println("2. 创建系统角色模板...");
        PromptTemplate systemTemplate = PromptTemplate.from(
                "你是一个{{role}}专家，擅长{{domain}}领域。请用{{language}}回答。"
        );

        Map<String, Object> systemVars = Map.of(
                "role", "Java",
                "domain", "并发编程",
                "language", "中文"
        );

        String systemPrompt = systemTemplate.apply(systemVars).text();
        System.out.println("   系统提示词: " + systemPrompt);
        System.out.println();

        // 3. 创建带指令的复杂模板
        System.out.println("3. 创建带指令的复杂模板...");
        PromptTemplate instructionTemplate = PromptTemplate.from(
                "请完成以下任务：\n" +
                        "任务类型: {{taskType}}\n" +
                        "输入数据: {{inputData}}\n" +
                        "输出格式: {{outputFormat}}\n" +
                        "要求: {{requirements}}"
        );

        Map<String, Object> instructionVars = Map.of(
                "taskType", "代码审查",
                "inputData", "一个Java方法实现",
                "outputFormat", "结构化JSON",
                "requirements", "找出潜在的性能问题和并发安全问题"
        );

        String instructionPrompt = instructionTemplate.apply(instructionVars).text();
        System.out.println("   指令提示词:");
        System.out.println(instructionPrompt);
        System.out.println();

        // 4. 展示多变量替换
        System.out.println("4. 多变量替换示例...");
        PromptTemplate multiVarTemplate = PromptTemplate.from(
                "{{greeting}}，{{userName}}！\n" +
                        "您有 {{unreadCount}} 条未读消息，其中 {{importantCount}} 条是重要消息。\n" +
                        "最后登录时间: {{lastLogin}}"
        );

        Map<String, Object> multiVars = Map.of(
                "greeting", "早上好",
                "userName", "李四",
                "unreadCount", 5,
                "importantCount", 2,
                "lastLogin", "2024-01-15 09:30:00"
        );

        String multiVarResult = multiVarTemplate.apply(multiVars).text();
        System.out.println("   多变量模板结果:");
        System.out.println(multiVarResult);
        System.out.println();

        // 5. 模板与消息结合使用
        System.out.println("5. 模板与消息结合...");
        PromptTemplate contextTemplate = PromptTemplate.from(
                "基于以下上下文回答问题：\n上下文: {{context}}\n问题: {{question}}"
        );

        Map<String, Object> contextVars = Map.of(
                "context", "LangChain4j 是一个Java语言的LLM应用开发框架",
                "question", "LangChain4j 是什么？"
        );

        String contextPrompt = contextTemplate.apply(contextVars).text();
        System.out.println("   结合上下文的提示词:");
        System.out.println(contextPrompt);
        System.out.println();

        System.out.println("=== 演示结束 ===");
        System.out.println();
        System.out.println("要点回顾：");
        System.out.println("- PromptTemplate.from() 用于创建模板");
        System.out.println("- {{variable}} 是变量占位符语法");
        System.out.println("- Map.of() 用于传递变量值");
        System.out.println("- apply() 返回 Prompt 对象，text() 获取字符串");
    }
}
