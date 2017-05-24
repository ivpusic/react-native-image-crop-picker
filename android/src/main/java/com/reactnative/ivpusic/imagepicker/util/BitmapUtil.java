package com.reactnative.ivpusic.imagepicker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by jack on 2017/5/5.
 */

public class BitmapUtil {
    //根据图片需要显示的宽和高对图片进行压缩
    public static Bitmap decodeSampleBitmapFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }


    //根据需求的宽和高以及图片实际的宽和高计算SampleSize
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if(width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f/reqWidth);
            int heightRadio = Math.round(height * 1.0f/reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }
}
