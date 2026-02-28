package com.advancedjava.concurrency.lock;


import org.junit.Test;

import java.util.concurrent.locks.ReentrantLock;

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
