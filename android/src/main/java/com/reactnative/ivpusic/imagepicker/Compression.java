package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.reactnative.ivpusic.imagepicker.util.ImageUtil;

import java.io.File;

import id.zelory.compressor.Compressor;

/**
 * Created by ipusic on 12/27/16.
 */

public class Compression {

  public File compressImage(final Activity activity, final ReadableMap options, final String originalImagePath) {
    Integer maxWidth = options.hasKey("compressImageMaxWidth") ? options.getInt("compressImageMaxWidth") : 0;
    Integer maxHeight = options.hasKey("compressImageMaxHeight") ? options.getInt("compressImageMaxHeight") : 0;
    Double quality = options.hasKey("compressImageQuality") ? options.getDouble("compressImageQuality") : 0;

    if (maxWidth == 0 && maxHeight == 0 && quality == 0) {
      Log.d("image-crop-picker", "Skipping image compression");
      return new File(originalImagePath);
    }

    PointF size = ImageUtil.getBmpSize(originalImagePath);
    if (maxWidth > size.x && maxHeight > size.y) {
      Log.d("image-crop-picker", "Skipping image compression");
      return new File(originalImagePath);
    }

    Log.d("image-crop-picker", "Image compression activated");
    Compressor.Builder builder = new Compressor.Builder(activity)
        .setCompressFormat(Bitmap.CompressFormat.JPEG)
        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getAbsolutePath());

    if (quality > 0) {
      Log.d("image-crop-picker", "Compressing image with quality " + (quality * 100));
      builder.setQuality((int) (quality * 100));
    } else {
      Log.d("image-crop-picker", "Compressing image with quality 100");
      builder.setQuality(100);
    }

    if (maxWidth > 0) {
      Log.d("image-crop-picker", "Compressing image with max width " + maxWidth);
      builder.setMaxWidth(maxWidth);
    }

    if (maxHeight > 0) {
      Log.d("image-crop-picker", "Compressing image with max height " + maxHeight);
      builder.setMaxHeight(maxHeight);
    }

    return builder
        .build()
        .compressToFile(new File(originalImagePath));
  }

  public synchronized void compressVideo(final Activity activity, final ReadableMap options, final String originalVideo, final String compressedVideo, final Promise promise) {
    // todo: video compression
    // failed attempt 1: ffmpeg => slow and licensing issues
    promise.resolve(originalVideo);
  }
}
