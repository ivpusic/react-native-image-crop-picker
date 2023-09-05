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
        /**
         * Enable or disable multiple image selection.
         *
         * @default false
         */
        multiple?: boolean;

        /**
         * Min number of files to select when using `multiple` option.
         *
         * @platform iOS only
         * @default 1
         */
        minFiles?: number;

        /**
         * Max number of files to select when using `multiple` option.
         *
         * @platform iOS only
         * @default 5
         */
        maxFiles?: number;

        /**
         * Promise will resolve/reject once ViewController completion block is called.
         *
         * @platform iOS only
         * @default true
         */
        waitAnimationEnd?: boolean;

        /**
         * List of smart albums to choose from.
         *
         * @platform iOS only
         * @default ['UserLibrary', 'PhotoStream', 'Panoramas', 'Videos', 'Bursts']
         */
        smartAlbums?: SmartAlbums[];

        /**
         * Whether to default to the front camera when opened. Please note that not all
         * Android devices handle this parameter, see
         * [issue #1058](https://github.com/ivpusic/react-native-image-crop-picker/issues/1058).
         *
         * @default false
         */
        useFrontCamera?: boolean;

        /**
         * Text displayed while photo is loading in picker.
         *
         * @default 'Processing assets...'
         */
        loadingLabelText?: string;

        /**
         * Whether to show the number of selected assets.
         *
         * @default true
         */
        showsSelectedCount?: boolean;

        /**
         * Applies a sort order on the creation date on how media is displayed within the
         * albums/detail photo views when opening the image picker.
         *
         * @platform iOS only
         * @default 'none'
         */
        sortOrder?: 'none' | 'asc' | 'desc';

        /**
         * Whether to display bottom controls.
         *
         * @platform Android only
         * @default false
         */
        hideBottomControls?: boolean;

        /**
         * When set to false, does not write temporary files for the selected images. This is useful
         * to improve performance when you are retrieving file contents with the includeBase64 option
         * and don't need to read files from disk.
         *
         * @platform iOS only
         * @default true
         */
        writeTempFile?: boolean;
    }

    type ImageOptions = CommonOptions & {
        mediaType: 'photo';

        /**
         * Width of result image when used with `cropping` option.
         */
        width?: number;

        /**
         * Height of result image when used with `cropping` option.
         */
        height?: number;

        /**
         * When set to true, the image file content will be available as a base64-encoded string in
         * the data property. Hint: To use this string as an image source, use it like:
         * <Image source={{uri: `data:${image.mime};base64,${image.data}`}} />
         *
         * @default false
         */
        includeBase64?: boolean;

        /**
         * Include image exif data in the response.
         *
         * @default false
         */
        includeExif?: boolean;

        /**
         * Whether to convert photos to JPG. This will also convert any Live Photo into its JPG representation.
         *
         * @default false
         */
        forceJpg?: boolean;

        /**
         * Enable or disable cropping.
         *
         * @default false
         */
        cropping?: boolean;

        /**
         * When set to true, the image will always fill the mask space.
         *
         * @default true
         */
        avoidEmptySpaceAroundImage?: boolean;

        /**
         * When cropping image, determines ActiveWidget color.
         *
         * @platform Android only
         * @default '#424242'
         */
        cropperActiveWidgetColor?: string;

        /**
         * When cropping image, determines the color of StatusBar.
         *
         * @platform Android only
         * @default '#424242'
         */
        cropperStatusBarColor?: string;

        /**
         * When cropping image, determines the color of Toolbar.
         *
         * @platform Android only
         * @default '#424242'
         */
        cropperToolbarColor?: string;

        /**
         * When cropping image, determines the color of Toolbar text and buttons.
         *
         * @platform Android only
         * @default 'darker orange'
         */
        cropperToolbarWidgetColor?: string;

        /**
         * When cropping image, determines the title of Toolbar.
         *
         * @default 'Edit Photo'
         */
        cropperToolbarTitle?: string;

        /**
         * Enables user to apply custom rectangle area for cropping.
         *
         * @platform iOS only
         * @default false
         */
        freeStyleCropEnabled?: boolean;

        /**
         * cropperTintColor
         */
        cropperTintColor?: string;

        /**
         * Enable or disable circular cropping mask.
         *
         * @default false
         */
        cropperCircleOverlay?: boolean;

        /**
         * Cancel button text.
         *
         * @default 'Cancel'
         */
        cropperCancelText?: string;

        /**
         * Cancel button color. HEX-like string color.
         *
         * @example '#ff00ee'
         * @platform iOS only
         */
        cropperCancelColor?: string;

        /**
         * Choose button text.
         *
         * @default 'Choose'
         */
        cropperChooseText?: string;

        /**
         * Choose button color. HEX-like string color.
         *
         * @example '#EE00DD'
         * @platform iOS only
         */
        cropperChooseColor?: string;

         /**
         * Enable or disable cropper rotate buttons.
         *
         * @platform iOS only
         * @default false
         */
          cropperRotateButtonsHidden?: boolean

        /**
         * Whether to show the 3x3 grid on top of the image during cropping.
         *
         * @platform Android only
         * @default true
         */
        showCropGuidelines?: boolean;

        /**
         * Whether to show the square crop frame during cropping
         *
         * @platform Android only
         * @default true
         */
        showCropFrame?: boolean;

        /**
         * Whether to enable rotating the image by hand gesture.
         *
         * @platform Android only
         * @default false
         */
        enableRotationGesture?: boolean;

        /**
         * When cropping image, disables the color setters for cropping library.
         *
         * @platform Android only
         * @default false
         */
        disableCropperColorSetters?: boolean;

        /**
         * Compress image with maximum width.
         *
         * @default null
         */
        compressImageMaxWidth?: number;

        /**
         * Compress image with maximum height.
         *
         * @default null
         */
        compressImageMaxHeight?: number;

        /**
         * Compress image with quality (from 0 to 1, where 1 is best quality). On iOS, values larger
         * than 0.8 don't produce a noticeable quality increase in most images, while a value of 0.8
         * will reduce the file size by about half or less compared to a value of 1.
         *
         * @default Android: 1, iOS: 0.8
         */
        compressImageQuality?: number;
    }

    type CropperOptions = ImageOptions & {
        /**
         * Selected image location
         */
        path: string;
    }

    type VideoOptions = CommonOptions & {
        mediaType: 'video';

        /**
         * Choose which preset will be used for video compression.
         *
         * @platform iOS only
         * @default 'MediumQuality'
         */
        compressVideoPreset?: CompressVideoPresets;
    };

    type AnyOptions = Omit<ImageOptions, 'mediaType'> & Omit<VideoOptions, 'mediaType'> & {
        mediaType?: 'any';
    };

    export type Options = AnyOptions | VideoOptions | ImageOptions;

    interface ImageVideoCommon {
        /**
         * Selected image location. This is null when the `writeTempFile` option is set to `false`.
         */
        path: string;

        /**
         * Selected image size in bytes.
         */
        size: number;

        /**
         * Selected image/video width.
         */
        width: number;

        /**
         * Selected image/video height.
         */
        height: number;

        /**
         * Selected image MIME type (image/jpeg, image/png, etc).
         */
        mime: string;

        /**
         * Extracted exif data from image. Response format is platform specific.
         */
        exif?: any;

        /**
         * Selected image's localidentifier, used for PHAsset searching.
         *
         * @platform iOS only
         */
        localIdentifier?: string;

        /**
         * Selected image's source path, do not have write access.
         *
         * @platform iOS only
         */
        sourceURL?: string;

        /**
         * Selected image/video's filename.
         *
         * @platform iOS only
         */
        filename?: string;

        /**
         * UNIX timestamp when image was created.
         *
         * @platform iOS only
         */
        creationDate?: string;

        /**
         * UNIX timestamp when image was last modified.
         */
        modificationDate?: string;
    }

    export interface Image extends ImageVideoCommon {
        /**
         * Optional base64 selected file representation.
         */
        data?: string | null;

        /**
         * Cropped image rectangle (width, height, x, y).
         */
        cropRect?: CropRect | null;
    }

    export interface Video extends ImageVideoCommon {
        /**
         * Video duration in milliseconds
         */
        duration: number | null;
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
        | 'E_NO_LIBRARY_PERMISSION'
        | 'E_NO_CAMERA_PERMISSION'
        | 'E_ERROR_WHILE_CLEANING_FILES';

    type PickerErrorCodeIOS =
        | 'E_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR'
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
    export function openCropper(options: CropperOptions): Promise<Image>;
    export function clean(): Promise<void>;
    export function cleanSingle(path: string): Promise<void>;

    export interface ImageCropPicker {
        openPicker<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
        openCamera<O extends Options>(options: O): Promise<PossibleArray<O, MediaType<O>>>;
        openCropper(options: CropperOptions): Promise<Image>;
        clean(): Promise<void>;
        cleanSingle(path: string): Promise<void>;
    }

    const ImageCropPicker: ImageCropPicker;

    export default ImageCropPicker;
}
