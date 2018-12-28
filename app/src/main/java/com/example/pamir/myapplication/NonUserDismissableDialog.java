package com.example.pamir.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.MotionEvent;

public class NonUserDismissableDialog extends Dialog {
    private OnDismissListener backListener;
    private boolean userDismissable = true;

    public NonUserDismissableDialog(Context context, int theme) {
        super(context, theme);
    }

    public void onBackPressed() {
        if (this.userDismissable) {
            super.onBackPressed();
        } else if (this.backListener != null) {
            this.backListener.onDismiss(this);
        } else {
            getOwnerActivity().onBackPressed();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.userDismissable) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public void setUserDismissable(boolean dismissable) {
        setUserDismissable(dismissable, null);
    }

    public void setUserDismissable(boolean dismissable, OnDismissListener backListener) {
        this.userDismissable = dismissable;
        this.backListener = backListener;
    }

    public void setBackListener(OnDismissListener backListener) {
        this.backListener = backListener;
    }
}
