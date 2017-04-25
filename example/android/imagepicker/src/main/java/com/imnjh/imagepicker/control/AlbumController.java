package com.imnjh.imagepicker.control;

import android.app.Activity;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;

import com.imnjh.imagepicker.adapter.AlbumAdapter;
import com.imnjh.imagepicker.loader.AlbumLoader;
import com.imnjh.imagepicker.model.Album;


/**
 * Created by Martin on 2017/1/17.
 */
public class AlbumController extends BaseLoaderController
    implements AdapterView.OnItemSelectedListener {

  private AlbumAdapter albumAdapter;
  private OnDirectorySelectListener directorySelectListener;

  public void onCreate(Activity activity, AppCompatSpinner spinner,
      OnDirectorySelectListener directorySelectListener) {
    super.onCreate(activity);
    this.albumAdapter = new AlbumAdapter(activity, null);
    this.directorySelectListener = directorySelectListener;
    spinner.setAdapter(albumAdapter);
    spinner.setOnItemSelectedListener(this);
  }

  @Override
  protected int getLoaderId() {
    return ALBUM_LOADER_ID;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return AlbumLoader.newInstance(context);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    albumAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    albumAdapter.swapCursor(null);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }


  public void loadAlbums() {
    loaderManager.initLoader(getLoaderId(), null, this);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    if (directorySelectListener != null) {
      Cursor cursor = (Cursor) parent.getItemAtPosition(position);
      Album album = Album.valueOf(cursor);
      directorySelectListener.onSelect(album);
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }


  public interface OnDirectorySelectListener {
    void onSelect(Album album);

    void onReset(Album album);
  }

}
