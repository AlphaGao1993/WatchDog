package com.alphagao.watchdog;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by AlphaGao on 2019-07-01 21:03
 */

class AlarmUtil {
    static void saveTimeValue(Context context, int inWorkValue, int outWorkValue) {
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("inTime", inWorkValue);
        editor.putInt("outTime", outWorkValue);
        editor.apply();
    }

    static int[] getTimeValue(Context context) {
        int[] value = new int[2];
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        value[0] = preferences.getInt("inTime", 0);
        value[1] = preferences.getInt("outTime", 0);
        return value;
    }

    static Date getNextAlarmDate(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        int in = preferences.getInt("inTime", 0);
        int out = preferences.getInt("outTime", 0);
        Calendar instance = Calendar.getInstance();
        int day = instance.get(Calendar.DAY_OF_YEAR);
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        int minute = instance.get(Calendar.MINUTE);
        int now = hour * 100 + minute + 1;
        instance.set(Calendar.SECOND, 0);
        if (now < in) {//上班之前
            instance.set(Calendar.HOUR_OF_DAY, in / 100);
            instance.set(Calendar.MINUTE, in % 100);
        } else if (now < out) {//下班之前
            instance.set(Calendar.HOUR_OF_DAY, out / 100);
            instance.set(Calendar.MINUTE, out % 100);
        } else if (now > out) {//下班之后
            instance.set(Calendar.DAY_OF_YEAR, day + 1);
            instance.set(Calendar.HOUR_OF_DAY, in / 100);
            instance.set(Calendar.MINUTE, in % 100);
        } else { //与上下班时间重合，2 分钟后立即打卡
            instance.set(Calendar.MINUTE, minute + 2);
        }
        return instance.getTime();
    }
}
