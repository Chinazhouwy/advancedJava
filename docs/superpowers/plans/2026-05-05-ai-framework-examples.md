# Java AI 框架与协议示例集 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 `com.advancedjava.ai` 下创建 7 个 AI 框架/协议模块的 Java 教学示例（约 34 个文件）

**Architecture:** 每个模块独立子包，demo 文件有 `main()` 方法可独立运行。统一入口 `AiDemoApp.java` 串联所有 demo。所有 demo 使用 MockLLM 而非真实 API key。

**Tech Stack:** Java 17, Maven, Spring Boot 3.3.0, LangChain4j 1.13.0, AgentScope 1.0.11, LangGraph4j 1.8.14, MCP SDK 0.18.1, A2A SDK 0.3.3.Final

---

## 阶段 0：基础设施与依赖

### Task 0.1: 更新 pom.xml — 添加依赖

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加新依赖**

在 `<dependencies>` 中添加以下依赖（按框架分组）：

```xml
        <!-- ===== AI 框架示例依赖 ===== -->
        <!-- LangChain4j -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-core</artifactId>
            <version>1.13.0</version>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j</artifactId>
            <version>1.13.0</version>
        </dependency>

        <!-- LangGraph4j -->
        <dependency>
            <groupId>org.bsc.langgraph4j</groupId>
            <artifactId>langgraph4j-core</artifactId>
            <version>1.8.14</version>
        </dependency>

        <!-- MCP SDK（独立于 Spring AI） -->
        <dependency>
            <groupId>io.modelcontextprotocol.sdk</groupId>
            <artifactId>mcp</artifactId>
            <version>0.18.1</version>
        </dependency>

        <!-- A2A SDK -->
        <dependency>
            <groupId>io.github.a2asdk</groupId>
            <artifactId>a2a-java-sdk-client</artifactId>
            <version>0.3.3.Final</version>
        </dependency>
        <dependency>
            <groupId>io.github.a2asdk</groupId>
            <artifactId>a2a-java-sdk-server-common</artifactId>
            <version>0.3.3.Final</version>
        </dependency>
        <dependency>
            <groupId>io.github.a2asdk</groupId>
            <artifactId>a2a-java-sdk-spec</artifactId>
            <version>0.3.3.Final</version>
        </dependency>
        <dependency>
            <groupId>io.github.a2asdk</groupId>
            <artifactId>a2a-java-sdk-reference-jsonrpc</artifactId>
            <version>0.3.3.Final</version>
        </dependency>
```

- [ ] **Step 2: 验证编译**

Run:
```bash
mvn clean compile -DskipTests 2>&1 | tail -20
```
Expected: `BUILD SUCCESS`

### Task 0.2: 创建包结构及 package-info.java

**Files:**
- Create: `src/main/java/com/advancedjava/ai/package-info.java`
- Create: `src/main/java/com/advancedjava/ai/langchain4j/package-info.java`
- Create: `src/main/java/com/advancedjava/ai/langchain4j/basic/package-info.java`
- Create: `src/main/java/com/advancedjava/ai/langchain4j/advanced/package-info.java`
- Create: 所有模块（agentscope, langgraph4j, adk-patterns, crewai-patterns, mcp, a2a）同理的各层 package-info.java

- [ ] **Step 1: 创建顶层 `com.advancedjava.ai` 包目录**

```bash
mkdir -p src/main/java/com/advancedjava/ai/langchain4j/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/agentscope/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/langgraph4j/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/adkpatterns/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/crewairpatterns/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/mcp/{basic,advanced}
mkdir -p src/main/java/com/advancedjava/ai/a2a/{basic,advanced}
mkdir -p src/test/java/com/advancedjava/ai
```

- [ ] **Step 2: 创建顶层 package-info.java**

`src/main/java/com/advancedjava/ai/package-info.java`:
```java
/**
 * Java AI 框架与协议示例集合。
 *
 * <p>本包及其子包展示主流 Java AI 框架和智能体通信协议的使用方式，
 * 作为「Java 进阶」项目的一部分，侧重于教学演示。
 *
 * <p>子包列表：
 * <ul>
 *   <li>{@code langchain4j} — LangChain4j 基础与高级用法</li>
 *   <li>{@code agentscope} — AgentScope Agent 开发框架</li>
 *   <li>{@code langgraph4j} — LangGraph4j 图工作流</li>
 *   <li>{@code adk-patterns} — ADK 设计模式 Java 等效实现</li>
 *   <li>{@code crewai-patterns} — CrewAI 设计模式 Java 等效实现</li>
 *   <li>{@code mcp} — Model Context Protocol 协议</li>
 *   <li>{@code a2a} — Agent-to-Agent 通信协议</li>
 * </ul>
 */
package com.advancedjava.ai;
```

- [ ] **Step 3: 创建 7 个模块的 package-info.java**

每个模块一个 package-info.java，格式类似：
```java
/**
 * [模块名] — 基础用法示例。
 *
 * <p>本包包含 [模块名] 的核心基础用法演示。
 */
package com.advancedjava.ai.langchain4j.basic;
```

每个模块的 advanced 包同理：
```java
/**
 * [模块名] — 高级用法示例。
 *
 * <p>本包包含 [模块名] 的高级特性演示。
 */
package com.advancedjava.ai.langchain4j.advanced;
```

需要创建的 package-info.java 列表（14 个）：
| 模块 | basic | advanced |
|------|-------|----------|
| langchain4j | ✅ | ✅ |
| agentscope | ✅ | ✅ |
| langgraph4j | ✅ | ✅ |
| adk-patterns | ✅ | ✅ |
| crewai-patterns | ✅ | ✅ |
| mcp | ✅ | ✅ |
| a2a | ✅ | ✅ |

### Task 0.3: MockLLM 工具类（供无 API key 时演示）

**Files:**
- Create: `src/main/java/com/advancedjava/ai/langchain4j/MockChatLanguageModel.java`

- [ ] **Step 1: 创建 MockChatLanguageModel**

实现 `dev.langchain4j.model.chat.ChatLanguageModel` 接口，返回固定响应供演示使用：

```java
package com.advancedjava.ai.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import java.util.List;

/**
 * Mock ChatLanguageModel —— 用于教学演示，不依赖真实 LLM API。
 *
 * <p>返回固定的模拟回复，让示例代码可以在无 API Key 的情况下编译运行。
 * 实际使用时可替换为 {@code OpenAiChatModel}、{@code OllamaChatModel} 等。
 */
public class MockChatLanguageModel implements ChatLanguageModel {

    private final String mockResponse;

    public MockChatLanguageModel(String mockResponse) {
        this.mockResponse = mockResponse;
    }

    public MockChatLanguageModel() {
        this("你好！我是 Mock LLM，这是一个模拟回复。");
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        AiMessage aiMessage = AiMessage.from(mockResponse);
        TokenUsage tokenUsage = new TokenUsage(10, 5, 15);
        return Response.from(aiMessage, tokenUsage);
    }

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler handler) {
        handler.onNext(mockResponse);
        handler.onComplete(new Response<>(AiMessage.from(mockResponse)));
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn clean compile -DskipTests 2>&1 | tail -15`
Expected: `BUILD SUCCESS`

---

## 阶段 1：LangChain4j 模块（4 个 demo 文件）

### Task 1.1: ChatDemo.java — 基础对话

**Files:**
- Create: `src/main/java/com/advancedjava/ai/langchain4j/basic/ChatDemo.java`

- [ ] **Step 1: 创建 ChatDemo.java**

```java
package com.advancedjava.ai.langchain4j.basic;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import java.util.List;

/**
 * LangChain4j 基础对话演示。
 *
 * <p>展示 ChatLanguageModel 的核心用法：
 * 1. 创建 Model 实例
 * 2. 构造用户消息
 * 3. 发送并获取回复
 * 4. 流式输出
 *
 * <p>关键概念：ChatLanguageModel 是所有 LLM 交互的统一接口。
 */
public class ChatDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 基础对话演示 ===\n");

        // 1. 创建 Model 实例（此处用 Mock，实际可用 OpenAiChatModel 等）
        ChatLanguageModel model = new MockChatLanguageModel(
                "Java 17 引入了密封类(sealed class)、模式匹配(pattern matching)等特性。"
        );

        // 2. 构造用户消息并发送
        UserMessage userMessage = UserMessage.from("Java 17 有哪些新特性？");
        System.out.println("用户: " + userMessage.text());

        Response<AiMessage> response = model.generate(List.of(userMessage));
        System.out.println("AI: " + response.content().text());

        // 3. 展示 Token 用量
        System.out.println("\nToken 统计:");
        System.out.println("  输入 tokens: " + response.tokenUsage().inputTokenCount());
        System.out.println("  输出 tokens: " + response.tokenUsage().outputTokenCount());
        System.out.println("  总 tokens: " + response.tokenUsage().totalTokenCount());

        // 4. 流式输出演示
        System.out.println("\n--- 流式输出演示 ---");
        model.generate(List.of(UserMessage.from("请逐字输出「你好世界」")),
                new dev.langchain4j.model.StreamingResponseHandler<>() {
                    @Override
                    public void onNext(String token) {
                        System.out.print(token);
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        System.out.println("\n\n流式输出完成！");
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.err.println("流式输出出错: " + error.getMessage());
                    }
                });

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 创建单元测试**

`src/test/java/com/advancedjava/ai/langchain4j/basic/ChatDemoTest.java`:
```java
package com.advancedjava.ai.langchain4j.basic;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;

public class ChatDemoTest {

    @Test
    public void shouldReturnMockResponse() {
        ChatLanguageModel model = new MockChatLanguageModel("测试回复");
        var response = model.generate(List.of(UserMessage.from("你好")));
        assertNotNull(response);
        assertNotNull(response.content());
        assertTrue(response.content().text().contains("测试回复"));
    }
}
```

- [ ] **Step 3: 验证编译 + 测试**

Run: `mvn compile -DskipTests && mvn test -Dtest=ChatDemoTest -pl .`
Expected: `BUILD SUCCESS` + `Tests run: 1, Failures: 0`

### Task 1.2: PromptTemplateDemo.java — 提示词模板

**Files:**
- Create: `src/main/java/com/advancedjava/ai/langchain4j/basic/PromptTemplateDemo.java`

- [ ] **Step 1: 创建 PromptTemplateDemo.java**

```java
package com.advancedjava.ai.langchain4j.basic;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import java.util.Map;

/**
 * LangChain4j 提示词模板演示。
 *
 * <p>展示 PromptTemplate 的使用：
 * 1. 定义带变量的模板
 * 2. 变量替换
 * 3. 系统性提示词构造
 */
public class PromptTemplateDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 提示词模板演示 ===\n");

        ChatLanguageModel model = new MockChatLanguageModel(
                "根据您提供的技术栈，我建议学习 Spring AI 和 LangChain4j。"
        );

        // 1. 基本模板：用 {{it}} 占位
        PromptTemplate template = PromptTemplate.from("请用{language}写一个{type}示例");
        Prompt prompt = template.apply(Map.of("language", "Java 17", "type", "Hello World"));
        System.out.println("模板输出: " + prompt.text());

        // 2. 系统级提示词
        PromptTemplate systemTemplate = PromptTemplate.from(
                "你是一位{role}专家。请用{language}回答以下问题。"
        );
        String systemPrompt = systemTemplate.apply(Map.of(
                "role", "Java",
                "language", "中文"
        )).text();
        System.out.println("系统提示词: " + systemPrompt);

        // 3. 实际调用
        var response = model.generate(java.util.List.of(
                dev.langchain4j.data.message.SystemMessage.from(systemPrompt),
                dev.langchain4j.data.message.UserMessage.from("推荐几个 AI 框架？")
        ));
        System.out.println("\nAI 回复: " + response.content().text());

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 1.3: ToolCallingDemo.java — 工具/函数调用

**Files:**
- Create: `src/main/java/com/advancedjava/ai/langchain4j/advanced/ToolCallingDemo.java`

- [ ] **Step 1: 创建 ToolCallingDemo.java**

```java
package com.advancedjava.ai.langchain4j.advanced;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import java.util.List;

/**
 * LangChain4j 工具/函数调用演示。
 *
 * <p>展示 Tool 的核心模式：
 * 1. 定义工具方法（@Tool 注解）
 * 2. 将工具注册到 Model
 * 3. Model 根据用户问题自动选择合适的工具
 *
 * <p>关键概念：Tool Calling 让 LLM 可以调用外部函数获取实时信息。
 */
public class ToolCallingDemo {

    /** 天气查询工具 */
    static class WeatherTools {
        @Tool("根据城市名查询当前天气")
        public String getWeather(String city) {
            return city + " 当前天气：晴，25°C，湿度 60%";
        }
    }

    /** 计算器工具 */
    static class CalculatorTools {
        @Tool("执行数学计算")
        public double calculate(String expression) {
            // 注：生产环境应使用安全表达式求值库
            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                return Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
            }
            throw new IllegalArgumentException("不支持的表达式: " + expression);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 工具调用演示 ===\n");

        // 1. 定义工具
        WeatherTools weatherTools = new WeatherTools();
        CalculatorTools calcTools = new CalculatorTools();

        // 2. Mock 模式下直接演示工具逻辑
        System.out.println("--- 工具 1: 天气查询 ---");
        String weather = weatherTools.getWeather("北京");
        System.out.println("结果: " + weather);

        System.out.println("\n--- 工具 2: 数学计算 ---");
        double result = calcTools.calculate("3 + 5");
        System.out.println("3 + 5 = " + result);

        // 3. 展示 ToolSpecification 元数据
        System.out.println("\n--- 工具元数据 ---");
        System.out.println("WeatherTools.getWeather 参数: city (String)");
        System.out.println("CalculatorTools.calculate 参数: expression (String)");

        // 4. 模拟 LLM 调用工具的场景
        System.out.println("\n--- LLM 调用工具模拟 ---");
        ChatLanguageModel model = new MockChatLanguageModel(
                "我已查询到北京的天气信息：晴，25°C。"
        );
        Response<AiMessage> response = model.generate(List.of(
                UserMessage.from("北京今天天气怎么样？")
        ));
        System.out.println("用户: 北京今天天气怎么样？");
        System.out.println("AI: " + response.content().text());

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 1.4: AiServiceAgentDemo.java — 声明式 Agent

**Files:**
- Create: `src/main/java/com/advancedjava/ai/langchain4j/advanced/AiServiceAgentDemo.java`

- [ ] **Step 1: 创建 AiServiceAgentDemo.java**

```java
package com.advancedjava.ai.langchain4j.advanced;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;

/**
 * LangChain4j 声明式 Agent 演示。
 *
 * <p>展示 @AiService 注解的核心模式：
 * 1. 定义带注解的接口
 * 2. AiServices 自动生成实现
 * 3. 声明式 Agent 开发
 *
 * <p>关键概念：AiService 是 LangChain4j 对 Agent 模式的声明式抽象。
 */
public class AiServiceAgentDemo {

    /** 声明式 Agent 接口 */
    interface TranslationAgent {
        @SystemMessage("你是一个专业翻译。将用户输入翻译成{targetLanguage}。")
        @UserMessage("请翻译：{{text}}")
        String translate(String text, String targetLanguage);
    }

    interface SummaryAgent {
        @SystemMessage("你是一个文本摘要专家。用3句话概括用户输入。")
        String summarize(String text);
    }

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 声明式 Agent 演示 ===\n");

        // 1. 创建 Model
        ChatLanguageModel model = new MockChatLanguageModel("这是声明式 Agent 的模拟回复。");

        // 2. 动态创建 Agent 实例
        // 注：MockChatLanguageModel 不支持完整的 AiServices 功能，
        // 此处展示接口定义方式和 AiServices 使用模式

        System.out.println("--- 翻译 Agent ---");
        System.out.println("接口 TranslationAgent 定义:");
        System.out.println("  @SystemMessage: 你是一个专业翻译...");
        System.out.println("  @UserMessage: 请翻译：{{text}}");
        System.out.println("  参数: text（待翻译文本）, targetLanguage（目标语言）");

        System.out.println("\n创建方式（需要真实 LLM 支持）:");
        System.out.println("  TranslationAgent agent = AiServices.create(TranslationAgent.class, model);");
        System.out.println("  String result = agent.translate(\"Hello World\", \"中文\");");

        System.out.println("\n--- 摘要 Agent ---");
        System.out.println("接口 SummaryAgent 定义:");
        System.out.println("  @SystemMessage: 你是一个文本摘要专家...");

        System.out.println("\n创建方式:");
        System.out.println("  SummaryAgent agent = AiServices.create(SummaryAgent.class, model);");
        System.out.println("  String result = agent.summarize(\"很长的一段文本...\");");

        System.out.println("\n--- 声明式 Agent 的优势 ---");
        System.out.println("1. 纯接口定义，无实现类");
        System.out.println("2. 编译期类型安全");
        System.out.println("3. Agent 行为通过注解声明");
        System.out.println("4. 易于测试和 Mock");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 2：AgentScope 模块（4 个 demo 文件）

### Task 2.1: AgentChatDemo.java — Agent 基础对话

**Files:**
- Create: `src/main/java/com/advancedjava/ai/agentscope/basic/AgentChatDemo.java`

- [ ] **Step 1: 创建 AgentChatDemo.java**

```java
package com.advancedjava.ai.agentscope.basic;

import io.agentscope.agent.Agent;
import io.agentscope.message.Msg;
import io.agentscope.message.TextMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgentScope Agent 基础对话演示。
 *
 * <p>展示 Agent 的核心用法：
 * 1. 自定义 Agent 类
 * 2. 消息收发
 * 3. 对话管理
 */
public class AgentChatDemo {

    private static final Logger log = LoggerFactory.getLogger(AgentChatDemo.class);

    /** 自定义 Agent：一个简单的对话助手 */
    static class ChatAssistant extends Agent {
        public ChatAssistant(String name) {
            super(name);
        }

        @Override
        public Msg reply(Msg msg) {
            String userInput = msg.getContent();
            log.info("{} 收到消息: {}", getName(), userInput);

            // 模拟 AI 回复（无真实 LLM）
            String reply = "我是 " + getName() + "，收到你的消息：「" + userInput + "」。"
                    + "（这是 AgentScope 的 Agent 模式演示）";
            return new TextMsg(getName(), reply);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AgentScope Agent 基础对话演示 ===\n");

        // 1. 创建 Agent 实例
        ChatAssistant assistant = new ChatAssistant("Assistant");

        // 2. 发送消息并获取回复
        Msg userMsg = new TextMsg("user", "今天天气怎么样？");
        System.out.println("用户: " + userMsg.getContent());

        Msg reply = assistant.reply(userMsg);
        System.out.println("Assistant: " + reply.getContent());

        // 3. 多轮对话
        System.out.println("\n--- 多轮对话 ---");
        Msg secondMsg = new TextMsg("user", "帮我写一个 Java 示例");
        System.out.println("用户: " + secondMsg.getContent());
        Msg secondReply = assistant.reply(secondMsg);
        System.out.println("Assistant: " + secondReply.getContent());

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 2.2: ToolAgentDemo.java — 带工具的 Agent

**Files:**
- Create: `src/main/java/com/advancedjava/ai/agentscope/basic/ToolAgentDemo.java`

- [ ] **Step 1: 创建 ToolAgentDemo.java**

```java
package com.advancedjava.ai.agentscope.basic;

import io.agentscope.agent.Agent;
import io.agentscope.message.Msg;
import io.agentscope.message.TextMsg;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * AgentScope 带工具的 Agent 演示。
 *
 * <p>展示 Agent 如何绑定工具调用：
 * 1. 注册工具函数
 * 2. Agent 根据用户输入自动选择工具
 * 3. 多工具路由
 */
public class ToolAgentDemo {

    /** 工具注册表 */
    static class ToolRegistry {
        private final Map<String, Function<String, String>> tools = new HashMap<>();

        public void register(String name, String description, Function<String, String> executor) {
            tools.put(name, executor);
            System.out.println("  注册工具: " + name + " —— " + description);
        }

        public String execute(String name, String arg) {
            Function<String, String> tool = tools.get(name);
            if (tool == null) {
                return "未知工具: " + name;
            }
            return tool.apply(arg);
        }

        public Map<String, Function<String, String>> getTools() {
            return tools;
        }
    }

    /** 带工具的 Agent */
    static class ToolAgent extends Agent {
        private final ToolRegistry registry;

        public ToolAgent(String name, ToolRegistry registry) {
            super(name);
            this.registry = registry;
        }

        @Override
        public Msg reply(Msg msg) {
            String content = msg.getContent();
            System.out.println("  " + getName() + " 分析: 用户输入「" + content + "」");

            // 简单的工具路由逻辑
            if (content.contains("天气") || content.contains("weather")) {
                String result = registry.execute("get_weather", content);
                return new TextMsg(getName(), result);
            } else if (content.contains("计算") || content.contains("+")) {
                String result = registry.execute("calculator", content);
                return new TextMsg(getName(), result);
            } else {
                return new TextMsg(getName(), "你好！我可以查询天气或执行计算。");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AgentScope 工具 Agent 演示 ===\n");

        // 1. 创建工具注册表
        ToolRegistry registry = new ToolRegistry();
        registry.register("get_weather", "查询城市天气", city -> city + "天气：晴，25°C");
        registry.register("calculator", "数学计算", expr -> {
            if (expr.contains("+")) {
                String[] parts = expr.replace("计算", "").split("\\+");
                double result = Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
                return "计算结果: " + result;
            }
            return "不支持的表达式";
        });

        // 2. 创建带工具的 Agent
        ToolAgent agent = new ToolAgent("ToolAssistant", registry);

        // 3. 测试工具路由
        System.out.println("\n--- 测试 1: 天气查询 ---");
        Msg result1 = agent.reply(new TextMsg("user", "北京天气怎么样？"));
        System.out.println("回复: " + result1.getContent());

        System.out.println("\n--- 测试 2: 计算 ---");
        Msg result2 = agent.reply(new TextMsg("user", "计算 3 + 5"));
        System.out.println("回复: " + result2.getContent());

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 2.3: ReActAgentDemo.java — ReAct 推理模式

**Files:**
- Create: `src/main/java/com/advancedjava/ai/agentscope/advanced/ReActAgentDemo.java`

- [ ] **Step 1: 创建 ReActAgentDemo.java**

```java
package com.advancedjava.ai.agentscope.advanced;

import io.agentscope.agent.Agent;
import io.agentscope.message.Msg;
import io.agentscope.message.TextMsg;
import java.util.ArrayList;
import java.util.List;

/**
 * AgentScope ReAct 推理-行动循环演示。
 *
 * <p>展示 ReAct（Reasoning + Acting）模式：
 * 1. Thought（思考）：分析当前问题
 * 2. Action（行动）：调用工具或获取信息
 * 3. Observation（观察）：获取行动结果
 * 4. 循环直到得出最终答案
 */
public class ReActAgentDemo {

    /** ReAct Agent：带思考-行动-观察循环的 Agent */
    static class ReActAgent extends Agent {
        private int maxIterations = 3;

        public ReActAgent(String name) {
            super(name);
        }

        @Override
        public Msg reply(Msg msg) {
            String question = msg.getContent();
            System.out.println("  问题: " + question);

            // ReAct 循环模拟
            List<String> thoughts = new ArrayList<>();
            String action = null;
            String observation = null;
            String answer = null;

            for (int i = 0; i < maxIterations; i++) {
                System.out.println("\n  === ReAct 迭代 " + (i + 1) + " ===");

                // Step 1: Thought
                String thought = generateThought(question, i);
                thoughts.add(thought);
                System.out.println("  Thought: " + thought);

                // Step 2: Action
                action = generateAction(thought, i);
                System.out.println("  Action: " + action);

                // Step 3: Observation
                observation = executeAction(action);
                System.out.println("  Observation: " + observation);

                // Step 4: 判断是否完成
                if (observation.contains("最终答案")) {
                    answer = observation;
                    break;
                }
            }

            if (answer == null) {
                answer = "经过 " + maxIterations + " 轮推理，最终答案："
                        + "根据查询，北京当前天气为 25°C，建议穿薄外套。";
            }
            return new TextMsg(getName(), answer);
        }

        private String generateThought(String question, int iteration) {
            return switch (iteration) {
                case 0 -> "我需要先了解用户问的是什么问题。";
                case 1 -> "根据已知信息，我需要查询相关数据。";
                case 2 -> "我已经收集了足够的信息，可以给出最终答案。";
                default -> "继续分析...";
            };
        }

        private String generateAction(String thought, int iteration) {
            return switch (iteration) {
                case 0 -> "search_weather(北京)";
                case 1 -> "analyze_data(天气数据)";
                case 2 -> "final_answer(综合所有信息)";
                default -> "unknown";
            };
        }

        private String executeAction(String action) {
            if (action.startsWith("search_weather")) {
                return "观察到天气数据：北京，晴，25°C，湿度 60%";
            } else if (action.startsWith("analyze_data")) {
                return "观察到分析结果：温度适宜，无降水";
            } else if (action.startsWith("final_answer")) {
                return "最终答案：北京今天天气晴朗，气温 25°C，适合户外活动。";
            }
            return "观察结果为空";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AgentScope ReAct 推理模式演示 ===\n");

        ReActAgent agent = new ReActAgent("ReActAgent");

        Msg result = agent.reply(new TextMsg("user", "北京今天天气如何？适合穿什么？"));
        System.out.println("\n最终回复: " + result.getContent());

        System.out.println("\n=== ReAct 模式总结 ===");
        System.out.println("Thought  →  推理当前状态");
        System.out.println("Action   →  执行具体操作");
        System.out.println("Observation → 观察操作结果");
        System.out.println("循环直到给出最终答案");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 2.4: MultiAgentDemo.java — 多 Agent 协作

**Files:**
- Create: `src/main/java/com/advancedjava/ai/agentscope/advanced/MultiAgentDemo.java`

- [ ] **Step 1: 创建 MultiAgentDemo.java**

```java
package com.advancedjava.ai.agentscope.advanced;

import io.agentscope.agent.Agent;
import io.agentscope.message.Msg;
import io.agentscope.message.TextMsg;
import java.util.Arrays;
import java.util.List;

/**
 * AgentScope 多 Agent 协作演示。
 *
 * <p>展示多 Agent 协作的工作模式：
 * 1. 定义多个专业 Agent
 * 2. 任务分解与分发
 * 3. 结果汇总
 */
public class MultiAgentDemo {

    /** 专业 Agent 基类 */
    static class SpecialistAgent extends Agent {
        private final String expertise;

        public SpecialistAgent(String name, String expertise) {
            super(name);
            this.expertise = expertise;
        }

        public String getExpertise() {
            return expertise;
        }

        @Override
        public Msg reply(Msg msg) {
            return new TextMsg(getName(),
                    "[" + expertise + "专家] 收到任务：「" + msg.getContent() + "」。"
                            + "已完成专业分析。");
        }
    }

    /** 协调 Agent：负责任务分解和结果汇总 */
    static class CoordinatorAgent extends Agent {
        private final List<SpecialistAgent> team;

        public CoordinatorAgent(String name, List<SpecialistAgent> team) {
            super(name);
            this.team = team;
        }

        @Override
        public Msg reply(Msg msg) {
            String task = msg.getContent();
            System.out.println("\n  Coordinator 收到任务: " + task);

            // 1. 任务分解
            System.out.println("  任务分解:");
            for (SpecialistAgent agent : team) {
                System.out.println("    → " + agent.getName() + "（" + agent.getExpertise() + "）");
            }

            // 2. 分发子任务给各 Agent
            StringBuilder summary = new StringBuilder("多 Agent 协作结果：\n");
            for (SpecialistAgent agent : team) {
                String subTask = "分析" + task + "中的" + agent.getExpertise() + "方面";
                Msg subResult = agent.reply(new TextMsg(getName(), subTask));
                summary.append("  ").append(subResult.getContent()).append("\n");
            }

            // 3. 汇总结果
            summary.append("\nCoordinator 汇总完成。");
            return new TextMsg(getName(), summary.toString());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AgentScope 多 Agent 协作演示 ===\n");

        // 1. 创建专业 Agent 团队
        SpecialistAgent architect = new SpecialistAgent("ArchAgent", "架构设计");
        SpecialistAgent coder = new SpecialistAgent("CodeAgent", "代码实现");
        SpecialistAgent tester = new SpecialistAgent("TestAgent", "测试验证");

        // 2. 创建协调 Agent
        CoordinatorAgent coordinator = new CoordinatorAgent(
                "Coordinator",
                Arrays.asList(architect, coder, tester)
        );

        // 3. 提交任务
        Msg result = coordinator.reply(new TextMsg("user", "开发一个用户登录功能"));
        System.out.println("\n最终结果:\n" + result.getContent());

        System.out.println("\n=== 多 Agent 模式总结 ===");
        System.out.println("1. Coordinator 负责任务分解");
        System.out.println("2. 各 Specialist 专注自身领域");
        System.out.println("3. Coordinator 汇总结果返回");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 3：LangGraph4j 模块（4 个 demo 文件）

### Task 3.1: SequentialGraphDemo.java — 顺序图工作流

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/langgraph4j/basic/SequentialGraphDemo.java`**

展示 LangGraph4j 的顺序节点图（需要了解 `org.bsc.langgraph4j` API）。使用简单的 Graph 构建顺序工作流：输入 → 处理步骤1 → 处理步骤2 → 输出。

```java
package com.advancedjava.ai.langgraph4j.basic;

import org.bsc.langgraph4j.GraphState;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.action.EdgeAction;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LangGraph4j 顺序图工作流演示。
 *
 * <p>展示 LangGraph4j 的核心概念：
 * 1. StateGraph — 状态化图工作流
 * 2. Node — 处理节点
 * 3. Edge — 节点间的连接
 * 4. 顺序执行流程
 */
public class SequentialGraphDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== LangGraph4j 顺序图工作流演示 ===\n");

        // 1. 构建图：输入 → 处理 → 验证 → 输出
        StateGraph<GraphState> graph = new StateGraph<>(GraphState::new);

        // 2. 添加节点
        graph.setEntryPoint("input_node");
        // 注意：以下 API 可能因 langgraph4j 版本而异，请参考官方文档调整
        System.out.println("LangGraph4j 图工作流的核心概念：");
        System.out.println("  1. StateGraph — 管理图的状态");
        System.out.println("  2. Node — 每个节点执行一个处理步骤");
        System.out.println("  3. Edge — 定义节点间的连接");
        System.out.println("  4. ConditionalEdge — 条件路由");

        System.out.println("\n--- 顺序工作流示例 ---");
        System.out.println("输入 → 数据清洗 → 特征提取 → 模型预测 → 输出格式化 → 结束");
        System.out.println("\n每个节点都接收 State 并返回 State 的增量更新。");
        System.out.println("状态通过 Map<String,Object> 在节点间传递。");

        System.out.println("\n--- 典型使用场景 ---");
        System.out.println("• 数据处理管道 (ETL)");
        System.out.println("• 多步推理链");
        System.out.println("• 文档处理工作流");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

注意：由于 `org.bsc.langgraph4j:langgraph4j-core` 是第一次引入，API 确认后需要修正以上代码。请在编译后根据实际 API 调整。

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -20`
Expected: `BUILD SUCCESS`（如果 API 不匹配，修正调用代码）

### Task 3.2: StateGraphDemo.java — 状态图

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/langgraph4j/basic/StateGraphDemo.java`**

```java
package com.advancedjava.ai.langgraph4j.basic;

import org.bsc.langgraph4j.GraphState;
import org.bsc.langgraph4j.StateGraph;
import java.util.HashMap;
import java.util.Map;

/**
 * LangGraph4j 状态图工作流演示。
 *
 * <p>展示带状态的图工作流：
 * 1. 自定义 State 定义
 * 2. 节点间状态共享
 * 3. 状态的读取与更新
 */
public class StateGraphDemo {

    /** 自定义状态 */
    static class ProcessingState {
        private final Map<String, Object> data = new HashMap<>();

        public void set(String key, Object value) {
            data.put(key, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(String key) {
            return (T) data.get(key);
        }

        public Map<String, Object> getAll() {
            return new HashMap<>(data);
        }

        @Override
        public String toString() {
            return "ProcessingState" + data;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LangGraph4j 状态图演示 ===\n");

        // 状态模拟
        ProcessingState state = new ProcessingState();
        state.set("input", "Hello LangGraph4j");
        state.set("step", "initialized");
        state.set("count", 0);

        System.out.println("初始状态: " + state);

        // 模拟节点执行
        System.out.println("\n--- 节点执行模拟 ---");

        // Node 1: 预处理
        state.set("processed", state.get("input") + " [已清洗]");
        state.set("step", "preprocessed");
        state.set("count", (int) state.get("count") + 1);
        System.out.println("节点1 (预处理): " + state.get("processed"));

        // Node 2: 转换
        state.set("transformed", ((String) state.get("processed")).toUpperCase());
        state.set("step", "transformed");
        state.set("count", (int) state.get("count") + 1);
        System.out.println("节点2 (转换): " + state.get("transformed"));

        // Node 3: 输出
        state.set("output", "最终结果: " + state.get("transformed"));
        state.set("step", "completed");
        state.set("count", (int) state.get("count") + 1);
        System.out.println("节点3 (输出): " + state.get("output"));

        System.out.println("\n最终状态: " + state);
        System.out.println("执行节点数: " + state.get("count"));
        System.out.println("当前步骤: " + state.get("step"));

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 3.3: ConditionalGraphDemo.java — 条件分支

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/langgraph4j/advanced/ConditionalGraphDemo.java`**

```java
package com.advancedjava.ai.langgraph4j.advanced;

import java.util.Random;

/**
 * LangGraph4j 条件分支图工作流演示。
 *
 * <p>展示图工作流中的条件路由：
 * 1. 根据条件选择不同路径
 * 2. 条件边（Conditional Edge）
 * 3. 分支与合并
 */
public class ConditionalGraphDemo {

    enum Route { APPROVED, REJECTED, REVIEW }

    static class Document {
        final String title;
        final int qualityScore;

        Document(String title, int qualityScore) {
            this.title = title;
            this.qualityScore = qualityScore;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== LangGraph4j 条件分支演示 ===\n");

        // 模拟文档审核工作流
        Random random = new Random(42);

        for (int i = 1; i <= 3; i++) {
            Document doc = new Document("文档#" + i, random.nextInt(100));
            System.out.println("--- 处理 " + doc.title + "（质量分: " + doc.qualityScore + "）---");

            // 条件路由
            Route route = routeDocument(doc.qualityScore);
            System.out.println("  路由决策 → " + route);

            switch (route) {
                case APPROVED -> {
                    System.out.println("  执行: 自动通过 → 归档处理");
                }
                case REJECTED -> {
                    System.out.println("  执行: 自动拒绝 → 返回修改");
                }
                case REVIEW -> {
                    System.out.println("  执行: 人工审核 → 分配给审核员");
                }
            }
            System.out.println();
        }

        System.out.println("--- 条件图工作流总结 ---");
        System.out.println("ConditionalEdge: 根据 State 内容选择下一个节点");
        System.out.println("典型场景: 文档审核、客服工单分类、内容审批流程");

        System.out.println("\n=== 演示结束 ===");
    }

    static Route routeDocument(int qualityScore) {
        if (qualityScore >= 80) return Route.APPROVED;
        if (qualityScore >= 50) return Route.REVIEW;
        return Route.REJECTED;
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 3.4: AgentLoopDemo.java — Agent 循环

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/langgraph4j/advanced/AgentLoopDemo.java`**

```java
package com.advancedjava.ai.langgraph4j.advanced;

/**
 * LangGraph4j Agent 循环图工作流演示。
 *
 * <p>展示 Agent 循环的核心模式（Think → Act → Observe 循环）：
 * 1. 用图工作流表达 Agent 的推理循环
 * 2. 条件边判断是否继续循环
 * 3. Agent as Graph 的设计思想
 */
public class AgentLoopDemo {

    private static int iteration = 0;
    private static final int MAX_ITERATIONS = 3;

    public static void main(String[] args) {
        System.out.println("=== LangGraph4j Agent 循环图演示 ===\n");

        String task = "查询北京天气并决定是否适合户外运动";

        System.out.println("任务: " + task);
        System.out.println("\n--- Agent 循环开始 ---");

        String thought, action, observation, result = null;

        while (iteration < MAX_ITERATIONS && result == null) {
            iteration++;
            System.out.println("\n迭代 #" + iteration);
            System.out.println("  ┌─────────────────────────────────────");

            // Think
            thought = switch (iteration) {
                case 1 -> "我需要查询今天的天气情况";
                case 2 -> "我需要分析天气数据是否适合运动";
                case 3 -> "综合所有信息，给出建议";
                default -> "继续分析...";
            };
            System.out.println("  │ Thought: " + thought);

            // Act
            action = switch (iteration) {
                case 1 -> "查询工具: get_weather(北京)";
                case 2 -> "分析工具: analyze_conditions(25°C, 晴)";
                case 3 -> "→ 条件满足，进入 Final 节点";
                default -> "";
            };
            System.out.println("  │ Action:  " + action);

            // Observe
            observation = switch (iteration) {
                case 1 -> "天气数据: 北京 25°C 晴";
                case 2 -> "分析结果: 温度适宜，无降水，适合运动";
                case 3 -> null;  // 无观察，直接输出
                default -> "";
            };

            if (observation != null) {
                System.out.println("  │ Observe: " + observation);
            }

            System.out.println("  └─────────────────────────────────────");

            // 决策：是否结束循环
            if (iteration == MAX_ITERATIONS) {
                result = "推荐: 北京今天 25°C 晴朗，非常适合户外运动！建议户外跑步或骑行。";
            }
        }

        System.out.println("\n最终结果:");
        System.out.println("  " + result);

        System.out.println("\n--- Agent 循环结构 ---");
        System.out.println("  ┌─────────┐");
        System.out.println("  │  Start  │");
        System.out.println("  └────┬────┘");
        System.out.println("       ↓");
        System.out.println("  ┌─────────┐     ┌──────────┐");
        System.out.println("  │  Think  │────→│   Act    │");
        System.out.println("  └─────────┘     └────┬─────┘");
        System.out.println("       ↑               ↓");
        System.out.println("  ┌─────────┐     ┌──────────┐");
        System.out.println("  │ Observe │←────│  Result  │───→ [继续?] ──→ Final");
        System.out.println("  └─────────┘     └──────────┘");
        System.out.println("                        ↑ no → 回到 Think");
        System.out.println("                        └ yes → 输出");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 4：ADK Patterns 模块（2 个 demo 文件）

### Task 4.1: AgentDefPattern.java — Agent 定义模式

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/adk-patterns/basic/AgentDefPatternDemo.java`**

```java
package com.advancedjava.ai.adkpatterns.basic;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * ADK (Agent Development Kit) 设计模式 —— Agent 定义模式。
 *
 * <p>本示例展示 Google ADK 的核心设计思想，使用 Java 等效实现。
 * 原始框架为 Python 实现，无官方 Java 移植。
 *
 * <p>ADK 的 Agent 定义模式包含：
 * 1. name — Agent 名称
 * 2. instruction — 系统指令
 * 3. tools — 可用工具列表
 * 4. model — 使用的 LLM 配置
 * 5. delegation — 可委派的子 Agent
 */
public class AgentDefPatternDemo {

    /** ADK 风格的 Agent 模型 */
    static class AgentDef {
        private final String name;
        private final String instruction;
        private final Map<String, Function<String, String>> tools;
        private final Map<String, AgentDef> subAgents;

        public AgentDef(String name, String instruction) {
            this.name = name;
            this.instruction = instruction;
            this.tools = new HashMap<>();
            this.subAgents = new HashMap<>();
        }

        public AgentDef registerTool(String name, Function<String, String> tool) {
            tools.put(name, tool);
            return this;
        }

        public AgentDef registerSubAgent(AgentDef agent) {
            subAgents.put(agent.name, agent);
            return this;
        }

        public String getName() { return name; }
        public String getInstruction() { return instruction; }
        public Map<String, Function<String, String>> getTools() { return tools; }
        public Map<String, AgentDef> getSubAgents() { return subAgents; }
    }

    public static void main(String[] args) {
        System.out.println("=== ADK Agent 定义模式演示 ===\n");
        System.out.println("（本示例为 Java 等效实现，原始框架为 Google ADK Python）\n");

        // 1. 定义 Customer Support Agent
        AgentDef supportAgent = new AgentDef(
                "customer_support",
                "你是客户支持 Agent。帮助用户解决产品使用问题。"
        );
        supportAgent.registerTool("search_kb", query -> "知识库搜索结果: " + query);

        // 2. 定义子 Agent
        AgentDef billingAgent = new AgentDef(
                "billing_agent",
                "你负责处理账单相关问题。"
        );
        billingAgent.registerTool("check_invoice", id -> "发票 #" + id + " 状态：已支付");

        AgentDef techAgent = new AgentDef(
                "tech_agent",
                "你负责处理技术问题。"
        );
        techAgent.registerTool("check_system", service -> service + " 运行状态：正常");

        // 3. Agent 委派关系
        supportAgent.registerSubAgent(billingAgent);
        supportAgent.registerSubAgent(techAgent);

        // 4. 输出 Agent 定义结构
        System.out.println("Agent: " + supportAgent.getName());
        System.out.println("  Instruction: " + supportAgent.getInstruction());
        System.out.println("  Tools: " + supportAgent.getTools().keySet());
        System.out.println("  Sub-Agents:");
        for (AgentDef sub : supportAgent.getSubAgents().values()) {
            System.out.println("    - " + sub.getName());
            System.out.println("      Instruction: " + sub.getInstruction());
            System.out.println("      Tools: " + sub.getTools().keySet());
        }

        System.out.println("\n--- ADK Agent 定义模式关键特点 ---");
        System.out.println("1. Agent 通过 Builder 模式链式构建");
        System.out.println("2. 工具通过 register_tool 注册");
        System.out.println("3. 子 Agent 通过 delegation 机制委派任务");
        System.out.println("4. 每个 Agent 有独立的 instruction 定义行为");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 4.2: AgentDelegation.java — Agent 委派模式

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/adk-patterns/advanced/AgentDelegationDemo.java`**

```java
package com.advancedjava.ai.adkpatterns.advanced;

import com.advancedjava.ai.adkpatterns.basic.AgentDefPatternDemo.AgentDef;
import java.util.function.Function;

/**
 * ADK Agent 委派模式演示。
 *
 * <p>展示 ADK 的 Agent 委派机制：
 * 1. 主 Agent 接收任务
 * 2. 分析任务类型
 * 3. 委派给对应子 Agent
 * 4. 汇总结果
 */
public class AgentDelegationDemo {

    interface DelegationHandler {
        String handle(String task, AgentDef agent);
    }

    public static void main(String[] args) {
        System.out.println("=== ADK Agent 委派模式演示 ===\n");

        // 1. 创建 Agent 定义
        AgentDef billingAgent = new AgentDef("billing_agent", "账单处理")
                .registerTool("check_invoice", id -> "发票 #" + id + "：已支付 $299");
        AgentDef techAgent = new AgentDef("tech_agent", "技术支持")
                .registerTool("check_system", s -> s + " 状态正常");
        AgentDef mainAgent = new AgentDef("main_agent", "总客服")
                .registerSubAgent(billingAgent)
                .registerSubAgent(techAgent);

        // 2. 委派逻辑
        DelegationHandler delegator = (task, agent) -> {
            System.out.println("  主 Agent 分析: 任务类型 = " + detectTaskType(task));

            // 根据任务类型委派
            for (AgentDef sub : agent.getSubAgents().values()) {
                if (task.contains(sub.getName().replace("_agent", ""))) {
                    System.out.println("  委派给: " + sub.getName());
                    System.out.println("  子 Agent Instruction: " + sub.getInstruction());
                    return sub.getName() + " 已处理: 「" + task + "」完成";
                }
            }
            return "由主 Agent 自行处理: " + task;
        };

        // 3. 测试委派
        System.out.println("--- 测试 1: 账单问题 ---");
        String result1 = delegator.handle("查询发票 #2024-001", mainAgent);
        System.out.println("  结果: " + result1 + "\n");

        System.out.println("--- 测试 2: 技术问题 ---");
        String result2 = delegator.handle("系统登录失败", mainAgent);
        System.out.println("  结果: " + result2 + "\n");

        System.out.println("--- ADK 委派模式总结 ---");
        System.out.println("• 主 Agent 负责任务路由");
        System.out.println("• 子 Agent 专注特定领域");
        System.out.println("• 通过 Delegation 实现可扩展架构");

        System.out.println("\n=== 演示结束 ===");
    }

    static String detectTaskType(String task) {
        if (task.contains("发票") || task.contains("账单") || task.contains("支付")) {
            return "billing";
        }
        if (task.contains("登录") || task.contains("系统") || task.contains("错误")) {
            return "tech";
        }
        return "general";
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 5：CrewAI Patterns 模块（2 个 demo 文件）

### Task 5.1: RoleBasedAgent.java — 角色 Agent 模式

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/crewai-patterns/basic/RoleBasedAgentDemo.java`**

```java
package com.advancedjava.ai.crewairpatterns.basic;

import java.util.ArrayList;
import java.util.List;

/**
 * CrewAI 角色 Agent 模式演示。
 *
 * <p>本示例展示 CrewAI 的核心设计思想，使用 Java 等效实现。
 * 原始框架为 Python 实现，无官方 Java 移植。
 *
 * <p>CrewAI 的角色 Agent 模式包含：
 * 1. role — 角色定义
 * 2. goal — 目标描述
 * 3. backstory — 背景故事
 * 4. tools — 可用工具
 * 5. allow_delegation — 是否允许委派
 */
public class RoleBasedAgentDemo {

    /** CrewAI 风格的 Agent */
    static class CrewAgent {
        private final String role;
        private final String goal;
        private final String backstory;
        private final List<String> tools;
        private final boolean allowDelegation;

        public CrewAgent(String role, String goal, String backstory) {
            this.role = role;
            this.goal = goal;
            this.backstory = backstory;
            this.tools = new ArrayList<>();
            this.allowDelegation = true;
        }

        public CrewAgent addTool(String tool) {
            tools.add(tool);
            return this;
        }

        public String execute(String task) {
            return String.format("[%s] 基于目标「%s」，执行任务: %s",
                    role, goal, task);
        }

        @Override
        public String toString() {
            return String.format("CrewAgent{role='%s', goal='%s', tools=%s}",
                    role, goal, tools);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CrewAI 角色 Agent 模式演示 ===\n");
        System.out.println("（本示例为 Java 等效实现，原始框架为 CrewAI Python）\n");

        // 1. 定义角色 Agent
        CrewAgent analyst = new CrewAgent(
                "数据分析师",
                "分析数据并提供 actionable insights",
                "你是拥有 10 年经验的数据分析师，擅长从数据中发现模式。"
        ).addTool("python_analysis").addTool("visualization");

        CrewAgent writer = new CrewAgent(
                "技术写手",
                "将技术分析转化为易懂的报告",
                "你是资深技术写手，擅长将复杂概念简单化。"
        ).addTool("grammar_check").addTool("markdown");

        // 2. 展示 Agent 定义
        System.out.println("Agent 定义:");
        System.out.println("  " + analyst);
        System.out.println("  " + writer);

        // 3. 执行任务
        System.out.println("\n--- 任务执行 ---");
        String analysisResult = analyst.execute("分析 2024 年销售数据");
        System.out.println("  " + analysisResult);

        String writingResult = writer.execute("撰写数据分析报告");
        System.out.println("  " + writingResult);

        System.out.println("\n--- CrewAI 角色模式关键特点 ---");
        System.out.println("1. role — 明确的角色定位");
        System.out.println("2. goal — 每个 Agent 有具体目标");
        System.out.println("3. backstory — 角色背景定义行为模式");
        System.out.println("4. tools — 按角色配置工具集");
        System.out.println("5. delegation — Agent 间可互相委派任务");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 5.2: CrewOrchestration.java — 团队编排

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/crewai-patterns/advanced/CrewOrchestrationDemo.java`**

```java
package com.advancedjava.ai.crewairpatterns.advanced;

import com.advancedjava.ai.crewairpatterns.basic.RoleBasedAgentDemo.CrewAgent;
import java.util.Arrays;
import java.util.List;

/**
 * CrewAI 团队编排模式演示。
 *
 * <p>展示 CrewAI 的团队编排机制：
 * 1. Crew — Agent 团队
 * 2. Process — 执行流程（顺序/层级）
 * 3. Task — 分配给 Agent 的任务
 * 4. 结果汇总
 */
public class CrewOrchestrationDemo {

    /** CrewAI 风格的团队 */
    static class Crew {
        private final String name;
        private final List<CrewAgent> agents;
        private final Process process;

        enum Process { SEQUENTIAL, HIERARCHICAL }

        public Crew(String name, List<CrewAgent> agents, Process process) {
            this.name = name;
            this.agents = agents;
            this.process = process;
        }

        public void run(String task) {
            System.out.println("Crew「" + name + "」开始执行任务: " + task);
            System.out.println("执行模式: " + process + "\n");

            if (process == Process.SEQUENTIAL) {
                runSequential(task);
            } else {
                runHierarchical(task);
            }
        }

        private void runSequential(String task) {
            String currentTask = task;
            for (CrewAgent agent : agents) {
                System.out.println("  → " + agent.execute(currentTask));
                // 模拟输出传递
                currentTask = agent.toString() + " 的输出结果";
            }
        }

        private void runHierarchical(String task) {
            if (agents.isEmpty()) return;
            // 第一个 Agent 作为管理者分配任务
            System.out.println("  Manager: " + agents.get(0).execute(task));
            for (int i = 1; i < agents.size(); i++) {
                System.out.println("  Worker: " + agents.get(i).execute(task + "（子任务）"));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CrewAI 团队编排模式演示 ===\n");

        // 1. 创建 Agent 团队
        CrewAgent researcher = new CrewAgent("研究员", "收集和分析信息", "擅长信息检索");
        CrewAgent analyst = new CrewAgent("分析师", "深入分析和洞察", "擅长数据建模");
        CrewAgent writer = new CrewAgent("写手", "撰写报告", "擅长内容创作");

        // 2. 顺序执行
        Crew sequentialCrew = new Crew("研究团队",
                Arrays.asList(researcher, analyst, writer),
                Crew.Process.SEQUENTIAL);
        sequentialCrew.run("分析 AI Agent 框架市场趋势");
        System.out.println();

        // 3. 层级执行
        CrewAgent manager = new CrewAgent("项目经理", "协调团队工作", "有丰富管理经验");
        Crew hierarchicalCrew = new Crew("项目团队",
                Arrays.asList(manager, analyst, writer),
                Crew.Process.HIERARCHICAL);
        hierarchicalCrew.run("完成 Q1 市场分析报告");

        System.out.println("\n=== 团队编排模式总结 ===");
        System.out.println("Sequential: Agent 按顺序执行，上一个输出作为下一个输入");
        System.out.println("Hierarchical: Manager Agent 分配任务，Worker Agent 执行");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 6：MCP 模块（3 个 demo 文件）

### Task 6.1: McpServerDemo.java — MCP Server

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/mcp/basic/McpServerDemo.java`**

展示如何使用 `io.modelcontextprotocol.sdk.mcp` 创建一个 MCP Server，暴露工具（计算器、字符串处理）。

```java
package com.advancedjava.ai.mcp.basic;

/**
 * MCP (Model Context Protocol) Server 演示。
 *
 * <p>展示 MCP 服务端的核心概念：
 * 1. 创建 MCP Server
 * 2. 注册工具 (Tool)
 * 3. 处理工具调用请求
 * 4. 提供资源 (Resource)
 *
 * <p>MCP Server 为 LLM 提供外部工具和资源访问能力。
 */
public class McpServerDemo {

    public static void main(String[] args) {
        System.out.println("=== MCP Server 演示 ===\n");

        System.out.println("MCP Server 启动（概念展示）");
        System.out.println("协议版本: 2025-03-26");
        System.out.println("传输方式: Streamable HTTP\n");

        // 注册工具
        System.out.println("--- 注册的工具列表 ---");
        registerTool("calculator", "执行数学计算",
                "参数: { \"operation\": \"add\", \"a\": 3, \"b\": 5 }");
        registerTool("get_weather", "查询天气",
                "参数: { \"city\": \"Beijing\" }");
        registerTool("search_docs", "搜索文档",
                "参数: { \"query\": \"Java 17\" }");

        System.out.println("\n--- 工具调用示例 ---");
        String addResult = callTool("calculator", "add(3, 5)");
        System.out.println("calculator: 3 + 5 = " + addResult);

        String weatherResult = callTool("get_weather", "Beijing");
        System.out.println("get_weather: " + weatherResult);

        System.out.println("\n--- MCP Server 生命周期 ---");
        System.out.println("1. initialize → 客户端-服务端握手");
        System.out.println("2. tools/list → 客户端获取工具列表");
        System.out.println("3. tools/call → 客户端调用指定工具");
        System.out.println("4. shutdown → 关闭连接");

        System.out.println("\n=== 演示结束 ===");
    }

    static void registerTool(String name, String description, String schema) {
        System.out.printf("  [%s] %s%n", name, description);
    }

    static String callTool(String name, String args) {
        return switch (name) {
            case "calculator" -> "8";
            case "get_weather" -> "北京: 晴, 25°C";
            case "search_docs" -> "找到 5 条关于 Java 17 的结果";
            default -> "未知工具";
        };
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 6.2: McpClientDemo.java — MCP Client

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/mcp/basic/McpClientDemo.java`**

```java
package com.advancedjava.ai.mcp.basic;

/**
 * MCP (Model Context Protocol) Client 演示。
 *
 * <p>展示 MCP 客户端的核心概念：
 * 1. 连接 MCP Server
 * 2. 发现可用工具
 * 3. 调用工具
 * 4. 处理结果
 */
public class McpClientDemo {

    public static void main(String[] args) {
        System.out.println("=== MCP Client 演示 ===\n");

        System.out.println("MCP 客户端初始化...");
        System.out.println("连接 Server: http://localhost:8080/mcp\n");

        // 1. 初始化连接
        System.out.println("--- Step 1: 初始化连接 ---");
        System.out.println("客户端信息: { name: \"mcp-client-demo\", version: \"1.0.0\" }");
        System.out.println("服务端回复: { protocolVersion: \"2025-03-26\", capabilities: { tools: {} } }");
        System.out.println("状态: 已连接\n");

        // 2. 列出工具
        System.out.println("--- Step 2: 列出可用工具 ---");
        String[][] tools = {
                {"calculator", "数学计算", "{ \"type\": \"object\", \"properties\": ... }"},
                {"get_weather", "天气查询", "{ \"type\": \"object\", ... }"}
        };
        for (String[] tool : tools) {
            System.out.printf("  工具: %s (%s)%n", tool[0], tool[1]);
        }

        // 3. 调用工具
        System.out.println("\n--- Step 3: 调用工具 ---");
        String result1 = callMcpTool("calculator", "{\"operation\": \"add\", \"a\": 10, \"b\": 20}");
        System.out.println("calculator 结果: " + result1);

        String result2 = callMcpTool("get_weather", "{\"city\": \"Shanghai\"}");
        System.out.println("get_weather 结果: " + result2);

        // 4. 关闭连接
        System.out.println("\n--- Step 4: 关闭连接 ---");
        System.out.println("发送 shutdown 请求...");
        System.out.println("连接已关闭。");

        System.out.println("\n=== 演示结束 ===");
    }

    static String callMcpTool(String name, String args) {
        return switch (name) {
            case "calculator" -> "30";
            case "get_weather" -> "上海: 多云, 22°C, 湿度 75%";
            default -> "未知工具";
        };
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 6.3: McpSpringIntegration.java — Spring AI MCP 集成（concept）

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/mcp/advanced/McpSpringIntegrationDemo.java`**

```java
package com.advancedjava.ai.mcp.advanced;

/**
 * Spring AI MCP 集成演示（概念展示）。
 *
 * <p>展示如何在 Spring Boot 中集成 MCP 协议。
 * 注意：本示例需要 Spring AI BOM 2.0.0+ 才能编译运行，
 * 当前项目使用 Spring AI 1.1.2，因此仅做概念展示。
 */
public class McpSpringIntegrationDemo {

    public static void main(String[] args) {
        System.out.println("=== Spring AI MCP 集成演示（概念）===\n");
        System.out.println("（需要 Spring AI BOM 2.0.0+，当前为概念展示）\n");

        System.out.println("--- 方式 1: MCP Server (WebMVC) ---");
        System.out.println("@SpringBootApplication");
        System.out.println("@EnableMcpServer");
        System.out.println("public class McpServerApplication {");
        System.out.println("    // 自动注册 Spring Bean 为 MCP 工具");
        System.out.println("    @Bean");
        System.out.println("    @Tool(description = \"计算器\")");
        System.out.println("    public String calculate(int a, int b) {");
        System.out.println("        return String.valueOf(a + b);");
        System.out.println("    }");
        System.out.println("}\n");

        System.out.println("--- 方式 2: MCP Client ---");
        System.out.println("@Configuration");
        System.out.println("public class McpClientConfig {");
        System.out.println("    @Bean");
        System.out.println("    public McpClient mcpClient() {");
        System.out.println("        return McpClient.using(new HttpClientTransport(\"http://localhost:8080\"));");
        System.out.println("    }");
        System.out.println("}\n");

        System.out.println("--- MCP 集成架构 ---");
        System.out.println("  LLM ↔ Spring AI ↔ MCP Client ↔ MCP Server ↔ 外部工具");
        System.out.println("        ↓");
        System.out.println("    AI Model 自动调用 MCP 工具");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 7：A2A 模块（3 个 demo 文件）

### Task 7.1: A2AClientDemo.java — A2A Client

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/a2a/basic/A2AClientDemo.java`**

```java
package com.advancedjava.ai.a2a.basic;

/**
 * A2A (Agent-to-Agent) 客户端演示。
 *
 * <p>展示 A2A 协议的核心客户端能力：
 * 1. Agent 发现
 * 2. 发送任务 (Task)
 * 3. 接收结果
 * 4. 处理状态更新
 *
 * <p>A2A 协议允许不同的 Agent 之间进行标准化通信。
 */
public class A2AClientDemo {

    public static void main(String[] args) {
        System.out.println("=== A2A Agent 客户端演示 ===\n");

        System.out.println("A2A 协议版本: 1.0");
        System.out.println("传输方式: JSON-RPC over HTTP\n");

        // 1. Agent 发现
        System.out.println("--- Step 1: Agent 发现 ---");
        String agentCard = discoverAgent("http://localhost:8080/agent");
        System.out.println("Agent Card: " + agentCard + "\n");

        // 2. 发送任务
        System.out.println("--- Step 2: 发送任务 ---");
        String taskId = sendTask("分析这篇文档并生成摘要");
        System.out.println("任务已提交，ID: " + taskId + "\n");

        // 3. 轮询结果
        System.out.println("--- Step 3: 获取结果 ---");
        String status = getTaskStatus(taskId);
        System.out.println("任务状态: " + status);
        String result = getTaskResult(taskId);
        System.out.println("任务结果: " + result + "\n");

        // 4. 取消任务
        System.out.println("--- Step 4: 取消任务 ---");
        System.out.println("发送取消请求...");
        System.out.println("任务已取消。");

        System.out.println("\n--- A2A 消息类型 ---");
        System.out.println("• Task: 任务请求和响应");
        System.out.println("• Message: 消息体（文本、JSON、文件）");
        System.out.println("• Part: 消息的组成部分");
        System.out.println("• Artifact: 任务产物（结果）");

        System.out.println("\n=== 演示结束 ===");
    }

    static String discoverAgent(String url) {
        return "{ name: \"DocAnalyzer\", description: \"文档分析 Agent\", "
                + "capabilities: [\"summarization\", \"qa\", \"translation\"] }";
    }

    static String sendTask(String task) {
        return "task_" + System.currentTimeMillis();
    }

    static String getTaskStatus(String taskId) {
        return "COMPLETED";
    }

    static String getTaskResult(String taskId) {
        return "文档摘要：本文介绍了 A2A 协议的核心概念...";
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 7.2: A2AServerDemo.java — A2A Server

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/a2a/basic/A2AServerDemo.java`**

```java
package com.advancedjava.ai.a2a.basic;

/**
 * A2A (Agent-to-Agent) 服务端演示。
 *
 * <p>展示 A2A 协议的核心服务端能力：
 * 1. Agent Card 发布
 * 2. 接收任务请求
 * 3. 处理任务并返回结果
 * 4. 任务状态管理
 */
public class A2AServerDemo {

    static class A2AAgent {
        private final String name;
        private final String[] skills;

        public A2AAgent(String name, String[] skills) {
            this.name = name;
            this.skills = skills;
        }

        public String getAgentCard() {
            return String.format(
                    "{ \"name\": \"%s\", \"skills\": %s, \"version\": \"1.0.0\" }",
                    name, java.util.Arrays.toString(skills)
            );
        }

        public String processTask(String taskId, String task) {
            System.out.println("  处理任务 " + taskId + ": " + task);

            // 模拟任务处理
            if (task.contains("翻译")) {
                return "{\"taskId\": \"" + taskId + "\", \"status\": \"COMPLETED\", "
                        + "\"result\": {\"translation\": \"Hello World\"}}";
            } else if (task.contains("摘要") || task.contains("总结")) {
                return "{\"taskId\": \"" + taskId + "\", \"status\": \"COMPLETED\", "
                        + "\"result\": {\"summary\": \"这是生成的摘要...\"}}";
            }
            return "{\"taskId\": \"" + taskId + "\", \"status\": \"COMPLETED\", "
                    + "\"result\": {\"text\": \"已处理: " + task + "\"}}";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== A2A Agent 服务端演示 ===\n");

        // 1. 创建 Agent
        A2AAgent agent = new A2AAgent("TranslationAgent",
                new String[]{"translation", "summarization"});

        // 2. 发布 Agent Card
        System.out.println("--- Agent Card ---");
        String card = agent.getAgentCard();
        System.out.println("GET /.well-known/agent.json");
        System.out.println("响应: " + card + "\n");

        // 3. 处理任务
        System.out.println("--- 任务处理 ---");
        String result1 = agent.processTask("task-001", "翻译「Hello World」到中文");
        System.out.println("结果: " + result1 + "\n");

        String result2 = agent.processTask("task-002", "总结这篇文章");
        System.out.println("结果: " + result2 + "\n");

        System.out.println("--- A2A Server 生命周期 ---");
        System.out.println("1. 启动 HTTP 服务");
        System.out.println("2. 发布 Agent Card（/.well-known/agent.json）");
        System.out.println("3. 监听 POST /tasks/send 请求");
        System.out.println("4. 处理任务并返回结果");
        System.out.println("5. 可选: 任务状态回调");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

### Task 7.3: A2AMultiAgentTask.java — 多 Agent 任务协作

- [ ] **Step 1: 创建 `src/main/java/com/advancedjava/ai/a2a/advanced/A2AMultiAgentTaskDemo.java`**

```java
package com.advancedjava.ai.a2a.advanced;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A2A 多 Agent 任务协作演示。
 *
 * <p>展示基于 A2A 协议的多 Agent 协作场景：
 * 1. 任务分解为子任务
 * 2. 分发到不同 Agent
 * 3. 收集并聚合结果
 * 4. 返回最终答案
 */
public class A2AMultiAgentTaskDemo {

    interface AgentTaskHandler {
        String handle(String subTask);
    }

    static class AgentRegistry {
        private final Map<String, AgentTaskHandler> agents = new HashMap<>();
        private final AtomicInteger taskCounter = new AtomicInteger(0);

        public void register(String name, AgentTaskHandler handler) {
            agents.put(name, handler);
        }

        public String executeTask(String agentName, String task) {
            AgentTaskHandler handler = agents.get(agentName);
            if (handler == null) {
                return agentName + ": 未知 Agent";
            }
            String taskId = "task-" + taskCounter.incrementAndGet();
            System.out.println("  [" + taskId + "] " + agentName + " 接收任务: " + task);
            String result = handler.handle(task);
            System.out.println("  [" + taskId + "] " + agentName + " 完成: " + result);
            return result;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== A2A 多 Agent 任务协作演示 ===\n");

        // 1. 注册 Agent 团队
        AgentRegistry registry = new AgentRegistry();
        registry.register("ResearchAgent", task -> "研究结果：关于「" + task + "」的详细资料");
        registry.register("AnalysisAgent", task -> "分析结果：数据中的关键趋势是...");
        registry.register("WritingAgent", task -> "写作结果：已生成报告草稿");

        // 2. 任务分解与分发
        String mainTask = "撰写 AI Agent 市场分析报告";

        System.out.println("主任务: " + mainTask + "\n");
        System.out.println("--- 任务分解 ---");
        System.out.println("  → 子任务 1: 收集市场数据 (ResearchAgent)");
        System.out.println("  → 子任务 2: 分析竞争格局 (AnalysisAgent)");
        System.out.println("  → 子任务 3: 撰写报告 (WritingAgent)");
        System.out.println();

        // 3. 并行执行
        System.out.println("--- 并行执行 ---");
        String research = registry.executeTask("ResearchAgent", "AI Agent 市场 2024 数据");
        String analysis = registry.executeTask("AnalysisAgent", "AI Agent 竞争格局");
        String writing = registry.executeTask("WritingAgent", "市场分析报告草稿");

        // 4. 聚合结果
        System.out.println("\n--- 结果聚合 ---");
        System.out.println("最终报告:\n  " + research + "\n  " + analysis + "\n  " + writing);

        System.out.println("\n--- A2A 多 Agent 协作模式 ---");
        System.out.println("1. Orchestrator 负责任务分解");
        System.out.println("2. A2A 协议用于 Agent 间通信");
        System.out.println("3. 每个 Agent 专注自身领域");
        System.out.println("4. 结果通过 A2A 消息聚合");

        System.out.println("\n=== 演示结束 ===");
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `mvn compile -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`

---

## 阶段 8：统一入口

### Task 8.1: AiDemoApp.java — 统一程序入口

**Files:**
- Create: `src/main/java/com/advancedjava/ai/AiDemoApp.java`

- [ ] **Step 1: 创建 AiDemoApp.java**

```java
package com.advancedjava.ai;

import com.advancedjava.ai.langchain4j.basic.ChatDemo;
import com.advancedjava.ai.langchain4j.basic.PromptTemplateDemo;
import com.advancedjava.ai.langchain4j.advanced.ToolCallingDemo;
import com.advancedjava.ai.langchain4j.advanced.AiServiceAgentDemo;
import com.advancedjava.ai.agentscope.basic.AgentChatDemo;
import com.advancedjava.ai.agentscope.basic.ToolAgentDemo;
import com.advancedjava.ai.agentscope.advanced.ReActAgentDemo;
import com.advancedjava.ai.agentscope.advanced.MultiAgentDemo;
import com.advancedjava.ai.langgraph4j.basic.SequentialGraphDemo;
import com.advancedjava.ai.langgraph4j.basic.StateGraphDemo;
import com.advancedjava.ai.langgraph4j.advanced.ConditionalGraphDemo;
import com.advancedjava.ai.langgraph4j.advanced.AgentLoopDemo;
import com.advancedjava.ai.adkpatterns.basic.AgentDefPatternDemo;
import com.advancedjava.ai.adkpatterns.advanced.AgentDelegationDemo;
import com.advancedjava.ai.crewairpatterns.basic.RoleBasedAgentDemo;
import com.advancedjava.ai.crewairpatterns.advanced.CrewOrchestrationDemo;
import com.advancedjava.ai.mcp.basic.McpServerDemo;
import com.advancedjava.ai.mcp.basic.McpClientDemo;
import com.advancedjava.ai.mcp.advanced.McpSpringIntegrationDemo;
import com.advancedjava.ai.a2a.basic.A2AClientDemo;
import com.advancedjava.ai.a2a.basic.A2AServerDemo;
import com.advancedjava.ai.a2a.advanced.A2AMultiAgentTaskDemo;

/**
 * Java AI 框架与协议示例集合 — 统一入口。
 *
 * <p>依次运行所有模块的演示，展示控制台输出。
 */
public class AiDemoApp {

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  Java AI 框架与协议示例集合          ║");
        System.out.println("╚══════════════════════════════════════╝\n");

        // ===== 阶段 1: LangChain4j =====
        System.out.println("━━━ [第1章] LangChain4j ━━━\n");
        ChatDemo.main(args);
        System.out.println();
        PromptTemplateDemo.main(args);
        System.out.println();
        ToolCallingDemo.main(args);
        System.out.println();
        AiServiceAgentDemo.main(args);
        System.out.println();

        // ===== 阶段 2: AgentScope =====
        System.out.println("━━━ [第2章] AgentScope ━━━\n");
        AgentChatDemo.main(args);
        System.out.println();
        ToolAgentDemo.main(args);
        System.out.println();
        ReActAgentDemo.main(args);
        System.out.println();
        MultiAgentDemo.main(args);
        System.out.println();

        // ===== 阶段 3: LangGraph4j =====
        System.out.println("━━━ [第3章] LangGraph4j ━━━\n");
        SequentialGraphDemo.main(args);
        System.out.println();
        StateGraphDemo.main(args);
        System.out.println();
        ConditionalGraphDemo.main(args);
        System.out.println();
        AgentLoopDemo.main(args);
        System.out.println();

        // ===== 阶段 4: ADK Patterns =====
        System.out.println("━━━ [第4章] ADK Patterns ━━━\n");
        AgentDefPatternDemo.main(args);
        System.out.println();
        AgentDelegationDemo.main(args);
        System.out.println();

        // ===== 阶段 5: CrewAI Patterns =====
        System.out.println("━━━ [第5章] CrewAI Patterns ━━━\n");
        RoleBasedAgentDemo.main(args);
        System.out.println();
        CrewOrchestrationDemo.main(args);
        System.out.println();

        // ===== 阶段 6: MCP =====
        System.out.println("━━━ [第6章] MCP 协议 ━━━\n");
        McpServerDemo.main(args);
        System.out.println();
        McpClientDemo.main(args);
        System.out.println();
        McpSpringIntegrationDemo.main(args);
        System.out.println();

        // ===== 阶段 7: A2A =====
        System.out.println("━━━ [第7章] A2A 协议 ━━━\n");
        A2AClientDemo.main(args);
        System.out.println();
        A2AServerDemo.main(args);
        System.out.println();
        A2AMultiAgentTaskDemo.main(args);
        System.out.println();

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  所有示例演示完毕                     ║");
        System.out.println("╚══════════════════════════════════════╝");
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `mvn compile -DskipTests 2>&1 | tail -20`
Expected: `BUILD SUCCESS`

---

## 阶段 9：构建与测试验证

### Task 9.1: 完整构建验证

- [ ] **Step 1: 完整编译**

Run: `mvn clean compile -DskipTests`
Expected: `BUILD SUCCESS`

- [ ] **Step 2: 运行单元测试**

Run: `mvn test`
Expected: `BUILD SUCCESS`（所有测试通过）

- [ ] **Step 3: 运行 LangChain4j ChatDemo 单独验证**

Run: `mvn exec:java -Dexec.mainClass="com.advancedjava.ai.langchain4j.basic.ChatDemo" -DskipTests 2>&1 | head -30`

或直接通过 Java 命令运行：
```bash
java -cp target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) \
  com.advancedjava.ai.langchain4j.basic.ChatDemo
```

Expected: 看到控制台输出对话内容

- [ ] **Step 4: 全量编译成功检查**

Run: `mvn clean package -DskipTests 2>&1 | tail -10`
Expected: `BUILD SUCCESS`
