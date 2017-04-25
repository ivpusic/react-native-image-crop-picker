package com.imnjh.imagepicker.control;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;

/**
 * Created by Martin on 2017/1/17.
 */
public abstract class BaseLoaderController implements LoaderManager.LoaderCallbacks<Cursor> {

  protected static final int PHOTO_LOADER_ID = 1;
  protected static final int ALBUM_LOADER_ID = 2;

  protected Context context;

  protected LoaderManager loaderManager;

  protected void onCreate(Activity activity) {
    context = activity;
    loaderManager = activity.getLoaderManager();
  }

  public void onDestroy() {
    loaderManager.destroyLoader(getLoaderId());
  }

  protected abstract int getLoaderId();

}
