# Java AI 框架与协议示例集 — 设计文档

## 概述

在 `com.advancedjava.ai` 包下创建一系列教学演示示例，涵盖主流 Java AI 框架（LangChain4j、AgentScope、LangGraph4j）的使用，
以及 MCP 和 A2A 两大协议的 Java 实现展示。对于无 Java 版本的框架（ADK、CrewAI），用 Java 等效模式进行概念演示。

**目标读者**：学习 Java AI 开发的中级开发者
**Java 版本**：17+
**构建工具**：Maven
**Spring Boot**：3.3.0

## 模块及依赖

### 使用真实 Java 库的模块

| 模块 | Maven 坐标 | 用途 | 本地仓库 |
|------|-----------|------|---------|
| LangChain4j | `dev.langchain4j:langchain4j:1.13.0` | LLM 对话、工具调用、Agent 模式 | ✅ 已有 |
| AgentScope | `io.agentscope:agentscope:1.0.11` | Agent 开发框架、多 Agent 协作 | ✅ 已有（pom.xml 已引入）|
| LangGraph4j | `org.bsc.langgraph4j:langgraph4j-core:1.8.14` | 图工作流、Agent 循环 | ❌ 需下载 |
| MCP SDK | `io.modelcontextprotocol.sdk:mcp:0.18.1` | MCP 协议的服务端/客户端 | ✅ 已有 |
| A2A SDK | `io.github.a2asdk:a2a-java-sdk-client:0.3.3.Final` | Agent 间通信协议 | ✅ 已有 |

### 模式演示模块（无 Java 版本的框架用 Java 展示其设计思想）

| 模块 | 原始框架 | 实现方案 |
|------|---------|---------|
| ADK Patterns | Google ADK (Python) | 用纯 Java 展示 Agent 定义、工具注册、委派模式 |
| CrewAI Patterns | CrewAI (Python) | 用纯 Java 展示角色 Agent、团队编排模式 |

### Spring Boot MCP 集成（可选）

项目中已有 Spring AI BOM 1.1.2，其 MCP 支持在 2.0.0-SNAPSHOT 轨道上，与当前 BOM 版本不兼容。
因此 MCP 基础示例使用**独立 MCP SDK**（`io.modelcontextprotocol.sdk:mcp:0.18.1`），
高级示例 `McpSpringIntegration.java` 以 concept 方式展示 Spring AI MCP 集成思路（不编译运行）。
如需完整运行 Spring AI MCP，需升级 Spring AI BOM 到 2.0.0+。

## 文件结构

```
com/advancedjava/ai/
├── package-info.java                     # 包文档
├── AiDemoApp.java                        # 统一入口，串联所有 Demo
│
├── langchain4j/
│   ├── package-info.java
│   ├── basic/
│   │   ├── ChatDemo.java                 # LLM 基础对话（同步/流式）
│   │   └── PromptTemplateDemo.java       # 提示词模板与参数注入
│   └── advanced/
│       ├── ToolCallingDemo.java          # 工具/函数注册与调用
│       └── AiServiceAgentDemo.java       # @AiService 声明式 Agent
│
├── agentscope/
│   ├── package-info.java
│   ├── basic/
│   │   ├── AgentChatDemo.java            # Agent 基础对话
│   │   └── ToolAgentDemo.java            # Agent 绑定工具调用
│   └── advanced/
│       ├── ReActAgentDemo.java           # ReAct 推理-行动循环
│       └── MultiAgentDemo.java           # 多 Agent 协作模式
│
├── langgraph4j/
│   ├── package-info.java
│   ├── basic/
│   │   ├── SequentialGraphDemo.java      # 顺序节点图工作流
│   │   └── StateGraphDemo.java           # 带状态的图工作流
│   └── advanced/
│       ├── ConditionalGraphDemo.java     # 条件分支与路由
│       └── AgentLoopDemo.java            # Agent 循环图（思考→行动→观察）
│
├── adk-patterns/
│   ├── package-info.java
│   ├── basic/
│   │   └── AgentDefPattern.java          # Agent 定义：工具注册、指令设置
│   └── advanced/
│       └── AgentDelegation.java          # Agent 委派：主 Agent 分配子任务
│
├── crewai-patterns/
│   ├── package-info.java
│   ├── basic/
│   │   └── RoleBasedAgent.java           # 角色 Agent：定义角色/目标/背景
│   └── advanced/
│       └── CrewOrchestration.java        # 团队编排：多角色顺序/并行执行
│
├── mcp/
│   ├── package-info.java
│   ├── basic/
│   │   ├── McpServerDemo.java            # MCP Server（暴露计算工具）
│   │   └── McpClientDemo.java            # MCP Client（调用远程工具）
│   └── advanced/
│       └── McpSpringIntegration.java     # Spring AI MCP 集成（完整方案）
│
└── a2a/
    ├── package-info.java
    ├── basic/
    │   ├── A2AClientDemo.java            # A2A Client：发送任务请求
    │   └── A2AServerDemo.java            # A2A Server：接收并处理任务
    └── advanced/
        └── A2AMultiAgentTask.java        # 多 Agent 任务编排与结果聚合
```

**共计**：约 25 个 Java 文件 + 8 个 package-info.java + 1 个主入口 = **34 个文件**

## 每个 Demo 文件的模板

每个演示文件的典型结构：

```java
package com.advancedjava.ai.<module>.<level>;

/**
 * [中文标题]
 *
 * <p>[中文详细说明，解释本 Demo 要展示的核心概念和设计意图]
 *
 * <p>关键点：
 * <ul>
 *   <li>要点 1</li>
 *   <li>要点 2</li>
 * </ul>
 *
 * <p>运行本 Demo：执行 main 方法即可看到控制台输出。
 */
public class XxxDemo {

    public static void main(String[] args) {
        // 1. 准备阶段 —— 说明
        // 2. 核心演示 —— 说明
        // 3. 输出结果 —— 说明
    }
}
```

## 代码风格规范

- **缩进**：4 空格
- **包名**：`com.advancedjava.ai.<module>.<level>`
- **注释**：中文 JavaDoc + 行内中文注释（符合项目现有教学风格）
- **构造器**：优先使用构造注入
- **异常**：方法内 try-catch + 打印堆栈，不向外抛
- **输出**：`System.out.println()` 控制台输出，展示运行过程

## 统一入口 AiDemoApp.java

```java
package com.advancedjava.ai;

/**
 * Java AI 框架与协议示例集合 — 统一入口。
 *
 * <p>依次运行所有模块的演示，展示控制台输出。
 */
public class AiDemoApp {
    public static void main(String[] args) {
        System.out.println("=== Java AI 框架与协议示例 ===\n");

        // 依次执行各 Demo 的 main
        // ...
    }
}
```

## MCP 模块说明

MCP 模块使用官方 SDK `io.modelcontextprotocol.sdk:mcp:0.18.1`，展示：
1. **McpServerDemo**：启动内嵌 HTTP Server，暴露工具（如计算器、字符串处理）
2. **McpClientDemo**：连接 Server，发现并调用工具
3. **McpSpringIntegration.java**（concept 展示）：展示 Spring AI MCP 集成思路（需 Spring AI BOM 2.0.0+ 才能编译运行）

注意：McpServerDemo 需要启动内嵌 HTTP 服务，McpClientDemo 需在 Server 运行后执行。

## A2A 模块说明

A2A 模块使用 `io.github.a2asdk:a2a-java-sdk`，展示：
1. Agent 服务端（接收并处理来自其他 Agent 的任务）
2. Agent 客户端（向其他 Agent 发送任务请求）
3. 多 Agent 任务编排（将一个复杂任务分解并分发）

## 模式演示（ADK / CrewAI）的定位

这两个模块会在类注释中明确注明：
> "本示例展示 [原始框架] 的核心设计思想，使用 Java 等效实现。
> 原始框架为 Python 实现，无官方 Java 移植。"

重点展示的设计概念：
- **ADK Patterns**：Agent 定义 → 工具注册 → 委派链
- **CrewAI Patterns**：角色定义 → 任务分配 → 团队执行

## 测试策略

关键测试覆盖范围（共约 5-8 个测试类）：
- 每个模块至少一个测试类
- 测试纯逻辑（不依赖真实 LLM 调用）
- 使用 mock/stub 替代外部服务
- 测试重点：配置解析、状态转换、错误处理

## 构建验证

```bash
mvn clean compile   # 编译检查
mvn test            # 运行测试
```

---

*设计版本：v1.0*
*创建日期：2026-05-05*
