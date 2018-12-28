package com.example.pamir.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@SuppressLint("AppCompatCustomView")
public class LoadingSpinner extends ImageView {
    private static final int SPIN_DURATION = 2000;
    public int size;

    public LoadingSpinner(Context context) {
        super(context);
        init();
    }

    public LoadingSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.size = getResources().getDimensionPixelSize(R.dimen.spinner_size);
        setId(R.id.spinner);
        setImageResource(R.drawable.spinner);
        setScaleType(ScaleType.CENTER_INSIDE);
        setLayoutParams(new LayoutParams(this.size, this.size));
        spin();
    }

    public void spin() {
        clearAnimation();
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, (float) (this.size / 2), (float) (this.size / 2));
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(-1);
        anim.setDuration(2000);
        setAnimation(anim);
    }

    public void stopSpinning() {
        clearAnimation();
    }
}
