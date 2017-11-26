package com.example.hang.socketserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    TextView status_textview;
    Button startCollect;
    Button stopCollect;
//    Button track;
    SocketServerDemo socketServerDemo;
    static List<Integer> strengthList;
    static int maxRSSI = -200;
//    Map<String, Double> map;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textview);
        status_textview = (TextView) findViewById(R.id.status_textview);
        startCollect = (Button) findViewById(R.id.startCollect);
        stopCollect = (Button) findViewById(R.id.stopCollect);
//        track = (Button) findViewById(R.id.track);

        strengthList = new ArrayList<>();
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
                socketServerDemo.sendCommand(1);
            }
        });

        stopCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("Stop Button is clicked!");
                socketServerDemo.sendCommand(0);

                //analyze strengthList
                //先用平均数来处理
                StringBuilder builder = new StringBuilder();
                double temp = 0;
                for (int val : strengthList) {
                    builder.append(val);
                    builder.append(',');
                    temp += val;
                }
                temp = temp / strengthList.size();
                textView.setText(new String(builder) + '\n' + "averageRSSI = " + temp);

                strengthList.clear();
                maxRSSI = -200;
            }
        });

//        track.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (map.size() == 0) {
//                    textView.setText("No data is available. Please learn some data first!");
//                    return;
//                }
//
//                double maxVal = -200;
//                String maxLabel = null;
//                for (String key : map.keySet()) {
//                    if (map.get(key) > maxVal) {
//                        maxLabel = key;
//                        maxVal = map.get(key);
//                    }
//                }
//                textView.setText("The most possible position is " + maxLabel + "with RSSI = " + maxVal);
//            }
//        });
    }


//
//    @Override
//    public void applyText(String label) {
//        this.label = label;
//        textView.setText("label is " + label);
//
//    }
}
