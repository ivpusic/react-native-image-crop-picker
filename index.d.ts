declare module "react-native-image-crop-picker" {
    /**
     * AVAssetExportPreset presets.
     *
     * @see https://developer.apple.com/documentation/avfoundation/avassetexportsession/export_preset_names_for_quicktime_files_of_a_given_size
     */
    type CompressVideoPresets =
        | '640x480'
        | '960x540'
        | '1280x720'
        | '1920x1080'
        | 'HEVC3840x2160'
        | 'LowQuality'
        | 'MediumQuality'
        | 'HighestQuality'
        | 'Passthrough';

    /**
     * iOS smart album types
     *
     * @see https://developer.apple.com/documentation/photokit/phassetcollectionsubtype
     */
    type SmartAlbums =
        | 'Regular'
        | 'SyncedEvent'
        | 'SyncedFaces'
        | 'SyncedAlbum'
        | 'Imported'
        | 'PhotoStream'
        | 'CloudShared'
        | 'Generic'
        | 'Panoramas'
        | 'Videos'
        | 'Favorites'
        | 'Timelapses'
        | 'AllHidden'
        | 'RecentlyAdded'
        | 'Bursts'
        | 'SlomoVideos'
        | 'UserLibrary'
        | 'Screenshots'
        | 'SelfPortraits'
        /** >= iOS 10.2 */
        | 'DepthEffect'
        /** >= iOS 10.3 */
        | 'LivePhotos'
        /** >= iOS 11 */
        | 'Animated'
        | 'LongExposure';

    export interface CommonOptions {
        multiple?: boolean;
        path?: string;
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

    type ImageOptions = CommonOptions & {
        mediaType: 'photo';
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
        cropperToolbarTitle?: string;
        cropperToolbarWidgetColor?: string;
        freeStyleCropEnabled?: boolean;
        cropperTintColor?: string;
        cropperCircleOverlay?: boolean;
        cropperCancelText?: string;
        cropperChooseText?: string;
        showCropGuidelines?: boolean;
        enableRotationGesture?: boolean;
        disableCropperColorSetters?: boolean;
        compressImageMaxWidth?: number;
        compressImageMaxHeight?: number;
        compressImageQuality?: number;
    }

    type VideoOptions = CommonOptions & {
        mediaType: 'video';
        compressVideoPreset?: CompressVideoPresets;
    };

    type AnyOptions = Omit<ImageOptions, 'mediaType'> & Omit<VideoOptions, 'mediaType'> & {
        mediaType?: 'any';
    };

    export type Options = AnyOptions | VideoOptions | ImageOptions;

    interface ImageVideoCommon {
        path: string;
        size: number;
        data: null | string;
        width: number;
        height: number;
        mime: string;
        exif: null | object;
        filename: string;
        creationDate: string;
        modificationDate?: string;
    }

    export interface Image extends ImageVideoCommon {
        data: null | string;
        cropRect: null | CropRect;
    }

    export interface Video extends ImageVideoCommon {
      /** Video duration in milliseconds */
      duration: null | number;
    }

    export type ImageOrVideo = Image | Video;

    export interface CropRect {
        x: number;
        y: number;
        width: number;
        height: number;
    }

    type PickerErrorCodeCommon =
        | 'E_PICKER_CANCELLED'
        | 'E_NO_IMAGE_DATA_FOUND'
        | 'E_PERMISSION_MISSING'
        | 'E_ERROR_WHILE_CLEANING_FILES';

    type PickerErrorCodeIOS =
        | 'E_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR'
        | 'E_PICKER_NO_CAMERA_PERMISSION'
        | 'E_CROPPER_IMAGE_NOT_FOUND'
        | 'E_CANNOT_SAVE_IMAGE'
        | 'E_CANNOT_PROCESS_VIDEO';

    type PickerErrorCodeAndroid =
        | 'E_ACTIVITY_DOES_NOT_EXIST'
        | 'E_CALLBACK_ERROR'
        | 'E_FAILED_TO_SHOW_PICKER'
        | 'E_FAILED_TO_OPEN_CAMERA'
        | 'E_CAMERA_IS_NOT_AVAILABLE'
        | 'E_CANNOT_LAUNCH_CAMERA';

    export type PickerErrorCode = PickerErrorCodeCommon | PickerErrorCodeIOS | PickerErrorCodeAndroid;

    /** Change return type based on `multiple` property. */
    export type PossibleArray<O, T> = O extends { multiple: true; } ? T[] : T;

    /** Isolate return type based on `mediaType` property. */
    type MediaType<O> =
        O extends { mediaType: 'photo'; } ? Image :
        O extends { mediaType: 'video'; } ? Video :
        ImageOrVideo;

    export function openPicker<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
    export function openCamera<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
    export function openCropper(options: ImageOptions): Promise<Image>;
    export function clean(): Promise<void>;
    export function cleanSingle(path: string): Promise<void>;

    export interface ImageCropPicker {
        openPicker<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
        openCamera<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
        openCropper(options: ImageOptions): Promise<Image>;
        clean(): Promise<void>;
        cleanSingle(path: string): Promise<void>;
    }

    const ImageCropPicker: ImageCropPicker;

    export default ImageCropPicker;
}
