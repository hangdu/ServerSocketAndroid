package com.example.hang.socketserver;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button startCollect;
    Button stopCollect;
    List<Socket> socketList = new ArrayList<Socket>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textview);
        startCollect = (Button) findViewById(R.id.startCollect);
        stopCollect = (Button) findViewById(R.id.stopCollect);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(12345);
                    while(true){
                        Socket s = server.accept();
                        socketList.add(s);
                        //每当客户端连接之后启动一条ServerThread线程为该客户端服务
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();

        startCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Button is clicked!");

                if (socketList.size() == 0) {
                    return;
                }
                new Thread(new SocketServerDemo(textView, 0, socketList.get(0))).start();
                socketList.remove(0);

            }
        });

//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.postDelayed(new RepeatClick(startCollect), 2000);

    }

//    static class RepeatClick implements Runnable {
//        View v;
//        public RepeatClick(Button btn) {
//            this.v = btn;
//        }
//        @Override
//        public void run() {
//            v.performClick();
//            v.postDelayed(this, 2000);
//        }
//    }
}
