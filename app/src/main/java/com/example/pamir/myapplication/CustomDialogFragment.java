package com.example.pamir.myapplication;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;


import butterknife.Unbinder;

public class CustomDialogFragment extends DialogFragment implements DialogInterface {
    public static final String DIALOG_TAG = "custom-dialog";
    protected OnDismissListener backListener;
    protected OnDismissListener dismissListener;
    protected View layout;
    protected boolean userDismissable = true;
    private Unbinder unbinder;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(1, 0);
        setRetainInstance(true);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        try {
            super.onActivityCreated(savedInstanceState);
        } catch (Exception e) {
            dismiss();
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        NonUserDismissableDialog dialog = new NonUserDismissableDialog(getActivity(), getTheme());
        dialog.setUserDismissable(this.userDismissable, this.backListener);
        return dialog;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.layout = inflater.inflate(getLayoutResource(), container, false);
        unbinder = ButterKnife.bind((Object) this, this.layout);
        onViewInflated();
        return this.layout;
    }

    protected void onViewInflated() {
    }

    public void cancel() {
        dismiss();
    }

    public void onStart() {
        super.onStart();
        setDialogSize();
    }

    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setDialogSize() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
            int margin = getMargin();
            int screenHeight = metrics.heightPixels - (margin * 2);
            int dialogWidth = Math.min(metrics.widthPixels - (margin * 2), getMaxWidth());
            int dialogHeight = Math.min(screenHeight, getMaxHeight());
            dialog.getWindow().setLayout(dialogWidth, dialogHeight);
            onDialogSizeUpdated(dialogWidth, dialogHeight);
        }
    }

    private void onDialogSizeUpdated(int dialogWidth, int dialogHeight) {
    }

    public int getMaxWidth() {
        return 420;
    }

    public int getMaxHeight() {
        return -2;
    }

    public int getMargin() {
        return 16;
    }

    public int getLayoutResource() {
        return 0;
    }

    public View getLayout() {
        return this.layout;
    }

    public void setLayout(View layout) {
        this.layout = layout;
    }

    public void setUserDismissable(boolean userDismissable) {
        setUserDismissable(userDismissable, null);
    }

    public void setUserDismissable(boolean userDismissable, OnDismissListener backListener) {
        this.userDismissable = userDismissable;
        this.backListener = backListener;
        NonUserDismissableDialog dialog = (NonUserDismissableDialog) getDialog();
        if (dialog != null) {
            dialog.setUserDismissable(userDismissable, backListener);
        }
    }

    public void showInContext(Context context) {
        show(((Activity) context).getFragmentManager(), getDialogTag());
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void setOnDismissListener(OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.dismissListener != null) {
            this.dismissListener.onDismiss(dialog);
        }
    }

    protected String getDialogTag() {
        return DIALOG_TAG;
    }
}
