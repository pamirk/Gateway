package com.example.pamir.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    private static final String TAG = "mainActivity";
    private static final String VERIFICATION_MESSAGE = "Your Geosocio One Time Pass Code is: ";
    private static final String EVENT_NEW_VALIDATION = "new-validation";
    private static final String EVENT_STATUS_CHANGE = "status-change";

    private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver, networkStatusReceiver;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private String _id;
    private String number;
    private String code;
    private String country;

    int smsCounter = 0;
    private Socket mSocket;
    ArrayList<String> subInfoStrings;
    SectionsPagerAdapter adapter;
    //static init o socket
    {
        try {
            mSocket = IO.socket("https://gs-validations.herokuapp.com/");
        } catch (URISyntaxException e) {
            Log.i(TAG, e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sentPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(DELIVERED), 0);

        getUserSimInfo();
        setupViewPager();
    }


    private void getUserSimInfo() {
        SubscriptionManager mSubscriptionManager;
        List<Integer> sims = new ArrayList<>();
        List<SubscriptionInfo> subInfoList;

        subInfoStrings = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            mSubscriptionManager = SubscriptionManager.from(this);
            subInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
            for (int i = 0; i < subInfoList.size(); i++) {
                SubscriptionInfo lsuSubscriptionInfo = subInfoList.get(i);
                sims.add(lsuSubscriptionInfo.getSubscriptionId());
                subInfoStrings.add(subInfoList.get(i).getCarrierName().toString());
            }

        }
    }

    private void setupViewPager() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(SelectSimFragment.newInstance(subInfoStrings));
        adapter.addFragment(StatsFragment.newInstance("",""));

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));


        tabLayout.getTabAt(0).setText("Select Sim");
        tabLayout.getTabAt(1).setText("Stats");
    }

    @Override
    public void newValidationSocket(int simIndex) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            SmsManager sm = SmsManager.getSmsManagerForSubscriptionId(SubscriptionManager.from(this).getActiveSubscriptionInfoList().get(simIndex).getSubscriptionId());

            mSocket.connect();
            mSocket.on(EVENT_NEW_VALIDATION, args -> {
                runOnUiThread(() -> {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(TAG, data.toString());

                    try {
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
            });
        }

    }

    @Override
    public void stopSocketConnection() {
        mSocket.disconnect();
        mSocket.off(EVENT_NEW_VALIDATION);
        Toast.makeText(getApplicationContext(),"Socket Disconnected Successfully" , Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean isSocketConnected() {
        return mSocket.connected();
    }

    @Override
    public void connectSocket() {
        mSocket.connect();
    }
    public void sendMessage(SmsManager sms, String phone, String message) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            sms.sendTextMessage(phone, null, message, sentPI, deliveredPI);
        }
    }
    private void statusChangeSocket(String status) {
        JSONObject json = new JSONObject();
        try {
            json.put("_id", _id);
            json.put("status", status);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit(EVENT_STATUS_CHANGE, json);

    }
    private void broadcastReciverHelper() {

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show();
                        smsCounter++;
                        SelectSimFragment selectSimFragment = (SelectSimFragment) adapter.getItem(0);
                        selectSimFragment.setSMSCounterTv(smsCounter);
                        statusChangeSocket("sent");
                        break;

                    //Something went wrong and there's no way to tell what, why or how.
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure!", Toast.LENGTH_SHORT).show();
                        statusChangeSocket("failed");
                        break;

                    //Your device simply has no cell reception. You're probably in the middle of
                    //nowhere, somewhere inside, underground, or up in space.
                    //Certainly away from any cell phone tower.
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service!", Toast.LENGTH_SHORT).show();
                        break;

                    //Something went wrong in the SMS stack, while doing something with a protocol
                    //description unit (PDU) (most likely putting it together for transmission).
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU!", Toast.LENGTH_SHORT).show();
                        break;

                    //You switched your device into airplane mode, which tells your device exactly
                    //"turn all radios off" (cell, wifi, Bluetooth, NFC, ...).
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off!", Toast.LENGTH_SHORT).show();
                        break;

                }

            }


        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        statusChangeSocket("delivered");
                        break;

                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered!", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };

        networkStatusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int status = NetworkUtil.getConnectivityStatusString(context);
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                    SelectSimFragment selectSimFragment = (SelectSimFragment) adapter.getItem(0);

                    if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                        selectSimFragment.setNetworkStatusTextView("Not connected");
                    } else {
                        selectSimFragment.setNetworkStatusTextView("connected");
                    }
                }

            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
        registerReceiver(networkStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReciverHelper();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(networkStatusReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(EVENT_NEW_VALIDATION);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
