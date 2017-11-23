package com.example.hang.socketserver;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.orhanobut.logger.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hang on 11/13/17.
 */

public class SocketServerDemo extends Thread {
    TextView textView;
    TextView status_textview;
    Socket mSocket;
    long lastReceiveHeartbeatTime;

    SocketReadThread readThread;
    Queue<Integer> commandQueue = new LinkedList<>();
    private LineGraphSeries<DataPoint> mSeries2;
    private static double graph2LastXValue = 5d;
    public SocketServerDemo(TextView textView, TextView status_textview, Socket s, LineGraphSeries<DataPoint> mSeries2) {
        this.textView = textView;
        this.status_textview = status_textview;
        this.mSocket = s;
        this.mSeries2 = mSeries2;
        lastReceiveHeartbeatTime = System.currentTimeMillis();

        new Thread(monitorHeartBeat).start();
    }

    public void sendCommand(int command) {
        commandQueue.add(command);
    }


    /**
     * 断开连接
     *
     */
    private void releaseLastSocket() {
        try {
            if (null != mSocket) {
                if (!mSocket.isClosed()) {
                    mSocket.close();
                }
            }
            mSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //There should be two thread
    //One thread is used to receive
    //Another thread is used to send



//    如果主线程接收到的是心跳包，将该客户端对应的计数器 count 清零；
//    在子线程中，每隔3秒遍历一次所有客户端的计数器 count：
    Runnable monitorHeartBeat = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (System.currentTimeMillis() - lastReceiveHeartbeatTime > 10000) {
                    Logger.d("SocketServerDemo", "Not receiving heartbeat on time. Closing this socket");
                    Message msg = new Message();
                    msg.what = 7;
                    msg.obj = "Not receiving heartbeat on time. Closing this socket";
                    myHandler.sendMessage(msg);
                    releaseLastSocket();

                    graph2LastXValue += 1d;
                    mSeries2.appendData(new DataPoint(graph2LastXValue, -100), true, 40);
                    return;
                }

                try {
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    public class SocketWriteThread extends Thread {
        private static final String TAG = "SocketWriteThread";
        private volatile boolean mStopThread = false;
        public void release() {
            mStopThread = true;
        }

        @Override
        public void run() {
            DataOutputStream mOutputStream = null;
            try {
                mOutputStream = new DataOutputStream(mSocket.getOutputStream());
                while (mStopThread) {
                    while (!commandQueue.isEmpty()) {
                        int curCommand = commandQueue.poll();
                        mOutputStream.writeBytes(String.valueOf(curCommand));
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mOutputStream != null) {
                    try {
                        mOutputStream.close();
                        mOutputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public class SocketReadThread extends Thread {
        private static final String TAG = "SocketReadThread";
        private volatile boolean mStopThread = false;

        public void release() {
            mStopThread = true;
            releaseLastSocket();
        }
        @Override
        public void run() {
            DataInputStream mInputStream = null;
            try {
                mInputStream = new DataInputStream(mSocket.getInputStream());
                Logger.d(TAG, "SocketThread running!");
                while (!mStopThread) {
                    String resultStr = mInputStream.readUTF();
                    Logger.d(TAG, "read:" + resultStr);

                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = resultStr;
                    myHandler.sendMessage(msg);

                    graph2LastXValue += 1d;
                    int index = resultStr.indexOf('=');
                    double RSSI = Double.valueOf(resultStr.substring(index+1));

                    mSeries2.appendData(new DataPoint(graph2LastXValue, RSSI), true, 40);
                    lastReceiveHeartbeatTime = System.currentTimeMillis();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mSocket != null) {
                        mSocket.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mInputStream != null) {
                    try {
                        mInputStream.close();
                        mInputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                status_textview.setText("new client joins in");
            } else if (msg.what == 1) {
                status_textview.setText(msg.obj + "");
            } else if (msg.what == 2) {
                textView.setText("Stop is clicked");
            } else if(msg.what == 3) {
                textView.setText("send command 1");
            } else if (msg.what == 4) {
                status_textview.setText("Client is not connected.");
            } else if (msg.what == 5) {
                status_textview.setText("HeatBeart");
            } else if (msg.what == 6) {
                status_textview.setText("receive " + msg.obj);
            } else if (msg.what == 7) {
                status_textview.setText(msg.obj + "!");
            }

            else {
                textView.setText("something wrong");
            }
        }
    };



    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        myHandler.sendEmptyMessage(0);
        Logger.d("SocketServer", "Testing");

        readThread = new SocketReadThread();
        readThread.start();
    }
}
