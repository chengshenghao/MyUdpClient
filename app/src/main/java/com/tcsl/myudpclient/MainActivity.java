package com.tcsl.myudpclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tcsl.myudpclient.udp.UdpReceive;
import com.tcsl.myudpclient.udp.UdpReceiveBean;
import com.tcsl.myudpclient.udp.UdpSend;

/**
 * 描述：进行广播获取服务器ip地址
 * <p/>作者：csh
 * <p/>创建时间: 2019/5/8 11:10
 */
public class MainActivity extends AppCompatActivity {

    private UdpReceive udpReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //客户端接收到服务端广播
        udpReceive = new UdpReceive(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Object obj = msg.obj;
                if (obj instanceof UdpReceiveBean) {
                    UdpReceiveBean bean = (UdpReceiveBean) obj;
                    String url = new StringBuilder("ws://").append(bean.getIp()).append(":").append(bean.getPort()).append("/").toString();
                    Log.d("UdpReceive", "handleMessage: " + url);
                    udpReceive.stopTask();
                }
            }
        });
        udpReceive.start();
        //客户端主动发送udp广播，用于获取服务器ip地址
        new UdpSend().start();
    }
}
