package cn.net.duxingzhe.netty.udp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class ChineseProverbClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        String response = datagramPacket.content().toString(CharsetUtil.UTF_8);
        if (response.startsWith("谚语查询结果")) {
            System.out.println(response);
            channelHandlerContext.close();
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
