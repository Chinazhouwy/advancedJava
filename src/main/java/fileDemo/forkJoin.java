package fileDemo;

import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class forkJoin {

    @Test
    public void forkJoinTest() throws InterruptedException {
        Task task = new Task(0, 300);
        //创建实例，并执行分割任务
        ForkJoinPool pool = new ForkJoinPool();
        pool.submit(task);
        //线程阻塞，等待所有任务完成
        pool.awaitTermination(2, TimeUnit.SECONDS);
        pool.shutdown();
    }

    class Task extends RecursiveAction {

        //最多只能打印50个数
        private static final int THRESHOLD = 50;
        private int start;
        private int end;

        public Task(int start, int end) {
            super();
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start < THRESHOLD) {
                for (int i = start; i < end; i++) {
                    System.out.println(
                            "[" + System.currentTimeMillis() + "]" +
                                    Thread.currentThread().getName() + "的i值：" + i);
                }
            } else {
                int middle = (start >> 2 + end >> 2 );
                Task left = new Task(start, middle);
                Task right = new Task(middle, end);
                //并行执行两个“小任务”
                left.fork();
                right.fork();
            }
        }
    }


}
