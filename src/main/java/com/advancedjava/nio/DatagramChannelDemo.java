package com.advancedjava.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

/**
 * DatagramChannel UDP通信演示
 * 面向无连接的UDP协议通信，支持非阻塞模式
 */
public class DatagramChannelDemo {

    private static final int PORT = 9999;

    public static void main(String[] args) {
        // 启动服务端
        new Thread(() -> {
            try {
                startUdpServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 等待服务端启动
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 启动客户端发送消息
        startUdpClient("Hello UDP DatagramChannel!");
    }

    /**
     * 启动UDP服务端，接收客户端消息
     */
    private static void startUdpServer() throws IOException {
        // 1. 打开DatagramChannel，绑定端口
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.socket().bind(new InetSocketAddress(PORT));
        datagramChannel.configureBlocking(false); // 非阻塞模式

        // 2. 注册到Selector，监听读事件
        Selector selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("UDP服务端启动，监听端口" + PORT);

        // 3. 轮询Selector
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isReadable()) {
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    InetSocketAddress clientAddress = (InetSocketAddress) channel.receive(buffer);

                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    String message = new String(data).trim();
                    System.out.println("收到客户端 " + clientAddress + " 消息：" + message);

                    // 响应客户端
                    ByteBuffer responseBuffer = ByteBuffer.wrap(("UDP服务端收到：" + message).getBytes());
                    channel.send(responseBuffer, clientAddress);
                }
            }
        }
    }

    /**
     * 启动UDP客户端，发送消息到服务端
     */
    private static void startUdpClient(String message) {
        try {
            // 1. 打开DatagramChannel
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);

            // 2. 发送消息到服务端
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            datagramChannel.send(buffer, new InetSocketAddress("localhost", PORT));
            System.out.println("UDP客户端发送消息：" + message);

            // 3. 接收服务端响应
            ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
            // 因为是非阻塞模式，可能需要重试几次确保收到响应
            int retry = 0;
            while (retry < 10) {
                InetSocketAddress serverAddress = (InetSocketAddress) datagramChannel.receive(responseBuffer);
                if (serverAddress != null) {
                    responseBuffer.flip();
                    byte[] data = new byte[responseBuffer.remaining()];
                    responseBuffer.get(data);
                    System.out.println("收到服务端响应：" + new String(data));
                    break;
                }
                retry++;
                Thread.sleep(100);
            }

            // 4. 关闭通道
            datagramChannel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
