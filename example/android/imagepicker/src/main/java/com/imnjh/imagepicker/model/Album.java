package com.imnjh.imagepicker.model;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.StringRes;

import com.imnjh.imagepicker.R;


/**
 * Created by Martin on 2017/1/17.
 */
public class Album implements Parcelable {

  public static final String ALBUM_ID_ALL = String.valueOf(-1);
  public @StringRes static final int ALBUM_NAME_ALL_RES_ID = R.string.general_all_pictures;

  private final String id;
  private final long coverId;
  private final String displayName;
  private final long count;


  public Album(String id, long coverId, String albumName, long count) {
    this.id = id;
    this.coverId = coverId;
    this.displayName = albumName;
    this.count = count;
  }

  public static Album valueOf(Cursor cursor) {
    return new Album(
        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)),
        cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID)),
        cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)),
        cursor.getLong(3));
  }


  public String getId() {
    return id;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeLong(this.coverId);
    dest.writeString(this.displayName);
    dest.writeLong(this.count);
  }

  protected Album(Parcel in) {
    this.id = in.readString();
    this.coverId = in.readLong();
    this.displayName = in.readString();
    this.count = in.readLong();
  }

  public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
    @Override
    public Album createFromParcel(Parcel source) {
      return new Album(source);
    }

    @Override
    public Album[] newArray(int size) {
      return new Album[size];
    }
  };

  public boolean isAll() {
    return ALBUM_ID_ALL.equals(id);
  }

  public Uri buildCoverUri() {
    return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, coverId);
  }

  public String getDisplayName() {
    return displayName;
  }

  public long getCount() {
    return count;
  }
}
