package com.example.pamir.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "GeosocioServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geosocio sms Serive Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager =  getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}

