package com.advancedjava.ai.agentscope.basic;

import io.agentscope.core.agent.AgentBase;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * AgentScope 基础Agent演示
 * 展示如何扩展AgentBase创建自定义Agent，并使用Reactor Mono进行响应式调用
 */
public class AgentChatDemo {

    public static void main(String[] args) {
        // 1. 创建自定义Agent实例
        SimpleEchoAgent agent = new SimpleEchoAgent("echo-agent", "简单的回显Agent");

        // 2. 构建用户输入消息
        Msg userMessage = Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text("你好，AgentScope！").build())
                .build();

        System.out.println("=== AgentScope 基础Agent演示 ===");
        System.out.println("Agent ID: " + agent.getAgentId());
        System.out.println("Agent名称: " + agent.getName());
        System.out.println("Agent描述: " + agent.getDescription());
        System.out.println();

        // 3. 调用Agent（使用.block()进行同步阻塞调用，适合演示）
        System.out.println("用户输入: " + userMessage.getTextContent());
        Msg response = agent.call(List.of(userMessage)).block();

        // 4. 显示输出结果
        System.out.println("Agent回复: " + response.getTextContent());
        System.out.println("回复角色: " + response.getRole());
    }

    /**
     * 简单的回显Agent实现
     * 继承AgentBase并覆盖doCall方法实现自定义逻辑
     */
    static class SimpleEchoAgent extends AgentBase {

        public SimpleEchoAgent(String name, String description) {
            super(name, description);
        }

        /**
         * 核心处理逻辑：接收消息列表并返回响应
         * 使用Mono包装返回值以支持响应式编程
         */
        @Override
        protected Mono<Msg> doCall(List<Msg> msgs) {
            // 获取最后一条用户消息
            String lastMessage = msgs.isEmpty() ? "无输入" :
                    msgs.get(msgs.size() - 1).getTextContent();

            // 构建回复消息
            Msg response = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder()
                            .text("[Echo] 收到消息: " + lastMessage)
                            .build())
                    .build();

            // 使用Mono.just包装返回值
            return Mono.just(response);
        }

        @Override
        protected Mono<Msg> handleInterrupt(io.agentscope.core.interruption.InterruptContext context, Msg... msgs) {
            // 处理中断请求，返回中断响应
            Msg interruptResponse = Msg.builder()
                    .role(MsgRole.ASSISTANT)
                    .content(TextBlock.builder().text("Agent被中断").build())
                    .build();
            return Mono.just(interruptResponse);
        }
    }
}
