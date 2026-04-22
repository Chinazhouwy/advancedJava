package com.advancedjava.springwebflux;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

/**
 * 异步输入桥接示例（教学版）。
 *
 * <p>这个 Demo 用来解释狼人项目里的核心机制：
 * 1. 游戏线程调用 waitForInput() 后“等待用户输入”。
 * 2. Web 线程调用 submitInput() 把输入提交进来。
 * 3. 两边通过 Sinks.One 连接。
 *
 * <p>关键对象：
 * - Sinks.One<String>: 只能发送 1 次值的“单次管道”。
 * - Mono<String>: Sinks.One 的只读视图，供业务方订阅。
 *
 * <p>这个例子的核心价值不在于控制台输入本身，而在于说明：
 * “外部世界稍后才会到来的结果”，如何桥接回一段已经建立好的响应式流程。
 */
public class AsyncInputDemo {

    /**
     * 存放待处理输入请求。
     * key: inputType_timestamp（例如 SPEAK_1710000000000）
     * value: 对应这次等待请求的 Sinks.One。
     */
    private static final ConcurrentHashMap<String, Sinks.One<String>> pendingInputs =
            new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 异步输入等待 Demo ===\n");

        // 线程 A：模拟“游戏逻辑线程”（等待用户输入）。
        Thread gameThread = new Thread(() -> {
            try {
                System.out.println("[游戏线程] 开始等待用户发言...");

                // waitForInput 返回的是 Mono，不会立刻有值。
                Mono<String> inputMono = waitForInput("SPEAK", "请输入你的发言内容：");

                // 订阅后，线程不会阻塞；等到有人 tryEmitValue 才会触发回调。
                inputMono.subscribe(
                        input -> {
                            System.out.println("[游戏线程] ✅ 收到输入: " + input);
                            System.out.println("[游戏线程] 继续执行后续游戏逻辑...");
                        },
                        error -> System.err.println("[游戏线程] ❌ 发生错误: " + error.getMessage()));

                System.out.println("[游戏线程] 已订阅 Mono，现在处于非阻塞等待状态...\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        gameThread.start();

        // 等待游戏线程先进入“订阅等待”状态。
        Thread.sleep(1000);

        // 线程 B：模拟“Web 层收到用户请求并提交输入”。
        System.out.println("\n[模拟REST API] 用户在前端提交了输入");
        Scanner scanner = new Scanner(System.in);
        System.out.print("[控制台] 请输入测试内容: ");
        String userInput = scanner.nextLine();

        boolean success = submitInput("SPEAK", userInput);

        if (success) {
            System.out.println("[模拟REST API] ✅ 输入已成功提交，游戏线程将被唤醒\n");
        } else {
            System.out.println("[模拟REST API] ❌ 没有找到待处理的输入请求\n");
        }

        Thread.sleep(500);
        scanner.close();
        System.out.println("\n=== Demo 结束 ===");
    }

    /**
     * 创建“等待输入”的异步请求。
     *
     * @param inputType 输入类型，例如 SPEAK/VOTE。
     * @param prompt 提示文案（这里仅打印日志，真实项目里会发给前端）。
     * @return Mono<String> 用户输入结果，未来某个时刻才会完成。
     */
    public static Mono<String> waitForInput(String inputType, String prompt) {
        // 1) 生成唯一请求 ID，避免多个请求互相覆盖。
        String inputId = inputType + "_" + System.currentTimeMillis();

        // 2) 创建一次性 Sink。
        Sinks.One<String> inputSink = Sinks.one();

        // 3) 缓存起来，等待 submitInput 时找到它。
        pendingInputs.put(inputId, inputSink);

        // 4) 模拟发事件给前端：请展示一个输入框。
        System.out.println("[waitForInput] 📢 向前端发送事件: " + prompt);
        System.out.println("[waitForInput] 创建 inputId: " + inputId);

        // 5) 返回 Mono 给业务层订阅。
        // doOnSuccess/doOnError 都做清理，防止 map 泄漏。
        // 这里要注意：等待输入的这段时间里，并没有线程一直卡在 nextLine() 或 sleep() 上，
        // 而是通过“先返回一个未来会完成的 Mono”来表达“结果稍后回来”。
        return inputSink.asMono()
                .doOnSuccess(
                        value -> {
                            System.out.println("[waitForInput] 🧹 清理 pendingInputs: " + inputId);
                            pendingInputs.remove(inputId);
                        })
                .doOnError(
                        error -> {
                            System.err.println("[waitForInput] ⚠️ 发生错误，清理 pendingInputs");
                            pendingInputs.remove(inputId);
                        });
    }

    /**
     * 提交用户输入（模拟 REST API 行为）。
     *
     * @param inputType 输入类型（用于匹配 pendingInputs）。
     * @param content 用户输入内容。
     * @return true 表示已成功投递到对应的等待请求；false 表示未找到匹配请求。
     */
    public static boolean submitInput(String inputType, String content) {
        System.out.println("[submitInput] 🔍 查找匹配的待处理请求...");

        String matchingKey = null;
        for (String key : pendingInputs.keySet()) {
            if (key.startsWith(inputType + "_")) {
                // 为了简化演示，只取第一个匹配项。
                // 真实项目里通常会带上 requestId / sessionId / playerId 做精确匹配。
                matchingKey = key;
                System.out.println("[submitInput] 找到匹配项: " + key);
                break;
            }
        }

        if (matchingKey != null) {
            // 取出并删除，避免重复提交同一个请求。
            Sinks.One<String> sink = pendingInputs.remove(matchingKey);

            if (sink != null) {
                System.out.println("[submitInput] 📨 向前端发送确认: USER_INPUT_RECEIVED");

                // 核心动作：把值送入 Sink，立即触发 waitForInput 订阅回调。
                Sinks.EmitResult result = sink.tryEmitValue(content);

                if (result.isSuccess()) {
                    System.out.println("[submitInput] ✅ tryEmitValue 成功，值为: " + content);
                    return true;
                }

                System.err.println("[submitInput] ❌ tryEmitValue 失败: " + result);
                return false;
            }
        }

        System.err.println("[submitInput] ❌ 未找到匹配的待处理请求");
        return false;
    }
}
