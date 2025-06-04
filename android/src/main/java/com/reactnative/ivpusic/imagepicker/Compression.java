package com.reactnative.ivpusic.imagepicker;

import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
import static android.os.Looper.getMainLooper;

import static androidx.media3.effect.FrameDropEffect.createSimpleFrameDropEffect;
import static androidx.media3.transformer.Transformer.PROGRESS_STATE_NOT_STARTED;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.FrameDropEffect;
import androidx.media3.effect.Presentation;
import androidx.media3.transformer.AudioEncoderSettings;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.DefaultEncoderFactory;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.ProgressHolder;
import androidx.media3.transformer.TransformationRequest;
import androidx.media3.transformer.Transformer;
import androidx.media3.transformer.VideoEncoderSettings;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by ipusic on 12/27/16.
 */

class Compression {

    File resize(
            Context context,
            String originalImagePath,
            int originalWidth,
            int originalHeight,
            int maxWidth,
            int maxHeight,
            int quality
    ) throws IOException {
        Pair<Integer, Integer> targetDimensions =
                this.calculateTargetDimensions(originalWidth, originalHeight, maxWidth, maxHeight);

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

        bitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

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

    @OptIn(markerClass = UnstableApi.class)
    synchronized void compressVideo(
            final Activity activity,
            final VideoCompressionOptions options,
            final String originalVideo,
            final String compressedVideo,
            final Promise promise
    ) {

        if(options == null) {
            promise.resolve(originalVideo);
            return;
        }

        Size src = getVideoSize(originalVideo);
        Size dst = options.getSize();
        int srcBitrate = getBitrate(originalVideo);
        float fpsValue = getFpsValue(originalVideo);

        boolean needResize = src.getWidth()  > dst.getWidth() || src.getHeight() > dst.getHeight();
        boolean needRecode = !isHevcVideo(originalVideo);
        boolean needDownFrameRate = fpsValue > VideoCompressionOptions.DEFAULT_FPS;

        //
        if(!needResize && !needRecode && !needDownFrameRate) {
            promise.resolve(originalVideo);
            return;
        }

        //
        VideoEncoderSettings videoEncoderSettings = new VideoEncoderSettings.Builder()
                .setBitrateMode(BITRATE_MODE_VBR)
                .build();

        //
        AudioEncoderSettings audioEncoderSettings = new AudioEncoderSettings.Builder()
                .setBitrate(VideoCompressionOptions.DEFAULT_AUDIO_BITRATE)
                .build();

        //
        DefaultEncoderFactory encoderFactory = new DefaultEncoderFactory.Builder(
                activity.getApplicationContext())
                .setRequestedVideoEncoderSettings(videoEncoderSettings)
                .setRequestedAudioEncoderSettings(audioEncoderSettings)
                .build();


        // setup progress holder
        ProgressHolder compressionProgressHolder = new ProgressHolder();

        // progress updater
        Handler mainHandler = new Handler(getMainLooper());

        // setup transformer
        Transformer videoCompressionTransformer =
                new Transformer.Builder(activity.getApplicationContext())
                        .addListener(new Transformer.Listener() {
                            @Override
                            public void onCompleted(
                                    @NonNull Composition composition,
                                    @NonNull ExportResult exportResult
                            ) {
                                Transformer.Listener.super.onCompleted(composition, exportResult);
                                promise.resolve(compressedVideo);
                                mainHandler.removeCallbacksAndMessages(null); // clear progress handler
                            }

                            @Override
                            public void onError(
                                    @NonNull Composition composition,
                                    @NonNull ExportResult exportResult,
                                    @NonNull ExportException exportException
                            ) {
                                Transformer.Listener.super.onError(composition, exportResult, exportException);
                                promise.reject(exportException);
                                mainHandler.removeCallbacksAndMessages(null); // clear progress handler
                            }

                            @Override
                            public void onFallbackApplied(
                                    @NonNull Composition composition,
                                    @NonNull TransformationRequest originalTransformationRequest,
                                    @NonNull TransformationRequest fallbackTransformationRequest
                            ) {
                                Transformer.Listener.super.onFallbackApplied(
                                        composition,
                                        originalTransformationRequest,
                                        fallbackTransformationRequest
                                );
                            }
                        })
                        .setEncoderFactory(encoderFactory)
                        .setPortraitEncodingEnabled(true)
                        .setVideoMimeType(hasHevcHwEncoder() ? MimeTypes.VIDEO_H265 : MimeTypes.VIDEO_H264)
                        .setEnsureFileStartsOnVideoFrameEnabled(true)
                        .build();

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if(videoCompressionTransformer.getProgress(compressionProgressHolder) != PROGRESS_STATE_NOT_STARTED) {
                    //todo update progress?
                }
                mainHandler.postDelayed(this, 100);
            }
        });

        //
        MediaItem inputMediaItem = MediaItem.fromUri(originalVideo);

        //
        ArrayList<AudioProcessor> audioProcessors = new ArrayList<>();
        ArrayList<Effect> effectList = new ArrayList<>();

        //
        if(needDownFrameRate) {
            FrameDropEffect fpsEffect = createSimpleFrameDropEffect(
                fpsValue,
                VideoCompressionOptions.DEFAULT_FPS
            );
            effectList.add(fpsEffect);
        }

        //
        if(needResize) {
            Presentation sizePresentation = Presentation.createForHeight(options.getSize().getHeight());
            effectList.add(sizePresentation);
        }

        if(needResize || needDownFrameRate) {
            //
            Effects effects = new Effects(audioProcessors, effectList);

            //
            EditedMediaItem editedMediaItem = new EditedMediaItem.Builder(inputMediaItem)
                    .setEffects(effects)
                    .build();

            activity.runOnUiThread(() -> {
                videoCompressionTransformer.start(editedMediaItem, compressedVideo);
            });
        }else {
            activity.runOnUiThread(() -> {
                videoCompressionTransformer.start(inputMediaItem, compressedVideo);
            });
        }
    }

    boolean hasHevcHwEncoder() {
        MediaCodecList list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        for (MediaCodecInfo info : list.getCodecInfos()) {
            if (info.isEncoder()
                    && info.isHardwareAccelerated() // API 29+
                    && Arrays.asList(info.getSupportedTypes())
                    .contains(MediaFormat.MIMETYPE_VIDEO_HEVC)) {
                return true;
            }
        }
        return false;
    }

    private Size getVideoSize(String uri) {
        int width;
        int height;
        int rotation;

        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
            mmr.setDataSource(uri);

            String extractedWidthMetadata =
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String extractedHeightMetadata =
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

            if(extractedWidthMetadata == null || extractedHeightMetadata == null) {
                return new Size(0, 0);
            }

            width = Integer.parseInt(extractedWidthMetadata);
            height = Integer.parseInt(extractedHeightMetadata);

            // Some devices saved videos rotated - check rotation and return proper size
            String rotationStr = mmr.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            rotation = rotationStr == null ? 0 : Integer.parseInt(rotationStr);
            try {
                mmr.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // If rotation is 90° or 270°, swap width/height
        return (rotation == 90 || rotation == 270)
                ? new Size(height, width)
                : new Size(width,  height);
    }

    private boolean isHevcVideo(@NonNull String path) {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(path);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat fmt = extractor.getTrackFormat(i);
                String mime = fmt.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("video/")) {
                    extractor.release();
                    return mime.equalsIgnoreCase(MimeTypes.VIDEO_H265);
                }
            }
            extractor.release();
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    private float getFpsValue(@NonNull String path) {

        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
            mmr.setDataSource(path);
            String mmrFps = mmr.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            if (mmrFps != null) {
                mmr.release();
                return Float.parseFloat(mmrFps);
            }
        } catch (IOException ignore) {}

        try {
            // fallback when first approach will fail
            MediaExtractor ex = new MediaExtractor();
            ex.setDataSource(path);
            for (int i = 0; i < ex.getTrackCount(); i++) {
                MediaFormat fmt = ex.getTrackFormat(i);
                String mime = fmt.getString(MediaFormat.KEY_MIME);
                if (mime != null && mime.startsWith("video/")
                        && fmt.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    int fps = fmt.getInteger(MediaFormat.KEY_FRAME_RATE);
                    ex.release();
                    return fps;
                }
            }
            ex.release();
        } catch (Exception ignore) {}

        return 0;
    }

    /**
     * Calculates the compression gain as a percentage.
     *
     *  • positive value  -> the output file is smaller (e.g., 32.5 %)
     *  • negative value  -> the output file is larger  (e.g., –12.1 %)
     *  • zero            -> no change in size
     *
     * @param originalBytes  size of the source file in bytes
     * @param resultBytes    size of the resulting file in bytes
     * @return compression gain rounded to one decimal place,
     *         or Float.NaN if originalBytes <= 0
     */
    static float compressionGainPercent(long originalBytes, long resultBytes) {
        if (originalBytes <= 0) return Float.NaN; // undefined when the original size is non-positive

        double gain = (originalBytes - resultBytes)
                / (double) originalBytes * 100.0; // percentage gain
        return (float) Math.round(gain * 10) / 10f;   // round to one decimal place
    }

}
