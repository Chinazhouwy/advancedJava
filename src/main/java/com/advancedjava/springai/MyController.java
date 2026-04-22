package com.advancedjava.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring AI 控制器预留示例。
 *
 * <p>当前类处于占位状态，保留了未来接入 ChatClient/OpenAI 模型调用的扩展位置。
 * 现有注释代码可以作为后续补齐 AI 接口时的起点。
 */
@RestController
public class MyController {

//    @GetMapping("/ai")
//    String generation(String userInput) {
//        OpenAiApi gpt4Api = baseOpenAiApi.mutate()
//                .baseUrl("https://api.openai.com")
//                .apiKey(System.getenv("OPENAI_API_KEY"))
//                .build();
//        return OpenAiChatModel.builder().chatClientBuilder.chatClient.prompt()
//                .user(userInput)
//                .call()
//                .content();
//    }
}
