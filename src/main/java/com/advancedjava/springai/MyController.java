package com.advancedjava.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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