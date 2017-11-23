package com.example.hang.socketserver;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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
    TextView status_textview;
    Button startCollect;
    Button stopCollect;
    List<Socket> socketList = new ArrayList<Socket>();
    SocketServerDemo socketServerDemo;
    private LineGraphSeries<DataPoint> mSeries2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textview);
        status_textview = (TextView) findViewById(R.id.status_textview);
        startCollect = (Button) findViewById(R.id.startCollect);
        stopCollect = (Button) findViewById(R.id.stopCollect);

        final GraphView graph2 = (GraphView) findViewById(R.id.graph2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setYAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        graph2.getViewport().setMaxY(0);
        graph2.getViewport().setMinY(-100);

        graph2.getViewport().setScrollable(true); // enables horizontal scrolling
        graph2.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph2.getViewport().setScrollableY(true); // enables horizontal scrolling
        graph2.getViewport().setScalableY(true); // enables horizontal zooming and scrolling

        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket server = new ServerSocket(12345);
                    while(true){
                        Socket s = server.accept();
                        socketServerDemo = new SocketServerDemo(textView, status_textview, s, mSeries2);
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
                textView.setText("Button is clicked!");
                socketServerDemo.sendCommand(1);
            }
        });


        stopCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketServerDemo.sendCommand(0);
            }
        });
    }
}
