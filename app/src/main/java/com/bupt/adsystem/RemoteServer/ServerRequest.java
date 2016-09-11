package com.bupt.adsystem.RemoteServer;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hadoop on 16-8-8.
 */
public class ServerRequest {
    private static final String TAG = "ServerRequest";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    private static final String HOST_NAME = "http://10.210.12.237:8080";

    private static final int MSG_REQUEST_OK = 0x01;
    private Context mContext = null;
    private JSONObject mJSONObject;
    private static int mReceivedDataSize = 0;
    private String mUsingServerUrl = HOST_NAME + "/adsystem/heart";
    private HttpURLConnection mURLConnection;

    public ServerRequest(Context context) {
        this.mContext = context;
        // Register HashMap for Route Control
        MessageDispatcher.registerAllMessageReceiver();


//        try {
//            URL url = null;
//            url = new URL(mUsingServerUrl);
//            mURLConnection = (HttpURLConnection) url.openConnection();
//            mURLConnection.setConnectTimeout(3000);
//            mURLConnection.setRequestMethod("POST");
////            mURLConnection.setRequestProperty("Connection", "Keep-Alive");
////            mURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
////            mURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; X11)");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                longLiveHeart();
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 5000);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    String urlPath = "http://10.210.12.237:8080/download/videolist.json";
//                    URL url = new URL(urlPath);
//                    HttpURLConnection mURLConnection = (HttpURLConnection) url.openConnection();
//                    mURLConnection.setConnectTimeout(3000);
//                    mURLConnection.setRequestMethod("GET");
//                    mURLConnection.setRequestProperty("Connection", "Keep-Alive");
//                    if (DEBUG) Log.d(TAG, "Send Request!");
//                    if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                        InputStream inputStream = mURLConnection.getInputStream();
//                        String json = read(inputStream).toString();
//                        if (DEBUG) Log.d(TAG, "get Response:\n" +
//                                "ReceivedSize:" + mReceivedDataSize + "\n" +
//                                "Byte Size:" + json.length() + "\n" +
//                                json);
//                        mURLConnection.disconnect();
//                        try {
//                            mJSONObject = new JSONObject(json);
//                            mWebRequestHandler.sendMessage(mWebRequestHandler.obtainMessage(MSG_REQUEST_OK, mJSONObject));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            if (DEBUG) Log.d(TAG, "JSON File Format Error!");
//                        }
//                        mURLConnection.disconnect();
//                    }
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    Handler mWebRequestHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            if (msg.what == MSG_REQUEST_OK) {
                MessageContext messageContext = new MessageContext(mContext, mJSONObject);
                String result = MessageDispatcher.dispatchMessage(messageContext);
            }
        }
    };

    /**
     * 读取流中的数据
     */
    public static StringBuilder read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        mReceivedDataSize = 0;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
            mReceivedDataSize += line.getBytes().length;
        }
        return stringBuilder;
    }

    public void httpDisconnect() {
        if (mURLConnection != null) {
            mURLConnection.disconnect();
        }
    }

    public void longLiveHeart() {
        try {
            URL url = null;
            url = new URL(mUsingServerUrl);
            mURLConnection = (HttpURLConnection) url.openConnection();
            mURLConnection.setConnectTimeout(3000);
            mURLConnection.setRequestMethod("POST");

            if (DEBUG) Log.d(TAG, "Send Request!");
            if (mURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = mURLConnection.getInputStream();
                String json = read(inputStream).toString();
                if (DEBUG) Log.d(TAG, "get Response:\n" +
                        "ReceivedSize:" + mReceivedDataSize + "\n" +
                        "Byte Size:" + json.length() + "\n" +
                        json);
                mURLConnection.disconnect();
                try {
                    mJSONObject = new JSONObject(json);
                    mWebRequestHandler.sendMessage(mWebRequestHandler.obtainMessage(MSG_REQUEST_OK, mJSONObject));
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (DEBUG) Log.d(TAG, "JSON File Format Error!");
                }
            } else {
                mURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
