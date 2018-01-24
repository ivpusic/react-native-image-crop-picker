package com.reactnative.ivpusic.imagepicker;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.lang.ref.WeakReference;


/**
 * Created by Martin on 2017/1/17.
 */

public class PickerConfig {

    private ImageLoader imageLoader;
    private WeakReference<Context> appContext ;
    private int toolbarColor;
    public Point displaySize = new Point();
    public DisplayMetrics displayMetrics = new DisplayMetrics();
    private float density;

    private PickerConfig(Builder builder) {
        this.imageLoader = builder.imageLoader;
        this.appContext = new WeakReference<>(builder.context);
        this.toolbarColor = builder.toolbarColor;
        if (appContext.get() != null) {
            density = appContext.get().getResources().getDisplayMetrics().density;
            try {
                WindowManager manager = (WindowManager) appContext.get().getSystemService(Context.WINDOW_SERVICE);
                if (manager != null) {
                    Display display = manager.getDefaultDisplay();
                    if (display != null) {
                        display.getMetrics(displayMetrics);
                        display.getSize(displaySize);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public int getToolbarColor() {
        return toolbarColor;
    }

    public Point getDisplaySize() {
        return displaySize;
    }

    public DisplayMetrics getDisplayMetrics() {
        return displayMetrics;
    }

    public float getDensity() {
        return density;
    }

    public static class Builder {

        private ImageLoader imageLoader;
        private Context context;
        private int toolbarColor;

        public Builder() {
        }

        public Builder setAppContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setImageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            return this;
        }

        public Builder setToolbaseColor(int color) {
            this.toolbarColor = color;
            return this;
        }

        public PickerConfig build() {
            return new PickerConfig(this);
        }
    }
}
