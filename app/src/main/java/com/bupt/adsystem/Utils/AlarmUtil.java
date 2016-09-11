package com.bupt.adsystem.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.format.Time;
import android.util.Log;

import com.bupt.adsystem.receiver.TimerAlarmReceiver;
import com.bupt.adsystem.view.Settings;

/**
 * Created by hadoop on 16-8-21.
 */
public class AlarmUtil {

    private static final String TAG = "AlarmUtil";
    private static final boolean DEBUG = AdSystemConfig.DEBUG;

    public static final String VideoIntervalStart = "com.bupt.adsystem.VIDEO_INTERVAL_START";
    public static final String VideoIntervalEnd = "com.bupt.adsystem.VIDEO_INTERVAL_END";
    public static final String ImageIntervalStart = "com.bupt.adsystem.IMAGE_INTERVAL_START";
    public static final String ImageIntervalEnd = "com.bupt.adsystem.IMAGE_INTERVAL_END";
    public static final String SystemVoiceOn = "com.bupt.adsystem.SYSTEM_VOICE_ON";
    public static final String SystemVoiceOff = "com.bupt.adsystem.SYSTEM_VOICE_OFF";
    public static final String SystemRestart = "com.bupt.adsystem.SYSTEM_RESTART";

    public static final int RESTART_RequestCode = 100;
    public static final int VOICE_ON_RequestCode = 101;
    public static final int VOICE_OFF_RequestCode = 102;
    public static int VIDEO_INTERVAL_RequestCode = 200;      // 200 ~ 299 to recycling use
    public static int IMAGE_INTERVAL_RequestCode = 300;      // 200 ~ 399 to recycling use


    private static AlarmManager sAlarmManager;
    private static int requestCode = 0;

    public static AlarmManager getAlarmManager(Context ctx) {
        if (sAlarmManager != null) {
            return sAlarmManager;
        }
        return (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * 指定时间后进行更新信息(有如闹钟的设置)
     * 注意: Receiver记得在manifest.xml中注册
     */
    public static void setVideoChangeTimeBroadcast(Context context, String time, boolean isStart) {

        long alarmUpTimeInMillis = ConvertAlarmUpTime(time);
        if (alarmUpTimeInMillis <= System.currentTimeMillis() + 20) return;

        AlarmManager am = getAlarmManager(context);
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        if (isStart) {
            intent.setAction(VideoIntervalStart);
        } else {
            intent.setAction(VideoIntervalEnd);
        }
        VIDEO_INTERVAL_RequestCode++;
        if (VIDEO_INTERVAL_RequestCode >= 300) VIDEO_INTERVAL_RequestCode = 200;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, VIDEO_INTERVAL_RequestCode,
                intent, 0);
        if (DEBUG) Log.d(TAG, "Video Time Diff: " + (alarmUpTimeInMillis - System.currentTimeMillis()));
        am.set(AlarmManager.RTC_WAKEUP, alarmUpTimeInMillis, pendingIntent);
    }

    public static void setImageChangeTimeBroadcast(Context context, String time, boolean isStart) {
        long alarmUpTimeInMillis = ConvertAlarmUpTime(time);
        if (alarmUpTimeInMillis <= System.currentTimeMillis()) return;

        AlarmManager am = getAlarmManager(context);
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        if (isStart) {
            intent.setAction(ImageIntervalStart);
        } else {
            intent.setAction(ImageIntervalEnd);
        }
        IMAGE_INTERVAL_RequestCode++;
        if (IMAGE_INTERVAL_RequestCode >= 300) IMAGE_INTERVAL_RequestCode = 200;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IMAGE_INTERVAL_RequestCode,
                intent, 0);

        if (DEBUG) Log.d(TAG, "Image Change Start: " + isStart + "at: " + (alarmUpTimeInMillis - System.currentTimeMillis()));
//        long timeDiff = 10000;
        am.set(AlarmManager.RTC_WAKEUP, alarmUpTimeInMillis, pendingIntent);
    }

    public static void setVoiceSwitchBroadcast(Context context, String time, boolean isOn) {

        long alarmUpTimeInMillis = ConvertAlarmUpTime(time);
        if (alarmUpTimeInMillis <= System.currentTimeMillis()) return;

        AlarmManager am = getAlarmManager(context);
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        int requestCode;
        if (isOn) {
            requestCode = VOICE_ON_RequestCode;
            intent.setAction(SystemVoiceOn);
        } else {
            requestCode = VOICE_OFF_RequestCode;
            intent.setAction(SystemVoiceOff);
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                intent, 0);
        if (DEBUG) Log.d(TAG, "Voice Switch: " + isOn + " in: " + (alarmUpTimeInMillis - System.currentTimeMillis()));
        am.set(AlarmManager.RTC_WAKEUP, alarmUpTimeInMillis, pendingIntent);
    }

    public static void setRestartTimeBroadcast(Context context, String time) {

        long alarmUpTimeInMillis = ConvertAlarmUpTime(time);
        if (alarmUpTimeInMillis <= System.currentTimeMillis()) {
            alarmUpTimeInMillis += 24 * 60 * 60 * 1000;
        }

        AlarmManager am = getAlarmManager(context);
        Intent intent = new Intent(context, TimerAlarmReceiver.class);
        intent.setAction(SystemRestart);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, RESTART_RequestCode,
                intent, 0);
        if (DEBUG) Log.d(TAG, "Restart Time in " + (alarmUpTimeInMillis - System.currentTimeMillis()) );
        am.set(AlarmManager.RTC_WAKEUP, alarmUpTimeInMillis, pendingIntent);
    }


    /**
     * 取消定时执行(有如闹钟的取消)
     *
     * @param ctx
     */
    public static void cancelUpdateBroadcast(Context ctx) {
        AlarmManager am = getAlarmManager(ctx);
        Intent i = new Intent(ctx, TimerAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, i, 0);
        am.cancel(pendingIntent);
    }

    public static long ConvertAlarmUpTime(String time) {
        if (time == null && time.length() == 0) return -1;
        String[] hms = time.trim().split(":");
        int timeInt[] = {Integer.parseInt(hms[0].trim()),
                Integer.parseInt(hms[1].trim()), Integer.parseInt(hms[2].trim())};
        Time timeDate = new Time();
        timeDate.setToNow();
        timeDate.hour = timeInt[0];
        timeDate.minute = timeInt[1];
        timeDate.second = timeInt[2];

        return timeDate.toMillis(true);

    }

    public static void initAlarmWhenStartUp(Context context) {
        setRestartTimeBroadcast(context, Settings.System.RestartTime);
        setVoiceSwitchBroadcast(context, Settings.Voice.OnTime, true);
        setVoiceSwitchBroadcast(context, Settings.Voice.OffTime, false);

        Cursor videoIntervalCursor = FileListMgr.instance(context).getAllVideoTimeInterval();
        Cursor imageIntervalCursor = FileListMgr.instance(context).getAllImageTimeInterval();

        if (videoIntervalCursor != null && videoIntervalCursor.getCount() > 0) {
            videoIntervalCursor.moveToFirst();
            do {
                setVideoChangeTimeBroadcast(context, videoIntervalCursor.getString(0), true);
                setVideoChangeTimeBroadcast(context, videoIntervalCursor.getString(1), false);
            } while (videoIntervalCursor.moveToNext());
        }

        if (imageIntervalCursor != null && imageIntervalCursor.getCount() > 0) {
            imageIntervalCursor.moveToFirst();
            do {
                setImageChangeTimeBroadcast(context, imageIntervalCursor.getString(0), true);
                setImageChangeTimeBroadcast(context, imageIntervalCursor.getString(1), false);
            } while (imageIntervalCursor.moveToNext());
        }

    }

}
