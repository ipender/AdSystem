package com.bupt.adsystem.RemoteServer;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.bupt.adsystem.Utils.AdSystemConfig;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by hadoop on 16-10-21.
 */
public class MiscUtil {

    private static final String TAG = "MiscUtil";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    public static final int QUEST_FILE_SUCCESS = 1;
    public static final int MALFORMED_URL = 2;
    public static final int IO_EXCEPTION = 3;

    public static void postRequestTextFile(final String serverUrl, final String content, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setDoInput(true);     //设置这个连接是否可以写入数据
                    httpURLConnection.setDoOutput(true);    //设置这个连接是否可以输出数据
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");//设置消息的类型
                    httpURLConnection.connect();
                    OutputStream out = httpURLConnection.getOutputStream();
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
                    bw.write(content);
                    bw.flush();
                    out.close();
                    bw.close();

                    int httpCode;
                    if ((httpCode = httpURLConnection.getResponseCode()) == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        String text = read(inputStream).toString();
                        if (DEBUG) Log.d(TAG, text);
                        Message message = new Message();
                        message.what = QUEST_FILE_SUCCESS;
                        message.obj = text;
                        handler.sendMessage(message);
                    } else {
                        if (DEBUG) Log.d(TAG, "Url Connection Failed!\n" +
                                        "\tResponse Code is " + httpCode);
                    }

                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    // url converting failed
                    handler.sendEmptyMessage(MALFORMED_URL);
                    e.printStackTrace();
                } catch (IOException e) {
                    // url openConnection failed
                    handler.sendEmptyMessage(IO_EXCEPTION);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void getRequestTextFile(final String serverUrl, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        String text = read(inputStream).toString();
                        if (DEBUG) Log.d(TAG, text);
                        Message message = new Message();
                        message.what = QUEST_FILE_SUCCESS;
                        message.obj = text;
                        handler.sendMessage(message);
                    } else {
                        if (DEBUG) Log.d(TAG, "Url Connection Failed!");
                    }

                    httpURLConnection.disconnect();
                } catch (MalformedURLException e) {
                    // url converting failed
                    handler.sendEmptyMessage(MALFORMED_URL);
                    e.printStackTrace();
                } catch (IOException e) {
                    // url openConnection failed
                    handler.sendEmptyMessage(IO_EXCEPTION);
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 读取流中的数据
     */
    public static StringBuilder read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder;
    }

    public static String generateHttpGetUrl(int isRepair, int moverDir, int battery, int doorOpen,
                                            int hasPerson, int CFloor, int CSignal) {
        String baseUrl = "http://117.158.178.198:8010/LNG-LOCAL-WEB/inshn/SignalAdd.do?";
        StringBuilder sb = new StringBuilder();
        sb.append("Device_Id=10000000000000000001");
        sb.append("&isRepair=" + isRepair + "&");
        sb.append("&moveDirection" + moverDir);
        sb.append("&battery="+battery);
        sb.append("&isDoorOpen="+doorOpen);
        sb.append("&hasPerson="+hasPerson);
        sb.append("&CFloor="+CFloor);
        sb.append("&CSignal="+CSignal);
        return baseUrl + sb.toString();
    }

}
