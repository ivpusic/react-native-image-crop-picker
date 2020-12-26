import React from 'react';

import {NativeModules, processColor} from 'react-native';

const { ImageCropPicker } = NativeModules;

const processColors = (options) => {
if (options.cropperToolbarWidgetColor) {
    options.cropperToolbarWidgetColor = processColor(options.cropperToolbarWidgetColor)
  }

  if (options.cropperStatusBarColor) {
    options.cropperStatusBarColor = processColor(options.cropperStatusBarColor);
  }

  if (options.cropperToolbarColor) {
    options.cropperToolbarColor = processColor(options.cropperToolbarColor);
  }

  if (options.cropperActiveWidgetColor) {
    options.cropperActiveWidgetColor = processColor(options.cropperActiveWidgetColor);
  }

  if (options.cropperChooseTextColor) {
    options.cropperChooseTextColor = processColor(options.cropperChooseTextColor);
  }

  if (options.cropperCancelTextColor) {
    options.cropperCancelTextColor = processColor(options.cropperCancelTextColor);
  }

  if (options.cropperToolbarButtonsColor) {
    options.cropperToolbarButtonsColor = processColor(options.cropperToolbarButtonsColor);
  }

  return options;
}

const openPicker = (options) => {
  options = processColors(options);
  return ImageCropPicker.openPicker(options);
}

const openCamera = (options) => {
  options = processColors(options);
  return ImageCropPicker.openCamera(options);
}

const RNImageCropPicker = {
  ...ImageCropPicker,
  openCamera,
  openPicker
}

export default RNImageCropPicker;
