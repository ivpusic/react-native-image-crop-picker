# react-native-image-crop-picker
iOS/Android image picker with support for camera, configurable compression, multiple images and cropping

[![Backers on Open Collective](https://opencollective.com/react-native-image-crop-picker/backers/badge.svg)](#backers) [![Sponsors on Open Collective](https://opencollective.com/react-native-image-crop-picker/sponsors/badge.svg)](#sponsors)

## Result

<p align="left">
  <img width=200 title="iOS Single Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_single_pick.png">
<img width=200 title="iOS Crop" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_crop.png">
<img width=200 title="iOS Multiple Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_multiple_pick.png">
</p>

## Usage

Import library

```javascript
import ImagePicker from 'react-native-image-crop-picker';
```

### Select from gallery

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

Select video only from gallery

```javascript
ImagePicker.openPicker({
  mediaType: "video",
}).then((video) => {
  console.log(video);
});
```

**Android: The prop 'cropping' has been known to cause videos not to be display in the gallery on Android. Please do not set cropping to true when selecting videos.**


### Select from camera

```javascript
ImagePicker.openCamera({
  width: 300,
  height: 400,
  cropping: true
}).then(image => {
  console.log(image);
});
```

### Crop picture

```javascript
ImagePicker.openCropper({
  path: 'my-file-path.jpg',
  width: 300,
  height: 400
}).then(image => {
  console.log(image);
});
```

### Optional cleanup

Module is creating tmp images which are going to be cleaned up automatically somewhere in the future. If you want to force cleanup, you can use `clean` to clean all tmp files, or `cleanSingle(path)` to clean single tmp file.

```javascript
ImagePicker.clean().then(() => {
  console.log('removed all tmp images from tmp directory');
}).catch(e => {
  alert(e);
});
```

### Request Object

| Property                                |                   Type                   | Description                              |
| --------------------------------------- | :--------------------------------------: | :--------------------------------------- |
| cropping                                |           bool (default false)           | Enable or disable cropping               |
| width                                   |                  number                  | Width of result image when used with `cropping` option |
| height                                  |                  number                  | Height of result image when used with `cropping` option |
| multiple                                |           bool (default false)           | Enable or disable multiple image selection |
| includeBase64                           |           bool (default false)           | Enable or disable returning base64 data with image |
| includeExif                           |           bool (default false)           | Include image exif data in the response |
| cropperActiveWidgetColor (android only) |       string (default `"#424242"`)       | When cropping image, determines ActiveWidget color. |
| cropperStatusBarColor (android only)    |        string (default `#424242`)        | When cropping image, determines the color of StatusBar. |
| cropperToolbarColor (android only)      |        string (default `#424242`)        | When cropping image, determines the color of Toolbar. |
| cropperToolbarTitle (android only)      |        string (default `Edit Photo`)     | When cropping image, determines the title of Toolbar. |
| cropperCircleOverlay                    |           bool (default false)           | Enable or disable circular cropping mask. |
| minFiles (ios only)                     |            number (default 1)            | Min number of files to select when using `multiple` option |
| maxFiles (ios only)                     |            number (default 5)            | Max number of files to select when using `multiple` option |
| waitAnimationEnd (ios only)             |           bool (default true)            | Promise will resolve/reject once ViewController `completion` block is called |
| smartAlbums (ios only)                  | array (default ['UserLibrary', 'PhotoStream', 'Panoramas', 'Videos', 'Bursts']) | List of smart albums to choose from      |
| useFrontCamera (ios only)               |           bool (default false)           | Whether to default to the front/'selfie' camera when opened |
| compressVideoPreset (ios only)          |      string (default MediumQuality)      | Choose which preset will be used for video compression |
| compressImageMaxWidth                   |          number (default none)           | Compress image with maximum width        |
| compressImageMaxHeight                  |          number (default none)           | Compress image with maximum height       |
| compressImageQuality                    |            number (default 1)            | Compress image with quality (from 0 to 1, where 1 is best quality) |
| loadingLabelText (ios only)             | string (default "Processing assets...")  | Text displayed while photo is loading in picker |
| mediaType                               |           string (default any)           | Accepted mediaType for image selection, can be one of: 'photo', 'video', or 'any' |
| showsSelectedCount (ios only)           |           bool (default true)            | Whether to show the number of selected assets |
| showCropGuidelines (android only)       |           bool (default true)            | Whether to show the 3x3 grid on top of the image during cropping |
| hideBottomControls (android only)       |           bool (default false)           | Whether to display bottom controls       |
| enableRotationGesture (android only)    |           bool (default false)           | Whether to enable rotating the image by hand gesture |
### Response Object

| Property                  |  Type  | Description                              |
| ------------------------- | :----: | :--------------------------------------- |
| path                      | string | Selected image location                  |
| localIdentifier(ios only) | string | Selected images' localidentifier, used for PHAsset searching |
| sourceURL(ios only)       | string | Selected images' source path, do not have write access |
| filename(ios only)        | string | Selected images' filename                |
| width                     | number | Selected image width                     |
| height                    | number | Selected image height                    |
| mime                      | string | Selected image MIME type (image/jpeg, image/png) |
| size                      | number | Selected image size in bytes             |
| data                      | base64 | Optional base64 selected file representation |
| exif                      | object | Extracted exif data from image. Response format is platform specific |
| cropRect                  | object | Cropped image rectangle (width, height, x, y)    |
| creationDate (ios only)   | string | UNIX timestamp when image was created    |
| modificationDate          | string | UNIX timestamp when image was last modified |

# Install

## Install package

```bash
npm i react-native-image-crop-picker --save
```

Link the package using react-native link:

```bash
react-native link react-native-image-crop-picker
```

## Post-install steps

### iOS

#### Step 1:

In Xcode open Info.plist and add string key `NSPhotoLibraryUsageDescription` with value that describes why you need access to user photos. More info here https://forums.developer.apple.com/thread/62229. Depending on what features you use, you also may need `NSCameraUsageDescription` and `NSMicrophoneUsageDescription` keys.

#### Step 2:

##### Cocoapods (Highly recommended)

```bash
cd ios
pod init
```

After this you have to add pod dependencies to `Podfile`. Open `Podfile` with your editor, and add or adjust example configuration:

```bash
platform :ios, '8.0'

target '<your_project_name>' do
    pod 'RSKImageCropper'
    pod 'QBImagePickerController'
end
```

After this run:

```bash
pod install
```

##### Manual

- Drag and drop the ios/ImageCropPickerSDK folder to your xcode project. (Make sure Copy items if needed IS ticked)
- Click on project General tab
  - Under `Deployment Info` set `Deployment Target` to `8.0`
  - Under `Embedded Binaries` click `+` and add `RSKImageCropper.framework` and `QBImagePicker.framework`

### Android

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

- Add the following to your `build.gradle`'s repositories section. (project build.gradle)

```gradle
allprojects {
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
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

## Production build

### iOS

#### Cocoapods (Highly recommended)

- You are already done

#### Manual

If you are using pre-built frameworks from `ios/ImageCropPickerSDK`, then before deploying app to production you should strip off simulator ARCHs from these, or you can add frameworks from `Libraries/imageCropPicker/Libraries/_framework_name_.xcodeproj/Products/_framework_name_.framework` to Embedded Binaries instead of pre-built ones.
Related issue: https://github.com/ivpusic/react-native-image-crop-picker/issues/61.

Details for second approach:

1. Remove the pre-built frameworks from `Embedded Binaries`
2. Build for Device
3. Add the newly built binaries for both frameworks to `Embedded Binaries` (located at `Libraries/imageCropPicker/Libraries/_framework_name_.xcodeproj/Products/_framework_name_.framework`)

## TO DO

- [ ] [Android] Standardize multiple select
- [ ] [Android] Pick remote media
- [ ] [Android] Video compression


## Contributors

This project exists thanks to all the people who contribute. [[Contribute]](CONTRIBUTING.md).
<a href="graphs/contributors"><img src="https://opencollective.com/react-native-image-crop-picker/contributors.svg?width=890" /></a>


## Backers

Thank you to all our backers! 🙏 [[Become a backer](https://opencollective.com/react-native-image-crop-picker#backer)]

<a href="https://opencollective.com/react-native-image-crop-picker#backers" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/backers.svg?width=890"></a>


## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [[Become a sponsor](https://opencollective.com/react-native-image-crop-picker#sponsor)]

<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/0/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/1/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/2/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/3/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/4/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/5/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/6/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/7/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/8/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/react-native-image-crop-picker/sponsor/9/website" target="_blank"><img src="https://opencollective.com/react-native-image-crop-picker/sponsor/9/avatar.svg"></a>



## License
*MIT*
