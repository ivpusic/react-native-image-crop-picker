package com.reactnative.ivpusic.imagepicker;

import android.net.Uri;

import java.util.ArrayList;

/**
 * Created by Martin on 2017/2/11.
 * niejunhong@panda.tv
 */

public interface PhotoLoadListener {
    void onLoadComplete(ArrayList<Uri> photoUris);

    void onLoadError();
}
