package com.advancedjava.nio;

import java.nio.ByteBuffer;

/**
 * NIO Buffer 基本用法演示
 * Buffer核心属性：
 * 1. capacity: 缓冲区容量，创建后不可修改
 * 2. position: 当前读写位置，下一个要读写的元素索引
 * 3. limit: 读写的上限，position不能超过limit
 * 4. mark: 标记位置，用于reset()回到该位置
 */
public class BufferBasicDemo {

    public static void main(String[] args) {
        // 1. 分配一个容量为10的ByteBuffer（堆内存缓冲区）
        ByteBuffer buffer = ByteBuffer.allocate(10);
        printBufferState("初始化分配后", buffer);

        // 2. 向缓冲区写入数据
        buffer.put((byte) 'H');
        buffer.put((byte) 'e');
        buffer.put((byte) 'l');
        buffer.put((byte) 'l');
        buffer.put((byte) 'o');
        printBufferState("写入5个字节后", buffer);

        // 3. 切换到读模式：将limit设为当前position，position重置为0
        buffer.flip();
        printBufferState("flip()切换到读模式后", buffer);

        // 4. 读取数据
        System.out.println("读取第一个字节：" + (char) buffer.get());
        System.out.println("读取第二个字节：" + (char) buffer.get());
        printBufferState("读取2个字节后", buffer);

        // 5. mark()标记当前position，reset()回到标记位置
        buffer.mark();
        System.out.println("读取第三个字节：" + (char) buffer.get());
        printBufferState("mark后再读取1个字节", buffer);

        buffer.reset();
        printBufferState("reset()回到mark位置", buffer);

        // 6. 读取剩余所有数据
        System.out.print("剩余数据：");
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        System.out.println();
        printBufferState("读取完所有剩余数据后", buffer);

        // 7. clear()清空缓冲区（实际上只是重置position和limit，数据还在，只是被遗忘了）
        buffer.clear();
        printBufferState("clear()后", buffer);

        // 8. compact()压缩缓冲区：将未读的数据复制到缓冲区开头，position设为未读数据长度，limit设为capacity
        buffer.put((byte) 'W');
        buffer.put((byte) 'o');
        buffer.put((byte) 'r');
        buffer.put((byte) 'l');
        buffer.put((byte) 'd');
        buffer.flip();
        System.out.println("读取第一个字节：" + (char) buffer.get());
        printBufferState("读取1个字节后", buffer);
        buffer.compact();
        printBufferState("compact()压缩后", buffer);
    }

    /**
     * 打印Buffer当前状态
     */
    private static void printBufferState(String step, ByteBuffer buffer) {
        System.out.printf("%s: position=%d, limit=%d, capacity=%d%n",
                step, buffer.position(), buffer.limit(), buffer.capacity());
    }
}
