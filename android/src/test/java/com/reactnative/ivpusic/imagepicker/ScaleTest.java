package com.reactnative.ivpusic.imagepicker;

import org.junit.Test;
import static org.junit.Assert.*;

public class ScaleTest {
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