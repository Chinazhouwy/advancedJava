# EchoServer 代码逐行解释

```java
package com.advancedjava.netty;  // 1. 声明包名
```
声明该类属于`com.advancedjava.netty`包。

```java
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
```
2-10. 导入Netty相关类：
- `ServerBootstrap`: 服务端启动辅助类
- `ChannelFuture`: 异步操作结果
- `ChannelInitializer`: 通道初始化器
- `NioEventLoopGroup`: NIO事件循环组
- `SocketChannel`: 客户端通道
- `NioServerSocketChannel`: NIO服务端通道
- `InetSocketAddress`: IP端口地址

```java
public class EchoServer {

    private final int port;  // 11. 端口字段
```
11-12. 定义EchoServer类，包含一个final端口字段。

```java
    public EchoServer(int port) {
        this.port = port;  // 13. 构造器赋值
    }
```
13-18. 构造方法，接收端口参数并赋值给字段。

```java
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {  // 14. 检查参数
            System.err.println("Usage: " + EchoServer.class.getSimpleName() + " <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);  // 15. 解析端口
        new EchoServer(port).start();          // 16. 创建并启动服务器
    }
```
14-28. main方法：检查命令行参数，解析端口号，创建EchoServer实例并调用start()。

```java
    public void start() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(); // 17. 创建线程组
        try {
            ServerBootstrap b = new ServerBootstrap();      // 18. 创建启动辅助类
            b.group(group)                                  // 19. 绑定线程组
                    .channel(NioServerSocketChannel.class)        // 20. 指定NIO通道
                    .localAddress(new InetSocketAddress(port))    // 21. 绑定端口
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 22. 添加处理器
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync();            // 23. 同步绑定端口
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();            // 24. 阻塞直到关闭
        } finally {
            group.shutdownGracefully().sync();            // 25. 优雅关闭线程组
        }
    }

}
```
17-52. start()方法核心逻辑：
- 17: 创建NioEventLoopGroup（boss线程组，处理连接）
- 18: 创建ServerBootstrap
- 19: 绑定线程组
- 20: 指定使用NioServerSocketChannel
- 21: 绑定指定端口
- 22: 添加ChannelInitializer，初始化通道时添加EchoServerHandler
- 23: 同步绑定端口
- 24: 阻塞等待服务器关闭
- 25: finally块确保线程组被优雅关闭
