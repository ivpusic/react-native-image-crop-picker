package com.imnjh.imagepicker.util;

import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

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
}
