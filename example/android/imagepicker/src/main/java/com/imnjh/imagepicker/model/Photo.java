package com.imnjh.imagepicker.model;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

/**
 * Created by Martin on 2017/1/17.
 */
public class Photo implements Parcelable {

  private final long id;
  private final String displayName;
  private final String filePath;


  public Photo(long id, String displayName, String filePath) {
    this.id = id;
    this.displayName = displayName;
    this.filePath = filePath;
  }


  public long getId() {
    return id;
  }

  public Uri buildContentUri() {
    return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
  }

  public String getFilePath() {
    return filePath;
  }

  public static Photo fromCursor(Cursor cursor) {
    return new Photo(cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.id);
    dest.writeString(this.displayName);
    dest.writeString(this.filePath);
  }

  protected Photo(Parcel in) {
    this.id = in.readLong();
    this.displayName = in.readString();
    this.filePath = in.readString();
  }

  public static final Creator<Photo> CREATOR = new Creator<Photo>() {
    @Override
    public Photo createFromParcel(Parcel source) {
      return new Photo(source);
    }

    @Override
    public Photo[] newArray(int size) {
      return new Photo[size];
    }
  };
}
