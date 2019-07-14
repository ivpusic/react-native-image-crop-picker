package com.reactnative.ivpusic.imagepicker;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Log.class)
public class ScaleTest {

    @Before
    public void beforeTest() {
        PowerMockito.mockStatic(Log.class);
        Mockito.when(Log.d(anyString(), anyString())).thenReturn(0);
    }

    @Test
    public void scaleTest_getScaledImageDimensionsByMaxWidthHeight_returns_correct_values() {
        Scale.Dimension d = Scale.getScaledImageDimensionsByMaxWidthHeight(
                4032,
        3024,
        2560,
        2560
        );
        assertEquals(2560, d.getWidth());
        assertEquals(1920, d.getHeight() );
    }

    @Test
    public void scaleTest_getScaledImageDimensionsByMaxPixels_returns_correct_values() {
        Scale.Dimension d = Scale.getScaledImageDimensionsByMaxPixels(
                4032,
                3024,
                5000000
        );
        assertEquals(2581, d.getWidth());
        assertEquals(1936, d.getHeight() );
    }

}