package com.bupt.adsystem.view;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.VideoView;

import com.bupt.adsystem.Camera.CameraApp;
import com.bupt.adsystem.R;
import com.bupt.adsystem.RemoteServer.AdMediaInfo;
import com.bupt.adsystem.RemoteServer.MiscUtil;
import com.bupt.adsystem.RemoteServer.ServerRequest;
import com.bupt.adsystem.Utils.AdImageCtrl;
import com.bupt.adsystem.Utils.AdSystemConfig;
import com.bupt.adsystem.Utils.AdVideoCtrl;
import com.bupt.adsystem.Utils.AlarmUtil;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    // for thread pool
//    private static final int CORE_POOL_SIZE = 1;		// initial/minimum threads
//    private static final int MAX_POOL_SIZE = 4;			// maximum threads
//    private static final int KEEP_ALIVE_TIME = 10;		// time periods while keep the idle thread
//    protected static final ThreadPoolExecutor EXECUTER
//            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
//            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UsbManager mUsbManager;
    private UVCCamera mUVCCamera;

    private ImageSwitcher mImageSwitcher;
    private VideoView mVideoView;
    private TextureView mTextureView;

    private Button button;
    private MediaPlayer mediaPlayer;
    private TextView textView;
    private String resPath;
    private CameraApp mCameraApp;
    private TelephonyManager mTelephonyManager;
    private Context mContext;
    private ServerRequest mServerRequest;
    private AdImageCtrl mAdImageCtrl;
    private AdVideoCtrl mAdVideoCtrl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.setContentView(R.layout.activity_main);
        mContext = this;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mImageSwitcher = (ImageSwitcher) findViewById(R.id.image_switcher);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mVideoView = (VideoView) findViewById(R.id.surface_view);
        mTextureView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
        mVideoView.setZOrderOnTop(true);
        textView.setText("Just for seeing!");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Time time = new Time("GMT+8");
//                time.set(System.currentTimeMillis() + 10000);
//                String alarmTime = String.format("%02d:%02d:%02d", time.hour, time.minute, time.second);
//                Log.d(TAG, alarmTime);
//                AlarmUtil.setImageChangeTimeBroadcast(mContext, alarmTime, true);
//                AlarmUtil.setVideoChangeTimeBroadcast(mContext, alarmTime, true);

                String url = "http://117.158.178.198:8010/esmp-ly-o-websvr/ws/esmp?wsdl";
                JSONObject jsonObject = new JSONObject();
                Handler handler = new Handler();
                try {
                    jsonObject.put("deviceId", "10000000000000000001");
                    Log.d(TAG, "Request Json Content: \n" +
                            jsonObject.toString());

//                    MiscUtil.postRequestTextFile(url, jsonObject.toString(), handler);
                    MiscUtil.getRequestTextFile(url+"="+jsonObject.toString(), handler);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String urlGet = MiscUtil.generateHttpGetUrl(0, 1, 80, 0, 0, 1, -89);
                MiscUtil.getRequestTextFile(urlGet, handler);

                if (DEBUG) Log.d(TAG, "Button Pressed!");
            }
        });

        AdMediaInfo mediaInfo;
        String xmlText = "<root resolution=\"1080*1920\" ver=\"0000000466\" templet=\"e4fcd3c6a7f84d91b82244bacdc4bcd8.zip\">\n" +
                "<files>\n" +
                "<media>\n" +
                "<file id=\"7cc2f7f7-a190-40bb-8e1d-954b63eaffa4\" file=\"7f29e8764a504eeab2bea9d13e2c8a90.mp4\" md5=\"EBC7B612BB6C4B347262F1001D660DC9\" voice=\"50\" b_day=\"20160510\" e_day=\"20200510\" elipse=\"130\"/>\n" +
                "<file id=\"b27cd857-6221-4243-bda3-3902012d4a3c\" file=\"87717128dc904e0cbb130c4f38e73a5a.mp4\" md5=\"E78CA01AA6144550614869DF78B4E825\" voice=\"50\" b_day=\"20160331\" e_day=\"20170331\" elipse=\"15\"/>\n" +
                "<file id=\"9a77c414-ae92-4817-acde-66565b37502e\" file=\"7c299faba3df4bc9984a8d7f93e083dd.avi\" md5=\"8C8D216E6CA406CCA99EB43559B4E833\" voice=\"50\" b_day=\"20160331\" e_day=\"20170331\" elipse=\"45\"/>\n" +
                "</media>\n" +
                "<pic>\n" +
                "<file id=\"d4963d63-7d62-432a-a3a4-df4bbf544010\" file=\"p1agkdeb931hhq1voo1jkf1aqqvh33.jpg\" md5=\"EE318B5E8DD566DFF30C0B67B3FF29B4\" b_day=\"20160418\" e_day=\"20200418\" elipse=\"10\"/>\n" +
                "<file id=\"a8a2dd99-6bae-4448-bbe5-4aaab93f1e1c\" file=\"p1ahggqi141g5r6o7om4idei1d3.png\" md5=\"54F2C73ABF62C559DC6EFF25A42FE0B0\" b_day=\"20160429\" e_day=\"20200429\" elipse=\"15\"/>\n" +
                "<file id=\"e031810f-acd5-4a7d-95b0-01bedb46328d\" file=\"p1ahggr8oe4ir1sa71pho19occ3p3.png\" md5=\"FF6833DE6EDDA0938269DC58890CE473\" b_day=\"20160429\" e_day=\"20200429\" elipse=\"15\"/>\n" +
                "</pic>\n" +
                "</files>\n" +
                "<media_play id=\"1\">\n" +
                "<p ID=\"f7ff4045-118e-4a7f-bdfc-78c8eb3d5f64\" begin=\"08:00:00\" end=\"20:00:00\" prime_time=\"0\">\n" +
                "<media file=\"7cc2f7f7-a190-40bb-8e1d-954b63eaffa4\"/>\n" +
                "<media file=\"b27cd857-6221-4243-bda3-3902012d4a3c\"/>\n" +
                "<media file=\"9a77c414-ae92-4817-acde-66565b37502e\"/>\n" +
                "</p>\n" +
                "<p ID=\"f27cd208-8b49-499f-9a26-2a6f02012cc2\" begin=\"16:00:00\" end=\"18:00:00\" prime_time=\"1\">\n" +
                "<media file=\"b27cd857-6221-4243-bda3-3902012d4a3c\"/>\n" +
                "</p>\n" +
                "</media_play>\n" +
                "<pic_play id=\"2\">\n" +
                "<p ID=\"8391eb26-05c0-4863-b7cc-c7ff71d112a9\" begin=\"08:00:00\" end=\"20:00:00\" prime_time=\"0\">\n" +
                "<media file=\"d4963d63-7d62-432a-a3a4-df4bbf544010\"/>\n" +
                "<media file=\"a8a2dd99-6bae-4448-bbe5-4aaab93f1e1c\"/>\n" +
                "<media file=\"e031810f-acd5-4a7d-95b0-01bedb46328d\"/>\n" +
                "</p>\n" +
                "<p ID=\"a9032933-6642-40aa-b36b-6b6ae87f75c6\" begin=\"16:00:00\" end=\"18:00:00\" prime_time=\"1\">\n" +
                "<media file=\"d4963d63-7d62-432a-a3a4-df4bbf544010\"/>\n" +
                "</p>\n" +
                "</pic_play>\n" +
                "</root>";

        mediaInfo = AdMediaInfo.parseXmlFromText(xmlText);
        Log.d(TAG, mediaInfo.toString());
//        mAdVideoCtrl = AdVideoCtrl.instance(mContext, mVideoView);
//        mAdImageCtrl = AdImageCtrl.instance(mContext, mImageSwitcher);
//        mServerRequest = new ServerRequest(this);
//        mCameraApp = new CameraApp(this, mTextureView);
//        AdImageCtrl.instance(this, mImageSwitcher);
//        String url = "http://192.168.1.101:8080/download/purge_piece.mp4";
//        String url2 = "http://192.168.1.101:8080/download/coherence_piece.mp4";
//        String filename = URLUtil.guessFileName(url, null, null);
//        String filename2 = URLUtil.guessFileName(url2, null, null);
//        String filepath = FileDirMgr.instance().getCameraStoragePath();
//        DownloadManager.instance(this).startDownload(url, filepath, filename,
//                new OnDownload() {
//                    @Override
//                    public void onDownloading(String url, int finished) {
//                        if (DEBUG) Log.d(TAG, "downloaded1:" + finished);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(File downloadFile) {
//                        if (DEBUG) Log.d(TAG, downloadFile.getAbsolutePath());
//                    }
//                });
//        DownloadManager.instance(this).startDownload(url2, filepath, filename2,
//                new OnDownload() {
//                    @Override
//                    public void onDownloading(String url, int finished) {
//                        if (DEBUG) Log.d(TAG, "downloaded2:" + finished);
//                    }
//
//                    @Override
//                    public void onDownloadFinished(File downloadFile) {
//                        if (DEBUG) Log.d(TAG, downloadFile.getAbsolutePath());
//                    }
//                });
//        int callState = mTelephonyManager.getCallState();
//        CellLocation cellLocation = mTelephonyManager.getCellLocation();
//        cellLocation.requestLocationUpdate();
//        mAdVideoCtrl = AdVideoCtrl.instance();
//        mTextureView.setVisibility(View.INVISIBLE);
//        mVideoView.setVisibility(View.VISIBLE);
//        mAdVideoCtrl.setVideoView(mVideoView);
//        mAdVideoCtrl.startPlayView();

//        resPath = mAdVideoCtrl.getVideoByOrder();
//        mVideoView.setVideoPath(resPath);
//        mVideoView.setZOrderOnTop(true);
//        mVideoView.start();
//        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mp) {
//                mVideoView.start();
//            }
//        });

//        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                mVideoView.setVideoPath(mAdVideoCtrl.getVideoByOrder());
//                mVideoView.start();
//            }
//        });
//        SurfaceHolder surfaceHolder = adVideoView.getHolder();
//        surfaceHolder.setFixedSize(720, 480);
//        surfaceHolder.addCallback(this);
//        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
//        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(mUSBMonitor.ACTION_USB_PERMISSION), 0);
//        HashMap<String, UsbDevice> usbDevcieList = mUsbManager.getDeviceList();
//        if(usbDevcieList.size() == 1){
//            Toast.makeText(this, "find a USB device!", Toast.LENGTH_LONG).show();
//            Set<String> keySet = usbDevcieList.keySet();
//            for (String key : keySet)
//            mUsbManager.requestPermission(usbDevcieList.get(key), mPermissionIntent);
//        } else {
//            Toast.makeText(this, "USB device Num is:" + usbDevcieList.size(), Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mCameraApp.registerUsbMonitor();
//        mCameraApp.startPreview();
    }

    @Override
    protected void onPause() {
//        mCameraApp.unregisterUsbMonitor();
//        mCameraApp.startPreview();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        mCameraApp.destroy();
        mServerRequest.httpDisconnect();
        super.onDestroy();
    }

//    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
//        @Override
//        public void onAttach(UsbDevice device) {
//
//        }
//
//        @Override
//        public void onDettach(UsbDevice device) {
//
//        }
//
//        @Override
//        public void onConnect(UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
//            if (mUVCCamera != null)
//                mUVCCamera.destroy();
//            mUVCCamera = new UVCCamera();
//            EXECUTER.execute(new Runnable() {
//                @Override
//                public void run() {
//                    mUVCCamera.open(ctrlBlock);
//                    mUVCCamera.setStatusCallback(new IStatusCallback() {
//                        @Override
//                        public void onStatus(final int statusClass, final int event, final int selector,
//                                             final int statusAttribute, final ByteBuffer data) {
//
//                        }
//                    });
//                    if (mSurface != null) {
//                        mSurface.release();
//                        mSurface = null;
//                    }
//                    try {
//                        mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.FRAME_FORMAT_MJPEG);
//                    } catch (final IllegalArgumentException e) {
//                        // fallback to YUV mode
//                        try {
//                            mUVCCamera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
//                        } catch (final IllegalArgumentException e1) {
//                            mUVCCamera.destroy();
//                            mUVCCamera = null;
//                        }
//                    }
//                    if (mUVCCamera != null) {
//                        final SurfaceTexture st = mTextureView.getSurfaceTexture();
//                        if (st != null)
//                            mSurface = new Surface(st);
//                        mUVCCamera.setPreviewDisplay(mSurface);
////                        mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_RGB565/*UVCCamera.PIXEL_FORMAT_NV21*/);
//                        mUVCCamera.startPreview();
//                    }
//                }
//            });
//
//        }
//
//        @Override
//        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
//            if (mUVCCamera != null) {
//                mUVCCamera.close();
//                if (mSurface != null) {
//                    mSurface.release();
//                    mSurface = null;
//                }
//            }
//        }
//
//        @Override
//        public void onCancel() {
//
//        }
//    };
}
