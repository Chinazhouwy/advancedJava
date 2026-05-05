package com.advancedjava.ai.langchain4j;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import java.util.List;
import java.util.Set;
import java.util.Collections;

/**
 * Mock ChatLanguageModel —— 用于教学演示，不依赖真实 LLM API。
 *
 * <p>返回固定的模拟回复，让示例代码可以在无 API Key 的情况下编译运行。
 * 实际使用时可替换为 {@code OpenAiChatModel}、{@code OllamaChatModel} 等。
 */
public class MockChatLanguageModel implements ChatModel, StreamingChatModel {

    private final String mockResponse;

    public MockChatLanguageModel(String mockResponse) {
        this.mockResponse = mockResponse;
    }

    public MockChatLanguageModel() {
        this("你好！我是 Mock LLM，这是一个模拟回复。");
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        AiMessage aiMessage = AiMessage.from(mockResponse);
        TokenUsage tokenUsage = new TokenUsage(10, 5, 15);
        return ChatResponse.builder().aiMessage(aiMessage).tokenUsage(tokenUsage).build();
    }

    @Override
    public void chat(List<ChatMessage> messages, StreamingChatResponseHandler handler) {
        AiMessage aiMessage = AiMessage.from(mockResponse);
        TokenUsage tokenUsage = new TokenUsage(10, 5, 15);
        ChatResponse response = ChatResponse.builder().aiMessage(aiMessage).tokenUsage(tokenUsage).build();
        handler.onCompleteResponse(response);
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return Set.of(); // No special capabilities for Mock
    }

    @Override
    public ModelProvider provider() {
        return null; // No real provider for mock
    }

    @Override
    public List<ChatModelListener> listeners() {
        return Collections.emptyList(); // No listeners for mock
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return DefaultChatRequestParameters.builder().build(); // Default parameters for mock
    }
}