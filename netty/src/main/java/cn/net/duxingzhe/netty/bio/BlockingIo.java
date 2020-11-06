package cn.net.duxingzhe.netty.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author luke yan
 * @Description netty 学习 阻塞IO实例
 * 这个Demo的方案只能同时处理一个连接，要管理多个并发客户端，需要为每个新的客户端Socket创建一个新的Thread
 * 多线程的方案对于支撑中小数量的客户端来说还算可以接受，但是对高并发连接表现就很差消耗资源很不理想
 * 该方案的缺点：
 * 1、任何时候都有可能有大量的线程处于休眠状态，只是等待输入或者输出数据就绪，造成资源浪费
 * 2、需要为每个线程的调用栈都分配内存
 * 3、虽然JVM在物理上可以支持非常大的线程，但是远在达到该极限之前，上下文切换带来的开销就会带来麻烦
 * @CreateDate 2020/11/05
 */
public class BlockingIo {
    public static void main(String[] args) throws IOException {
        // 创建一个新的ServerSocket，用以监听指定端口上的连接请求
        ServerSocket serverSocket = new ServerSocket(80);
        /*
        ServerSocket上的accept()方法将会一直阻塞到一个连接建立，随后返回一个新的Socket用于客户端和服务器之间的通信
        该ServerSocket将继续监听传入的连接
         */
        // 对accept()方法的调用将被阻塞，直到一个连接建立
        Socket clientSocket = serverSocket.accept();
        /*
        BufferedReader 和 PrintWriter 都衍生自Socket的输入输出流，前者从一个字符输入流中读取文本，后者打印对象的格式化的表示到文本输出流
         */
        // 下面的流对象都派生于该套接字的流对象
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String request, response;
        /*
        readLine()方法将会被阻塞，直到在由一个换行符或者回车符与的字符串被读取
         */
        // 开始循环处理
        while ((request = in.readLine()) != null) {
            // 如果客户端发送了“Done”则退出循环处理
            if ("Done".equals(request)) {
                break;
            }
            // 请求被传递给服务器的处理方法
            response = processRequest(request);
            // 服务器的响应被发送给了客户端之后继续循环处理
            out.println(response);
        }
    }
    public static String processRequest(String request) {
        return "";
    }
}
