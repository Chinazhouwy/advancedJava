package com.advancedjava.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * FileChannel 基本用法演示
 * Channel是NIO中用于IO操作的通道，双向可读可写，与Buffer配合使用
 */
public class FileChannelDemo {

    public static void main(String[] args) {
        String content = "Hello NIO FileChannel!";
        String filePath = "/tmp/file-channel-demo.txt";

        // 写入文件
        writeFile(content, filePath);

        // 读取文件
        String readContent = readFile(filePath);
        System.out.println("读取到的文件内容：" + readContent);

        // 文件拷贝示例
        copyFile(filePath, "/tmp/file-channel-copy.txt");
        System.out.println("文件拷贝完成");
    }

    /**
     * 使用FileChannel写入内容到文件
     */
    private static void writeFile(String content, String filePath) {
        // 1. 创建文件输出流，获取Channel
        try (FileOutputStream fos = new FileOutputStream(filePath);
             FileChannel channel = fos.getChannel()) {

            // 2. 分配缓冲区，写入数据
            ByteBuffer buffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));

            // 3. 将Buffer中的数据写入Channel
            int bytesWritten = channel.write(buffer);
            System.out.println("写入字节数：" + bytesWritten);

        } catch (IOException e) {
            throw new RuntimeException("写入文件失败", e);
        }
    }

    /**
     * 使用FileChannel读取文件内容
     */
    private static String readFile(String filePath) {
        StringBuilder result = new StringBuilder();

        // 1. 创建文件输入流，获取Channel
        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel channel = fis.getChannel()) {

            // 2. 分配缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            // 3. 从Channel读取数据到Buffer
            int bytesRead;
            while ((bytesRead = channel.read(buffer)) != -1) {
                // 切换到读模式
                buffer.flip();

                // 读取Buffer中的数据
                while (buffer.hasRemaining()) {
                    result.append((char) buffer.get());
                }

                // 清空Buffer，准备下一次读取
                buffer.clear();
            }

        } catch (IOException e) {
            throw new RuntimeException("读取文件失败", e);
        }

        return result.toString();
    }

    /**
     * 使用FileChannel的transferTo方法高效拷贝文件
     * 零拷贝技术，直接在内核空间完成数据传输，不需要用户态内存拷贝
     */
    private static void copyFile(String sourcePath, String targetPath) {
        try (FileInputStream fis = new FileInputStream(sourcePath);
             FileOutputStream fos = new FileOutputStream(targetPath);
             FileChannel sourceChannel = fis.getChannel();
             FileChannel targetChannel = fos.getChannel()) {

            // 直接将源Channel的数据传输到目标Channel，效率更高
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);

        } catch (IOException e) {
            throw new RuntimeException("文件拷贝失败", e);
        }
    }
}
