package com.imnjh.imagepicker.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.imnjh.imagepicker.R;


/**
 * Created by Martin on 2017/1/17.
 */
public class PickerBottomLayout extends FrameLayout {


  public android.widget.CheckBox originalCheckbox;

  public TextView originalSize;

  public View originalContainer;

  public TextView send;

  private int pickTextRes = R.string.general_send;

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

  private void init(Context context) {
    inflate(context, R.layout.picker_bottom_layout, this);
    send = (TextView) findViewById(R.id.send);
    originalSize = (TextView) findViewById(R.id.original_size);
    originalContainer = findViewById(R.id.original_container);
    originalCheckbox = (android.widget.CheckBox) findViewById(R.id.original_checkbox);
    originalCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        originalSize.setTextColor(isChecked
            ? getResources().getColor(R.color.color_48baf3)
            : getResources().getColor(R.color.gray));
      }
    });
  }

  public void updateSelectedCount(int count) {
    if (count == 0) {
      send.setTextColor(getResources().getColor(R.color.gray));
      send.setEnabled(false);
      send.setText(getResources().getString(pickTextRes));
      originalContainer.setVisibility(View.GONE);
    } else {
      send.setTextColor(getResources().getColor(R.color.color_48baf3));
      send.setEnabled(true);
      send.setText(getResources().getString(pickTextRes) + " "
          + getResources().getString(R.string.bracket_num, count));
      originalContainer.setVisibility(View.VISIBLE);
    }
  }

  public void updateSelectedSize(String size) {
    if (TextUtils.isEmpty(size)) {
      originalContainer.setVisibility(View.GONE);
      originalCheckbox.setChecked(false);
    } else {
      originalContainer.setVisibility(View.VISIBLE);
      originalSize.setText(getResources().getString(R.string.general_original) + " "
          + getResources().getString(R.string.bracket_str, size));
    }
  }

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
