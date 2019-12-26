# react-native-image-crop-picker

[![Backers on Open Collective](https://opencollective.com/react-native-image-crop-picker/backers/badge.svg)](#backers) [![Sponsors on Open Collective](https://opencollective.com/react-native-image-crop-picker/sponsors/badge.svg)](#sponsors)

<img src="svg.svg" width="50%" height="50%" />

iOS/Android image picker with support for camera, video, configurable compression, multiple images and cropping

## Result

<p align="left">
  <img width=200 title="iOS Single Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_single_pick.png">
<img width=200 title="iOS Crop" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_crop.png">
<img width=200 title="iOS Multiple Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_multiple_pick.png">
</p>

## Important note

If you are using react-native >= 0.60 use react-native-image-crop-picker version >= 0.25.0. Otherwise use version < 0.25.0.

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

#### Image

```javascript
ImagePicker.openCamera({
  width: 300,
  height: 400,
  cropping: true,
}).then(image => {
  console.log(image);
});
```

#### Video

```javascript
ImagePicker.openCamera({
  mediaType: 'video',
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

| Property                                |                   Type                   | Description                           |
| --------------------------------------- | :--------------------------------------: | :--------------------------------------- |
| cropping                                |           bool (default false)           | Enable or disable cropping               |
| width                                   |                  number                  | Width of result image when used with `cropping` option |
| height                                  |                  number                  | Height of result image when used with `cropping` option |
| multiple                                |           bool (default false)           | Enable or disable multiple image selection |
| writeTempFile (ios only)                |           bool (default true)            | When set to false, does not write temporary files for the selected images. This is useful to improve performance when you are retrieving file contents with the `includeBase64` option and don't need to read files from disk. |
| includeBase64                           |           bool (default false)           | When set to true, the image file content will be available as a base64-encoded string in the `data` property. Hint: To use this string as an image source, use it like: ``<Image source={{uri: `data:${image.mime};base64,${image.data}`}} />`` |
| includeExif                           |           bool (default false)           | Include image exif data in the response |
| avoidEmptySpaceAroundImage            |           bool (default true)           |  When set to true, the image will always fill the mask space. |
| cropperActiveWidgetColor (android only) |       string (default `"#424242"`)       | When cropping image, determines ActiveWidget color. |
| cropperStatusBarColor (android only)    |        string (default `#424242`)        | When cropping image, determines the color of StatusBar. |
| cropperToolbarColor (android only)      |        string (default `#424242`)        | When cropping image, determines the color of Toolbar. |
| freeStyleCropEnabled (android only)      |        bool (default false)        | Enables user to apply custom rectangle area for cropping |
| cropperToolbarTitle                     |        string (default `Edit Photo`)     | When cropping image, determines the title of Toolbar. |
| cropperCircleOverlay                    |           bool (default false)           | Enable or disable circular cropping mask. |
| disableCropperColorSetters (android only)|           bool (default false)           | When cropping image, disables the color setters for cropping library. |
| minFiles (ios only)                     |            number (default 1)            | Min number of files to select when using `multiple` option |
| maxFiles (ios only)                     |            number (default 5)            | Max number of files to select when using `multiple` option |
| waitAnimationEnd (ios only)             |           bool (default true)            | Promise will resolve/reject once ViewController `completion` block is called |
| smartAlbums (ios only)                  | array ([supported values](https://github.com/ivpusic/react-native-image-crop-picker/blob/master/README.md#smart-album-types-ios)) (default ['UserLibrary', 'PhotoStream', 'Panoramas', 'Videos', 'Bursts']) | List of smart albums to choose from      |
| useFrontCamera                          |           bool (default false)           | Whether to default to the front/'selfie' camera when opened. Please note that not all Android devices handle this parameter, see [issue #1058](https://github.com/ivpusic/react-native-image-crop-picker/issues/1058)|
| compressVideoPreset (ios only)          |      string (default MediumQuality)      | Choose which preset will be used for video compression |
| compressImageMaxWidth                   |          number (default none)           | Compress image with maximum width        |
| compressImageMaxHeight                  |          number (default none)           | Compress image with maximum height       |
| compressImageQuality                    |            number (default 1 (Android)/0.8 (iOS))            | Compress image with quality (from 0 to 1, where 1 is best quality). On iOS, values larger than 0.8 don't produce a noticeable quality increase in most images, while a value of 0.8 will reduce the file size by about half or less compared to a value of 1. |
| loadingLabelText (ios only)             | string (default "Processing assets...")  | Text displayed while photo is loading in picker |
| mediaType                               |           string (default any)           | Accepted mediaType for image selection, can be one of: 'photo', 'video', or 'any' |
| showsSelectedCount (ios only)           |           bool (default true)            | Whether to show the number of selected assets |
| forceJpg (ios only)           |           bool (default false)            | Whether to convert photos to JPG. This will also convert any Live Photo into its JPG representation |
| showCropGuidelines (android only)       |           bool (default true)            | Whether to show the 3x3 grid on top of the image during cropping |
| showCropFrame (android only)       |           bool (default true)            | Whether to show crop frame during cropping |
| hideBottomControls (android only)       |           bool (default false)           | Whether to display bottom controls       |
| enableRotationGesture (android only)    |           bool (default false)           | Whether to enable rotating the image by hand gesture |
| cropperChooseText (ios only)            |           string (default choose)        | Choose button text |
| cropperCancelText (ios only)            |           string (default Cancel)        | Cancel button text |

#### Smart Album Types (ios)

```
['PhotoStream', 'Generic', 'Panoramas', 'Videos', 'Favorites', 'Timelapses', 'AllHidden', 'RecentlyAdded', 'Bursts', 'SlomoVideos', 'UserLibrary', 'SelfPortraits', 'Screenshots', 'DepthEffect', 'LivePhotos', 'Animated', 'LongExposure']
```

### Response Object

| Property                  |  Type  | Description                              |
| ------------------------- | :----: | :--------------------------------------- |
| path                      | string | Selected image location. This is null when the `writeTempFile` option is set to false. |
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

## Step 1

```bash
npm i react-native-image-crop-picker --save
```

## Step 2

### iOS

#### react-native >= 0.60 with cocoapods

- Run the following:

```bash
cd ios
pod install
```

After this use `ios/<project_name>.xcworkspace`. **Do not use** `ios/<project_name>.xcodeproj`.

##### Using local QBImagePicker

It is recommended to use locally provided QBImagePicker pod, because it contains few improvements over original version.

```
pod 'QBImagePickerController', :path => '../node_modules/react-native-image-crop-picker/ios/QBImagePicker/QBImagePickerController.podspec'
```

#### - If you are not using Cocoapods which is not recommended:

```bash
react-native link react-native-image-crop-picker
```

### Android

NOTE: If you are using react-native >= 0.60 autolinking, you can skip this step.

```bash
react-native link react-native-image-crop-picker
```

## Post-install steps

### iOS

#### Step 1

In Xcode open Info.plist and add string key `NSPhotoLibraryUsageDescription` with value that describes why you need access to user photos. More info here https://forums.developer.apple.com/thread/62229. Depending on what features you use, you also may need `NSCameraUsageDescription` and `NSMicrophoneUsageDescription` keys.

#### Step 2

##### Only if you are not using Cocoapods

- Click on project General tab
  - Under `Deployment Info` set `Deployment Target` to `8.0`
  - Under `Embedded Binaries` click `+` and add `RSKImageCropper.framework` and `QBImagePicker.framework`
  
#### Step Optional - To localizate the camera / gallery text buttons

- Open your Xcode project
- Go to your project settings by opening the project name on the Navigation (left side)
- Select your project in the project list 
- Should be into the Info tab and add in Localizations the language your app was missing throughout the +
- Rebuild and you should now have your app camera and gallery with the classic ios text in the language you added.

### Android

- Make sure you are using Gradle >= `2.2.x` (android/build.gradle)

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

- **VERY IMPORTANT** Add the following to your `build.gradle`'s repositories section. (android/build.gradle)

```gradle
allprojects {
    repositories {
      mavenLocal()
      jcenter()
      maven { url "$rootDir/../node_modules/react-native/android" }

      // ADD THIS
      maven { url 'https://maven.google.com' }

      // ADD THIS
      maven { url "https://www.jitpack.io" }
    }
}
```

- Add `useSupportLibrary` (android/app/build.gradle)

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

- Use Android SDK >= 26 (android/app/build.gradle)

```gradle
android {
    compileSdkVersion 27
    buildToolsVersion "27.0.3"
    ...
    
    defaultConfig {
      ...
      targetSdkVersion 27
      ...
    }
    ...
}
```

- [Optional] If you want to use camera picker in your project, add following to `app/src/main/AndroidManifest.xml`
  - `<uses-permission android:name="android.permission.CAMERA"/>`

- [Optional] If you want to use front camera, also add following to `app/src/main/
AndroidManifest.xml`
  - `<uses-feature android:name="android.hardware.camera" android:required="false" />`
  - `<uses-feature android:name="android.hardware.camera.front" android:required="false" />`

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
