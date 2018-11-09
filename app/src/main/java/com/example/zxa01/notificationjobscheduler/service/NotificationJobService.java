package com.example.zxa01.notificationjobscheduler.service;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import static com.example.zxa01.notificationjobscheduler.BuildConfig.*;
import static com.example.zxa01.notificationjobscheduler.MainActivity.MESSENGER_INTENT_KEY;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService  extends JobService {

    public static final int MSG_COLOR_START = 2;
    public static final int MSG_COLOR_STOP = 3;
    public static final String WORK_DURATION_KEY = APPLICATION_ID + ".WORK_DURATION_KEY";
    private static final String TAG = NotificationJobService.class.getSimpleName();
    private Messenger mActivityMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service Start Command");
        mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(TAG, "Service Start Job");
        sendMessage(MSG_COLOR_START, params.getJobId());
        long duration = params.getExtras().getLong(WORK_DURATION_KEY);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jobFinished(params, false);
            }
        }, duration);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Service Stop Job");
        sendMessage(MSG_COLOR_STOP, params.getJobId());
        return false;
    }

    // 送出訊息
    private void sendMessage(int messageID, @Nullable Object params) {
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.obj = params;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }
}
