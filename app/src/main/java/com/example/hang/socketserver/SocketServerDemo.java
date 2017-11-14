package com.example.hang.socketserver;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by hang on 11/13/17.
 */

public class SocketServerDemo extends Thread {
    TextView textView;
    int command;
    Socket s;
    public SocketServerDemo(TextView textView, int command, Socket s) {
        this.textView = textView;
        this.command = command;
        this.s = s;
    }

    public Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                textView.setText("new client joins in");
            } else if (msg.what == 1) {
                textView.setText("receive data from client:" + msg.obj);
            } else {
                textView.setText("something wrong");
            }
        }

    };
    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        try {
            myHandler.sendEmptyMessage(0);
            //command==0 means:  start to collect data
            //command==1 means:  stop to collect data
            OutputStream os = s.getOutputStream();
            os.write(command);
            os.flush();

            //receive
            InputStream in = s.getInputStream();
//            myHandler.sendEmptyMessage(1);

            byte[] b = new byte[32];
            int count = in.read(b);
            byte temp[] = new byte[count];
            for (int i = 0; i < count; i++) {
                temp[i] = b[i];
            }
            String str =  new String(temp);
            Message msg = new Message();
            msg.what = 1;
            msg.obj = str;
            myHandler.sendMessage(msg);

            os.close();
            in.close();

            s.close();
        } catch (IOException e1) {
            myHandler.sendEmptyMessage(2);
            e1.printStackTrace();
        }
    }
}
