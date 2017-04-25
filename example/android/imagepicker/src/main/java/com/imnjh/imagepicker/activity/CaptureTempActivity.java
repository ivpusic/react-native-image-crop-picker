package com.imnjh.imagepicker.activity;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.imnjh.imagepicker.CapturePhotoHelper;
import com.imnjh.imagepicker.R;

/**
 * Created by Martin on 2017/1/17.
 */

public class CaptureTempActivity extends BasePickerActivity {

  public static final String CAPTURE_URI = "capture_uri";

  private CapturePhotoHelper capturePhotoHelper;

  private Uri uri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    capturePhotoHelper = new CapturePhotoHelper(this);
    if (savedInstanceState == null) {
      uri = getIntent().getParcelableExtra(CAPTURE_URI);
      capturePhotoHelper.capturePhoto(uri);
    } else {
      uri = (Uri) savedInstanceState.get(CAPTURE_URI);
    }
    if (uri == null) {
      finishWith(Activity.RESULT_CANCELED);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(CAPTURE_URI, uri);
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_capture_temp;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == CaptureConfirmActivity.REQUEST_CODE_CONFIRM) {
        finishWith(RESULT_OK);
      } else if (requestCode == CapturePhotoHelper.CAPTURE_PHOTO_REQUEST_CODE) {
        CaptureConfirmActivity.launch(this, uri);
      }
    } else if (resultCode == RESULT_CANCELED) {
      finishWith(RESULT_CANCELED);
    } else if (resultCode == CaptureConfirmActivity.RESULT_CODE_RETRY) {
      File photoFile = new File(uri.getPath());
      if (photoFile.exists()) {
        photoFile.delete();
      }
      try {
        photoFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
      capturePhotoHelper.capturePhoto(uri);
    }
  }

  private void finishWith(int resultCode) {
    setResult(resultCode);
    finish();
  }
}
