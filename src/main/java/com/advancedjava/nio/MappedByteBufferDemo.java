package com.advancedjava.nio;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * MappedByteBuffer 内存映射文件用法演示
 * 直接将文件映射到操作系统的虚拟内存，不需要用户态和内核态之间的数据拷贝，操作大文件效率极高
 */
public class MappedByteBufferDemo {

    public static void main(String[] args) throws Exception {
        String filePath = "/tmp/mapped-file-demo.txt";
        long fileSize = 1024 * 1024; // 1MB文件

        // 写入数据到内存映射文件
        writeMappedFile(filePath, fileSize);

        // 读取内存映射文件的数据
        readMappedFile(filePath, fileSize);
    }

    /**
     * 使用内存映射写入大文件
     */
    private static void writeMappedFile(String filePath, long fileSize) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
             FileChannel channel = raf.getChannel()) {

            // 映射文件到内存，模式为读写，映射0到fileSize字节
            MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);

            // 直接操作内存写入数据，不需要再调用channel.write
            for (int i = 0; i < fileSize; i++) {
                mappedBuffer.put((byte) ('a' + i % 26)); // 循环写入a-z
            }

            // 强制刷写到磁盘（可选，操作系统会自动同步，调用force确保持久化）
            mappedBuffer.force();
            System.out.println("内存映射文件写入完成，大小：" + fileSize + "字节");

        }
    }

    /**
     * 从内存映射文件读取数据
     */
    private static void readMappedFile(String filePath, long fileSize) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r");
             FileChannel channel = raf.getChannel()) {

            // 映射文件到内存，模式为只读
            MappedByteBuffer mappedBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            // 直接从内存读取数据
            System.out.println("读取第一个字节：" + (char) mappedBuffer.get(0));
            System.out.println("读取第1000个字节：" + (char) mappedBuffer.get(999));
            System.out.println("读取最后一个字节：" + (char) mappedBuffer.get((int) fileSize - 1));

            // 批量读取数据
            mappedBuffer.flip();
            byte[] first10Bytes = new byte[10];
            mappedBuffer.get(first10Bytes);
            System.out.println("前10个字节：" + new String(first10Bytes));

        }
    }
}
