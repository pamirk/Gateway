package com.example.pamir.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import static com.example.pamir.myapplication.App.CHANNEL_ID;

public class MyService extends Service {
    private static final String TAG = "MyService";
    public static final String SMS_COUNTER = "smsCounter";
    private static final String VERIFICATION_MESSAGE = "Geosocio Passcode is:  ";
    private static final String EVENT_NEW_VALIDATION = "new-validation";
    private static final String EVENT_STATUS_CHANGE = "status-change";
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    public static int SERVICE_STARTED_BY_SIM = 0;
    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;

    private String _id;
    private String number;
    private String code;
    private String country;
    public static int smsCounter;
    SmsManager sm;
    public static Socket mSocket;
    {
        try {/*
            IO.Options options = new IO.Options();
            options.forceNew=true;
            options.reconnection = false;*/
            mSocket = IO.socket("https://gs-validations.herokuapp.com/");
        } catch (URISyntaxException e) {
        }
    }

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        restoreSharedPrefs();
        //setupNotification();


        sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(DELIVERED), 0);

        broadcastReciverHelper();
        Log.d(TAG, "onCreate: called.");
    }

    private void saveSharedPrefs() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(SMS_COUNTER, smsCounter);
        editor.apply();
    }

    private void restoreSharedPrefs() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        smsCounter = prefs.getInt(SMS_COUNTER, 0);
    }

    private void setupNotification() {

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "My Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        } else {

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.correct_check_mark)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.correct_check_mark))
                    .setContentTitle("Geosocio is running")
                    .setContentText("Service is running")
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setTicker("TICKER")
                    .setProgress(100, 0, false)
                    .setContentIntent(pendingIntent).build();

            stopForeground(true);
            startForeground(1, notification);
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: called.");


        int i = 1;
        if (intent != null) {
            i = intent.getIntExtra("index", -1);
            SERVICE_STARTED_BY_SIM = i;
        }
        newValidationSocket(i);

        setupNotification();
        return START_STICKY;
    }


    public void newValidationSocket(int simIndex) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                sm = SmsManager.getSmsManagerForSubscriptionId(SubscriptionManager.from(getApplicationContext()).getActiveSubscriptionInfoList().get(simIndex).getSubscriptionId());
            } else {
                sm = SmsManager.getDefault();
            }
            mSocket.on(EVENT_NEW_VALIDATION, onLogin);


            mSocket.connect();
        }
    }

    private Emitter.Listener onLogin = args -> new Handler(Looper.getMainLooper()).post(() -> {
                JSONObject data = (JSONObject) args[0];
                Log.i(TAG, data.toString());
                try {
                    //Toast.makeText(this, data.toString(), Toast.LENGTH_LONG).show();
                    _id = data.getString("_id");
                    number = data.getString("number");
                    code = data.getString("code");
                    country = data.getString("country");

                    String encrypted = code;
                    String password = "$-&Aahj!.n12=.@";
                    String decryptedCode = Crypto.decrypt(password, encrypted);

                    String message = VERIFICATION_MESSAGE + decryptedCode;
                    sendMessage(sm, number, message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    public void sendMessage(SmsManager sms, String phone, String message) {
        Log.d(TAG, "sendMessage: called.");

        sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);
    }

    private void broadcastReciverHelper() {

        smsSentReceiver = new SmsSentReceiver();
        smsDeliveredReceiver = new SmsDeliveredReceiver();
        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveSharedPrefs();
        mSocket.disconnect();
        mSocket.off("new message", onLogin);

        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
        Log.d(TAG, "onDestroy: called.");

    }

    private void emitStatus(String status) {
        JSONObject json = new JSONObject();
        try {
            json.put("_id", _id);
            json.put("status", status);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(EVENT_STATUS_CHANGE, json);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public class SmsSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SMS sent successfully: called.");
                    smsCounter++;
                    saveSharedPrefs();

                    if (MainActivity.adapter != null) {
                        SelectSimFragment selectSimFragment = (SelectSimFragment) MainActivity.adapter.getItem(0);
                        if (selectSimFragment != null)
                            selectSimFragment.setSMSCounterTv(smsCounter);
                    }
                    emitStatus("sent");
                    break;

                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "Generic failure!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "SMS Generic failure: called.");
                    emitStatus("failed");

                    break;

                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "No service!", Toast.LENGTH_SHORT).show();
                    break;

                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "Null PDU!", Toast.LENGTH_SHORT).show();
                    break;

                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "Radio off!", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    }

    public class SmsDeliveredReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show();
                    emitStatus("delivered");
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
