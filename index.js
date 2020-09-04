import {NativeModules} from 'react-native';
export default NativeModules.ImageCropPicker;

export const isCancel = error =>
  Boolean(error && error.code === "E_PICKER_CANCELLED");
