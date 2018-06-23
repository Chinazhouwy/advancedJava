package src.main.java.com.zhouwy.advancedjava.concurrency.thread;

import org.junit.Test;

public class ThreadsInterrupt {

    @Test
    public void testThread(){
        Runnable run = new MyRunnable();
        Thread thread = new Thread(run);
        thread.start();
    }

    class MyRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println(System.currentTimeMillis());
        }

    }

    @Test
    public void Interrupt(){
//        Thread.currentThread().interrupt();
        System.out.println(Thread.currentThread().isInterrupted());
    }

    @Test
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
