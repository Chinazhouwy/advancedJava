package com.advancedjava.ai.agentscope.basic;

import io.agentscope.core.agent.AgentBase;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * AgentScope 工具Agent演示
 * 展示Agent如何根据消息内容路由到不同的工具执行逻辑
 */
public class ToolAgentDemo {

    public static void main(String[] args) {
        // 创建具备工具能力的Agent
        ToolCapableAgent toolAgent = new ToolCapableAgent("tool-agent", "具备工具调用能力的Agent");

        System.out.println("=== AgentScope 工具Agent演示 ===");
        System.out.println("Agent ID: " + toolAgent.getAgentId());
        System.out.println("Agent名称: " + toolAgent.getName());
        System.out.println();

        // 测试场景1: 调用计算工具
        testToolCall(toolAgent, "计算 15 加 27 等于多少");

        // 测试场景2: 调用天气查询工具
        testToolCall(toolAgent, "查询北京的天气怎么样");

        // 测试场景3: 普通对话（无需工具）
        testToolCall(toolAgent, "你好，请介绍一下你自己");

        // 测试场景4: 调用时间查询工具
        testToolCall(toolAgent, "现在几点了？");
    }

    /**
     * 辅助方法：测试Agent对特定输入的响应
     */
    private static void testToolCall(ToolCapableAgent agent, String userInput) {
        Msg userMessage = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(userInput).build())
                .build();

        System.out.println("用户输入: " + userInput);
        Msg response = agent.call(List.of(userMessage)).block();
        System.out.println("Agent回复: " + response.getTextContent());
        System.out.println("-".repeat(50));
    }

    /**
     * 具备工具调用能力的Agent
     * 根据消息内容智能路由到相应的工具执行逻辑
     */
    static class ToolCapableAgent extends AgentBase {

        public ToolCapableAgent(String name, String description) {
            super(name, description);
        }

        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            String userInput = extractUserInput(msgs);

            // 根据用户输入内容判断需要调用哪个工具
            Msg response;
            if (containsKeywords(userInput, "计算", "加", "减", "乘", "除", "等于")) {
                response = executeCalculatorTool(userInput);
            } else if (containsKeywords(userInput, "天气", "温度", "下雨")) {
                response = executeWeatherTool(userInput);
            } else if (containsKeywords(userInput, "时间", "几点", "日期")) {
                response = executeTimeTool(userInput);
            } else {
                response = executeChat(userInput);
            }

            return Mono.just(response);
        }

        /**
         * 从消息列表中提取用户输入文本
         */
        private String extractUserInput(List<Msg> msgs) {
            if (msgs.isEmpty()) {
                return "";
            }
            return msgs.get(msgs.size() - 1).getTextContent();
        }

        /**
         * 检查输入是否包含指定关键词
         */
        private boolean containsKeywords(String input, String... keywords) {
            String lowerInput = input.toLowerCase();
            for (String keyword : keywords) {
                if (lowerInput.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 执行计算器工具（模拟）
         */
        private Msg executeCalculatorTool(String input) {
            // 简单的模拟计算逻辑
            int result = 42; // 模拟计算结果

            String content = String.format("[工具调用: 计算器]\n根据您的请求: \"%s\"\n计算结果: %d", input, result);

            return Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(content).build())
                    .build();
        }

        /**
         * 执行天气查询工具（模拟）
         */
        private Msg executeWeatherTool(String input) {
            String content = String.format("[工具调用: 天气查询]\n根据您的请求: \"%s\"\n查询结果: 北京当前天气晴朗，温度25°C，湿度45%%", input);

            return Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(content).build())
                    .build();
        }

        /**
         * 执行时间查询工具
         */
        private Msg executeTimeTool(String input) {
            String currentTime = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String content = String.format("[工具调用: 时间查询]\n当前时间: %s", currentTime);

            return Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(content).build())
                    .build();
        }

        /**
         * 执行普通对话
         */
        private Msg executeChat(String input) {
            String content = String.format("我是一个具备工具调用能力的Agent。\n您说: \"%s\"\n\n我可以帮您计算、查询天气或获取当前时间。", input);

            return Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text(content).build())
                    .build();
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            Msg interruptResponse = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("工具执行被中断").build())
                    .build();
            return Mono.just(interruptResponse);
        }
    }
}
