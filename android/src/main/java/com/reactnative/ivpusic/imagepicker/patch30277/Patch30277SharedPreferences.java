package com.reactnative.ivpusic.imagepicker.patch30277;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.facebook.react.bridge.ReadableMap;

import javax.annotation.Nullable;

public class Patch30277SharedPreferences {

    private static final String SP_LAST_IMAGE_URI = "Patch60277SharedPreferences/lastImageUri";
    private static final String SP_LAST_OPTIONS = "Patch60277SharedPreferences/options";
    private static final String SP_CURRENT_MEDIA_PATH = "Patch60277SharedPreferences/currentMediaPath";

    public static void clear(Context context) {
        if(context == null){
            return;
        }
        SharedPreferences sharedPref = getSP(context);
        SharedPreferences.Editor editor = sharedPref.edit().clear();
        editor.apply();
    }

    public static void saveLastTempImageUri(Context context, Uri uri) {
        if(context == null || uri == null){
            return;
        }
        SharedPreferences sharedPref = getSP(context);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SP_LAST_IMAGE_URI, uri.toString());
        editor.apply();
    }

    @Nullable
    public static Uri getLastTempImageUri(Context context) {
        if(context == null){
            return null;
        }
        SharedPreferences sharedPref = getSP(context);
        String spString = sharedPref.getString(SP_LAST_IMAGE_URI, null);
        if(spString != null){
            return Uri.parse(spString);
        }

        return null;
    }

    public static void saveCurrentMediaPath(Context context, String path) {
        if(context == null){
            return;
        }
        SharedPreferences sharedPref = getSP(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SP_CURRENT_MEDIA_PATH,  path);
        editor.apply();
    }

    @Nullable
    public static String getCurrentMediaPath(Context context) {
        if(context == null){
            return null;
        }
        SharedPreferences sharedPref = getSP(context);
        return sharedPref.getString(SP_CURRENT_MEDIA_PATH, null);
    }

    public static void saveOptions(Context context,@Nullable ReadableMap readableMap) {
        if(context == null || readableMap == null){
            return;
        }
        SharedPreferences sharedPref = getSP(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(SP_LAST_OPTIONS, ReadableMapUtil.toString(readableMap));
        editor.apply();
    }

    @Nullable
    public static ReadableMap getOptions(Context context) {
        if(context == null){
            return null;
        }
        SharedPreferences sharedPref = getSP(context);
        return ReadableMapUtil.toReadableMap(sharedPref.getString(SP_LAST_OPTIONS, null));
    }

    private static SharedPreferences getSP(Context context) {
        return context.getSharedPreferences("Patch60277SharedPreferences", Context.MODE_PRIVATE);
    }

}
