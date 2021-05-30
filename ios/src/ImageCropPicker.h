//
//  ImageManager.h
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#ifndef RN_IMAGE_CROP_PICKER_h
#define RN_IMAGE_CROP_PICKER_h

#import <UIKit/UIKit.h>

@class Compression;

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

@interface ImageCropPicker : NSObject<UINavigationControllerDelegate, RCTBridgeModule>

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
