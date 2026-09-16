package com.advancedjava.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
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
        // 手动配置兼容 OpenAI 协议的服务；地址、Key、模型都从环境变量读取。
        String baseUrl = envOrDefault("DEMO_AI_BASE_URL", "https://api.deepseek.com");
        String apiKey = requiredEnv("DEMO_AI_API_KEY");
        String modelId = envOrDefault("DEMO_AI_MODEL", "deepseek-chat");

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

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "缺少环境变量 " + name + "。请先执行：set -a; source .env; set +a"
            );
        }
        return value;
    }
}
