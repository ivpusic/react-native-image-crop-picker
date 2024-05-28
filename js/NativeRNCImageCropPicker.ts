import type { TurboModule } from "react-native/library/TurboModule/RCTExport";
import { TurboModuleRegistry } from "react-native";

type ImageOrVideo = Image | Video;
type SmartAlbums = | 'Regular' | 'SyncedEvent' | 'SyncedFaces';
type CompressVideoPresets = | 'LowQuality' | 'MediumQuality' | 'HighestQuality' | 'Passthrough';
type Options = AnyOptions | VideoOptions | ImageOptions;
type MediaType = 'photo' | 'video' | 'any';
type AnyOptions = Omit<ImageOptions, 'mediaType'> & Omit<VideoOptions, 'mediaType'> & {
  mediaType?: 'any';
}
type VideoOptions = CommonOptions & {
  mediaType: 'video';
  compressVideoPreset?: CompressVideoPresets;
}

type CropperOptions = ImageOptions & {
  path: string;
}

type ImageOptions = CommonOptions & {
  mediaType: MediaType;
  width?: number;
  height?: number;
  includeBase64?: boolean;
  includeExif?: boolean;
  forceJpg?: boolean;
  cropping?: boolean;
  avoidEmptySpaceAroundImage?: boolean;
  cropperActiveWidgetColor?: string;
  cropperStatusBarColor?: string;
  cropperToolbarColor?: string;
  cropperToolbarWidgetColor?: string;
  cropperToolbarTitle?: string;
  freeStyleCropEnabled?: boolean;
  cropperTintColor?: string;
  cropperCircleOverlay?: boolean;
  cropperCancelText?: string;
  cropperCancelColor?: string;
  cropperChooseText?: string;
  cropperChooseColor?: string;
  cropperRotateButtonHidden?: boolean
  showCropGuidelines?: boolean;
  showCropFrame?: boolean;
  enableRotationGesture?: boolean;
  disableCropperColorSetters?: boolean;
  compressImageMaxWidth?: number;
  compressImageMaxHeight?: number;
  compressImageQuality?: number;
}

export interface CommonOptions {
  multiple?: boolean;
  minFiles?: number;
  maxFiles?: number;
  waitAnimationEnd?: boolean;
  smartAlbums?: SmartAlbums[];
  useFrontCamera?: boolean;
  loadingLabelText?: string;
  showsSelectedCount?: boolean;
  sortOrder?: 'none' | 'asc' | 'desc';
  hideBottomControls?: boolean;
  writeTempFile?: boolean;
}

export interface CropRect {
  x: number;
  y: number;
  width: number;
  height: number;
}

interface ImageVideoCommon {
  path: string;
  size: number;
  width: number;
  height: number;
  mime: string;
  exif?: Exif;
  localIdentifier?: string;
  sourceURL?: string;
  filename?: string;
  creationDate?: string;
  modificationDate?: string;
}

export interface Exif {

}

interface Image extends ImageVideoCommon {
  data?: string | null;
  cropRect?: CropRect | null;
}

interface Video extends ImageVideoCommon {
  duration: number | null;
}

export interface Spec extends TurboModule {
  openPicker(options: Options): Promise<Video[] | Video | ImageOrVideo[] | ImageOrVideo | Image[] | Image>;
  openCamera(options: Options): Promise<Video[] | Video | ImageOrVideo[] | ImageOrVideo | Image[] | Image>;
  openCropper(options: CropperOptions): Promise<Image>;
  clean(): Promise<void>;
  cleanSingle(path: string): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ImageCropPicker')