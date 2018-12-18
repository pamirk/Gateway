package com.example.pamir.myapplication;

interface OnFragmentInteractionListener {
    void newValidationSocket(int simIndex);
    void stopSocketConnection();
    boolean isSocketConnected();
    void connectSocket();

}
