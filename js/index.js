import React from 'react';
import ImageCropPicker from './NativeRNCImageCropPicker';

const ImagePicker = {
    async openPicker(options) {
        const res = await ImageCropPicker.openPicker(options);
        return res
    },
    async openCamera(options) {
        const res = await ImageCropPicker.openCamera(options);
        return res
    },
    async openCropper(options) {
        const res = await ImageCropPicker.openCropper(options);
        return res
    },
    async cleanSingle(path) {
        ImageCropPicker.cleanSingle(path);
    },
    async clean() {
        ImageCropPicker.clean();
    },
}

export default ImagePicker;

export const openPicker = ImagePicker.openPicker;
export const openCamera = ImagePicker.openCamera;
export const openCropper = ImagePicker.openCropper;
export const clean = ImagePicker.clean;
export const cleanSingle = ImagePicker.cleanSingle;