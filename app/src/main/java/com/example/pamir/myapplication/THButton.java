package com.example.pamir.myapplication;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;

@SuppressLint("AppCompatCustomView")
public class THButton extends Button {
    private int fontStyle = 0;
    private int fontType;

    public THButton(Context context) {
        super(context);
        setFont(1);
    }

    public THButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFontFilenameFromAttrs(attrs);
    }

    public THButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFontFilenameFromAttrs(attrs);
    }

    public void setFont(int fontType) {
        setFont(fontType, 0);
    }

    public void setFont(int fontType, int fontStyle) {
        this.fontType = fontType;
        this.fontStyle = fontStyle;
        if (fontType != 0 && !isInEditMode()) {
            setTypeface(TypefaceHelper.get(getContext(), fontType), fontStyle);
        }
    }

    private void setFontFilenameFromAttrs(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.Treehouse, 0, 0);
        try {
            this.fontType = a.getInt(0, 0);
            if (getTypeface() != null) {
                this.fontStyle = getTypeface().getStyle();
            }
            if (this.fontType != 0 && !isInEditMode()) {
                setTypeface(TypefaceHelper.get(getContext(), this.fontType), this.fontStyle);
            }
        } finally {
            a.recycle();
        }
    }
}
