package com.reactnative.ivpusic.imagepicker;

import android.graphics.Matrix;
import android.graphics.Rect;

import com.facebook.drawee.drawable.ScalingUtils;

/**
 * Created on 6/6/16.
 *
 * @author yingyi.xu@rush.im (Yingyi Xu)
 */
public class ScaleTypeFillCenterInside extends ScalingUtils.AbstractScaleType {

    public static final ScalingUtils.ScaleType INSTANCE = new ScaleTypeFillCenterInside();

    @Override
    public void getTransformImpl(
            Matrix outTransform,
            Rect parentRect,
            int childWidth,
            int childHeight,
            float focusX,
            float focusY,
            float scaleX,
            float scaleY) {
        float scale = Math.min(scaleX, scaleY);
        float dx = parentRect.left + (parentRect.width() - childWidth * scale) * 0.5f;
        float dy = parentRect.top + (parentRect.height() - childHeight * scale) * 0.5f;
        outTransform.setScale(scale, scale);
        outTransform.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
    }
}
