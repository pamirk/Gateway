package com.example.pamir.myapplication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.github.clans.fab.FloatingActionMenu;
import com.heetch.countrypicker.CountryPickerCallbacks;
import com.heetch.countrypicker.CountryPickerDialog;
import com.heetch.countrypicker.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static com.example.pamir.myapplication.MyService.MY_PREFS_NAME;
import static com.example.pamir.myapplication.MyService.SMS_COUNTER;

public class SelectSimFragment extends Fragment {
    private static final String TAG = "SelectSimFragment";
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    private ArrayList<com.example.pamir.myapplication.Country> mCountries;
    Intent serviceIntent;

    private static final String ARG_SUBSCRIPTION_INFO = "sims_number";
    private OnFragmentInteractionListener mListener;
    View rootView;

    public SelectSimFragment() {
    }

    public static SelectSimFragment newInstance(ArrayList<String> subInfoStrings) {
        SelectSimFragment fragment = new SelectSimFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_SUBSCRIPTION_INFO, subInfoStrings);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        return rootView;
    }

    public void setSMSCounterTv(int count) {
        if (rootView != null) {
            TextView textView = rootView.findViewById(R.id.tv_counter);
            textView.setText("Counter: " + count);
        }
    }

    public void setNetworkStatusTextView(String connected) {
        if (rootView != null) {

            TextView textView_network_status = rootView.findViewById(R.id.tv_network_status);
            textView_network_status.setText(" Network: " + connected);

        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isNetworkAvailable()) {
            setNetworkStatusTextView("ON");
        } else {
            setNetworkStatusTextView("OFF");
        }

        TextView textView_sim1 = rootView.findViewById(R.id.sim_1_tv);
        TextView textView_sim2 = rootView.findViewById(R.id.sim_2_tv);
        TextView textView_socket_status = rootView.findViewById(R.id.tv_socket_status);
        com.suke.widget.SwitchButton sim1Switch = rootView.findViewById(R.id.sim_1_switch);
        com.suke.widget.SwitchButton sim2Switch = rootView.findViewById(R.id.sim_2_switch);
        FloatingActionMenu mFloatingActionMenu = rootView.findViewById(R.id.fabMenu);
        com.github.clans.fab.FloatingActionButton add_country_sim1 = rootView.findViewById(R.id.add_country_sim1);
        com.github.clans.fab.FloatingActionButton reset = rootView.findViewById(R.id.reset);

        reset.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            setSMSCounterTv(0);
            MyService.smsCounter = 0;
            editor.putInt(SMS_COUNTER, 0);
            editor.apply();
        });
        ListView listView = rootView.findViewById(R.id.country_list_sim1);

        mCountries = new ArrayList<>();
        int flagResID = Utils.getMipmapResId(getContext(), "PK".toLowerCase(Locale.ENGLISH) + "_flag");
        mCountries.add(new Country("PK", "+92", String.valueOf(flagResID), "Pakistan"));
        CountryAdapter countryAdapter = new CountryAdapter(getActivity(), mCountries);


        listView.setAdapter(countryAdapter);
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            /*countryAdapter.remove(countryAdapter.getItem(position));
            countryAdapter.notifyDataSetChanged();*/


            return true;
        });


        add_country_sim1.setOnClickListener(view -> {
            mFloatingActionMenu.close(true);
            Country newcountry = new Country();

            CountryPickerDialog countryPicker =
                    new CountryPickerDialog(getContext(), (country, flagResId) -> {
                        newcountry.setDialingCode(country.getDialingCode());
                        newcountry.setFlagResId(String.valueOf(flagResId));
                        newcountry.setIsoCode(country.getIsoCode());
                        Locale l = new Locale("", country.getIsoCode());
                        newcountry.setName(l.getDisplayCountry());

                        mCountries.add(newcountry);

                        countryAdapter.notifyDataSetChanged();

                    });
            countryPicker.show();
        });


        ArrayList<String> subInfoStrings = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            subInfoStrings = bundle.getStringArrayList(ARG_SUBSCRIPTION_INFO);
        }
        if (subInfoStrings != null) {
            if (subInfoStrings.size() > 1) {
                textView_sim1.setText(String.format("Sim 1: %s ", subInfoStrings.get(0)));
                textView_sim2.setText(String.format("Sim 2: %s", subInfoStrings.get(1)));
            } else if (subInfoStrings.size() == 1) {
                textView_sim1.setText(String.format("Sim 1: %s ", subInfoStrings.get(0)));
                rootView.findViewById(R.id.sim_2_layout).setVisibility(View.GONE);

            }
        }
        sim1Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mListener != null) {
                sim2Switch.setChecked(false);
                sim2Switch.setEnabled(false);

                startMyService(0);
                textView_socket_status.setText("Socket: ON");
            } else {

                stopMyService();
                sim2Switch.setEnabled(true);
                textView_socket_status.setText("Socket: OFF");

                waitPlease();

            }
        });
        sim2Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mListener != null) {
                sim1Switch.setChecked(false);
                sim1Switch.setEnabled(false);
                textView_socket_status.setText("Socket: ON");
                startMyService(1);

            } else {
                sim1Switch.setEnabled(true);
                stopMyService();
                textView_socket_status.setText("Socket: OFF");

                waitPlease();
            }
        });

    }


    private void waitPlease(){
        final LoadingDialog dialog = LoadingDialog.show(getActivity(), "Wait janii...");
        dialog.setCancelable(false);



        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runnable, 2 * 1000);


    }

    @Override
    public void onResume() {
        super.onResume();

        if (isMyServiceRunning()){


            int simNo = MyService.SERVICE_STARTED_BY_SIM;
            if (simNo == 0) {
                com.suke.widget.SwitchButton sim1Switch = rootView.findViewById(R.id.sim_1_switch);
                sim1Switch.setChecked(true);
            } else if (simNo == 1){
                com.suke.widget.SwitchButton sim2Switch = rootView.findViewById(R.id.sim_2_switch);
                sim2Switch.setChecked(true);
            }

            TextView textView_socket_status = rootView.findViewById(R.id.tv_socket_status);
            textView_socket_status.setText("Socket: ON");
        }


        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int smsCounter = prefs.getInt(SMS_COUNTER, 0);
        setSMSCounterTv(smsCounter);
    }

    @SuppressLint("BatteryLife")
    private void startMyService(int i) {
        if (!isMyServiceRunning() && getContext() != null){
            serviceIntent = new Intent(getContext(), MyService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                serviceIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            }
            serviceIntent.putExtra("index", i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                getContext().startForegroundService(serviceIntent);
            } else {
                getContext().startService(serviceIntent);
            }
        }
    }

    private void stopMyService() {
        if (isMyServiceRunning() && getContext() != null) {
            if (serviceIntent == null ) {
                Intent serviceIntentnew = new Intent(getContext(), MyService.class);
                getContext().stopService(serviceIntentnew);

            } else {
                getContext().stopService(serviceIntent);
            }

        }

    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getContext().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.example.pamir.myapplication.MyService".equals(service.service.getClassName())) {
                Log.d(TAG, "isMyServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isMyServiceRunning: location service is not running.");
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}

