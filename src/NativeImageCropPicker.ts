import { TurboModule, TurboModuleRegistry } from "react-native";

export type PickerResponse = {
  path: string;
  localIdentifier?: string;
  sourceURL?: string;
  filename?: string;
  width: number;
  height: number;
  mime: string;
  size: number;
  duration?: number;
  data?: string;
  exif?: {
    [key: string]: string;
  };
  cropRect?: {
    width: number;
    height: number;
    x: number;
    y: number;
  };
  creationDate?: string;
  modificationDate?: string;
};

export type PickerOptions = {
  mediaType?: string;
  multiple?: boolean;
  includeBase64?: boolean;
  includeExif?: boolean;
  cropping?: boolean;
  width?: number;
  height?: number;
  cropperActiveWidgetColor?: string;
  cropperStatusBarLight?: boolean;
  cropperNavigationBarLight?: boolean;
  cropperToolbarColor?: string;
  cropperToolbarTitle?: string;
  cropperToolbarWidgetColor?: string;
  cropperCircleOverlay?: boolean;
  freeStyleCropEnabled?: boolean;
  showCropGuidelines?: boolean;
  showCropFrame?: boolean;
  hideBottomControls?: boolean;
  enableRotationGesture?: boolean;
  disableCropperColorSetters?: boolean;
  useFrontCamera?: boolean;
};

export interface Spec extends TurboModule {
  openPicker(options: PickerOptions): Promise<PickerResponse>;
  openCamera(options: PickerOptions): Promise<PickerResponse>;
  openCropper(options: PickerOptions): Promise<PickerResponse>;
  clean(): Promise<void>;
  cleanSingle(path: string): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>(
  "RNCImageCropPicker"
) as Spec;
