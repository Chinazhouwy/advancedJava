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
 * 涵盖 LangChain4j、AgentScope、LangGraph4j、ADK、CrewAI、
 * MCP 和 A2A 七大模块。
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
