package com.advancedjava.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Selector 多路复用器基本用法演示
 * Selector是NIO核心组件，可以监控多个Channel的IO事件，实现单线程管理多个连接
 */
public class SelectorDemo {

    public static void main(String[] args) throws IOException {
        // 启动服务端
        new Thread(() -> {
            try {
                startServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 等待服务端启动
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 启动客户端，发送测试数据
        startClient("Hello Selector!");
    }

    /**
     * 启动NIO服务端，使用Selector监听连接
     */
    private static void startServer() throws IOException {
        // 1. 创建ServerSocketChannel，绑定端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8888));
        serverSocketChannel.configureBlocking(false); // 设置为非阻塞模式

        // 2. 创建Selector
        Selector selector = Selector.open();

        // 3. 将ServerSocketChannel注册到Selector，监听ACCEPT事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("NIO服务端启动，监听端口8888");

        // 4. 轮询Selector
        while (true) {
            // 阻塞等待就绪的Channel
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }

            // 获取所有就绪的SelectionKey
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 处理完要移除，避免重复处理

                // 处理ACCEPT事件：新客户端连接
                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverChannel.accept();
                    socketChannel.configureBlocking(false);
                    // 将新连接的SocketChannel注册到Selector，监听READ事件
                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("新客户端连接：" + socketChannel.getRemoteAddress());
                }

                // 处理READ事件：客户端发送数据
                if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = socketChannel.read(buffer);
                    if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String message = new String(data).trim();
                        System.out.println("收到客户端消息：" + message);

                        // 响应客户端
                        ByteBuffer responseBuffer = ByteBuffer.wrap(("收到你的消息：" + message).getBytes());
                        socketChannel.write(responseBuffer);
                    } else if (bytesRead == -1) {
                        // 客户端关闭连接
                        socketChannel.close();
                        System.out.println("客户端断开连接");
                    }
                }
            }
        }
    }

    /**
     * 启动NIO客户端，发送消息到服务端
     */
    private static void startClient(String message) throws IOException {
        // 1. 创建SocketChannel，连接服务端
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8888));
        socketChannel.configureBlocking(false);

        // 2. 等待连接完成
        while (!socketChannel.finishConnect()) {
            // 非阻塞模式下可以做其他事情
            System.out.println("等待连接完成...");
        }

        // 3. 发送消息
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);
        System.out.println("客户端发送消息：" + message);

        // 4. 读取服务端响应
        buffer.clear();
        int bytesRead = socketChannel.read(buffer);
        if (bytesRead > 0) {
            buffer.flip();
            byte[] response = new byte[buffer.remaining()];
            buffer.get(response);
            System.out.println("收到服务端响应：" + new String(response));
        }

        // 5. 关闭连接
        socketChannel.close();
    }
}
