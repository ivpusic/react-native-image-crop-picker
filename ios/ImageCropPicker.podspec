require 'json'
version = JSON.parse(File.read('../package.json'))["version"]

Pod::Spec.new do |s|

    s.name            = "ImageCropPicker"
    s.version         = version
    s.homepage        = "https://github.com/ivpusic/react-native-image-crop-picker"
    s.summary         = "iOS/Android image picker with support for camera, configurable compression, multiple images and cropping"
    s.license         = "MIT"
    s.author          = { "Ivan Pusic" => "pusic007@gmail.com" }
    s.ios.deployment_target = '8.0'
    s.source          = { :git => "https://github.com/ivpusic/react-native-image-crop-picker.git", :tag => "#{s.version}" }
    s.source_files    = '*.{h,m}'
    s.preserve_paths  = "**/*.js"

    s.dependency 'React'
    s.dependency 'RSKImageCropper', '1.6.3'
    s.dependency 'QBImagePickerController', '3.4.0'
    s.dependency 'UIImage-Resize', '1.0.1'

  end
