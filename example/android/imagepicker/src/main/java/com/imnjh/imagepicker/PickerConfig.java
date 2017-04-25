package com.imnjh.imagepicker;

import android.content.Context;


/**
 * Created by Martin on 2017/1/17.
 */

public class PickerConfig {

  private ImageLoader imageLoader;
  private Context appContext;
  private int toolbarColor;

  private PickerConfig(Builder builder) {
    this.imageLoader = builder.imageLoader;
    this.appContext = builder.context;
    this.toolbarColor = builder.toolbarColor;
  }

  public ImageLoader getImageLoader() {
    return imageLoader;
  }

  public Context getAppContext() {
    return appContext;
  }

  public int getToolbarColor() {
    return toolbarColor;
  }

  public static class Builder {

    private ImageLoader imageLoader;
    private Context context;
    private int toolbarColor;

    public Builder() {}

    public Builder setAppContext(Context context) {
      this.context = context;
      return this;
    }

    public Builder setImageLoader(ImageLoader imageLoader) {
      this.imageLoader = imageLoader;
      return this;
    }

    public Builder setToolbaseColor(int color) {
      this.toolbarColor = color;
      return this;
    }

    public PickerConfig build() {
      return new PickerConfig(this);
    }
  }


}
