package com.example.hang.socketserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements LabelDialog.LabelDialogListener {
    TextView textView;
    TextView status_textview;
    Button startCollect;
    Button stopCollect;
    SocketServerDemo socketServerDemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textview);
        status_textview = (TextView) findViewById(R.id.status_textview);
        startCollect = (Button) findViewById(R.id.startCollect);
        stopCollect = (Button) findViewById(R.id.stopCollect);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(12345);
                    while(true){
                        Socket s = server.accept();
                        socketServerDemo = new SocketServerDemo(textView, status_textview, s);
                        new Thread(socketServerDemo).start();
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
                textView.setText("StartCollect Button is clicked!");
                openDialog();
            }
        });


        stopCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Stop Button is clicked!");
                socketServerDemo.sendCommand(0);
            }
        });
    }

    public void openDialog() {
        LabelDialog labelDialog = new LabelDialog();
        labelDialog.show(getSupportFragmentManager(), "label dialog");

    }


    @Override
    public void applyText(String label) {
        textView.setText("label is " + label);
        socketServerDemo.sendCommand(1);
    }
}
