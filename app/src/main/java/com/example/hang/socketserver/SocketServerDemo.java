package com.example.hang.socketserver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hang on 11/13/17.
 */

public class SocketServerDemo extends Thread {
    TextView textView;
//    int command;
    Socket s;
    InputStream in;
    OutputStream os;

    boolean isReceive = false;
    Queue<Integer> commandQueue = new LinkedList<>();
    public SocketServerDemo(TextView textView, Socket s) {
        this.textView = textView;
//        this.command = command;
        this.s = s;
//        sendCommand(command);
    }

    public void sendCommand(int command) {
        commandQueue.add(command);
    }

    public Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                textView.setText("new client joins in");
            } else if (msg.what == 1) {
                textView.setText("receive data from client:" + msg.obj);
            } else if (msg.what == 2) {
                textView.setText("Stop is clicked");
            } else {
                textView.setText("something wrong");
            }
        }

    };
    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        while (true) {
            while (!commandQueue.isEmpty()) {
                int curCommand = commandQueue.poll();
                try {
                    os = s.getOutputStream();
                    os.write(String.valueOf(curCommand).getBytes());
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (curCommand == 1) {
                    isReceive = true;
                    myHandler.sendEmptyMessage(0);
                    Runnable runnable= new Runnable() {
                        @Override
                        public void run() {
                            while (isReceive) {
                                try {
                                    in = s.getInputStream();
                                    byte[] b = new byte[32];
                                    int count = in.read(b);
                                    //check the value of count so that the server know whether the client close the socket or not
                                    byte temp[] = new byte[count];
                                    for (int i = 0; i < count; i++) {
                                        temp[i] = b[i];
                                    }
                                    String str =  new String(temp);
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = str;
                                    myHandler.sendMessage(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    new Thread(runnable).start();
                } else {
                    myHandler.sendEmptyMessage(2);
                    isReceive = false;
                }
            }
        }


        //support to send command continuously

//        try {
//            os.close();
//            in.close();
//            s.close();
//        } catch (IOException e1) {
//            myHandler.sendEmptyMessage(2);
//            e1.printStackTrace();
//        }
    }
}
