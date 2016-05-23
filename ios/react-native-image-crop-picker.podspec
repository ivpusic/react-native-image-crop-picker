require 'json'

package = JSON.parse(File.read(File.join(__dir__, '../package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-image-crop-picker"
  s.version      = package['version']
  s.license      = "MIT"
  s.homepage     = "https://github.com/ivpusic/react-native-image-crop-picker"
  s.authors      = { 'Ivan Pusic' => 'pusic007@gmail.com' }
  s.summary      = "Select single or multiple images with cropping option"
  s.source       = { :git => "https://github.com/ivpusic/react-native-image-crop-picker.git", :tag => "v#{s.version}" }
  s.source_files  = "*.{h,m}"

  s.platform     = :ios, "8.0"
  s.dependency 'React', '>= 0.26.1'
  s.dependency 'QBImagePickerController', '3.4.0'
  s.dependency 'RSKImageCropper', '1.5.1'
  s.dependency 'UIImage-Resize', '~> 1.0'
end
