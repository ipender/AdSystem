package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bupt.adsystem.Utils.FileDirMgr;
import com.bupt.adsystem.Utils.UpdateMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by hadoop on 16-10-24.
 * this class should used in Singleton Mode
 */
public class MediaStrategyMgr {

    static private MediaStrategyMgr sStrategyMgr = null;

    private static final String KEY_XML_TEXT = "media_strategy";
    private Context mContext;
    AdMediaInfo adMediaInfo;

    public void changeVideoContainer(HashMap<String, AdMediaInfo.VideoAdInfo> videoContainer) {
        adMediaInfo.setVideoContainer(videoContainer);
    }

    public void changeImageContainer(HashMap<String, AdMediaInfo.ImageAdInfo> imageContainer) {
        adMediaInfo.setImageContainer(imageContainer);
    }

    public String getMediaPath() {
        return mMediaPath;
    }

    public void setMediaPath(String mediaPath) {
        mMediaPath = mediaPath;
    }

    private String mMediaPath;

    private UpdateMedia mVideoUpdateMedia;

    public UpdateMedia getVideoUpdateMedia() {
        return mVideoUpdateMedia;
    }

    public void setVideoUpdateMedia(UpdateMedia videoUpdateMedia) {
        mVideoUpdateMedia = videoUpdateMedia;
    }

    public UpdateMedia getImageUpdateMedia() {
        return mImageUpdateMedia;
    }

    public void setImageUpdateMedia(UpdateMedia imageUpdateMedia) {
        mImageUpdateMedia = imageUpdateMedia;
    }

    private UpdateMedia mImageUpdateMedia;

    public static MediaStrategyMgr instance(Context context) {
        if (sStrategyMgr == null) {
            sStrategyMgr = new MediaStrategyMgr(context);
        }
        return sStrategyMgr;
    }

    public MediaStrategyMgr(Context context) {
        mContext = context;
        String xmlText = getXmlMediaStrategy();
        mMediaPath = FileDirMgr.instance().getVideoStoragePath();
        if (xmlText != null) {
            adMediaInfo = AdMediaInfo.parseXmlFromText(xmlText);
        } else {
            adMediaInfo = new AdMediaInfo();
        }
    }

    public List<String> getVideoList() {
        if (adMediaInfo == null) return null;
        List<String> videoList = new ArrayList<>();
        Set<String> keySets = adMediaInfo.videoContainer.keySet();
        for (String key : keySets) {
            String videoPath = mMediaPath + adMediaInfo.videoContainer.get(key).filename;
            videoList.add(videoPath);
        }
        return videoList;
    }

    public List<String> getImageList() {
        if (adMediaInfo == null) return null;
        List<String> imageList = new ArrayList<>();
        Set<String> keySets = adMediaInfo.imageContainer.keySet();
        for (String key : keySets) {
            String imagePath = mMediaPath + adMediaInfo.imageContainer.get(key).filename;
            imageList.add(imagePath);
        }
        return imageList;
    }

    public void savaXmlMediaStrategy(String xmlText){
        SharedPreferences xmlStorage = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = xmlStorage.edit();
        editor.putString(KEY_XML_TEXT, xmlText);
        editor.commit();
    }

    public String getXmlMediaStrategy() {
        SharedPreferences xmlStorage = PreferenceManager.getDefaultSharedPreferences(mContext);
        return xmlStorage.getString(KEY_XML_TEXT, null);
    }
}
