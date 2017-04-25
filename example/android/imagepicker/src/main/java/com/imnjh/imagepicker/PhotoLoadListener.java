package com.imnjh.imagepicker;

import java.util.ArrayList;

import android.net.Uri;

/**
 * Created by Martin on 2017/2/11.
 * niejunhong@panda.tv
 */

public interface PhotoLoadListener {
  void onLoadComplete(ArrayList<Uri> photoUris);

  void onLoadError();
}
