package com.advancedjava.ai.langchain4j.basic;

import com.advancedjava.ai.langchain4j.MockChatLanguageModel;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * LangChain4j 基础对话演示
 *
 * <p>展示如何创建 ChatModel、发送用户消息、接收 AI 回复，以及流式输出。
 */
public class ChatDemo {

    public static void main(String[] args) {
        System.out.println("=== LangChain4j 基础对话演示 ===\n");

        // 1. 创建 MockChatLanguageModel（模拟 LLM，无需 API Key）
        System.out.println("1. 创建模拟 LLM...");
        MockChatLanguageModel model = new MockChatLanguageModel("你好！我是一个模拟的AI助手，很高兴为你服务。");
        System.out.println("   ✓ 模型已创建\n");

        // 2. 创建用户消息
        System.out.println("2. 构建用户消息...");
        UserMessage userMessage = UserMessage.from("请介绍一下你自己");
        System.out.println("   用户消息: " + userMessage.singleText());
        System.out.println();

        // 3. 发送消息并获取回复（同步调用）
        System.out.println("3. 同步调用 chat() 方法...");
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(userMessage);

        ChatResponse response = model.chat(messages);
        System.out.println("   AI回复: " + response.aiMessage().text());
        System.out.println("   Token消耗: input=" + response.tokenUsage().inputTokenCount()
                + ", output=" + response.tokenUsage().outputTokenCount()
                + ", total=" + response.tokenUsage().totalTokenCount());
        System.out.println();

        // 4. 展示流式调用（Streaming）
        System.out.println("4. 流式调用（Streaming）演示...");
        System.out.println("   模拟流式接收数据：");

        List<ChatMessage> streamMessages = new ArrayList<>();
        streamMessages.add(UserMessage.from("请用流式方式回复"));

        model.chat(streamMessages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                // Mock 模型直接返回完整回复，不会触发此方法
                System.out.print(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                System.out.println("   ✓ 流式响应完成");
                System.out.println("   完整回复: " + completeResponse.aiMessage().text());
            }

            @Override
            public void onError(Throwable error) {
                System.err.println("   ✗ 错误: " + error.getMessage());
            }
        });

        System.out.println();
        System.out.println("=== 演示结束 ===");
        System.out.println();
        System.out.println("要点回顾：");
        System.out.println("- ChatModel.chat(messages) 用于同步对话");
        System.out.println("- StreamingChatResponseHandler 用于处理流式回复");
        System.out.println("- ChatResponse 包含 aiMessage 和 tokenUsage");
    }
}
