package com.tcsl.myudpclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.tcsl.myudpclient.rxwebsocket.RxWebSocketUtil;
import com.tcsl.myudpclient.rxwebsocket.WebSocketInfo;
import com.tcsl.myudpclient.udp.UdpReceive;
import com.tcsl.myudpclient.udp.UdpReceiveBean;
import com.tcsl.myudpclient.udp.UdpSend;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

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
                    connect(url);
                    Log.d("UdpReceive", "handleMessage: " + url);
                    udpReceive.stopTask();
                }
            }
        });
        udpReceive.start();
        //客户端主动发送udp广播，用于获取服务器ip地址
        new UdpSend().start();
    }

    /**
     * 连接WebSocket服务端
     * @param url
     */
    private void connect(String url) {
        RxWebSocketUtil.getInstance().getWebSocketInfo(url,2000, TimeUnit.SECONDS).subscribe(new Observer<WebSocketInfo>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(WebSocketInfo webSocketInfo) {
                Log.d("csh", "onNext: " + webSocketInfo.getWebSocket());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}
