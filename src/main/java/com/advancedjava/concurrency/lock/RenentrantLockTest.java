package com.advancedjava.concurrency.lock;


import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock 基础示例。
 *
 * <p>该类通过“直接自增”和“加锁自增”的对比，
 * 说明对共享变量的复合操作在并发场景下为何需要互斥保护。
 */
public class RenentrantLockTest {


    public static int num = 0;

    public static void decrease(){
        num ++;
    }

    private static final int THREADS_COUNT = 200;

    @Test
    public void ErrorCode() {

        long start = System.currentTimeMillis();

        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT ; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0;i<10000;i++){
                        decrease();
                    }
                }
            });
            threads[i].start();
        }

        while(Thread.activeCount() > 2){
            Thread.yield();
        }

        System.out.println(num+":"+(System.currentTimeMillis()-start)+"ms");
    }

    @Test
    public void LockUnlock(){

        long start = System.currentTimeMillis();

        ReentrantLock lock = new ReentrantLock();

        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT ; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0;i<10000;i++){
                        lock.lock();
                        decrease();
                        lock.unlock();
                    }
                }
            });
            threads[i].start();
        }

        while(Thread.activeCount() > 2){
            Thread.yield();
        }

        System.out.println(num+":"+(System.currentTimeMillis()-start)+"ms");

    }


}
