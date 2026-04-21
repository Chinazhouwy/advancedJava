package com.advancedjava.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * EchoServerHandler - 服务端处理器
 * 继承ChannelInboundHandlerAdapter，处理接收到的消息并回显给客户端
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 接收到客户端消息时的处理方法
     * 将接收到的消息原封不动地写回给客户端
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 将接收到的消息转换为ByteBuf
        ByteBuf in = (ByteBuf) msg;
        // 打印接收到的消息内容
        System.out.println("Server received: " + in.toString(io.netty.util.CharsetUtil.UTF_8));
        // 将接收到的消息写回给客户端（回显）
        ctx.write(in);
    }

    /**
     * 消息读取完成后的处理方法
     * 刷新缓冲区确保数据发送给客户端
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 刷新所有待写出的消息到客户端
        ctx.flush();
    }

    /**
     * 发生异常时的处理方法
     * 关闭通道连接
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 打印异常信息
        cause.printStackTrace();
        // 关闭通道
        ctx.close();
    }

}
