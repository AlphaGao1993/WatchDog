package com.alphagao.watchdog;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by AlphaGao on 2019-07-01 21:03
 */

class AlarmUtil {
    public static void saveTimeValue(Context context, int inWorkValue, int outWorkValue) {
        SharedPreferences preferences = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("inTime", inWorkValue);
        editor.putInt("outTime", outWorkValue);
        editor.apply();
    }

    public static int[] getTimeValue(Context context) {
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
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int now = hour * 100 + minute;
        Date date = new Date();
        long nextTime = date.getTime();
        if (now < in) {
            nextTime += (in / 100 - hour) * 3600 * 1000 + (in % 100 - minute) * 60 * 1000;
        } else if (now < out) {
            nextTime += (out / 100 - hour) * 3600 * 1000 + (out % 100 - minute) * 60 * 1000;
        } else if (now > out) {
            nextTime += (in / 100 - hour + (23 - hour)) * 3600 * 1000 + (in % 100 - minute) * 60 * 1000;
        } else {
            nextTime += 1000 * 3600 * 24;
        }

        date.setTime(nextTime);
        return date;
    }
}
