package com.tcsl.myudpclient.rxwebsocket;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


/**
 * Created by dhh on 2017/9/21.
 * <p>
 * WebSocketUtil based on okhttp and RxJava
 * </p>
 * Core Feature : WebSocket will be auto reconnection onFailed.
 */
public class RxWebSocketUtil {
    private static RxWebSocketUtil instance;

    private OkHttpClient client;

    private Map<String, Observable<WebSocketInfo>> observableMap;
    private Map<String, WebSocket> webSocketMap;
    private boolean showLog = true;

    private RxWebSocketUtil() {
        try {
            Class.forName("okhttp3.OkHttpClient");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency okhtt");
        }
        try {
            Class.forName("io.reactivex.Observable");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency rxjava 2.+");
        }
        try {
            Class.forName("io.reactivex.android.schedulers.AndroidSchedulers");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency rxandroid 2.+");
        }
        observableMap = new ArrayMap<>();
        webSocketMap = new ArrayMap<>();
        client = new OkHttpClient();
    }

    public static RxWebSocketUtil getInstance() {
        if (instance == null) {
            synchronized (RxWebSocketUtil.class) {
                if (instance == null) {
                    instance = new RxWebSocketUtil();
                }
            }
        }
        return instance;
    }

    /**
     * @param url      ws://127.0.0.1:8080/websocket
     * @param timeout  The WebSocket will be reconnected after the specified time interval is not "onMessage",
     *                 <p>
     *                 在指定时间间隔后没有收到消息就会重连WebSocket,为了适配小米平板,因为小米平板断网后,不会发送错误通知
     * @param timeUnit unit
     * @return
     */
    public Observable<WebSocketInfo> getWebSocketInfo(final String url, final long timeout, final TimeUnit timeUnit) {
        Observable<WebSocketInfo> observable = observableMap.get(url);
        if (observable == null) {
            observable = Observable.create(new WebSocketOnSubscribe(url))
                    .timeout(timeout, timeUnit)
                    .retry(new Predicate<Throwable>() {
                        @Override
                        public boolean test(Throwable throwable) throws Exception {
                            return throwable instanceof IOException || throwable instanceof TimeoutException;
                        }
                    })
                    .doOnDispose(new Action() {
                        @Override
                        public void run() throws Exception {
                            WebSocket webSocket = webSocketMap.get(url);
                            if (webSocket != null) {
                                webSocket.close(1000, "主动关闭");
                            }
                            observableMap.remove(url);
                            webSocketMap.remove(url);
                            if (showLog) {
                                Log.d("RxWebSocketUtil", "注销");
                            }
                        }
                    })
                    .doOnNext(new Consumer<WebSocketInfo>() {
                        @Override
                        public void accept(WebSocketInfo webSocketInfo) throws Exception {
                            if (webSocketInfo.isOnOpen()) {
                                webSocketMap.put(url, webSocketInfo.getWebSocket());
                            }
                        }
                    })
                    .share()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            observableMap.put(url, observable);
        } else {
            observable = Observable.merge(Observable.just(new WebSocketInfo(webSocketMap.get(url), true)), observable);
        }
        return observable;
    }

    /**
     * default timeout: 30 days
     * <p>
     * 若忽略小米平板,请调用这个方法
     * </p>
     */
    public Observable<WebSocketInfo> getWebSocketInfo(String url) {
        return getWebSocketInfo(url, 30, TimeUnit.DAYS);
    }

    /**
     * 如果url的WebSocket已经打开,可以直接调用这个发送消息.
     */
    public void send(String url, String msg) {
        WebSocket webSocket = webSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(msg);
        } else {
            Log.i("csh", "服务器未连接");
        }
    }


    /**
     * 如果url的WebSocket已经打开,可以直接调用这个发送消息.
     *
     * @param url
     * @param byteString
     */

    public void send(String url, ByteString byteString) {
        WebSocket webSocket = webSocketMap.get(url);
        if (webSocket != null) {
            webSocket.send(byteString);
        } else {
            throw new IllegalStateException("The WebSokcet not open");
        }
    }

    /**
     * 不用关心url 的WebSocket是否打开,可以直接发送
     */

    public void asyncSend(String url, final String msg) {
        getWebSocket(url)
                .take(1)
                .subscribe(new Consumer<WebSocket>() {
                    @Override
                    public void accept(WebSocket webSocket) throws Exception {
                        webSocket.send(msg);
                    }
                });
    }

    public Observable<WebSocket> getWebSocket(String url) {
        return getWebSocketInfo(url)
                .map(new Function<WebSocketInfo, WebSocket>() {
                    @Override
                    public WebSocket apply(@NonNull WebSocketInfo webSocketInfo) throws Exception {
                        return webSocketInfo.getWebSocket();
                    }
                });
    }

    /**
     * 不用关心url 的WebSocket是否打开,可以直接发送
     *
     * @param url
     * @param byteString
     */

    public void asyncSend(String url, final ByteString byteString) {
        getWebSocket(url)
                .take(1)
                .subscribe(new Consumer<WebSocket>() {
                    @Override
                    public void accept(WebSocket webSocket) throws Exception {
                        webSocket.send(byteString);
                    }
                });
    }

    private Request getRequest(String url) {
        return new Request.Builder().get().url(url).build();
    }

    private final class WebSocketOnSubscribe implements ObservableOnSubscribe<WebSocketInfo> {
        private String url;

        private WebSocket webSocket;

        private WebSocketInfo startInfo, stringInfo, byteStringInfo;

        public WebSocketOnSubscribe(String url) {
            this.url = url;
            startInfo = new WebSocketInfo(true);
            stringInfo = new WebSocketInfo();
            byteStringInfo = new WebSocketInfo();
        }

        @Override
        public void subscribe(ObservableEmitter<WebSocketInfo> subscriber) throws Exception {
            if (webSocket != null) {
                //降低重连频率
                if (!"main".equals(Thread.currentThread().getName())) {
                    SystemClock.sleep(2000);
                }
            }
            initWebSocket(subscriber);
        }

        private void initWebSocket(final ObservableEmitter<WebSocketInfo> subscriber) {
            webSocket = client.newWebSocket(getRequest(url), new WebSocketListener() {
                @Override
                public void onOpen(final WebSocket webSocket, final Response response) {
                    if (showLog) {
                        Log.d("RxWebSocketUtil", url + " --> onOpen");
                    }
                    webSocketMap.put(url, webSocket);
                    AndroidSchedulers.mainThread().createWorker().schedule(new Runnable() {

                        @Override
                        public void run() {
                            if (!subscriber.isDisposed()) {
                                startInfo.setWebSocket(webSocket);
                                startInfo.setConnected(true);
                                subscriber.onNext(startInfo);
                            }
                        }
                    });
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.e("onMessage", text);
                    if (!subscriber.isDisposed()) {
                        subInfo(webSocket, text, true);
                    }
                }

                /**
                 *传递状态信息
                 * @param webSocket
                 * @param text
                 * @param state
                 */
                private void subInfo(WebSocket webSocket, String text, boolean state) {
                    WebSocketInfo stringInfo = new WebSocketInfo();
                    stringInfo.setWebSocket(webSocket);
                    stringInfo.setString(text);
                    stringInfo.setConnected(state);
                    subscriber.onNext(stringInfo);
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    if (showLog) {
                        Log.e("RxWebSocketUtil", "onFailure" + t.toString() + webSocket.request().url().uri().getPath());
                    }
                    if (!subscriber.isDisposed()) {
                        subInfo(webSocket, t.getMessage(), false);
                    }
                }

                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    webSocket.close(1000, reason);
                }

                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    if (showLog) {
                        Log.d("RxWebSocketUtil", url + " --> onClosed:code= " + code + "reason" + reason);
                    }
                    if (!subscriber.isDisposed()) {
                        subInfo(webSocket, "onClosed", false);
                    }
                }
            });
        }


    }
}
