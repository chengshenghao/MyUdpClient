package com.tcsl.myudpclient.rxwebsocket;

import okhttp3.WebSocket;
import okio.ByteString;

/**
 * 描述:发送udp广播，广播socket对应的端口号
 * <p/>作者：wjx
 * <p/>创建时间: 2017/10/10 16:33
 */
public class WebSocketInfo implements Cloneable {
    private WebSocket mWebSocket;
    private String mString;
    private ByteString mByteString;
    private boolean onOpen;
    private boolean connected = true;

    public WebSocketInfo() {
    }

    public WebSocketInfo(boolean onOpen) {
        this.onOpen = onOpen;
    }

    public WebSocketInfo(WebSocket webSocket, boolean onOpen) {
        mWebSocket = webSocket;
        this.onOpen = onOpen;
    }

    public WebSocketInfo(WebSocket webSocket, String mString) {
        mWebSocket = webSocket;
        this.mString = mString;
    }

    public WebSocketInfo(WebSocket webSocket, ByteString byteString) {
        mWebSocket = webSocket;
        mByteString = byteString;
    }

    public WebSocket getWebSocket() {
        return mWebSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        mWebSocket = webSocket;
    }

    public String getString() {
        return mString;
    }

    public void setString(String string) {
        this.mString = string;
    }

    public ByteString getByteString() {
        return mByteString;
    }

    public void setByteString(ByteString byteString) {
        mByteString = byteString;
    }

    public boolean isOnOpen() {
        return onOpen;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
