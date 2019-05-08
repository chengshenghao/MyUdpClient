package com.tcsl.myudpclient.udp;

import android.util.Log;

import com.google.gson.Gson;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 描述:发送udp广播，广播socket对应的端口号
 * <p/>作者：csh
 * <p/>创建时间: 2019/5/8 11:10
 */
public class UdpSend extends Thread {
    private static final String TAG = "UdpSend";

    @Override
    public void run() {
        DatagramSocket udpSocket = null;
        try {
            udpSocket = new DatagramSocket(null);
            udpSocket.setReuseAddress(true);
            byte[] bytes = new Gson().toJson(new UdpSendBean()).getBytes();
            DatagramPacket dataPacket = new DatagramPacket(bytes, bytes.length);
            dataPacket.setAddress(InetAddress.getByName("255.255.255.255"));
            dataPacket.setPort(9001);
            Log.e(TAG, "发送数据");
            udpSocket.send(dataPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (udpSocket != null) {
                udpSocket.close();
            }
        }
    }
}
