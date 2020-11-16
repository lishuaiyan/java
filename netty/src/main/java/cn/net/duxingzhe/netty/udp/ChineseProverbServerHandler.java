package cn.net.duxingzhe.netty.udp;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class ChineseProverbServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    /**
     * 谚语列表
      */
    private static final String[] DICTIONARY = {
            "只要功夫深，铁棒磨成绣花针。",
            "旧时王谢堂前燕，飞入寻常百姓家。",
            "洛阳亲友如相问，一片冰心在玉壶。",
            "一寸光阴一寸金，寸金难买寸光阴",
            "老骥伏枥，志在千里。烈士暮年，壮心不已！"
    };
    private String nextQuote() {
        int quoteId = ThreadLocalRandom.current().nextInt(DICTIONARY.length);
        return DICTIONARY[quoteId];
    }

    /**
     * Netty对UDP进行了封装，因此接受到的是Netty封装后的io.netty.channel.socket.DatagramPacket对象
     * @param channelHandlerContext
     * @param datagramPacket
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // 将Packet内容转换为字符串
        String req = datagramPacket.content().toString(CharsetUtil.UTF_8);
        System.out.println(req);
        // 对请求消息的合法性进行判断，如果是“谚语字典查询？”，则构造应答消息返回
        if ("谚语字典查询？".equals(req)) {
            channelHandlerContext.writeAndFlush(
                    /*
                    DatagramPacket有两个参数，第一个是需要发送的内容，为ByteBuf。另一个是目的地址，包括IP和端口号，可以从发送的报文DatagramPacket中获取
                     */
                    new DatagramPacket(Unpooled.copiedBuffer(
                            "谚语查询结果： " + nextQuote(), CharsetUtil.UTF_8
                    ), datagramPacket.sender())
            );

        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        cause.printStackTrace();
    }

    }

