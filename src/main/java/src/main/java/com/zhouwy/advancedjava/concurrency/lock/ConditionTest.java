package src.main.java.com.zhouwy.advancedjava.concurrency.lock;

import org.junit.Test;

import java.util.Random;
import java.util.Stack;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionTest {

    public static int THREADS_COUNT = 20;

    public static int accountA = 2000000;

    public static int accountB = 0;

    @Test
    public void Error() {

        long start = System.currentTimeMillis();

        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        int amount = new Random().nextInt(30);
                        accountA -= amount;
                        accountB += amount;
                    }
                }
            });
            threads[i].start();
        }

        while (Thread.activeCount() > 3) {
            Thread.yield();
        }

        System.out.println("accountA:" + accountA + "\n" +
                "accountB:" + accountB + "\n" +
                "A+B=" + (accountA + accountB) +
                (System.currentTimeMillis() - start) + "ms");
    }

    @Test
    public void LockTest() {

        long start = System.currentTimeMillis();

        ReentrantLock lock = new ReentrantLock();

        Thread[] threads = new Thread[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        lock.lock();
                        int amount = new Random().nextInt(30);
                        accountA -= amount;
                        accountB += amount;
                        lock.unlock();
                    }
                }
            });
            threads[i].start();
        }

        while (Thread.activeCount() > 3) {
            Thread.yield();
        }

        System.out.println("accountA:" + accountA + "\n" +
                "accountB:" + accountB + "\n" +
                "A+B=" + (accountA + accountB) +
                (System.currentTimeMillis() - start) + "ms");
    }


    @Test
    public void ConditionTest() {

        ReentrantLock lock = new ReentrantLock();

        Condition cond = lock.newCondition();

        Stack<Integer> stack = new Stack<>();

        Thread[] threads = new Thread[THREADS_COUNT];

        for (int i = 0; i < THREADS_COUNT; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
                    try {
                        int  a = new Random().nextInt(2);
                        if( a == 0){
                            // get
                            if(stack.isEmpty()){
                               System.out.println("get: wait");
                               cond.await();
                            }else{
                                System.out.println("get:"+stack.pop());
                            }
                        }else {
                            // put
                            stack.push(new Random().nextInt(10));
                            System.out.println("put: "+stack);
                            cond.signal();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                }
            });

            threads[i].start();
        }

        while (Thread.activeCount() > 3) {
            Thread.yield();
        }

    }


}
