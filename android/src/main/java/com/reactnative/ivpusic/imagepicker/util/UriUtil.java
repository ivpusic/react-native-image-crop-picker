package com.reactnative.ivpusic.imagepicker.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 2017/1/17.
 */

public class UriUtil {
  public static final String HTTP_SCHEME = "http";
  public static final String HTTPS_SCHEME = "https";
  public static final String LOCAL_FILE_SCHEME = "file";
  public static final String LOCAL_CONTENT_SCHEME = "content";
  private static final String LOCAL_CONTACT_IMAGE_PREFIX;
  public static final String LOCAL_ASSET_SCHEME = "asset";
  public static final String LOCAL_RESOURCE_SCHEME = "res";
  public static final String DATA_SCHEME = "data";

  public UriUtil() {}

  public static boolean isNetworkUri(@Nullable Uri uri) {
    String scheme = getSchemeOrNull(uri);
    return "https".equals(scheme) || "http".equals(scheme);
  }

  public static boolean isLocalFileUri(@Nullable Uri uri) {
    String scheme = getSchemeOrNull(uri);
    return "file".equals(scheme);
  }

  public static boolean isLocalContentUri(@Nullable Uri uri) {
    String scheme = getSchemeOrNull(uri);
    return "content".equals(scheme);
  }

  public static boolean isLocalContactUri(Uri uri) {
    return isLocalContentUri(uri) && "com.android.contacts".equals(uri.getAuthority())
        && !uri.getPath().startsWith(LOCAL_CONTACT_IMAGE_PREFIX);
  }

  public static boolean isLocalCameraUri(Uri uri) {
    String uriString = uri.toString();
    return uriString.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
        || uriString.startsWith(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
  }

  public static boolean isLocalAssetUri(@Nullable Uri uri) {
    String scheme = getSchemeOrNull(uri);
    return "asset".equals(scheme);
  }

  public static boolean isLocalResourceUri(@Nullable Uri uri) {
    String scheme = getSchemeOrNull(uri);
    return "res".equals(scheme);
  }

  public static boolean isDataUri(@Nullable Uri uri) {
    return "data".equals(getSchemeOrNull(uri));
  }

  @Nullable
  public static String getSchemeOrNull(@Nullable Uri uri) {
    return uri == null ? null : uri.getScheme();
  }

  public static Uri parseUriOrNull(@Nullable String uriAsString) {
    return uriAsString != null ? Uri.parse(uriAsString) : null;
  }

  static {
    LOCAL_CONTACT_IMAGE_PREFIX =
        Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "display_photo").getPath();
  }

  public static ArrayList<Uri> getUris(Context context, List<String> paths) {
    ArrayList<Uri> uris = new ArrayList<>();
    for (String path : paths) {
      Uri uri = pathToUri(context, Uri.fromFile(new File(path)));
      uris.add(uri);
      Log.e("URI :", uri.toString());
    }
    return uris;
  }

  private static Uri pathToUri(Context context, Uri uri){
    String path = uri.getEncodedPath();
    if (path != null) {
      path = Uri.decode(path);
      ContentResolver cr = context.getContentResolver();
      StringBuffer buff = new StringBuffer();
      buff.append("(")
              .append(MediaStore.Images.ImageColumns.DATA)
              .append("=")
              .append("'" + path + "'")
              .append(")");
      Cursor cur = cr.query(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
              new String[] { MediaStore.Images.ImageColumns._ID },
              buff.toString(), null, null);
      int index = 0;
      for (cur.moveToFirst(); !cur.isAfterLast(); cur
              .moveToNext()) {
        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
        index = cur.getInt(index);
      }
      if (index == 0) {
      } else {
        Uri uri_temp = Uri
                .parse("content://media/external/images/media/"
                        + index);
        if (uri_temp != null) {
          uri = uri_temp;
        }
      }
    }
    return uri;
  }

  //通过Uri获取图片的实际存储路径
  public static String getRealFilePath( final Context context, final Uri uri ) {
    if ( null == uri ) return null;
    final String scheme = uri.getScheme();
    String data = null;
    if ( scheme == null )
      data = uri.getPath();
    else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
      data = uri.getPath();
    } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
      Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
      if ( null != cursor ) {
        if ( cursor.moveToFirst() ) {
          int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
          if ( index > -1 ) {
            data = cursor.getString( index );
          }
        }
        cursor.close();
      }
    }
    return data;
  }
}
