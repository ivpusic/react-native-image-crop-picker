import React from 'react';

import {NativeModules} from 'react-native';

const ImageCropPicker = NativeModules.ImageCropPicker;

export default ImageCropPicker;
export const openPicker = ImageCropPicker.openPicker;
export const openCamera = ImageCropPicker.openCamera;
export const openCropper = ImageCropPicker.openCropper;
export const clean = ImageCropPicker.clean;
export const cleanSingle = ImageCropPicker.cleanSingle;
