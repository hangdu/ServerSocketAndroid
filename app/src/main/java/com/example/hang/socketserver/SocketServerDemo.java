package com.example.hang.socketserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hang on 11/13/17.
 */

public class SocketServerDemo extends Thread {
    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(12345);
            while (true) {
                // 等待客户端请求，如果没有客户端请求，会一直堵塞在这里
                Socket s = serverSocket.accept();
                OutputStream os = s.getOutputStream();
                os.write("Hello client".getBytes());
                os.close();
                s.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();

        }
    }
}
