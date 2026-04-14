package com.advancedjava.netty;
// 导入Netty服务端启动类
import io.netty.bootstrap.ServerBootstrap;
// 导入异步操作结果接口，用于绑定端口等操作
import io.netty.channel.ChannelFuture;
// 导入通道初始化器，用于配置ChannelPipeline
import io.netty.channel.ChannelInitializer;
// 导入NIO事件循环组，负责处理连接和IO事件
import io.netty.channel.nio.NioEventLoopGroup;
// 导入客户端通道类型
import io.netty.channel.socket.SocketChannel;
// 导入NIO服务端通道类型
import io.netty.channel.socket.nio.NioServerSocketChannel;
// 导入IP地址和端口封装类
import java.net.InetSocketAddress;

/**
 * EchoServer - 回显服务器
 * 接收客户端发送的消息，并将消息原封不动地返回给客户端
 */
public class EchoServer {

    // 服务端口号
    private final int port;

    // 构造方法，接收端口参数
    public EchoServer(int port) {
        this.port = port;
    }

    // 程序入口main方法
    public static void main(String[] args) throws Exception {
        // 检查命令行参数数量，必须传入一个端口参数
        if (args.length != 1) {
            System.err.println(
                    "Usage: " + EchoServer.class.getSimpleName() +
                            " <port>");
            return;
        }
        // 解析命令行传入的端口号
        int port = Integer.parseInt(args[0]);
        // 创建EchoServer实例并调用start方法启动服务器
        new EchoServer(port).start();
    }

    // 启动服务器的方法
    public void start() throws Exception {
        // 创建NIO事件循环组（boss组），负责处理连接请求
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建ServerBootstrap，服务端启动辅助类
            ServerBootstrap b = new ServerBootstrap();
            // 绑定boss事件循环组
            b.group(group)
                    // 指定使用NIO服务端的Socket通道实现
                    .channel(NioServerSocketChannel.class)
                    // 绑定本地监听地址和端口
                    .localAddress(new InetSocketAddress(port))
                    // 配置子通道（客户端连接）的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 向ChannelPipeline添加EchoServerHandler处理器
                            // 该处理器负责处理客户端消息的读取和回显
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            // 同步绑定端口，返回ChannelFuture表示绑定结果
            ChannelFuture f = b.bind().sync();
            // 打印服务器启动信息，显示监听地址
            System.out.println(EchoServer.class.getName() + " started and listen on " + f.channel().localAddress());
            // 同步阻塞，等待服务器通道关闭（通常是接收到关闭信号）
            f.channel().closeFuture().sync();
        } finally {
            // 优雅关闭事件循环组，释放相关资源
            group.shutdownGracefully().sync();
        }
    }

}
