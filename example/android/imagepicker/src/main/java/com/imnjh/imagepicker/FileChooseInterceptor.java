package com.imnjh.imagepicker;

import java.util.ArrayList;

import android.content.Context;
import android.os.Parcelable;

/**
 * Created by Martin on 2017/1/17.
 */
public interface FileChooseInterceptor extends Parcelable {
  boolean onFileChosen(Context context, ArrayList<String> selectedPic, boolean original,
      int resultCode, PickerAction action);
}
