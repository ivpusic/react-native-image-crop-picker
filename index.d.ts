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
        showCropGuidelines?: boolean;
        hideBottomControls?: boolean;
        enableRotationGesture?: boolean;
        cropperCancelText?: string;
        cropperChooseText?: string;
        writeTempFile: boolean;
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

    }

    export interface CropRect {
        x: number;
        y: number;
        width: number;
        height: number;
    }

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
