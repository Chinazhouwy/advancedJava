package com.advancedjava.springwebflux;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 演示 WebUserInput 的异步等待原理
 *
 * 核心思想：用 Sinks.One 作为"桥梁"，连接两个异步操作
 */
public class AsyncInputDemo {

    // 模拟 pendingInputs，存储待处理的输入请求
    private static final ConcurrentHashMap<String, Sinks.One<String>> pendingInputs = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 异步输入等待 Demo ===\n");

        // 启动游戏线程（模拟 Agent 等待用户输入）
        Thread gameThread = new Thread(() -> {
            try {
                System.out.println("[游戏线程] 开始等待用户发言...");

                // 调用 waitForInput，返回 Mono（异步流）
                Mono<String> inputMono = waitForInput("SPEAK", "请输入你的发言内容：");

                // subscribe 订阅这个 Mono，当有数据时会执行回调
                inputMono.subscribe(
                        input -> {
                            // 这里是有数据时的处理逻辑
                            System.out.println("[游戏线程] ✅ 收到输入: " + input);
                            System.out.println("[游戏线程] 继续执行后续游戏逻辑...");
                        },
                        error -> {
                            System.err.println("[游戏线程] ❌ 发生错误: " + error.getMessage());
                        }
                );

                System.out.println("[游戏线程] 已订阅 Mono，现在处于非阻塞等待状态...\n");

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 启动游戏线程
        gameThread.start();

        // 等待一下，让游戏线程先执行到等待状态
        Thread.sleep(1000);

        // 模拟前端通过 REST API 提交输入
        System.out.println("\n[模拟REST API] 用户在前端提交了输入");
        Scanner scanner = new Scanner(System.in);
        System.out.print("[控制台] 请输入测试内容: ");
        String userInput = scanner.nextLine();

        // 调用 submitInput，触发异步回调
        boolean success = submitInput("SPEAK", userInput);

        if (success) {
            System.out.println("[模拟REST API] ✅ 输入已成功提交，游戏线程将被唤醒\n");
        } else {
            System.out.println("[模拟REST API] ❌ 没有找到待处理的输入请求\n");
        }

        // 等待游戏线程处理完成
        Thread.sleep(500);

        scanner.close();
        System.out.println("\n=== Demo 结束 ===");
    }

    /**
     * 模拟 waitForInput 方法
     *
     * @param inputType 输入类型
     * @param prompt 提示信息
     * @return Mono<String> 异步流，当有输入时会发出数据
     */
    public static Mono<String> waitForInput(String inputType, String prompt) {
        // 1. 生成唯一 ID
        String inputId = inputType + "_" + System.currentTimeMillis();

        // 2. 创建一个 Sinks.One（一次性管道）
        Sinks.One<String> inputSink = Sinks.one();

        // 3. 存入映射表，等待后续触发
        pendingInputs.put(inputId, inputSink);

        // 4. 模拟向前端发送事件
        System.out.println("[waitForInput] 📢 向前端发送事件: " + prompt);
        System.out.println("[waitForInput] 创建 inputId: " + inputId);

        // 5. 返回 Mono（游戏线程会订阅它）
        // doOnSuccess: 当成功接收到值后，自动清理
        return inputSink.asMono()
                .doOnSuccess(value -> {
                    System.out.println("[waitForInput] 🧹 清理 pendingInputs: " + inputId);
                    pendingInputs.remove(inputId);
                })
                .doOnError(error -> {
                    System.err.println("[waitForInput] ⚠️ 发生错误，清理 pendingInputs");
                    pendingInputs.remove(inputId);
                });
    }

    /**
     * 模拟 submitInput 方法（REST API 调用）
     *
     * @param inputType 输入类型
     * @param content 用户输入的内容
     * @return 是否成功提交
     */
    public static boolean submitInput(String inputType, String content) {
        System.out.println("[submitInput] 🔍 查找匹配的待处理请求...");

        // 1. 查找匹配的待处理请求
        String matchingKey = null;
        for (String key : pendingInputs.keySet()) {
            if (key.startsWith(inputType + "_")) {
                matchingKey = key;
                System.out.println("[submitInput] 找到匹配项: " + key);
                break;
            }
        }

        if (matchingKey != null) {
            // 2. 取出并移除"管道"
            Sinks.One<String> sink = pendingInputs.remove(matchingKey);

            if (sink != null) {
                // 3. 模拟通知前端："已收到输入"
                System.out.println("[submitInput] 📨 向前端发送确认: USER_INPUT_RECEIVED");

                // 4. ⭐关键步骤：向管道推送数据
                //    这会立即唤醒所有订阅了该 Mono 的回调
                Sinks.EmitResult result = sink.tryEmitValue(content);

                if (result.isSuccess()) {
                    System.out.println("[submitInput] ✅ tryEmitValue 成功，值为: " + content);
                    return true;
                } else {
                    System.err.println("[submitInput] ❌ tryEmitValue 失败: " + result);
                    return false;
                }
            }
        }

        System.err.println("[submitInput] ❌ 未找到匹配的待处理请求");
        return false;
    }
}
