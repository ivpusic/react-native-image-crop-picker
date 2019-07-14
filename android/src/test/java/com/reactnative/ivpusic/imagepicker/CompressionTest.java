package com.reactnative.ivpusic.imagepicker;

import android.graphics.BitmapFactory;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class CompressionTest {

    @Before
    public void beforeTest() {
        PowerMockito.mockStatic(Log.class);
        Mockito.when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void compressionTest_shouldCompress_returns_true_for_maxPixels_lt_currentPixels() {
        Compression compression = new Compression();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.outHeight = 3024;
        bitmapOptions.outWidth = 4032;
        bitmapOptions.outMimeType = "image/jpeg";

        assertTrue(compression.shouldCompress(bitmapOptions, null, null, null, 4000000));
    }

    @Test
    public void compressionTest_shouldNotCompress_returns_false_for_maxPixels_gt_currentPixels() {
        Compression compression = new Compression();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.outHeight = 768;
        bitmapOptions.outWidth = 1024;
        bitmapOptions.outMimeType = "image/jpeg";

        assertFalse(compression.shouldCompress(bitmapOptions, null, null, null, 4000000));
    }

    @Test
    public void compressionTest_shouldCompress_returns_true_for_maxWidth_lt_currentWidth() {
        Compression compression = new Compression();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.outHeight = 3024;
        bitmapOptions.outWidth = 4032;
        bitmapOptions.outMimeType = "image/jpeg";

        assertTrue(compression.shouldCompress(bitmapOptions, null, 3000, null, 0));
    }

    @Test
    public void compressionTest_shouldNotCompress_returns_false_for_maxWidth_gt_currentWidth() {
        Compression compression = new Compression();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.outHeight = 768;
        bitmapOptions.outWidth = 1024;
        bitmapOptions.outMimeType = "image/jpeg";

        assertFalse(compression.shouldCompress(bitmapOptions, null, 2048, null, 0));
    }

    @Test
    public void compressionTest_shouldCompress_returns_true_for_unknown_mime_type() {
        Compression compression = new Compression();

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.outHeight = 768;
        bitmapOptions.outWidth = 1024;
        bitmapOptions.outMimeType = "image/nachos";

        assertTrue(compression.shouldCompress(bitmapOptions, null, 3000, null, 0));
    }
}