package com.reactnative.ivpusic.imagepicker.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;

import java.io.File;


/**
 * Created by Martin on 2017/1/17.
 */
public class SystemUtil {

    public static float density = 1;
    public static Point displaySize = new Point();
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static int statusBarHeight = 0;

    private SystemUtil() {
    }


    public static void init(float density, DisplayMetrics displayMetrics, Point point) {
        SystemUtil.density = density;
        SystemUtil.displayMetrics = displayMetrics;
        SystemUtil.displaySize = point;
        checkDisplaySize();
        statusBarHeight = getStatusBarHeight();
    }

    /**
     * get root directory
     *
     * @param applicationContext
     * @return
     */
    public static File getStoreDir(Context applicationContext) {
        File dataDir = null;
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment
                .getExternalStorageState())) {
            dataDir = Environment.getExternalStorageDirectory();
        } else {
            dataDir = applicationContext.getApplicationContext().getFilesDir();
        }
        return dataDir;
    }


    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static void checkDisplaySize() {
        if (displayMetrics != null && displayMetrics.heightPixels < displayMetrics.widthPixels) {
            final int tmp = displayMetrics.heightPixels;
            displayMetrics.heightPixels = displayMetrics.widthPixels;
            displayMetrics.widthPixels = tmp;
        }
        if (displaySize != null && displaySize.y < displaySize.x) {
            final int tmp = displaySize.y;
            displaySize.y = displaySize.x;
            displaySize.x = tmp;
        }
    }

    public static int getSdkVersionInt() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        final Handler uiHandler = new Handler(Looper.getMainLooper());
        if (delay == 0) {
            uiHandler.post(runnable);
        } else {
            uiHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelTask(Runnable runnable) {
        final Handler uiHandler = new Handler(Looper.getMainLooper());
        if (runnable != null) {
            uiHandler.removeCallbacks(runnable);
        }
    }

    public static int getStatusBarHeight() {
        return Resources.getSystem().getDimensionPixelSize(
                Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android"));
    }
}
