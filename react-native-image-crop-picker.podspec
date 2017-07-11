require "json"
package = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package['version']
  s.summary       = package['description']
  s.author        = "Ivan Pusic"
  s.license       = package['license']
  s.requires_arc  = true
  s.homepage      = "https://github.com/ivpusic/react-native-image-crop-picker"
  s.source        = { :git => 'https://github.com/ivpusic/react-native-image-crop-picker.git' }
  s.platform      = :ios, '8.0'
  s.source_files  = "ios/*.{h,m}", "ios/UIImage-Resize/*.{h,m}"

  s.dependency "QBImagePickerController"
  s.dependency "RSKImageCropper"
end
