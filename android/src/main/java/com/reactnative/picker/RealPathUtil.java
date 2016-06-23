package com.reactnative.picker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

/**
 * Created by ipusic on 5/18/16.
 */
public class RealPathUtil {

    public static String getRealPathFromURI(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT < 19)
            return RealPathUtil.getRealPathFromURI_API11to18(context, uri);
        else
            return RealPathUtil.getRealPathFromURI_API19(context, uri);
    }

    @SuppressLint("NewApi")
    private static String getRealPathFromURI_API19(Context context, Uri uri) {
      // DocumentProvider
      if (DocumentsContract.isDocumentUri(context, uri)) {
          // ExternalStorageProvider
          if (isExternalStorageDocument(uri)) {
              final String docId = DocumentsContract.getDocumentId(uri);
              final String[] split = docId.split(":");
              final String type = split[0];

              if ("primary".equalsIgnoreCase(type)) {
                  return Environment.getExternalStorageDirectory() + "/" + split[1];
              }

              // TODO handle non-primary volumes
          }
          // DownloadsProvider
          else if (isDownloadsDocument(uri)) {

              final String id = DocumentsContract.getDocumentId(uri);
              final Uri contentUri = ContentUris.withAppendedId(
                      Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

              return getDataColumn(context, contentUri, null, null);
          }
          // MediaProvider
          else if (isMediaDocument(uri)) {
              final String docId = DocumentsContract.getDocumentId(uri);
              final String[] split = docId.split(":");
              final String type = split[0];

              Uri contentUri = null;
              if ("image".equals(type)) {
                  contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
              } else if ("video".equals(type)) {
                  contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
              } else if ("audio".equals(type)) {
                  contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
              }

              final String selection = "_id=?";
              final String[] selectionArgs = new String[] {
                      split[1]
              };

              return getDataColumn(context, contentUri, selection, selectionArgs);
          }
      }
      // MediaStore (and general)
      else if ("content".equalsIgnoreCase(uri.getScheme())) {

          // Return the remote address
          if (isGooglePhotosUri(uri))
              return uri.getLastPathSegment();

          return getDataColumn(context, uri, null, null);
      }
      // File
      else if ("file".equalsIgnoreCase(uri.getScheme())) {
          return uri.getPath();
      }

      return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    private static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }
}
