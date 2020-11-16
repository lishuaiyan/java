package cn.net.duxingzhe.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class TimeServer {
    public void bind(int port) throws Exception {
        /*
        NioEventLoopGroup 是个线程组，它包含了一组NIO线程，专门用于网络事件的处理，实际上它们就是Reactor线程组
        这里创建两个的原因是一个用于服务端接受客户端的连接，另一个用于进行SocketChannel的网络读写
         */
        // 服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            /*
            创建ServerBootstrap，它是Netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
             */
            ServerBootstrap b = new ServerBootstrap();
            /*
           调用ServerBootstrap的group方法，将两个NIO线程组当作入参传递到ServerBootstrap中
           接着设置创建的Channel为NioServerSocketChannel，它的功能对应于JDK NIO类库中的ServerSocketChannel类
           然后配置NioServerSocketChannel的TCP参数，此处将它的backlog设置为1024，最后绑定IO事件的处理类ChildChannelHandler
           它的作用类似于Reactor模式中的handler类，主要用于处于网络IO事件，例如记录日志，对消息进行编解码
             */
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());
            /*
            服务端启动辅助类的配置完成之后，调用它的bind方法绑定监听端口，随后，调用它的同步阻塞方法sync等待绑定操作完成
            完成之后Netty会返回一个ChannelFuture，它的功能类似于JDK的java.util.concurrent.Future，主要用于异步操作的通知回调
             */
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();
            /*
            使用f.channel().closeFuture.sync()方法进行阻塞，等待服务端链路关闭之后main函数才退出
             */
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();

        } finally {
            // 优雅退出，释放线程资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline().addLast(new TimeServerHandler());
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
        new TimeServer().bind(port);
    }
}
