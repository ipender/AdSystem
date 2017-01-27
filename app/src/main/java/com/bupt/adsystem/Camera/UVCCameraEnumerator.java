package com.bupt.adsystem.Camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbManager;

import com.bupt.adsystem.view.LifeCycle;
import com.serenegiant.usb.USBMonitor;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.UVCCamera;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by hadoop on 17-1-27.
 */
public class UVCCameraEnumerator implements UVCCamera, LifeCycle{

    private static UVCCameraEnumerator sUVCCamera;
    private static Context sAppContext;
    private static SurfaceTexture sPreviewTexture;

    // for thread pool
    private static final int CORE_POOL_SIZE = 1;        // initial/minimum threads
    private static final int MAX_POOL_SIZE = 4;            // maximum threads
    private static final int KEEP_ALIVE_TIME = 10;        // time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private com.serenegiant.usb.UVCCamera mUVCCamera;

    public static UVCCameraEnumerator instance(Context appContext, SurfaceTexture previewTexture) {
        if (sUVCCamera == null) {
            sUVCCamera = new UVCCameraEnumerator(appContext, previewTexture);
        }
        return sUVCCamera;
    }

    public UVCCameraEnumerator(Context appContext, SurfaceTexture previewTexture) {
        sAppContext = appContext;
        sPreviewTexture = previewTexture;

    }

    @Override
    public void setUVCCameraFrameCallback(UVCFrameCallback frameCallback) {

    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        return null;
    }

    @Override
    public void toStop() {
        
    }

    @Override
    public void toResume() {

    }
}
