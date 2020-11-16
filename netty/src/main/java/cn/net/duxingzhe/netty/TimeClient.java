package cn.net.duxingzhe.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class TimeClient {
    public void connect(int port, String host) throws Exception {
        // 首先创建客户端处理IO读写的NioEventLoop Group线程组，然后继续创建客户端辅助启动类Bootstrap
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            // 对启动类Bootstrap进行配置，它的 Channel需要设置为NioSocketChannel，然后为其添加handler
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 这里直接创建匿名内部类，实现initChannel方法，其作用是当创建NioSocketChannel成功之后，
                    // 在初始化它的时候将它的ChannelHandler设置到ChannelPipeline()中，用于网络IO事件
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TimeClientHandler());
                        }
                    });
            // 客户端启动辅助类设置完成之后，调用connect方法发起异步连接，然后调用同步方法等待连接成功
            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 当客户端连接关闭之后，客户端主函数退出，在退出之前，释放NIO线程组的资源
            // 等待客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                //
            }

        }
        new TimeClient().connect(port, "127.0.0.1");
    }
}
