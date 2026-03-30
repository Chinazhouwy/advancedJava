package com.advancedjava.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Scatter/Gather 分散读取/聚集写入演示
 * Scatter：从一个Channel读取数据到多个Buffer中
 * Gather：将多个Buffer中的数据写入到一个Channel中
 * 适用于数据分块场景，比如协议头+协议体分开存储的情况
 */
public class ScatterGatherDemo {

    public static void main(String[] args) throws IOException {
        String filePath = "/tmp/scatter-gather-demo.txt";

        // Gather聚集写入：将多个Buffer的数据一次性写入Channel
        gatherWrite(filePath);

        // Scatter分散读取：从Channel一次性读取数据到多个Buffer
        scatterRead(filePath);
    }

    /**
     * Gather聚集写入示例
     * 模拟HTTP协议：第一个Buffer存头信息，第二个Buffer存正文信息
     */
    private static void gatherWrite(String filePath) throws IOException {
        // 两个Buffer：头Buffer和正文Buffer
        ByteBuffer headerBuffer = ByteBuffer.wrap("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n".getBytes());
        ByteBuffer bodyBuffer = ByteBuffer.wrap("Hello Scatter/Gather!".getBytes());

        ByteBuffer[] buffers = {headerBuffer, bodyBuffer};

        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel channel = fos.getChannel()) {

            // 一次性写入所有Buffer的数据，按数组顺序写入
            long bytesWritten = channel.write(buffers);
            System.out.println("聚集写入总字节数：" + bytesWritten);

        }
    }

    /**
     * Scatter分散读取示例
     * 将数据按结构分别读取到不同的Buffer中
     */
    private static void scatterRead(String filePath) throws IOException {
        // 分配两个Buffer：第一个Buffer放头信息（100字节足够），第二个放正文
        ByteBuffer headerBuffer = ByteBuffer.allocate(100);
        ByteBuffer bodyBuffer = ByteBuffer.allocate(1024);

        ByteBuffer[] buffers = {headerBuffer, bodyBuffer};

        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel channel = fis.getChannel()) {

            // 一次性读取数据到多个Buffer，按数组顺序填满
            long bytesRead = channel.read(buffers);
            System.out.println("分散读取总字节数：" + bytesRead);

            // 切换到读模式
            headerBuffer.flip();
            bodyBuffer.flip();

            // 输出结果
            System.out.println("=== 读取到的头信息 ===");
            while (headerBuffer.hasRemaining()) {
                System.out.print((char) headerBuffer.get());
            }

            System.out.println("=== 读取到的正文 ===");
            while (bodyBuffer.hasRemaining()) {
                System.out.print((char) bodyBuffer.get());
            }
            System.out.println();
        }
    }
}
