package cn.net.duxingzhe.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException {
        // 将msg转换成Netty的ByteBuf对象
        ByteBuf buf = (ByteBuf) msg;
        /*
        通过ByteBuf的readableBytes方法可以获取缓冲区可读的字节数，
        根据可读的字节数创建byte数组，通过ByteBuf的readBytes方法将缓冲区的字节数组复制到新建的byte数组中，最后通过new String的构造函数获取请求信息
         */
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("The Time server receive order : " + body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        // 通过ChannelHandlerContext的write方法异步发送应答消息给客户端
        ctx.write(resp);
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // flush()方法将发送的缓冲区中的消息全部写到SocketChannel中
        ctx.flush();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 当发生异常时，关闭ChannelHandlerContext，释放和ChannelHandlerContext相关联的句柄等资源
        ctx.close();
    }
}
