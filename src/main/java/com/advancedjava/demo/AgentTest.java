package com.advancedjava.demo;

/**
 * Java Agent 被增强的目标程序。
 *
 * <p>这个类会周期性调用 {@link #process()}，便于在 attach 或 premain 增强之后，
 * 直接观察控制台中是否出现方法前后织入的日志。
 */
public class AgentTest {

    /**
     * 持续执行目标方法，便于观察 Agent 增强后的输出。
     */
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            System.out.println("process result: " + process());
            Thread.sleep(5000);
        }
    }

    /**
     * 被 Agent 增强的业务方法。
     *
     * @return 固定成功结果
     */
    public static String process() {
        System.out.println("process!");
        return "success";
    }

}
