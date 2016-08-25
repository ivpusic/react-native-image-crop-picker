# react-native-image-crop-picker
iOS/Android image picker with support for multiple images and cropping

## Result

#### iOS
<img width=200 title="iOS Single Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_single_pick.png">
<img width=200 title="iOS Crop" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_crop.png">
<img width=200 title="iOS Multiple Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/ios_multiple_pick.png">

#### Android
<img width=200 title="iOS Single Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/android_single_pick.png">
<img width=200 title="iOS Crop" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/android_crop.png">
<img width=200 title="iOS Multiple Pick" src="https://github.com/ivpusic/react-native-image-crop-picker/blob/master/images/android_multiple.png">

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

#### Request Object

| Property        | Type           | Description  |
| ------------- |:-------------:| :-----|
| cropping | bool (default false)      | Enable or disable cropping |
| width          | number | Width of result image when used with `cropping` option |
| height      | number      | Height of result image when used with `cropping` option |
| multiple | bool (default false) | Enable or disable multiple image selection |
| includeBase64 | bool (default false) | Enable or disable returning base64 data with image |
| maxFiles (ios only) | number (default 5) | Max number of files to select when using `multiple` option |

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
npm install react-native-image-crop-picker --save
react-native link react-native-image-crop-picker
```

#### Post-install steps

##### iOS

- Click on project General tab
  - Under `Deployment Info` set `Deployment Target` to `8.0`
  - Under `Embedded Binaries` click `+` and add `RSKImageCropper.framework` and `QBImagePicker.framework`

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
