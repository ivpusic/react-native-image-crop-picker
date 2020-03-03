//
//  ImageManager.h
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef RN_IMAGE_CROP_PICKER_h
#define RN_IMAGE_CROP_PICKER_h

#import <Foundation/Foundation.h>

#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#import <React/RCTImageURLLoader.h>
#import <React/RCTImageShadowView.h>
#import <React/RCTImageView.h>
#import <React/RCTImageLoaderProtocol.h>
#import <React/RCTEventEmitter.h>
#else
#import "RCTBridgeModule.h"
#import "RCTImageURLLoader.h"
#import "RCTImageShadowView.h"
#import "RCTImageView.h"
#import "RCTImageLoaderProtocol.h"
#import "RCTEventEmitter.h"
#endif

#if __has_include("QBImagePicker.h")
#import "QBImagePicker.h"
#import "RSKImageCropper.h"
#elif __has_include(<QBImagePickerController/QBImagePickerController.h>)
#import <QBImagePickerController/QBImagePickerController.h>
#import <RSKImageCropper/RSKImageCropper.h>
#elif __has_include("QBImagePickerController.h") // local QBImagePickerController subspec
#import "QBImagePickerController.h"
#import <RSKImageCropper/RSKImageCropper.h>
#else
#import
#import "QBImagePicker/QBImagePicker.h"
#import <RSKImageCropper/RSKImageCropper.h>
#endif

#import "UIImage+Resize.h"
#import "Compression.h"
#import <math.h>

@interface ImageCropPicker : RCTEventEmitter<
RCTBridgeModule,
UIImagePickerControllerDelegate,
UINavigationControllerDelegate,
QBImagePickerControllerDelegate,
RSKImageCropViewControllerDelegate,
RSKImageCropViewControllerDataSource>

typedef enum selectionMode {
    CAMERA,
    CROPPING,
    PICKER
} SelectionMode;

@property (nonatomic, strong) NSMutableDictionary *croppingFile;
@property (nonatomic, strong) NSDictionary *defaultOptions;
@property (nonatomic, strong) Compression *compression;
@property (nonatomic, retain) NSMutableDictionary *options;
@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;
@property SelectionMode currentSelectionMode;

@end

#endif
