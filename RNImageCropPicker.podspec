Pod::Spec.new do |s|
  s.name             = "RNImageCropPicker"
  s.version          = "0.18.1"
  s.summary          = "iOS/Android image picker with support for camera, configurable compression, multiple images and cropping"
  s.requires_arc = true
  s.license      = 'MIT'
  s.homepage     = 'n/a'
  s.authors      = { "ivpusic" => "" }
  s.source       = { :git => "https://github.com/ivpusic/react-native-image-crop-picker", :tag => 'v0.18.1'}
  s.source_files = 'ios/src/*.{h,m}'
  s.platform     = :ios, "8.0"
  s.dependency 'React/Core'
  s.dependency 'RSKImageCropper'
  s.dependency 'QBImagePickerController'
end
