package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.Type;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ipusic on 12/27/16.
 */

public class Compression {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    // https://medium.com/@petrakeas/alias-free-resize-with-renderscript-5bf15a86ce3
    public static Bitmap resizeBitmapWithRenderScript(RenderScript rs, Bitmap src, int dstWidth) {
        Bitmap.Config  bitmapConfig = src.getConfig();
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        float srcAspectRatio = (float) srcWidth / srcHeight;
        int dstHeight = (int) (dstWidth / srcAspectRatio);

        float resizeRatio = (float) srcWidth / dstWidth;

        /* Calculate gaussian's radius */
        float sigma = resizeRatio / (float) Math.PI;
        // https://android.googlesource.com/platform/frameworks/rs/+/master/cpu_ref/rsCpuIntrinsicBlur.cpp
        float radius = 2.5f * sigma - 1.5f;
        radius = Math.min(25, Math.max(0.0001f, radius));

        /* Gaussian filter */
        Allocation tmpIn = Allocation.createFromBitmap(rs, src);
        Allocation tmpFiltered = Allocation.createTyped(rs, tmpIn.getType());
        ScriptIntrinsicBlur blurIntrinsic = ScriptIntrinsicBlur.create(rs, tmpIn.getElement());

        blurIntrinsic.setRadius(radius);
        blurIntrinsic.setInput(tmpIn);
        blurIntrinsic.forEach(tmpFiltered);

        tmpIn.destroy();
        blurIntrinsic.destroy();

        /* Resize */
        Bitmap dst = Bitmap.createBitmap(dstWidth, dstHeight, bitmapConfig);
        Type t = Type.createXY(rs, tmpFiltered.getElement(), dstWidth, dstHeight);
        Allocation tmpOut = Allocation.createTyped(rs, t);
        ScriptIntrinsicResize resizeIntrinsic = ScriptIntrinsicResize.create(rs);

        resizeIntrinsic.setInput(tmpFiltered);
        resizeIntrinsic.forEach_bicubic(tmpOut);
        tmpOut.copyTo(dst);

        tmpFiltered.destroy();
        tmpOut.destroy();
        resizeIntrinsic.destroy();

        return dst;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    File resize(Context context, String originalImagePath, int maxWidth, int maxHeight, int quality) throws IOException {
        Bitmap original = BitmapFactory.decodeFile(originalImagePath);

        int targetWidth = targetDimensions.first;
        int targetHeight = targetDimensions.second;

        Bitmap bitmap = null;
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            bitmap = BitmapFactory.decodeFile(originalImagePath);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, targetWidth, targetHeight);
            bitmap = BitmapFactory.decodeFile(originalImagePath, options);
        }

        // Use original image exif orientation data to preserve image orientation for the resized bitmap
        ExifInterface originalExif = new ExifInterface(originalImagePath);
        String originalOrientation = originalExif.getAttribute(ExifInterface.TAG_ORIENTATION);

        Matrix rotationMatrix = new Matrix();
        int rotationAngleInDegrees = getRotationInDegreesForOrientationTag(originalOrientation);
        rotationMatrix.postRotate(rotationAngleInDegrees);

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;

        if (ratioMax > 1) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        }

        Bitmap resized = resizeBitmapWithRenderScript(RenderScript.create(context), original, finalWidth);

        Bitmap rotatedBitmap = Bitmap.createBitmap(resized, 0, 0, resized.getWidth(), resized.getHeight(), rotationMatrix, true);

        if (rotatedBitmap != resized) {
            resized.recycle();
        }

        resized = rotatedBitmap;

        File imageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (!imageDirectory.exists()) {
            Log.d("image-crop-picker", "Pictures Directory is not existing. Will create this directory.");
            imageDirectory.mkdirs();
        }

        File resizeImageFile = new File(imageDirectory, UUID.randomUUID() + ".jpg");

        OutputStream os = new BufferedOutputStream(new FileOutputStream(resizeImageFile));
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, os);

        // Don't set unnecessary exif attribute
        if (shouldSetOrientation(originalOrientation)) {
            ExifInterface exif = new ExifInterface(resizeImageFile.getAbsolutePath());
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, originalOrientation);
            exif.saveAttributes();
        }

        os.close();
        bitmap.recycle();

        return resizeImageFile;
    }

    private int calculateInSampleSize(int originalWidth, int originalHeight, int requestedWidth, int requestedHeight) {
        int inSampleSize = 1;

        if (originalWidth > requestedWidth || originalHeight > requestedHeight) {
            final int halfWidth = originalWidth / 2;
            final int halfHeight = originalHeight / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfWidth / inSampleSize) >= requestedWidth
                    && (halfHeight / inSampleSize) >= requestedHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private boolean shouldSetOrientation(String orientation) {
        return !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_NORMAL))
                && !orientation.equals(String.valueOf(ExifInterface.ORIENTATION_UNDEFINED));
    }

    File compressImage(final Context context, final ReadableMap options, final String originalImagePath, final BitmapFactory.Options bitmapOptions) throws IOException {
        Integer maxWidth = options.hasKey("compressImageMaxWidth") ? options.getInt("compressImageMaxWidth") : null;
        Integer maxHeight = options.hasKey("compressImageMaxHeight") ? options.getInt("compressImageMaxHeight") : null;
        Double quality = options.hasKey("compressImageQuality") ? options.getDouble("compressImageQuality") : null;

        boolean isLossLess = (quality == null || quality == 1.0);
        boolean useOriginalWidth = (maxWidth == null || maxWidth >= bitmapOptions.outWidth);
        boolean useOriginalHeight = (maxHeight == null || maxHeight >= bitmapOptions.outHeight);

        List knownMimes = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "image/tiff");
        boolean isKnownMimeType = (bitmapOptions.outMimeType != null && knownMimes.contains(bitmapOptions.outMimeType.toLowerCase()));

        if (isLossLess && useOriginalWidth && useOriginalHeight && isKnownMimeType) {
            Log.d("image-crop-picker", "Skipping image compression");
            return new File(originalImagePath);
        }

        Log.d("image-crop-picker", "Image compression activated");

        // compression quality
        int targetQuality = quality != null ? (int) (quality * 100) : 100;
        Log.d("image-crop-picker", "Compressing image with quality " + targetQuality);

        if (maxWidth == null) maxWidth = bitmapOptions.outWidth;
        if (maxHeight == null) maxHeight = bitmapOptions.outHeight;

        return resize(context, originalImagePath, bitmapOptions.outWidth, bitmapOptions.outHeight, maxWidth, maxHeight, targetQuality);
    }

    private Pair<Integer, Integer> calculateTargetDimensions(int currentWidth, int currentHeight, int maxWidth, int maxHeight) {
        int width = currentWidth;
        int height = currentHeight;

        if (width > maxWidth) {
            float ratio = ((float) maxWidth / width);
            height = (int) (height * ratio);
            width = maxWidth;
        }

        if (height > maxHeight) {
            float ratio = ((float) maxHeight / height);
            width = (int) (width * ratio);
            height = maxHeight;
        }

        return Pair.create(width, height);
    }

    synchronized void compressVideo(final Activity activity, final ReadableMap options, final String originalVideo, final String compressedVideo, final Promise promise) {
        // todo: video compression
        // failed attempt 1: ffmpeg => slow and licensing issues
        promise.resolve(originalVideo);
    }
}
