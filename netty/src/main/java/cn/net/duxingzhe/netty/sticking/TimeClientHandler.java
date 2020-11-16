package cn.net.duxingzhe.netty.sticking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/17
 */
public class TimeClientHandler  extends ChannelHandlerAdapter {
    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());
    private int counter;
    private byte[] req;
    public TimeClientHandler() {
        req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
        
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf message = null;
        /*
        客户端连接成功后，循环发送100条消息，每发送一条就刷新一次，保证每条消息都会被写入Channel中
        按照我们的设计服务端应该接受到100条查询时间指令的请求消息
         */
        for (int i = 0; i < 100; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        /*
        客户端每接收到服务端一条应答消息之后，就打印一次计数器，按照设计初衷
        客户端应该打印100次服务端的系统时间
         */
        System.out.println("Now is : " + body + " ; the counter is : " + ++counter);
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 释放资源
        logger.warning("Unexpected exception from downstream : " + cause.getMessage());
        ctx.close();
    }
}
