import React from 'react';

import {NativeModules} from 'react-native';

const ImageCropPicker = NativeModules.ImageCropPicker;

const nativeOpenPicker = ImageCropPicker.openPicker;
ImageCropPicker.openPicker = options => {
  return nativeOpenPicker(options).then(res => {
    (Array.isArray(res) ? res : [res]).forEach(image => {
      const gps = image?.exif?.["{GPS}"];
      if (gps?.Latitude && gps?.Longitude) {
        image.exif.Latitude = gps.LatitudeRef === 'S' ? -gps.Latitude : gps.Latitude;
        image.exif.Longitude = gps.LongitudeRef === 'W' ? -gps.Longitude : gps.Longitude;
      }
    });
    return res;
  });
}

export default ImageCropPicker;
export const openPicker = ImageCropPicker.openPicker;
export const openCamera = ImageCropPicker.openCamera;
export const openCropper = ImageCropPicker.openCropper;
export const clean = ImageCropPicker.clean;
export const cleanSingle = ImageCropPicker.cleanSingle;
