package com.imnjh.imagepicker.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.imnjh.imagepicker.R;
import com.imnjh.imagepicker.util.ImageUtil;
import com.imnjh.imagepicker.util.SystemUtil;
import com.imnjh.imagepicker.widget.ClipImageLayout;


/**
 * Created by Martin on 2017/1/17.
 */
public class CropImageActivity extends BasePickerActivity {

  public static final String RESULT_PATH = "crop_image";
  private static final String PARAM_ORIGIN_PATH = "param_origin_path";
  public static final String PARAM_AVATAR_PATH = "param_path";

  private static final int SIZE_LIMIT = 2048;

  ClipImageLayout clipImageLayout;
  View cancel;
  View confirm;

  private String sourcePath;
  private int sampleSize;
  private String filePath;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    init();
  }

  private void init() {
    confirm = findViewById(R.id.confirm);
    cancel = findViewById(R.id.cancel);
    clipImageLayout = (ClipImageLayout) findViewById(R.id.clip_layout);
    sourcePath = getIntent().getStringExtra(PARAM_ORIGIN_PATH);
    filePath = getIntent().getStringExtra(PARAM_AVATAR_PATH);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
    confirm.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clipImage();
      }
    });
    setSourceUri(sourcePath);
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_crop_image;
  }

  public static void startImageCrop(Activity activity, String originPhotoPath, int requestCode,
      String dstFilePath) {
    Intent intent = new Intent();
    intent.putExtra(PARAM_ORIGIN_PATH, originPhotoPath);
    intent.putExtra(PARAM_AVATAR_PATH, dstFilePath);
    intent.setClass(activity, CropImageActivity.class);
    activity.startActivityForResult(intent, requestCode);
  }

  public static void startImageCrop(Fragment fragment, String originPhotoPath, int requestCode,
      String dstFilePath) {
    Intent intent = new Intent();
    intent.putExtra(PARAM_ORIGIN_PATH, originPhotoPath);
    intent.putExtra(PARAM_AVATAR_PATH, dstFilePath);
    intent.setClass(fragment.getActivity(), CropImageActivity.class);
    fragment.startActivityForResult(intent, requestCode);
  }

  private void setSourceUri(final String sourcePath) {
    this.sourcePath = sourcePath;
    this.sampleSize = 0;
    if (!TextUtils.isEmpty(sourcePath)) {
      new AsyncTask<Void, Void, Void>() {

        @Override
        protected Void doInBackground(Void... params) {
          try {
            sampleSize = calculateBitmapSampleSize(sourcePath);
            final Bitmap bitmap = ImageUtil.loadBitmap(sourcePath, sampleSize);
            if (bitmap != null) {
              SystemUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                  startCrop(bitmap);
                }
              });
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
          return null;
        }
      }.execute();
    }
  }

  private void startCrop(Bitmap bitmap) {
    clipImageLayout.setImageBitmap(bitmap);
  }


  private int calculateBitmapSampleSize(String originPath) throws IOException {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(originPath, options);
    int maxSize = SIZE_LIMIT;
    int sampleSize = 1;
    if (options.outHeight > 0 && options.outWidth > 0) {
      while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
        sampleSize = sampleSize << 1;
      }
    } else {
      sampleSize = sampleSize << 2;
    }
    return sampleSize;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  private void clipImage() {
    final Bitmap bitmap = clipImageLayout.clip();
    new AsyncTask<Void, Void, Boolean>() {

      @Override
      protected Boolean doInBackground(Void... params) {
        return ImageUtil.saveBitmap(bitmap, filePath, Bitmap.CompressFormat.JPEG, 85);
      }

      @Override
      protected void onPostExecute(Boolean success) {
        if (success) {
          Intent intent = new Intent();
          intent.putExtra(RESULT_PATH, filePath);
          setResult(RESULT_OK, intent);
        }
        finish();
      }
    }.execute();
  }
}
