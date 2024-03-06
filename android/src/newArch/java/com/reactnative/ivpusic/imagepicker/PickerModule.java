package com.reactnative.ivpusic.imagepicker;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = ImageCropPicker.NAME)
public class PickerModule extends NativeImageCropPickerSpec {
    private final ImageCropPicker picker;

    public PickerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        picker = new ImageCropPicker(reactContext);
    }

    @Override
    public void openPicker(ReadableMap options, Promise promise) {
        picker.openPicker(options, promise);
    }

    @Override
    public void openCamera(ReadableMap options, Promise promise) {
        picker.openCamera(options, promise);
    }

    @Override
    public void openCropper(ReadableMap options, Promise promise) {
        picker.openCropper(options, promise);
    }

    @Override
    public void clean(Promise promise) {
        picker.clean(promise);
    }

    @Override
    public void cleanSingle(String path, Promise promise) {
        picker.cleanSingle(path, promise);
    }
}