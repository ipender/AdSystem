package com.bupt.adsystem.RemoteServer;

import android.os.Handler;
import android.os.Message;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by hadoop on 16-10-21.
 */
public class MiscUtil {

    public static final int QUEST_FILE_SUCCESS = 1;
    public static final int MALFORMED_URL = 2;
    public static final int IO_EXCEPTION = 3;

    public static void requestTextFile(final String serverUrl, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("POST");

                    if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        String text = read(inputStream).toString();
                        Message message = new Message();
                        message.what = QUEST_FILE_SUCCESS;
                        message.obj = text;
                        handler.sendMessage(message);
                    }
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

}
