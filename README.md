# react-native-image-crop-picker
iOS/Android image picker with support for camera, video compression, multiple images and cropping

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
| maxFiles (ios only) | number (default 5) | Max number of files to select when using `multiple` option |
| compressVideo (ios only) | bool (default true) | When video is selected, compress it and convert it to mp4 |
| smartAlbums (ios only) | array (default ['UserLibrary', 'PhotoStream', 'Panoramas', 'Videos', 'Bursts']) | List of smart albums to choose from |
| useFrontCamera (ios only) | bool (default false) | Whether to default to the front/'selfie' camera when opened |
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

###### cocoapods users

- Add `platform :ios, '8.0'` to Podfile (!important)
- Add `pod 'RSKImageCropper'` and `pod 'QBImagePickerController'` to Podfile

###### non-cocoapods users

- Drag and drop the ios/ImageCropPickerSDK folder to your xcode project. (Make sure Copy items if needed IS ticked)
- Click on project General tab
  - Under `Deployment Info` set `Deployment Target` to `8.0`
  - Under `Embedded Binaries` click `+` and add `RSKImageCropper.framework` and `QBImagePicker.framework`

##### Android

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

## How it works?

It is basically wrapper around few libraries

#### Android
- Native Image Picker
- uCrop

#### iOS
- QBImagePickerController
- RSKImageCropper

## License
*MIT*
