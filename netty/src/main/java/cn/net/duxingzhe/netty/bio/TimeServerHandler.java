package cn.net.duxingzhe.netty.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @Author luke yan
 * @Description
 * @CreateDate 2020/11/06
 */
public class TimeServerHandler implements Runnable {
    private Socket socket;

    public TimeServerHandler(Socket socket) {
        this.socket = socket;
    }
    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    this.socket.getInputStream()
            ));
            out = new PrintWriter(this.socket.getOutputStream(), true);
            String currentTime = null;
            String body = null;
            while (true) {
                // 通过BufferedReader读取一行，如果已经读取到输入流尾部，则返回值为null，退出循环
                body = in.readLine();
                if (body == null) {
                    break;
                }
                System.out.println("The time server receive order : " + body);
                // 如果读到了非空值，则对内容进行判断，如果请求消息为查询时间的指令 "QUERY TIME ORDER" 则获取当前最新的系统时间，通过
                // PrintWriter的 println 函数发送给客户端
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(
                        System.currentTimeMillis()
                ).toString() : "BAD ORDER";
                out.println(currentTime);
            }
        } catch (Exception e) {
           e.printStackTrace();
        } finally {
            /*
            下面的代码 释放输入输出流和Socket套接字句柄资源，最后
            线程自动销毁并被虚拟机回收
             */
            if (in != null) {
                try {
                    in.close();
                } catch (IOException el) {
                    el.printStackTrace();
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                if (this.socket != null) {
                    try {
                        this.socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    this.socket = null;
                }
            }
        }
    }
}
