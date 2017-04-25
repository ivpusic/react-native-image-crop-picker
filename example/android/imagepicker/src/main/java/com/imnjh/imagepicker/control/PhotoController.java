package com.imnjh.imagepicker.control;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.imnjh.imagepicker.CapturePhotoHelper;
import com.imnjh.imagepicker.PhotoLoadListener;
import com.imnjh.imagepicker.R;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.adapter.CommonHeaderFooterAdapter;
import com.imnjh.imagepicker.adapter.PhotoAdapter;
import com.imnjh.imagepicker.loader.PhotoLoader;
import com.imnjh.imagepicker.model.Album;

/**
 * Created by Martin on 2017/1/17.
 */
public class PhotoController extends BaseLoaderController {

  private PhotoAdapter photoAdapter;
  private static final String ARGS_ALBUM = "ARGS_ALBUM";


  public void onCreate(@NonNull Activity context, @NonNull RecyclerView recyclerView,
      PhotoAdapter.OnPhotoActionListener selectionChangeListener, int maxCount) {
    this.onCreate(context, recyclerView, selectionChangeListener, maxCount, 4,
        SImagePicker.MODE_IMAGE);
  }

  public void onCreate(@NonNull Activity context, @NonNull RecyclerView recyclerView,
      PhotoAdapter.OnPhotoActionListener selectionChangeListener, int maxCount, int rowCount,
      int mode) {
    this.onCreate(context, recyclerView, selectionChangeListener, maxCount, rowCount, mode, null);
  }

  public void onCreate(@NonNull Activity context, @NonNull RecyclerView recyclerView,
      PhotoAdapter.OnPhotoActionListener selectionChangeListener, int maxCount, int rowCount,
      int mode, final CapturePhotoHelper capturePhotoHelper) {
    super.onCreate(context);
    photoAdapter = new PhotoAdapter(context, null, mode, rowCount);
    photoAdapter.setActionListener(selectionChangeListener);
    photoAdapter.setMaxCount(maxCount);

    if (capturePhotoHelper == null) {
      recyclerView.setAdapter(photoAdapter);
    } else {
      CommonHeaderFooterAdapter headerFooterAdapter = new CommonHeaderFooterAdapter();
      RecyclerView.ViewHolder holder =
          new RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(
              R.layout.item_picker_capture, recyclerView, false)) {};
      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          capturePhotoHelper.capture();
        }
      });
      headerFooterAdapter.addHeaderView(holder.itemView);
      headerFooterAdapter.setAdapter(photoAdapter);
      recyclerView.setAdapter(headerFooterAdapter);
    }
  }

  @Override
  protected int getLoaderId() {
    return PHOTO_LOADER_ID;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Album album = args.getParcelable(ARGS_ALBUM);
    if (album == null) {
      return null;
    }
    return PhotoLoader.newInstance(context, album);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    photoAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    photoAdapter.swapCursor(null);
  }

  /**
   * @param album
   */
  public void load(Album album) {
    Bundle args = new Bundle();
    args.putParcelable(ARGS_ALBUM, album);
    loaderManager.initLoader(getLoaderId(), args, this);
  }

  /**
   * 
   */
  public void loadAllPhoto(Context context) {
    Album album = new Album(Album.ALBUM_ID_ALL, -1,
        context.getString(Album.ALBUM_NAME_ALL_RES_ID), 0);
    load(album);
  }


  /**
   * restartLoader will cancel, stop and destroy the loader (and close the
   * underlying data source like a cursor) and create a new loader(which would also create a new
   * cursor and re-run the query if the loader is a CursorLoader).
   * 
   * @param target
   */
  public void resetLoad(Album target) {
    Bundle args = new Bundle();
    args.putParcelable(ARGS_ALBUM, target);
    loaderManager.restartLoader(getLoaderId(), args, this);
  }

  public ArrayList<String> getSelectedPhoto() {
    return photoAdapter.getSelectedPhoto();
  }

  public void setSelectedPhoto(ArrayList<String> selectedPhoto) {
    photoAdapter.setSelectedPhoto(selectedPhoto);
  }

  public void getAllPhoto(PhotoLoadListener photoLoadListener) {
    photoAdapter.getAllPhoto(photoLoadListener);
  }
}
