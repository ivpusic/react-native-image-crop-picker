package com.imnjh.imagepicker;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

/**
 * Created by Martin on 2017/1/17.
 */

public interface ImageLoader {
  void bindImage(ImageView imageView, Uri uri, int width, int height);

  void bindImage(ImageView imageView, Uri uri);


  ImageView createImageView(Context context);

  ImageView createFakeImageView(Context context);
}
