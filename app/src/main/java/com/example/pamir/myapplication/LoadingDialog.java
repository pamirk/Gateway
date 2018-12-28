package com.example.pamir.myapplication;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;

public class LoadingDialog extends CustomDialogFragment {
    public static final String LOADING_DIALOG_TAG = "loading-dialog";
    @BindView(R.id.loading_text)
    TextView loadingText;
    private String message;
    @BindView(R.id.spinner)
    LoadingSpinner spinner;

    public static LoadingDialog newInstance() {
        return newInstance(null);
    }

    public static LoadingDialog newInstance(String message) {
        LoadingDialog dialog = new LoadingDialog();
        dialog.setUserDismissable(false);
        dialog.setMessage(message);
        return dialog;
    }

    public int getLayoutResource() {
        return R.layout.dialog_loading;
    }

    public int getMaxWidth() {
        return getResources().getDimensionPixelSize(R.dimen.dialog_loading_max_width);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (this.message != null) {
            this.loadingText.setText(this.message);
        }
        return view;
    }

    public static LoadingDialog show(Context context) {
        return show(context, null);
    }

    public static LoadingDialog show(Context context, String message) {
        LoadingDialog dialog = newInstance(message);
        dialog.showInContext(context);
        return dialog;
    }

    public void setMessage(String message) {
        this.message = message;
        if (this.loadingText != null) {
            this.loadingText.setText(message);
        }
    }

    public String getDialogTag() {
        return LOADING_DIALOG_TAG;
    }
}
