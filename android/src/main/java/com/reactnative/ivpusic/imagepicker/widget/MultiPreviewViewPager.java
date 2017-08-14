package com.reactnative.ivpusic.imagepicker.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class MultiPreviewViewPager extends ViewPager {

    public MultiPreviewViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        //        if (v instanceof ImageViewTouch) {
        //            return ((ImageViewTouch) v).canScroll(dx) || super.canScroll(v, checkV, dx, x, y);
        //        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
