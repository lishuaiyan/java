package cn.net.duxingzhe.netty.nio;

import sun.nio.cs.StandardCharsets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/09
 */
public class TimeClientHandle implements Runnable {
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop = false;

    public TimeClientHandle(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            // 初始化NIO的多路复用器和SocketChannel对象
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            // 将SocketChannel设置为非阻塞模式
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
    @Override
    public void run() {
        try {
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        /*
        在循环体中轮询多路复用器Selector，当有就绪的Channel时，执行handleInput(key) 方法
         */
        while (!stop) {
            try {
//                System.out.println(111);
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
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        // 多路复用器关闭后，所有注册在上面的Channel和Pipe资源会自动去注册关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handleInput(SelectionKey key) throws IOException {

        if (key.isValid()) {

            // 判断连接是否成功
            SocketChannel sc = (SocketChannel) key.channel();
            /*
            如果SelectionKey处于连接状态，说明服务端已经返回ACK应答消息，这时我们需要对连接结果进行判断，调用SocketChannel的
            finishConnect()方法，如果返回值为true, 说明客户端连接成功，如果返回值为false或者直接抛出IOException,说明连接失败
             */

            if (key.isConnectable()) {
//                System.out.println(111);
                if (sc.finishConnect()) {
                    // 将SocketChannel注册到多路复用器上，注册Selection.OP_READ操作位，监听网络读操作，然后发送请求消息给服务端
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    // 连接失败，进程退出
                    System.exit(1);
                }


                }
            if (key.isReadable()) {

                // 由于无法事先判断应答码流的大小，我们就预分配1M的接受缓冲区用于读取应答消息
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                // 调用SocketChannel的read()方法进行异步读取操作，由于是异步操作所以必须对读取的结果进行判断
                int readBytes = sc.read(readBuffer);
                // 如果读取到了消息，则对消息进行解码，最后打印结果，执行完将stop设置为true，线程退出循环
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("Now is : " + body);
                    this.stop = true;
                } else if (readBytes < 0) {
                    // 对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    // 读到0字节，忽略
                    ;
                }
            }
        }
    }
    private void doConnect() throws IOException {
        // 首先对SocketChannel的connect操作进行判断，如果直接连接成功，则注册到多路复用器上，发送请求消息，读应答
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            // 如果没有连接成功，则说明服务端没有返回TCP握手应答消息，但是这并不代表失败，我们需要将SocketChannel注册到多路复用器Selector上，
            // 注册Selectionkey.OP_CONNECT ，当服务器端返回TCP syn-ack消息后，Selector就能够轮询到这个SocketChannel处于连接就绪状态
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }
    private void doWrite(SocketChannel sc) throws IOException {
        // 构造请求消息体然后对其编码
        byte[] req = "QUERY TIME ORDER".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        // 写入到发送缓冲区中
        writeBuffer.put(req);
        writeBuffer.flip();
        // 然后调用SocketWrite的write方法进行发送
        sc.write(writeBuffer);
        // 最后通过hasRemaining()方法对发送结果进行判断，如果缓冲区中的消息全部发送完成，打印 ""
        if (!writeBuffer.hasRemaining()) {
            System.out.println("Send order 2 server succeed.");
        }
    }
}
