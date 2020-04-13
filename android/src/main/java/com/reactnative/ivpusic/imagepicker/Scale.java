package com.reactnative.ivpusic.imagepicker;

import android.util.Log;

import java.math.BigDecimal;

public class Scale {
    public static class Dimension {
        private int width;
        private int height;
        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
        public int getWidth() {
            return width;
        }
        public int getHeight() {
            return height;
        }
    }

    public static Dimension getScaledImageDimensionsByMaxWidthHeight(int width, int height, int maxWidth, int maxHeight) {
        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > 1) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }
        return new Dimension(finalWidth, finalHeight);
    }

    public static Dimension getScaledImageDimensionsByMaxPixels(int width, int height, int maxPixels) {
        int currentPixels = width * height;
        double scale = 1.0;
        if (currentPixels > maxPixels && currentPixels > 0 && maxPixels > 0) {
            BigDecimal ratio = new BigDecimal(currentPixels).divide(new BigDecimal(maxPixels));
            scale = scale / Math.sqrt(ratio.doubleValue());
        }
        Log.d("image-crop-picker", "Compression: currentPixels: " + currentPixels + ", maxPixels: " + maxPixels + ", scale: " + scale);
        return new Dimension((int)(width * scale), (int)(height * scale));
    }

}
