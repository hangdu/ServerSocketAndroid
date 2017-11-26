package com.example.hang.socketserver;

import android.media.AudioManager;
import android.media.ToneGenerator;
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
import java.net.InterfaceAddress;
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
    long lastReceiveRSSITime;

    SocketReadThread readThread;
    SocketWriteThread writeThread;
    Queue<Integer> commandQueue = new LinkedList<>();
    ToneGenerator toneG;
    public SocketServerDemo(TextView textView, TextView status_textview, Socket s) {
        this.textView = textView;
        this.status_textview = status_textview;
        this.mSocket = s;
        lastReceiveHeartbeatTime = System.currentTimeMillis();

        new Thread(monitorHeartBeat).start();
        // send the tone to the "alarm" stream (classic beeps go there) with 50% volume
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
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
            if (System.currentTimeMillis() - lastReceiveHeartbeatTime > 9000 && System.currentTimeMillis() - lastReceiveRSSITime > 9000) {
                Logger.d("SocketServerDemo", "Not receiving heartbeat on time. Closing this socket");
                Message msg = new Message();
                msg.what = 7;
                msg.obj = "Not receiving heartbeat on time. Closing this socket";
                myHandler.sendMessage(msg);

                releaseLastSocket();
                writeThread.release();
                readThread.release();
                commandQueue.clear();
                myHandler.removeCallbacks(monitorHeartBeat);
                return;
            }

            myHandler.postDelayed(monitorHeartBeat, 9000);
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
                while (!mStopThread) {
                    while (!commandQueue.isEmpty()) {
                        int curCommand = commandQueue.poll();
                        mOutputStream.writeUTF(String.valueOf(curCommand));
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
                    if (resultStr.equals("heartbeat")) {
                        lastReceiveHeartbeatTime = System.currentTimeMillis();
                    } else {
                        lastReceiveRSSITime = System.currentTimeMillis();
                        int index = resultStr.indexOf('=');
                        int RSSI = Integer.valueOf(resultStr.substring(index+2));
                        MainActivity.strengthList.add(RSSI);
                        if (RSSI > MainActivity.maxRSSI) {
                            MainActivity.maxRSSI = RSSI;
                            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        }
                    }
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = resultStr;
                    myHandler.sendMessage(msg);
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

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();
        myHandler.sendEmptyMessage(0);
        Logger.d("SocketServer", "Testing");

        readThread = new SocketReadThread();
        readThread.start();

        writeThread = new SocketWriteThread();
        writeThread.start();
    }
}
