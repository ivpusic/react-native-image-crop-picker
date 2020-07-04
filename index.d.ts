declare module "react-native-image-crop-picker" {
    export interface Options {
        cropping?: boolean;
        width?: number;
        height?: number;
        multiple?: boolean;
        path?: string;
        includeBase64?: boolean;
        includeExif?: boolean;
        avoidEmptySpaceAroundImage?: boolean;
        cropperActiveWidgetColor?: string;
        cropperStatusBarColor?: string;
        cropperToolbarColor?: string;
        cropperToolbarTitle?: string;
        cropperToolbarWidgetColor?: string;
        freeStyleCropEnabled?: boolean;
        cropperTintColor?: string;
        cropperCircleOverlay?: boolean;
        disableCropperColorSetters?: boolean;
        maxFiles?: number;
        waitAnimationEnd?: boolean;
        smartAlbums?: string[];
        useFrontCamera?: boolean;
        compressVideoPreset?: string;
        compressImageMaxWidth?: number;
        compressImageMaxHeight?: number;
        compressImageQuality?: number;
        loadingLabelText?: string;
        mediaType?: 'photo' | 'video' | 'any';
        showsSelectedCount?: boolean;
        forceJpg?: boolean;
        sortOrder?: 'none' | 'asc' | 'desc';
        showCropGuidelines?: boolean;
        hideBottomControls?: boolean;
        enableRotationGesture?: boolean;
        cropperCancelText?: string;
        cropperChooseText?: string;
        writeTempFile?: boolean;
    }

    export interface Image {
        path: string;
        size: number;
        data: null | string;
        width: number;
        height: number;
        mime: string;
        exif: null | object;
        cropRect: null | CropRect;
        filename: string;
        creationDate: string;
        modificationDate?: string;
        duration: null | number;
    }

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

    export function openPicker(options: Options): Promise<Image | Image[]>;
    export function openCamera(options: Options): Promise<Image | Image[]>;
    export function openCropper(options: Options): Promise<Image>;
    export function clean(): Promise<void>;
    export function cleanSingle(path: string): Promise<void>;

    export interface ImageCropPicker {
        openPicker(options: Options): Promise<Image | Image[]>;
        openCamera(options: Options): Promise<Image | Image[]>;
        openCropper(options: Options): Promise<Image>;
        clean(): Promise<void>;
        cleanSingle(path: string): Promise<void>;
    }

    const ImageCropPicker: ImageCropPicker;

    export default ImageCropPicker;
}
