package com.example.weight;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

// service class to run notification
public class SMSService extends Service {
    private SMS sms;

    @Override
    public void onCreate() {
        super.onCreate();
        sms = new SMS(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sms.startSMS();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        sms.stopSMS();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
