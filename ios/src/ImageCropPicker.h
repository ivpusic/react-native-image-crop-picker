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
#else
#import "RCTBridgeModule.h"
#import "RCTImageURLLoader.h"
#import "RCTImageShadowView.h"
#import "RCTImageView.h"
#import "RCTImageLoaderProtocol.h"
#endif

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNCImageCropPickerSpec/RNCImageCropPickerSpec.h>
#endif

#if __has_include("QBImagePicker.h")
#import "QBImagePicker.h"
#elif __has_include(<QBImagePickerController/QBImagePickerController.h>)
#import <QBImagePickerController/QBImagePickerController.h>
#elif __has_include("QBImagePickerController.h") // local QBImagePickerController subspec
#import "QBImagePickerController.h"
#else
#import "QBImagePicker/QBImagePicker.h"
#endif


#import <TOCropViewController/TOCropViewController.h>

#import "UIImage+Resize.h"
#import "UIImage+Extension.h"
#import "Compression.h"
#import <math.h>

@interface ImageCropPicker : NSObject<
UIImagePickerControllerDelegate,
UINavigationControllerDelegate,
RCTBridgeModule,
QBImagePickerControllerDelegate,
TOCropViewControllerDelegate>

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
