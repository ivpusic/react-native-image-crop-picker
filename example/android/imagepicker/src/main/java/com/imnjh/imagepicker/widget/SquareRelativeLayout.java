package com.imnjh.imagepicker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.imnjh.imagepicker.R;


/**
 * Created by Martin on 2017/1/17.
 */
public class SquareRelativeLayout extends RelativeLayout {

  public ImageView photo;
  public CheckBox checkBox;

  public SquareRelativeLayout(Context context) {
    super(context);
  }

  public SquareRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    checkBox = (CheckBox) findViewById(R.id.checkbox);
  }

  public void setPhotoView(ImageView imageView) {
    ViewGroup.LayoutParams layoutParams =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    this.photo = imageView;
    addView(imageView, 0, layoutParams);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    setMeasuredDimension(getDefaultSize(0, widthMeasureSpec),
        getDefaultSize(0, heightMeasureSpec));
    int childWidthSize = getMeasuredWidth();
    heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(
        childWidthSize, MeasureSpec.EXACTLY);
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}
