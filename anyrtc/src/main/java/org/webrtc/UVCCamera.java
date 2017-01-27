package org.webrtc;

import java.util.List;

/**
 * Created by hadoop on 17-1-12.
 */
public interface UVCCamera {

    interface UVCFrameCallback{
        void onUVCFrame(byte[] frameData);
    }

    void setUVCCameraFrameCallback(UVCFrameCallback frameCallback);

    List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats();
}
