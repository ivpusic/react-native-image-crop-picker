package com.reactnative.picker;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by ipusic on 5/16/16.
 */
public class PickerModule extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final int IMAGE_PICKER_REQUEST = 1;
    private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";

    private static final String E_PICKER_CANCELLED_KEY = "picker_cancel";
    private static final String E_PICKER_CANCELLED_MSG = "User cancelled image selection";

    private static final String E_FAILED_TO_SHOW_PICKER = "E_FAILED_TO_SHOW_PICKER";
    private static final String E_NO_IMAGE_DATA_FOUND = "E_NO_IMAGE_DATA_FOUND";

    private Promise mPickerPromise;
    private Activity activity;

    private boolean cropping = false;
    private boolean multiple = false;
    private boolean includeBase64 = false;
    private int width = 100;
    private int height = 100;

    public PickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "ImageCropPicker";
    }

    @ReactMethod
    public void openPicker(final ReadableMap options, final Promise promise) {
        activity = getCurrentActivity();

        if (activity == null) {
            promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
            return;
        }

        multiple = options.hasKey("multiple") && options.getBoolean("multiple");
        includeBase64 = options.hasKey("includeBase64") && options.getBoolean("includeBase64");
        width = options.hasKey("width") ? options.getInt("width") : width;
        height = options.hasKey("height") ? options.getInt("height") : height;
        cropping = options.hasKey("cropping") ? options.getBoolean("cropping") : cropping;

        // Store the promise to resolve/reject when picker returns data
        mPickerPromise = promise;

        try {
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Pick an image");
            activity.startActivityForResult(chooserIntent, IMAGE_PICKER_REQUEST);
        } catch (Exception e) {
            mPickerPromise.reject(E_FAILED_TO_SHOW_PICKER, e);
            mPickerPromise = null;
        }
    }

    private String getBase64StringFromFile(String absoluteFilePath) {
      InputStream inputStream = null;
      try {
        inputStream = new FileInputStream(new File(absoluteFilePath));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

      byte[] bytes;
      byte[] buffer = new byte[8192];
      int bytesRead;
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try {
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          output.write(buffer, 0, bytesRead);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      bytes = output.toByteArray();
      return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private WritableMap getImage(Uri uri, boolean resolvePath) {
        WritableMap image = new WritableNativeMap();
        String path = uri.getPath();

        if (resolvePath) {
            path =  RealPathUtil.getRealPathFromURI(activity, uri);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        long fileLen = 0;
        if (path != null) {
            fileLen = new File(path).length();
        }

        BitmapFactory.decodeFile(path, options);
        image.putString("path", "file://" + path);
        image.putInt("width", options.outWidth);
        image.putInt("height", options.outHeight);
        image.putString("mime", options.outMimeType);
        image.putInt("size", (int)fileLen);

        if (includeBase64) {
          String data = getBase64StringFromFile(path);

          image.putString("data", data);
        }

        return image;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == IMAGE_PICKER_REQUEST) {
            if (mPickerPromise != null) {
                if (resultCode == Activity.RESULT_CANCELED) {
                    mPickerPromise.reject(E_PICKER_CANCELLED_KEY, E_PICKER_CANCELLED_MSG);
                } else if (resultCode == Activity.RESULT_OK) {
                    if (multiple) {
                        ClipData clipData = data.getClipData();
                        WritableArray result = new WritableNativeArray();

                        // only one image selected
                        if (clipData == null) {
                            result.pushMap(getImage(data.getData(), true));
                        } else {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                result.pushMap(getImage(clipData.getItemAt(i).getUri(), true));
                            }
                        }

                        mPickerPromise.resolve(result);
                        mPickerPromise = null;
                    } else {
                        Uri uri = data.getData();

                        if (cropping) {
                            UCrop.Options options = new UCrop.Options();
                            options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

                            UCrop.of(uri, Uri.fromFile(new File(activity.getCacheDir(), UUID.randomUUID().toString() + ".jpg")))
                                    .withMaxResultSize(width, height)
                                    .withAspectRatio(width, height)
                                    .withOptions(options)
                                    .start(activity);
                        } else {
                            mPickerPromise.resolve(getImage(uri, true));
                            mPickerPromise = null;
                        }
                    }
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (data != null) {
                final Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    mPickerPromise.resolve(getImage(resultUri, false));
                } else {
                    mPickerPromise.reject(E_NO_IMAGE_DATA_FOUND, "Cannot find image data");
                }
            } else {
                mPickerPromise.reject(E_PICKER_CANCELLED_KEY, E_PICKER_CANCELLED_MSG);
            }

            mPickerPromise = null;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
    }
}
