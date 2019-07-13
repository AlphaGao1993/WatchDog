package com.alphagao.watchdog;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSION = 101;
    private TextView inWorkView;
    private TextView outWorkView;
    private TextView mCurrentFocus;

    private int inTimeValue = 0;
    private int outTimeValue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, WatchService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        inWorkView = findViewById(R.id.in_work_time);
        outWorkView = findViewById(R.id.out_work_time);
        TimePicker timePicker = findViewById(R.id.time_picker);
        Button mConfirm = findViewById(R.id.clear_confirm);
        mCurrentFocus = inWorkView;

        int[] timeValue = AlarmUtil.getTimeValue(this);
        inTimeValue = timeValue[0];
        outTimeValue = timeValue[1];
        inWorkView.setText(getString(R.string.in_work_time, inTimeValue / 100, inTimeValue % 100));
        outWorkView.setText(getString(R.string.out_work_time, outTimeValue / 100, outTimeValue % 100));

        //AlarmHelper.initAlarm(this, AlarmUtil.getNextAlarmDate(this));

        inWorkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentFocus.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                mCurrentFocus = inWorkView;
                mCurrentFocus.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
            }
        });

        outWorkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentFocus.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                mCurrentFocus = outWorkView;
                mCurrentFocus.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
            }
        });

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                if (mCurrentFocus == inWorkView) {
                    inTimeValue = hourOfDay * 100 + minute;
                    mCurrentFocus.setText(getString(R.string.in_work_time, hourOfDay, minute));
                } else {
                    outTimeValue = hourOfDay * 100 + minute;
                    mCurrentFocus.setText(getString(R.string.out_work_time, hourOfDay, minute));
                }
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmUtil.saveTimeValue(MainActivity.this, inTimeValue, outTimeValue);
                AlarmHelper.initAlarm(MainActivity.this, AlarmUtil.getNextAlarmDate(MainActivity.this));
            }
        });

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this,
                    "读取屏幕解析文件需要授予读取内存权限", PERMISSION,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }
}
