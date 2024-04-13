package com.example.meetanywhere.Services;

import android.app.PendingIntent;
import android.content.Context;
import android.os.ResultReceiver;
import android.util.Log;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.meetanywhere.Activities.conference_room;

public class ScreenCaptureNotification extends Service {
    private String screenName = "[SERVICE]ScreenCapture:";  //  Fragment 일 경우 FRAGMENT 로 할 수도 있음
    private String tag_check = screenName + "[CHECK]";  // 특정 값을 확인할 때 사용
    private String tag_execute = screenName + "[EXECUTE]";  // 매서드나 다른 실행 가능한 코드를 실행할 때 사용
    private String tag_event = screenName + "[EVENT]";  // 특정 이벤트 발생을 확인할 때 사용

    public static final String ACTION_SCREEN_CAPTURE_PERMITTED = "com.example.meetAnywhere.ACTION_SCREEN_CAPTURE_PERMITTED";
    public static final String EXTRA_DATA = "extra_data";

    private String CHANNEL_ID = "SCREEN_SHARE_SERVICE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(tag_execute, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag_execute, "onCreate");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(tag_execute, "onStartCommand");

        //Context context = (Context) intent.getSerializableExtra("context");
        Context context = getApplicationContext();
        Log.d(tag_check, "context from intent : " + context);

        Intent notificationIntent = new Intent(this, conference_room.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_IMMUTABLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Screen Capture Service")
                    .setContentText("Capturing screen...")
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(1, notification);
            Log.d(tag_execute, "start notification");
        }

        int resultCode = intent.getIntExtra("resultCode", -1);
        Intent resultData = intent.getParcelableExtra("resultData");
        Log.d(tag_check, "resultCode : " + resultCode);
        Log.d(tag_check, "resultData : " + resultData);

        Intent dataIntent = new Intent(ACTION_SCREEN_CAPTURE_PERMITTED);
        dataIntent.putExtra("resultCode", resultCode);
        dataIntent.putExtra("resultData", resultData);
        sendBroadcast(dataIntent);
        Log.d(tag_check, "sendBroadCast to the activity(conference_room)");

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        Log.d(tag_execute, "createNotificationChannel");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Capture Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(tag_execute, "onDestroy");
        super.onDestroy();
    }
}
