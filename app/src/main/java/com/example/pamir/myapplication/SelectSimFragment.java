package com.example.pamir.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.github.clans.fab.FloatingActionMenu;
import com.heetch.countrypicker.Country;
import com.heetch.countrypicker.CountryPickerCallbacks;
import com.heetch.countrypicker.CountryPickerDialog;

import java.util.ArrayList;
import java.util.List;

public class SelectSimFragment extends Fragment {
    private static final String TAG = "SelectSimFragment";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        com.github.clans.fab.FloatingActionButton add_country_sim2 = rootView.findViewById(R.id.add_country_sim2);
        ListView listView = rootView.findViewById(R.id.country_list_sim1);

        List<String> countryIsoCodes = new ArrayList<>();
         ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, countryIsoCodes);
        listView.setAdapter(adapter);

        add_country_sim1.setOnClickListener(view -> {
            mFloatingActionMenu.close(true);
            CountryPickerDialog countryPicker =
                    new CountryPickerDialog(getContext(), (country, flagResId) -> {
                        countryIsoCodes.add(country.getIsoCode());
                        adapter.notifyDataSetChanged();

                    });
            countryPicker.show();
        });
        add_country_sim2.setOnClickListener(view -> mFloatingActionMenu.close(true));


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
                add_country_sim2.setVisibility(View.GONE);

            }
        }
        sim1Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mListener != null) {
                sim2Switch.setChecked(false);
                sim2Switch.setEnabled(false);
                mListener.newValidationSocket(0);
                textView_socket_status.setText("Socket: ON");
            } else {
                mListener.stopSocketConnection();
                sim2Switch.setEnabled(true);
                textView_socket_status.setText("Socket: OFF");

            }
        });
        sim2Switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && mListener != null) {
                sim1Switch.setChecked(false);
                sim1Switch.setEnabled(false);
                textView_socket_status.setText("Socket: ON");
                mListener.newValidationSocket(1);

            } else {
                sim1Switch.setEnabled(true);
                mListener.stopSocketConnection();
                textView_socket_status.setText("Socket: OFF");

            }
        });

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

