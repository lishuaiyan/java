package cn.net.duxingzhe.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/06
 */
public class MultiplexerTimeServer implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean stop;

    /**
     * 初始化多路复用器、绑定监听端口
     * @param port
     */
    public MultiplexerTimeServer(int port) {
        try {
            // 创建多路复用器Selector\ServerSocketChannel
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            // 设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024);
            // 系统资源初始化成功后,将ServerSocketChannel注册到selector，监听SelectionKey.OP_ACCEPT操作位
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            // 如果资源初始化失败则退出
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void stop() {
        this.stop = true;
    }
    @Override
    public void run() {
        while (!stop) {
            try {
                /*
                while循环体中遍历 selector，休眠时间为1s，无论是否有读写事件发生，selector每隔1s都被唤醒一次
                selector也提供了一个无参的select方法，当有处于就绪状态的Channel时，selector将返回就绪态的Channel
                的SelectionKey集合，通过对就绪态的Channel集合进行迭代，可以进行网络的异步读写操作
                 */
                selector.select(1000);
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // 多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 处理新接入的客户端请求信息
     * @param key
     * @throws IOException
     */
    private void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            /*
            根据SelectionKey的操作位进行判断即可获知网络事件的类型
             */
            // 处理新接入的请求信息
            if (key.isAcceptable()) {
                // 接受新的连接
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // 通过ServerSocketChannel的accept接收客户端的连接请求并创建SocketChannel实例
                SocketChannel sc = ssc.accept();
                // 将新创建的SocketChannel设置为异步非阻塞
                sc.configureBlocking(false);
                // 注册新的连接到selector
                sc.register(selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {
                // 读取数据
                SocketChannel sc = (SocketChannel) key.channel();
                // 首先创建一个ByteBuffer,开辟一个1K的缓冲区
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // 然后调用SocketChannel的read方法读取请求码流
                int readBytes = sc.read(readBuffer);
                // 返回值大于0 读到了字节 对字节进行编解码
                if (readBytes > 0) {
                    // 当读取到码流以后，进行解码首先需要对readBuffer进行flip操作，它的作用是将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作
                    readBuffer.flip();
                    // 然后根据缓冲区可读的字节个数创建字节数组
                    byte[] bytes = new byte[readBuffer.remaining()];
                    // 调用ByteBuffer的get操作将缓冲区可读的字节数组复制到新创建的字节数组中
                    readBuffer.get(bytes);
                    // 最后调用字符串的构造函数创建请求消息体
                    String body = new String(bytes, "UTF-8");
                    System.out.println("The time server receive order : " + body);
                    // 如果请求指令是 "QUERY TIME ORDER"则把服务器的当前时间编码后返回给客户端
                    String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString()
                             : "BAD ORDER";
                    doWrite(sc, currentTime);
                    // 返回值为 -1 链路已经关闭，需要关闭SocketChannel，释放资源
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                    // 返回值等于0 属于正常场景 忽略
                } else {
                    // 读取到0字节 忽略
                    ;
                }
            }
        }
    }

    /**
     * 将应答消息异步点发送给客户端
     * @param channel
     * @param response
     * @throws IOException
     */
    private void doWrite(SocketChannel channel, String response) throws IOException {
        if (response != null && response.trim().length() > 0) {
            // 首先将字符串编码成字节数组
            byte[] bytes = response.getBytes();
            // 根据字节数组的容量创建ByteBuffer
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            // 调用ByteBuffer的put操作将字节数组复制到缓冲区
            writeBuffer.put(bytes);
            // 然后对缓冲区进行flip操作
            writeBuffer.flip();
            // 最后调用SocketChannel的write方法将缓冲区中的字节数组发送出去
            channel.write(writeBuffer);
        }
    }
}
