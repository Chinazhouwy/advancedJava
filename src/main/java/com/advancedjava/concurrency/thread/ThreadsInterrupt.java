package com.advancedjava.concurrency.thread;

import org.junit.Test;

/**
 * 线程中断基础示例。
 *
 * <p>本类通过几个很小的测试方法说明：
 * 1. 线程可以被正常创建和启动。
 * 2. 中断标记可以被设置和读取。
 * 3. 阻塞方法抛出 {@code InterruptedException} 后，调用方如何重新设置中断位。
 */
public class ThreadsInterrupt {

    @Test
    /**
     * 启动一个最简单的线程任务，观察线程执行入口。
     */
    public void testThread(){
        Runnable run = new MyRunnable();
        Thread thread = new Thread(run);
        thread.start();
    }

    /**
     * 最小 Runnable 实现，仅用于证明线程已经开始执行。
     */
    class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println(System.currentTimeMillis());
        }

    }

    @Test
    /**
     * 读取当前线程的中断状态。
     */
    public void Interrupt(){
//        Thread.currentThread().interrupt();
        System.out.println(Thread.currentThread().isInterrupted());
    }

    @Test
    /**
     * 模拟阻塞操作被中断后，重新设置中断位的常见处理方式。
     */
    public void InterruptSleep(){
        try{
            Thread.sleep(1000L);
            throw new InterruptedException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

}
