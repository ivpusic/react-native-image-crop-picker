# react-native-image-crop-picker
iOS/Android image picker with support for camera, configurable compression, multiple images and cropping

## Result

<img width=200 title="iOS Single Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_single_pick.png">
<img width=200 title="iOS Crop" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_crop.png">
<img width=200 title="iOS Multiple Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_multiple_pick.png">

## Usage

Import library
```javascript
import ImagePicker from 'react-native-image-crop-picker';
```

#### Select from gallery

Call single image picker with cropping
```javascript
ImagePicker.openPicker({
  width: 300,
  height: 400,
  cropping: true
}).then(image => {
  console.log(image);
});
```

Call multiple image picker
```javascript
ImagePicker.openPicker({
  multiple: true
}).then(images => {
  console.log(images);
});
```

#### Select from camera
```javascript
ImagePicker.openCamera({
  width: 300,
  height: 400,
  cropping: true
}).then(image => {
  console.log(image);
});
```

#### Crop picture
```javascript
ImagePicker.openCropper({
  path: 'my-file-path.jpg',
  width: 300,
  height: 400
}).then(image => {
  console.log(image);
});
```

#### Optional cleanup
Module is creating tmp images which are going to be cleaned up automatically somewhere in the future. If you want to force cleanup, you can use `clean` to clean all tmp files, or `cleanSingle(path)` to clean single tmp file.

```javascript
ImagePicker.clean().then(() => {
  console.log('removed all tmp images from tmp directory');
}).catch(e => {
  alert(e);
});
```

#### Request Object

| Property        | Type           | Description  |
| ------------- |:-------------:| :-----|
| cropping | bool (default false)      | Enable or disable cropping |
| width          | number | Width of result image when used with `cropping` option |
| height      | number      | Height of result image when used with `cropping` option |
| multiple | bool (default false) | Enable or disable multiple image selection |
| includeBase64 | bool (default false) | Enable or disable returning base64 data with image |
| cropperTintColor (android only) | string (default `"#424242"`) | When cropping image, determines the color of Toolbar and other UX elements.  Uses UCrop's `setToolbarColor, setActiveWidgetColor, and setStatusBarColor` with color specified. |
| cropperCircleOverlay | bool (default false) | Enable or disable circular cropping mask. |
| maxFiles (ios only) | number (default 5) | Max number of files to select when using `multiple` option |
| waitAnimationEnd (ios only) | bool (default true) | Promise will resolve/reject once ViewController `completion` block is called |
| smartAlbums (ios only) | array (default ['UserLibrary', 'PhotoStream', 'Panoramas', 'Videos', 'Bursts']) | List of smart albums to choose from |
| useFrontCamera (ios only) | bool (default false) | Whether to default to the front/'selfie' camera when opened |
| compressVideoPreset (ios only) | string (default MediumQuality) | Choose which preset will be used for video compression |
| compressImageMaxWidth | number (default none) | Compress image with maximum width |
| compressImageMaxHeight | number (default none) | Compress image with maximum height |
| compressImageQuality | number (default 1) | Compress image with quality (from 0 to 1, where 1 is best quality) |
| loadingLabelText (ios only) | string (default "Processing assets...") | Text displayed while photo is loading in picker |
| mediaType | string (default any) | Accepted mediaType for image selection, can be one of: 'photo', 'video', or 'any' |
| showsSelectedCount (ios only) | bool (default true) | Whether to show the number of selected assets |
| showCropGuidelines (android only) | bool (default true) | Whether to show the 3x3 grid on top of the image during cropping |
| hideBottomControls (android only) | bool (default false) | Whether to display bottom controls |
| enableRotationGesture (android only) | bool (default false) | Whether to enable rotating the image by hand gesture |
#### Response Object

| Property        | Type           | Description  |
| ------------- |:-------------:| :-----|
| path          | string | Selected image location |
| width      | number      | Selected image width |
| height | number      | Selected image height |
| mime | string | Selected image MIME type (image/jpeg, image/png) |
| size | number | Selected image size in bytes |
| data | base64 | Optional base64 selected file representation |

## Install

```
npm i react-native-image-crop-picker --save
react-native link react-native-image-crop-picker
```

#### Post-install steps

##### iOS

In Xcode open Info.plist and add string key `NSPhotoLibraryUsageDescription` with value that describes why do you need access to user photos. More info here https://forums.developer.apple.com/thread/62229. Depending on what features you use, you also may need `NSCameraUsageDescription` and `NSMicrophoneUsageDescription` keys.

###### cocoapods users

- Add `platform :ios, '8.0'` to Podfile (!important)
- Add `pod 'RSKImageCropper'` and `pod 'QBImagePickerController'` to Podfile

###### non-cocoapods users

- Drag and drop the ios/ImageCropPickerSDK folder to your xcode project. (Make sure Copy items if needed IS ticked)
- Click on project General tab
  - Under `Deployment Info` set `Deployment Target` to `8.0`
  - Under `Embedded Binaries` click `+` and add `RSKImageCropper.framework` and `QBImagePicker.framework`

##### Android

- Make sure you are using Gradle `2.2.x` (project build.gradle)
```gradle
buildscript {
    ...
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.3'
        ...
    }
    ...
}
```

- Add `useSupportLibrary` (app build.gradle)
```gradle
android {
    ...

    defaultConfig {
        ...
        vectorDrawables.useSupportLibrary = true
        ...
    }
    ...
}
```

- [Optional] If you want to use camera picker in your project, add following to `AndroidManifest.xml`
  - `<uses-permission android:name="android.permission.CAMERA"/>`

#### Production build

##### iOS

###### cocoapods users

- You are already done

###### non-cocoapods users

If you are using pre-built frameworks from `ios/ImageCropPickerSDK`, then before deploying app to production you should strip off simulator ARCHs from these, or you can add frameworks from `Libraries/imageCropPicker/Libraries/_framework_name_.xcodeproj/Products/_framework_name_.framework` to Embedded Binaries instead of pre-built ones.
Related issue: https://github.com/ivpusic/react-native-image-crop-picker/issues/61.

Details for second approach:

1. Remove the pre-built frameworks from `Embedded Binaries`
2. Build for Device
4. Add the newly built binaries for both frameworks to `Embedded Binaries` (located at `Libraries/imageCropPicker/Libraries/_framework_name_.xcodeproj/Products/_framework_name_.framework`)

## License
*MIT*
