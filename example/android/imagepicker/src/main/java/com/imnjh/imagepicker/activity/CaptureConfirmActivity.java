package com.imnjh.imagepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.imnjh.imagepicker.R;
import com.imnjh.imagepicker.SImagePicker;

/**
 * Created by Martin on 2017/1/17.
 */
public class CaptureConfirmActivity extends BasePickerActivity {

  public static final String EXTRA_URI = "uri";
  public static final int REQUEST_CODE_CONFIRM = 123;
  public static final int RESULT_CODE_RETRY = 5;

  ImageView resetView;
  ImageView confirmView;
  ImageView cancelView;
  RelativeLayout captureContainer;

  private Uri uri;

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_photo_confirm;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    uri = getIntent().getParcelableExtra(EXTRA_URI);
    initUI();
  }

  private void initUI() {
    captureContainer = (RelativeLayout) findViewById(R.id.captureContainer);
    cancelView = (ImageView) findViewById(R.id.cancel);
    confirmView = (ImageView) findViewById(R.id.confirm);
    resetView = (ImageView) findViewById(R.id.reset);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    layoutParams.addRule(RelativeLayout.ABOVE, R.id.operation_container);
    ImageView imageView = SImagePicker.getPickerConfig().getImageLoader().createImageView(this);
    captureContainer.addView(imageView, layoutParams);
    SImagePicker.getPickerConfig().getImageLoader().bindImage(imageView, uri);
    resetView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_CODE_RETRY);
        finish();
      }
    });
    cancelView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
    confirmView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setResult(RESULT_OK);
        finish();
      }
    });
  }

  public static void launch(Activity activity, Uri uri) {
    Intent intent = new Intent();
    intent.setClass(activity, CaptureConfirmActivity.class);
    intent.putExtra(EXTRA_URI, uri);
    activity.startActivityForResult(intent, REQUEST_CODE_CONFIRM);
  }
}
