package com.reactnative.ivpusic.imagepicker.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.reactnative.ivpusic.imagepicker.R;

/**
 * Created by Martin on 2017/1/17.
 */
public class PickerBottomLayout extends FrameLayout {

  public TextView send; //确定

  public TextView preview; //预览

  public Boolean showSendNumber = true; // 是否显示 send 上的数字

  private int pickTextRes = R.string.general_ok;

  public PickerBottomLayout(Context context) {
    this(context, null);
  }

  public PickerBottomLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PickerBottomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(final Context context) {
    inflate(context, R.layout.picker_bottom_layout, this);
    send = (TextView) findViewById(R.id.send);

    preview = (TextView) findViewById(R.id.preview);
  }

  public void updateSelectedCount(int count) {
    if (count == 0) {
      send.setTextColor(getResources().getColor(R.color.gray));
      send.setEnabled(false);
      send.setText(getResources().getString(R.string.general_ok));

      preview.setTextColor(getResources().getColor(R.color.gray));
      preview.setEnabled(false);
    } else {
      send.setTextColor(getResources().getColor(R.color.color_48baf3));
      send.setEnabled(true);
      if (showSendNumber) {
        send.setText(getResources().getString(R.string.general_ok) + " "
            + getResources().getString(R.string.bracket_num, count));
      }  else {
        send.setText(getResources().getString(R.string.general_ok));
      }
      preview.setTextColor(getResources().getColor(R.color.color_48baf3));
      preview.setEnabled(true);
    }
  }

//  public void updateSelectedSize(String size) {
//    if (TextUtils.isEmpty(size)) {
//      originalContainer.setVisibility(View.GONE);
//      originalCheckbox.setChecked(false);
//    } else {
//      originalContainer.setVisibility(View.VISIBLE);
//      originalSize.setText(getResources().getString(R.string.general_original) + " "
//          + getResources().getString(R.string.bracket_str, size));
//    }
//  }

  public void hide() {
    animate().translationY(getHeight())
        .setInterpolator(new AccelerateInterpolator(2));
  }

  public void show() {
    animate().translationY(0)
        .setInterpolator(new AccelerateInterpolator(2));
  }

  public void setCustomPickText(@StringRes int pickTextRes) {
    this.pickTextRes = pickTextRes;
  }
}
