package com.example.zxa01.notificationjobscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import com.example.zxa01.notificationjobscheduler.service.NotificationJobService;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    // Job Schduler
    public static final int MSG_UNCOLOR_START = 0;
    public static final int MSG_UNCOLOR_STOP = 1;
    public static final int MSG_COLOR_START = 2;
    public static final int MSG_COLOR_STOP = 3;
    public static final String MESSENGER_INTENT_KEY = BuildConfig.APPLICATION_ID + ".MESSENGER_INTENT_KEY";
    public static final String WORK_DURATION_KEY = BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";

    private JobScheduler jobScheduler;
    private int jobId = 0;

    private RadioButton networkDefault;
    private RadioButton networkWifi;
    private RadioButton networkAny;
    private Switch isCharged;
    private SeekBar volumeSeekBar;
    private Button cancelButton;
    private Button correctButton;
    private ComponentName serviceComponent;
    private SettingMessageHandler handler;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

        // handel setting value
        handler = new SettingMessageHandler(this);

        // Job Scheduler
        serviceComponent = new ComponentName(this,NotificationJobService.class.getName());
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // active Job Service Intent
        Intent startServiceIntent = new Intent(this, NotificationJobService.class);
        Messenger settingValue = new Messenger(handler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, settingValue);
        startService(startServiceIntent);
    }

    @Override
    protected void onStop() {
        stopService(new Intent(this, NotificationJobService.class));
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scheduleJob(View v) {

        JobInfo.Builder builder = new JobInfo.Builder(jobId++, serviceComponent)
                .setRequiresCharging(isCharged.isChecked()) // 設定充電模式
                .setMinimumLatency(5000) // 任務延遲時間
                .setOverrideDeadline(60000) // 任務最晚執行期限
                .setRequiresCharging(true) // 充電狀態
                .setRequiresDeviceIdle(false); // 設備閒置狀態

        // 設定連線模式
        if (networkDefault.isChecked()) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        } else if (networkAny.isChecked()) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        } else if (networkWifi.isChecked()) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        }

        // 設定Extra 工作時間
        PersistableBundle extras = new PersistableBundle();
        extras.putLong(WORK_DURATION_KEY, Long.valueOf(1000));
        builder.setExtras(extras);

        // Schedule job
        jobScheduler.schedule(builder.build());
        jobScheduler.getAllPendingJobs();
        Toast.makeText(MainActivity.this, R.string.start_all_job, Toast.LENGTH_SHORT).show();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void cancelAllJobs(View v) {
        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
        Toast.makeText(MainActivity.this, R.string.cancel_all_job, Toast.LENGTH_SHORT).show();
    }

    private void initUI(){
        // UI
        networkDefault = findViewById(R.id.networkDefaultButton);
        networkWifi = findViewById(R.id.networkWifiButton);
        networkAny = findViewById(R.id.networkAnyButton);
        isCharged = findViewById(R.id.chargeSwitch);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        cancelButton = findViewById(R.id.cancelButton);
        correctButton = findViewById(R.id.correctButton);
    }

    // 處理參數
    private static class SettingMessageHandler extends Handler {

        // Prevent possible leaks with a weak reference.
        private WeakReference<MainActivity> activity;

        SettingMessageHandler(MainActivity activity) {
            super();
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = activity.get();
            if (mainActivity == null) {
                return;
            }
            Message m;
            switch (msg.what) {
                case MSG_COLOR_START:
                    m = Message.obtain(this, MSG_UNCOLOR_START);
                    sendMessageDelayed(m, 1000L);
                    break;
                case MSG_COLOR_STOP:
                    m = obtainMessage(MSG_UNCOLOR_STOP);
                    sendMessageDelayed(m, 2000L);
                    break;
                case MSG_UNCOLOR_START:
                    break;
                case MSG_UNCOLOR_STOP:
                    break;
            }
        }
    }

}

