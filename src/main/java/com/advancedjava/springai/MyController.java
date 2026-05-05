package com.advancedjava.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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
    
    public static void main(String[] args) {
        // Manually configure OpenAiApi with custom URL, API Key and Model ID (e.g., for DeepSeek or other compatible providers)
        String baseUrl = "https://api.deepseek.com"; // Replace with your target API base URL
        String apiKey = "sk-71a8fccff3704fe2b6a81103d43fffa3"; // Replace with your actual API Key or environment variable
        String modelId = "deepseek-v4-flash"; // Replace with your desired model ID

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(modelId)
                        .build())
                .build();

        ChatClient chatClient = ChatClient.create(chatModel);

        String answer = chatClient.prompt()
                .user("What is the meaning of life?用中文回答")
                .call()
                .content();

        System.out.println(answer);
    }
}
