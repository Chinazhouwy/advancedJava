package com.advancedjava.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * NIO 综合教学示例。
 *
 * <p>这个类把初学者最容易混淆的几个点放在一个文件中：
 * 1. Buffer 状态切换（position/limit/capacity + flip/clear/compact）
 * 2. FileChannel 读写
 * 3. scatter/gather 思想
 *
 * <p>建议先运行此类，再去看同目录下更细分的 Demo。
 */
public class NioComprehensiveDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("\n=== NIO 综合教学 Demo ===");
        bufferLifecycle();
        fileChannelReadWrite();
        scatterGatherConcept();
        System.out.println("=== NIO Demo 结束 ===\n");
    }

    /**
     * Buffer 生命周期演示。
     */
    private static void bufferLifecycle() {
        System.out.println("\n[1] Buffer 生命周期");

        // allocate(16): 分配 16 字节堆内存缓冲区。
        ByteBuffer buffer = ByteBuffer.allocate(16);
        printState("allocate", buffer);

        // put: 写入模式下，position 会前进。
        buffer.put("hello".getBytes(StandardCharsets.UTF_8));
        printState("after put hello", buffer);

        // flip: 从“写模式”切换到“读模式”。
        // 规则：limit = old position, position = 0
        buffer.flip();
        printState("after flip", buffer);

        byte first = buffer.get();
        System.out.println("read first byte = " + (char) first);
        printState("after get 1 byte", buffer);

        // compact: 把未读数据挪到开头，适合“读一点继续写”的场景。
        buffer.compact();
        printState("after compact", buffer);

        // clear: 清空读写边界（不是清零数据）
        buffer.clear();
        printState("after clear", buffer);
    }

    /**
     * FileChannel 读写演示。
     */
    private static void fileChannelReadWrite() throws IOException {
        System.out.println("\n[2] FileChannel 读写");

        Path temp = Path.of(System.getProperty("java.io.tmpdir"), "nio-learning-demo.txt");

        // 写文件：WRITE + CREATE + TRUNCATE_EXISTING
        try (FileChannel writeChannel =
                FileChannel.open(
                        temp,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer writeBuffer = ByteBuffer.wrap("NIO file channel demo".getBytes(StandardCharsets.UTF_8));
            int written = writeChannel.write(writeBuffer);
            System.out.println("written bytes = " + written + ", file = " + temp);
        }

        // 读文件：READ
        try (FileChannel readChannel = FileChannel.open(temp, StandardOpenOption.READ)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(64);
            int bytesRead = readChannel.read(readBuffer);
            readBuffer.flip();
            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);
            System.out.println("read bytes = " + bytesRead + ", content = "
                    + new String(data, StandardCharsets.UTF_8));
        }
    }

    /**
     * Scatter / Gather 概念演示（不走网络，先用内存缓冲区模拟）。
     */
    private static void scatterGatherConcept() {
        System.out.println("\n[3] Scatter/Gather 概念");

        // 假设协议: 4字节头 + 可变长 body
        ByteBuffer source = ByteBuffer.allocate(32);
        source.putInt(6); // header: body length
        source.put("ABCDEF".getBytes(StandardCharsets.UTF_8)); // body
        source.flip();

        ByteBuffer header = ByteBuffer.allocate(4);
        ByteBuffer body = ByteBuffer.allocate(16);

        // 这里手工模拟 scatter read：先读 header，再读 body
        while (source.hasRemaining() && header.hasRemaining()) {
            header.put(source.get());
        }
        while (source.hasRemaining() && body.hasRemaining()) {
            body.put(source.get());
        }

        header.flip();
        body.flip();

        int len = header.getInt();
        byte[] payload = new byte[len];
        body.get(payload, 0, len);

        System.out.println("header.length = " + len + ", body = "
                + new String(payload, StandardCharsets.UTF_8));

        // gather write 的思想正相反：把 header/body 两个 buffer 依次写到 channel。
        System.out.println("gather write 思想: channel.write(new ByteBuffer[]{headerBuf, bodyBuf})");
    }

    /**
     * 打印 Buffer 三元状态。
     */
    private static void printState(String step, ByteBuffer buffer) {
        System.out.printf("%s -> position=%d, limit=%d, capacity=%d%n",
                step, buffer.position(), buffer.limit(), buffer.capacity());
    }
}
