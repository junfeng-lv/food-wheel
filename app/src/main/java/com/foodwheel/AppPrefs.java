package com.foodwheel;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPrefs {
    private static final String PREFS = "food_wheel_prefs";
    private static final String KEY_BG_TYPE = "bg_type";
    private static final String KEY_BG_ASSET = "bg_asset";
    private static final String KEY_BG_PHOTO = "bg_photo";

    private AppPrefs() {}

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String bgType(Context c) { return prefs(c).getString(KEY_BG_TYPE, "asset"); }
    public static String bgAsset(Context c) { return prefs(c).getString(KEY_BG_ASSET, ""); }
    public static String bgPhoto(Context c) { return prefs(c).getString(KEY_BG_PHOTO, ""); }

    public static void applyWallpaper(Context c, String asset) {
        prefs(c).edit().putString(KEY_BG_TYPE, "asset").putString(KEY_BG_ASSET, asset).apply();
    }

    public static void applyPhoto(Context c, String path) {
        prefs(c).edit().putString(KEY_BG_TYPE, "photo").putString(KEY_BG_PHOTO, path).commit();
    }
}

