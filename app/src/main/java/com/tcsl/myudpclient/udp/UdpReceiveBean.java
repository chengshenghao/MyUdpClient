package com.tcsl.myudpclient.udp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 描述：广播接收到的地址和端口号广播
 * <p/>作者：wu
 * <br/>创建时间：2018/9/3 17:34
 */
public class UdpReceiveBean implements Parcelable {
    private String ip;
    private String port = "9956";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ip);
        dest.writeString(this.port);
    }

    public UdpReceiveBean() {
    }

    public UdpReceiveBean(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    protected UdpReceiveBean(Parcel in) {
        this.ip = in.readString();
        this.port = in.readString();
    }

    public static final Parcelable.Creator<UdpReceiveBean> CREATOR = new Parcelable.Creator<UdpReceiveBean>() {
        @Override
        public UdpReceiveBean createFromParcel(Parcel source) {
            return new UdpReceiveBean(source);
        }

        @Override
        public UdpReceiveBean[] newArray(int size) {
            return new UdpReceiveBean[size];
        }
    };
}
