package com.advancedjava.ai.langchain4j.advanced;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.AiServices;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;

/**
 * LangChain4j AiService 声明式 Agent 演示
 *
 * <p>展示声明式 AiService 模式：通过接口和注解定义 AI 交互，框架自动生成实现。
 */
public class AiServiceAgentDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j AiService 声明式模式演示 ===\n");

        // 1. 介绍 AiService 概念
        System.out.println("1. AiService 概念说明：");
        System.out.println("   AiService 是 LangChain4j 提供的声明式编程模式");
        System.out.println("   通过定义接口 + 注解，让框架自动生成实现类");
        System.out.println("   类似于 Spring Data JPA 的 Repository 模式");
        System.out.println();

        // 2. 展示接口定义
        System.out.println("2. 声明式接口定义示例：");
        System.out.println("   ```java");
        System.out.println("   interface CodeReviewAssistant {");
        System.out.println("       @SystemMessage(\"你是一位资深的Java代码审查专家\")");
        System.out.println("       @UserMessage(\"请审查以下代码：{{code}}\")");
        System.out.println("       String reviewCode(@V(\"code\") String code);");
        System.out.println("   }");
        System.out.println("   ```");
        System.out.println();

        // 3. 创建 AiService 实例
        System.out.println("3. 创建 AiService 实例：");
        System.out.println("   ```java");
        System.out.println("   MockChatLanguageModel model = new MockChatLanguageModel();");
        System.out.println("   CodeReviewAssistant assistant = AiServices.create(");
        System.out.println("       CodeReviewAssistant.class, model);");
        System.out.println("   ```");
        System.out.println();

        // 实际创建（MockChatLanguageModel 不支持完整 AiService，仅演示 API）
        MockChatLanguageModel model = new MockChatLanguageModel("代码审查完成：没有发现明显问题。");

        // 注意：MockChatLanguageModel 不支持完整的 AiService 功能
        // 以下代码展示概念，实际运行会说明这一点
        System.out.println("4. 实际调用演示（概念性）：");
        System.out.println("   // 在真实场景中，调用如下：");
        System.out.println("   String code = \"public void hello() { System.out.println('Hi'); }\";");
        System.out.println("   String result = assistant.reviewCode(code);");
        System.out.println("   // 框架会自动将 @SystemMessage 和 @UserMessage 合并发送给 LLM");
        System.out.println();

        // 5. 注解说明
        System.out.println("5. 常用注解说明：");
        System.out.println();
        System.out.println("   @SystemMessage - 设置系统消息（AI 角色/人设）：");
        System.out.println("   ```java");
        System.out.println("   @SystemMessage(\"你是一位友好的客服助手\")");
        System.out.println("   String chat(String message);");
        System.out.println("   ```");
        System.out.println();
        System.out.println("   @UserMessage - 设置用户消息模板：");
        System.out.println("   ```java");
        System.out.println("   @UserMessage(\"请将以下内容翻译成{{language}}：{{text}}\")");
        System.out.println("   String translate(@V(\"language\") String lang,");
        System.out.println("                    @V(\"text\") String text);");
        System.out.println("   ```");
        System.out.println();
        System.out.println("   @V - 变量占位符，用于方法参数映射");
        System.out.println();

        // 6. 高级特性说明
        System.out.println("6. AiService 高级特性：");
        System.out.println();
        System.out.println("   a) 返回类型自动转换：");
        System.out.println("   ```java");
        System.out.println("   interface JsonExtractor {");
        System.out.println("       @UserMessage(\"从这段文字提取关键信息\")");
        System.out.println("       PersonInfo extract(String text); // 自动 JSON 转对象");
        System.out.println("   }");
        System.out.println("   ```");
        System.out.println();
        System.out.println("   b) 工具集成：");
        System.out.println("   ```java");
        System.out.println("   interface SmartAssistant {");
        System.out.println("       @SystemMessage(\"你是一个智能助手\")");
        System.out.println("       String answer(String question);");
        System.out.println("   }");
        System.out.println();
        System.out.println("   SmartAssistant assistant = AiServices.builder(SmartAssistant.class)");
        System.out.println("       .chatLanguageModel(model)");
        System.out.println("       .tools(new CalculatorTool(), new WeatherTool())");
        System.out.println("       .build();");
        System.out.println("   ```");
        System.out.println();
        System.out.println("   c) 记忆功能：");
        System.out.println("   ```java");
        System.out.println("   ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);");
        System.out.println("   Assistant assistant = AiServices.builder(Assistant.class)");
        System.out.println("       .chatLanguageModel(model)");
        System.out.println("       .chatMemory(chatMemory)");
        System.out.println("       .build();");
        System.out.println("   ```");
        System.out.println();

        // 7. 注意事项
        System.out.println("7. 当前 MockChatLanguageModel 限制说明：");
        System.out.println("   - MockChatLanguageModel 仅支持基础的 ChatModel 接口");
        System.out.println("   - 完整的 AiServices 功能需要更复杂的模型支持");
        System.out.println("   - 真实使用时请替换为 OpenAiChatModel、OllamaChatModel 等");
        System.out.println();

        System.out.println("=== 演示结束 ===");
        System.out.println();
        System.out.println("要点回顾：");
        System.out.println("- AiServices.create() 自动生成接口实现");
        System.out.println("- @SystemMessage 设置系统角色");
        System.out.println("- @UserMessage 定义用户消息模板");
        System.out.println("- @V 用于参数变量绑定");
        System.out.println("- 支持工具集成和记忆功能");
    }

    /**
     * 代码审查助手接口示例
     */
    interface CodeReviewAssistant {
        @SystemMessage("你是一位资深的Java代码审查专家，擅长发现潜在的并发问题、" +
                "性能瓶颈和安全漏洞。请用中文输出审查结果，格式清晰。")
        @UserMessage("请审查以下Java代码，找出潜在问题：\n{{code}}")
        String reviewCode(@dev.langchain4j.service.V("code") String code);
    }

    /**
     * 翻译助手接口示例
     */
    interface TranslationAssistant {
        @SystemMessage("你是一位专业的翻译专家，擅长多语言互译。")
        @UserMessage("请将以下内容翻译成{{targetLanguage}}：\n{{text}}")
        String translate(@dev.langchain4j.service.V("targetLanguage") String targetLanguage,
                         @dev.langchain4j.service.V("text") String text);
    }

    /**
     * 数据分析助手接口示例
     */
    interface DataAnalysisAssistant {
        @SystemMessage("你是一位数据分析专家，擅长从数据中提取洞察。")
        @UserMessage("请分析以下数据并提供关键发现：\n{{data}}")
        String analyzeData(@dev.langchain4j.service.V("data") String data);

        @UserMessage("根据{{data}}，给出{{count}}个主要结论")
        String summarize(@dev.langchain4j.service.V("data") String data,
                         @dev.langchain4j.service.V("count") int count);
    }
}
