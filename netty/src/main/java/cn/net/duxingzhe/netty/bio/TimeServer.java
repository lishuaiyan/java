package cn.net.duxingzhe.netty.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/06
 */
public class TimeServer {
    /**
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int port = 8080;
        /*
        TimeServer 根据传入的参数设置监听端口，没有入参则使用默认值8080
         */
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }

        ServerSocket server = null;
        try {
            // 通过构造函数创建ServerSocket，如果端口合法且没有被占用则服务端监听成功
            server = new ServerSocket(port);
            System.out.println("The time server is start in port : " + port);
            Socket socket = null;
            /*
            通过无限循环来监听客户端的连接，如果没有客户端接入，则主线程阻塞在ServerSocket的accept操作上
             */
            while (true) {
                socket = server.accept();
                // 当有新的客户端接入时，以Socket为参数构造TimeServerHandler对象，来处理这条socket链路
                new Thread(new TimeServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}
