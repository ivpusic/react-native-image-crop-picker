package com.reactnative.ivpusic.imagepicker;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

public class PickerModule extends ReactContextBaseJavaModule {
    private final ImageCropPicker picker;

    public PickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        picker = new ImageCropPicker(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return ImageCropPicker.NAME;
    }

    @ReactMethod
    public void openPicker(ReadableMap options, Promise promise) {
        picker.openPicker(options, promise);
    }

    @ReactMethod
    public void openCamera(ReadableMap options, Promise promise) {
        picker.openCamera(options, promise);
    }

    @ReactMethod
    public void openCropper(ReadableMap options, Promise promise) {
        picker.openCropper(options, promise);
    }

    @ReactMethod
    public void clean(Promise promise) {
        picker.clean(promise);
    }

    @ReactMethod
    public void cleanSingle(String path, Promise promise) {
        picker.cleanSingle(path, promise);
    }
}