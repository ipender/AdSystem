package com.bupt.adsystem.RemoteServer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * Created by hadoop on 16-10-21.
 */
public class VideoUpdateReciever implements  MessageTargetReceiver{

    Context mContext;

    private Handler mVideoStrategyUpdateHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }
    };

    @Override
    public String receiveMessage(MessageContext messageContext) {
        mContext = messageContext.getContext();


        return null;
    }


}
