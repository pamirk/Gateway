package com.example.pamir.myapplication;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.util.LruCache;
import android.util.SparseArray;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TypefaceHelper {
    public static final int TYPEFACE_GOTHAM_CUSTOM = 3;
    public static final int TYPEFACE_GOTHAM_ROUNDED_BOOK = 2;
    public static final int TYPEFACE_GOTHAM_ROUNDED_MEDIUM = 1;
    public static final int TYPEFACE_INVALID = 0;
    public static final int TYPEFACE_MESLO_REGULAR = 4;
    private static final LruCache<String, Typeface> cache = new LruCache(2);
    private static final SparseArray<String> typeMap = new SparseArray();
    private final AssetManager assets;

    static {
        typeMap.put(1, "GothamRounded-Medium.otf");
        typeMap.put(2, "GothamRounded-Book.otf");
        typeMap.put(4, "MesloLGLDZ-Regular.ttf");
        typeMap.put(3, "GothamCustom.ttf");
    }

    @Inject
    TypefaceHelper(AssetManager assets) {
        this.assets = assets;
    }

    public Typeface get(int type) {
        if (type == 0) {
            return null;
        }
        String fontName = (String) typeMap.get(type);
        Typeface typeface = (Typeface) cache.get(fontName);
        if (typeface != null) {
            return typeface;
        }
        typeface = Typeface.createFromAsset(this.assets, fontName);
        cache.put(fontName, typeface);
        return typeface;
    }

    public static Typeface get(Context context, int type) {
        return App.component(context).typefaceHelper().get(type);
    }

    public SpannableString getSpanWithFont(CharSequence string, int fontType) {
        SpannableString span = new SpannableString(string);
        span.setSpan(new CustomTypefaceSpan(get(fontType)), 0, span.length(), 33);
        return span;
    }
}
