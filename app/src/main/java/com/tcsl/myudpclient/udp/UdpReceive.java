package com.tcsl.myudpclient.udp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * 描述:接收udp广播的线程，接收到广播以后，发送一个端口号的广播
 * <p/>作者：wjx
 * <p/>创建时间: 2017/10/10 16:33
 */
public class UdpReceive extends Thread {
    private static final String TAG = "UdpReceive";
    private boolean flag = true;
    private DatagramSocket udpSocket;
    private Handler mHandler;

    public UdpReceive(Handler mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        try {
            udpSocket = new DatagramSocket(null);
            udpSocket.setReuseAddress(true);
            udpSocket.bind(new InetSocketAddress(9002));
            byte[] data = new byte[1024];
            DatagramPacket dataPacket = new DatagramPacket(data, data.length);
            while (flag) {
                try {
                    Log.e(TAG, "开启数据接收");
                    // 如果未收到数据，此句不会继续向下执行，一直处于监听状态
                    udpSocket.receive(dataPacket);
                    String port = new String(dataPacket.getData(), 0, dataPacket.getLength());
                    UdpReceiveBean bean = new UdpReceiveBean(dataPacket.getAddress().getHostAddress(), port);
                    Message message = mHandler.obtainMessage();
                    message.obj = bean;
                    mHandler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭线程
     */
    public void stopTask() {
        try {
            flag = false;
            if (udpSocket != null) {
                udpSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
