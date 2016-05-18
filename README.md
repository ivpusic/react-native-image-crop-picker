# react-native-image-crop-picker
iOS/Android image picker with support for multiple images and cropping

**NOTE:** This library is result of one-night hacking, so please use it with caution. Don't assume there are not bugs. It is tested just on simple cases.

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

## Install

`npm install react-native-image-crop-picker --save`

#### iOS

```
pod 'react-native-image-crop-picker', :path => '../node_modules/react-native-image-crop-picker/ios'
```

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
// file: MainActivity.java
...

import com.reactnative.picker.PickerPackage; // import package

public class MainActivity extends ReactActivity {

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

## License
*MIT*
