package com.alphagao.watchdog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.nio.file.Watchable;
import java.util.Date;

/**
 * Created by AlphaGao on 2019-07-01 21:05
 */

class AlarmHelper {


    static void initAlarm(Context context, Date nextAlarm) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long trggerTime = nextAlarm.getTime();
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        assert manager != null;
        manager.setExact(AlarmManager.RTC_WAKEUP, trggerTime, pi);
    }
}
