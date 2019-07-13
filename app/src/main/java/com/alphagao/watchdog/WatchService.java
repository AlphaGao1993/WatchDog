package com.alphagao.watchdog;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.xmlpull.v1.XmlPullParserException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by AlphaGao on 2019-05-03 18:11
 */

public class WatchService extends IntentService {
    public static final String CHANNEL_DEFAULT = "default";
    public static final int NOTIFICATION_ID = 1;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WatchService(String name) {
        super(name);
    }

    public WatchService() {
        super("WatchService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("watch", "监听已开启");
        setServiceForeground();
    }

    private void setServiceForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && manager != null
                && manager.getNotificationChannel(CHANNEL_DEFAULT) == null) {
            manager.createNotificationChannel(
                    new NotificationChannel(
                            CHANNEL_DEFAULT,
                            CHANNEL_DEFAULT,
                            NotificationManager.IMPORTANCE_HIGH
                    ));
        }
        Intent i = MainActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.push_small)
                .setChannelId(CHANNEL_DEFAULT)
                .setContentIntent(pi)
                .setContentTitle("看门狗")
                .setContentText("运行中")
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d("watch", "监听被 start 了");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d("watch", "收到监听请求");
        if (intent != null) {
            String pkgName = intent.getStringExtra("pkgName");
            if (!TextUtils.isEmpty(pkgName)) {
                Intent outIntent = getPackageManager().getLaunchIntentForPackage(pkgName);
                startWatch(pkgName, outIntent);
            } else {
                nextWatch();
            }
        } else {
            nextWatch();
        }
    }

    private void startWatch(final String pkgName, final Intent outIntent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    disableAndEnableTarget(pkgName);
                    Thread.sleep(1000 * 5);
                    if (outIntent != null && outIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(outIntent);
                        openTargetAndDump();
                        String path = Environment.getExternalStorageDirectory().getPath();
                        String name = "/ui.xml";
                        if (new File(path + name).exists()) {
                            resolveUI(path, name);
                        } else {
                            System.out.println("文件不存在");
                        }
                    } else {
                        Toast.makeText(WatchService.this, "未安装该应用", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    System.out.println("解析屏幕资源文件失败");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void disableAndEnableTarget(String pkgName) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream stream = new DataOutputStream(process.getOutputStream());
        stream.writeBytes("pm disable " + pkgName + "\n");
        stream.flush();
        stream.writeBytes("pm enable " + pkgName + "\n");
        stream.flush();
        stream.writeBytes("sleep 5\n");
        stream.flush();
        stream.close();
        process.waitFor();
    }

    private void resolveUI(String path, String name) throws XmlPullParserException, IOException, InterruptedException {
        Point[] points = XmlUtil.readBoundFromXml(path + name, "考勤");
        if (points != null) {
            Log.d("point:", points[0].x + "," + points[0].y + "," + points[1].x + "," + points[1].y);
            int centerX = (points[0].x + points[1].x) / 2;
            int centerY = (points[0].y + points[1].y) / 2;
            String tapCmd = "input tap " + centerX + " " + centerY + "\n";
            enterWatchPageAndDump(tapCmd);

            Point[] bound = XmlUtil.readBoundFromXml(path + name, "打卡");
            if (bound != null) {
                int buttonX = (bound[0].x + bound[1].x) / 2;
                int buttonY = (bound[0].y + bound[1].y) / 2;
                execWatch(buttonX, buttonY);
            }
        }
    }

    private void execWatch(int buttonX, int buttonY) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream stream = new DataOutputStream(process.getOutputStream());
        stream.writeBytes("input tap " + buttonX + " " + buttonY + "\n");
        stream.flush();
        stream.writeBytes("exit\n");
        stream.flush();
        stream.close();
        process.waitFor();
        nextWatch();
    }

    private void nextWatch() {
        Date date = AlarmUtil.getNextAlarmDate(this);
        //设定下一次闹钟时间,暂时未固定循环
        AlarmHelper.initAlarm(this, date);
    }

    private void enterWatchPageAndDump(String tapCmd) throws IOException, InterruptedException {
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes(tapCmd);
        outputStream.flush();
        outputStream.writeBytes("sleep 5\n");
        outputStream.flush();
        outputStream.writeBytes("uiautomator dump /sdcard/ui.xml\n");
        outputStream.flush();
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
        su.waitFor();
    }

    private void openTargetAndDump() throws IOException, InterruptedException {
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream stream = new DataOutputStream(su.getOutputStream());
        stream.writeBytes("sleep 10\n");
        stream.flush();
        stream.writeBytes("input swipe 600 900 600 300 300\n");
        stream.flush();
        stream.writeBytes("uiautomator dump /sdcard/ui.xml\n");
        stream.flush();
        stream.writeBytes("exit\n");
        stream.flush();
        stream.close();
        su.waitFor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("watch", "监听已关闭");
    }
}
