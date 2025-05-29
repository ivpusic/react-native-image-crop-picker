package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name             = "RNImageCropPicker"
  s.version          = package['version']
  s.summary          = package["description"]
  s.requires_arc = true
  s.license      = 'MIT'
  s.homepage     = 'n/a'
  s.authors      = { "ivpusic" => "" }
  s.source       = { :git => "https://github.com/ivpusic/react-native-image-crop-picker", :tag => "v#{s.version}"}
  s.source_files = 'ios/src/*.{h,m,mm}'
  s.platform     = :ios, "8.0"
  s.dependency 'React-Core'
  s.dependency 'React-RCTImage'
  s.dependency 'TOCropViewController', '~> 2.7.4'
  s.resource_bundles = {
    'RNImageCropPickerPrivacyInfo' => ['ios/PrivacyInfo.xcprivacy'],
  }

  s.subspec 'QBImagePickerController' do |qb|
    qb.name             = "QBImagePickerController"
    qb.source_files     = "ios/QBImagePicker/QBImagePicker/*.{h,m}"
    qb.exclude_files    = "ios/QBImagePicker/QBImagePicker/QBImagePicker.h"
    qb.resource_bundles = { "QBImagePicker" => "ios/QBImagePicker/QBImagePicker/*.{lproj,storyboard}" }
    qb.requires_arc     = true
    qb.frameworks       = "Photos", "PhotosUI"
  end

  install_modules_dependencies(s)
end
