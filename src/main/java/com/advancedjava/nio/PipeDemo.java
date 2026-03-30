package com.advancedjava.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * NIO Pipe 管道演示
 * Pipe是两个线程之间的单向数据连接，包含SinkChannel（写入端）和SourceChannel（读取端）
 * 用于同一个JVM内不同线程之间的高效数据传输
 */
public class PipeDemo {

    public static void main(String[] args) throws IOException {
        // 1. 打开Pipe
        Pipe pipe = Pipe.open();

        // 2. 启动写线程，向SinkChannel写入数据
        Thread writerThread = new Thread(() -> {
            try {
                Pipe.SinkChannel sinkChannel = pipe.sink();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                for (int i = 0; i < 5; i++) {
                    String message = "Pipe消息 " + i;
                    buffer.clear();
                    buffer.put(message.getBytes());
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        sinkChannel.write(buffer);
                    }
                    System.out.println("写入线程发送：" + message);
                    Thread.sleep(500);
                }
                // 关闭写入端
                sinkChannel.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 3. 启动读线程，从SourceChannel读取数据
        Thread readerThread = new Thread(() -> {
            try {
                Pipe.SourceChannel sourceChannel = pipe.source();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int bytesRead;
                while ((bytesRead = sourceChannel.read(buffer)) != -1) {
                    buffer.flip();
                    byte[] data = new byte[bytesRead];
                    buffer.get(data);
                    System.out.println("读取线程收到：" + new String(data));
                    buffer.clear();
                }
                // 关闭读取端
                sourceChannel.close();
                System.out.println("读取线程结束");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 启动线程
        readerThread.start();
        writerThread.start();
    }
}
