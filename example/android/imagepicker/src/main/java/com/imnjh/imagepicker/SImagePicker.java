package com.imnjh.imagepicker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.imnjh.imagepicker.activity.CropImageActivity;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.imnjh.imagepicker.util.SystemUtil;


/**
 * Created by Martin on 2017/1/17.
 */
public class SImagePicker {

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({MODE_IMAGE, MODE_AVATAR})
  public @interface PickMode {}

  public static final int MODE_IMAGE = 1;
  public static final int MODE_AVATAR = 2;

  private static PickerConfig pickerConfig;

  private Fragment fragment;
  private Activity activity;
  private int maxCount = 1;
  private int pickMode = MODE_IMAGE;
  private int rowCount = 4;
  private boolean showCamera = false;
  private String avatarFilePath;
  private ArrayList<String> selected;
  private @StringRes int pickRes = R.string.general_send;
  private FileChooseInterceptor fileChooseInterceptor;

  private SImagePicker(Fragment fragment) {
    this.fragment = fragment;
  }

  private SImagePicker(Activity activity) {
    this.activity = activity;
  }

  public static SImagePicker from(Fragment fragment) {
    return new SImagePicker(fragment);
  }

  public static SImagePicker from(Activity activity) {
    return new SImagePicker(activity);
  }

  public static void init(PickerConfig config) {
    pickerConfig = config;
    SystemUtil.init(config.getAppContext());
  }

  public static PickerConfig getPickerConfig() {
    if (pickerConfig == null) {
      throw new IllegalArgumentException("you must call init() first");
    }
    return pickerConfig;
  }

  public SImagePicker maxCount(int maxCount) {
    this.maxCount = maxCount;
    return this;
  }

  public SImagePicker rowCount(int rowCount) {
    this.rowCount = rowCount;
    return this;
  }

  public SImagePicker setSelected(ArrayList<String> selected) {
    this.selected = selected;
    return this;
  }

  public SImagePicker pickMode(@PickMode int mode) {
    this.pickMode = mode;
    return this;
  }

  public SImagePicker cropFilePath(String filePath) {
    this.avatarFilePath = filePath;
    return this;
  }

  public SImagePicker showCamera(boolean showCamera) {
    this.showCamera = showCamera;
    return this;
  }

  public SImagePicker pickText(@StringRes int pick) {
    this.pickRes = pick;
    return this;
  }

  public SImagePicker fileInterceptor(FileChooseInterceptor fileChooseInterceptor) {
    this.fileChooseInterceptor = fileChooseInterceptor;
    return this;
  }

  public void forResult(int requestCode) {
    if (pickerConfig == null) {
      throw new IllegalArgumentException("you must call init() first");
    }
    Intent intent = new Intent();
    intent.putExtra(PhotoPickerActivity.PARAM_MAX_COUNT, maxCount);
    intent.putExtra(PhotoPickerActivity.PARAM_MODE, pickMode);
    intent.putExtra(PhotoPickerActivity.PARAM_SELECTED, selected);
    intent.putExtra(PhotoPickerActivity.PARAM_ROW_COUNT, rowCount);
    intent.putExtra(PhotoPickerActivity.PARAM_SHOW_CAMERA, showCamera);
    intent.putExtra(PhotoPickerActivity.PARAM_CUSTOM_PICK_TEXT_RES, pickRes);
    intent.putExtra(PhotoPickerActivity.PARAM_FILE_CHOOSE_INTERCEPTOR, fileChooseInterceptor);
    intent.putExtra(CropImageActivity.PARAM_AVATAR_PATH, avatarFilePath);
    if (activity != null) {
      intent.setClass(activity, PhotoPickerActivity.class);
      activity.startActivityForResult(intent, requestCode);
    } else if (fragment != null) {
      intent.setClass(fragment.getActivity(), PhotoPickerActivity.class);
      fragment.startActivityForResult(intent, requestCode);
    } else {
      throw new IllegalArgumentException("you must call from() first");
    }
  }

}
