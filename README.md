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

`npm install react-native-image-crop-picker --save`

#### iOS

```
pod 'react-native-image-crop-picker', :path => '../node_modules/react-native-image-crop-picker/ios'
```

[- Step By Step tutorial](https://github.com/ivpusic/react-native-image-crop-picker#ios-step-by-step-installation)

#### Android
```gradle
// file: android/settings.gradle
...

include ':react-native-image-crop-picker'
project(':react-native-image-crop-picker').projectDir = new File(settingsDir, '../node_modules/react-native-image-crop-picker/android')
```
```gradle
// file: android/app/build.gradle
...

dependencies {
    ...
    compile project(':react-native-image-crop-picker')
}
```

```java
// file: MainApplication.java
...

import com.reactnative.picker.PickerPackage; // import package

public class MainApplication extends ReactApplication {
...
   /**
   * A list of packages used by the app. If the app uses additional views
   * or modules besides the default ones, add more packages here.
   */
    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new PickerPackage() // Add package
        );
    }
...
}
```

## How it works?

It is basically wrapper around few libraries

#### Android
- Native Image Picker
- uCrop

#### iOS
- QBImagePickerController
- RSKImageCropper

# iOS Step by Step installation

- Create new react native project with Pods support
```
react-native init picker
cd picker
npm i react-native-image-crop-picker --save
cd ios
pod init
```

- Inside `ios` directory change `Podfile` to following
```
platform :ios, '8.0'

target 'picker' do
    source 'https://github.com/CocoaPods/Specs.git'
    pod 'React', :path => '../node_modules/react-native'
    pod 'react-native-image-crop-picker', :path => '../node_modules/react-native-image-crop-picker/ios'
end
```

- Run
```
pod install
```

- open **project_name.xcworkspace**
- Add `$(inherited)` to other linker flags under Build Settings

Done!

## License
*MIT*
