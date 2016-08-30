//
//  ImageManager.m
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "ImageCropPicker.h"

#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_KEY @"cannot_run_camera_on_simulator"
#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_MSG @"Cannot run camera on simulator"

#define ERROR_PICKER_NO_CAMERA_PERMISSION_KEY @"missing_camera_permission"
#define ERROR_PICKER_NO_CAMERA_PERMISSION_MSG @"User did not grant camera permission."

#define ERROR_PICKER_CANCEL_KEY @"picker_cancel"
#define ERROR_PICKER_CANCEL_MSG @"User cancelled image selection"

#define ERROR_CANNOT_SAVE_IMAGE_KEY @"cannot_save_image"
#define ERROR_CANNOT_SAVE_IMAGE_MSG @"Cannot save image. Unable to write to tmp location."

@implementation ImageCropPicker

RCT_EXPORT_MODULE();

- (instancetype)init
{
    if (self = [super init]) {
        self.defaultOptions = @{
                                @"multiple": @NO,
                                @"cropping": @NO,
                                @"includeBase64": @NO,
                                @"maxFiles": @5,
                                @"width": @200,
                                @"height": @200
                                };
    }
    
    return self;
}

- (void)checkCameraPermissions:(void(^)(BOOL granted))callback
{
    AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (status == AVAuthorizationStatusAuthorized) {
        callback(YES);
        return;
    } else if (status == AVAuthorizationStatusNotDetermined){
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            callback(granted);
            return;
        }];
    } else {
        callback(NO);
    }
}

- (void) setConfiguration:(NSDictionary *)options
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject {
    
    self.resolve = resolve;
    self.reject = reject;
    self.options = [NSMutableDictionary dictionaryWithDictionary:self.defaultOptions];
    for (NSString *key in options.keyEnumerator) {
        [self.options setValue:options[key] forKey:key];
    }
}

- (UIViewController*) getRootVC {
    UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    while (root.presentedViewController != nil) {
        root = root.presentedViewController;
    }
    
    return root;
}

RCT_EXPORT_METHOD(openCamera:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    [self setConfiguration:options resolver:resolve rejecter:reject];
    
#if TARGET_IPHONE_SIMULATOR
    self.reject(ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_KEY, ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_MSG, nil);
    return;
#else
    [self checkCameraPermissions:^(BOOL granted) {
        if (!granted) {
            self.reject(ERROR_PICKER_NO_CAMERA_PERMISSION_KEY, ERROR_PICKER_NO_CAMERA_PERMISSION_MSG, nil);
            return;
        }
        
        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.delegate = self;
        picker.allowsEditing = NO;
        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        picker.delegate = self;
        
        [[self getRootVC] presentViewController:picker animated:YES completion:nil];
    }];
#endif
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    UIImage *chosenImage = [info objectForKey:UIImagePickerControllerOriginalImage];
    [self processSingleImagePick:chosenImage withViewController:picker];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    [picker dismissViewControllerAnimated:YES completion:NULL];
}

RCT_EXPORT_METHOD(openPicker:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    [self setConfiguration:options resolver:resolve rejecter:reject];
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        dispatch_async(dispatch_get_main_queue(), ^{
            // init picker
            QBImagePickerController *imagePickerController =
            [QBImagePickerController new];
            imagePickerController.delegate = self;
            imagePickerController.allowsMultipleSelection = [[self.options objectForKey:@"multiple"] boolValue];
            imagePickerController.maximumNumberOfSelection = [[self.options objectForKey:@"maxFiles"] intValue];
            imagePickerController.showsNumberOfSelectedAssets = YES;
            imagePickerController.mediaType = QBImagePickerMediaTypeImage;
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [[self getRootVC] presentViewController:imagePickerController animated:YES completion:nil];
            });
        });
    }];
}

- (void)qb_imagePickerController:
(QBImagePickerController *)imagePickerController
          didFinishPickingAssets:(NSArray *)assets {
    
    PHImageManager *manager = [PHImageManager defaultManager];
    
    if ([[[self options] objectForKey:@"multiple"] boolValue]) {
        NSMutableArray *images = [[NSMutableArray alloc] init];
        PHImageRequestOptions* options = [[PHImageRequestOptions alloc] init];
        options.synchronous = YES;
        
        for (PHAsset *asset in assets) {
            [manager
             requestImageDataForAsset:asset
             options:options
             resultHandler:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                 UIImage *image = [UIImage imageWithData:imageData];
                 NSData *data = UIImageJPEGRepresentation(image, 1);
                 
                 NSString *filePath = [self persistFile:data];
                 if (filePath == nil) {
                     self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
                     return;
                 }
                 
                 [images addObject:@{
                                     @"path": filePath,
                                     @"width": @(asset.pixelWidth),
                                     @"height": @(asset.pixelHeight),
                                     @"mime": @"image/jpeg",
                                     @"size": [NSNumber numberWithUnsignedInteger:data.length],
                                     @"data": [[self.options objectForKey:@"includeBase64"] boolValue] ? [data base64EncodedStringWithOptions:0] : [NSNull null],
                                     }];
             }];
        }
        
        self.resolve(images);
        [imagePickerController dismissViewControllerAnimated:YES completion:nil];
    } else {
        [manager
         requestImageDataForAsset:[assets objectAtIndex:0]
         options:nil
         resultHandler:^(NSData *imageData, NSString *dataUTI,
                         UIImageOrientation orientation,
                         NSDictionary *info) {
             [self processSingleImagePick:[UIImage imageWithData:imageData] withViewController:imagePickerController];
         }];
    }
}

- (void)qb_imagePickerControllerDidCancel:(QBImagePickerController *)imagePickerController {
    self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    [imagePickerController dismissViewControllerAnimated:YES completion:nil];
}

// when user selected single image, with camera or from photo gallery,
// this method will take care of attaching image metadata, and sending image to cropping controller
// or to user directly
- (void) processSingleImagePick:(UIImage*)image withViewController:(UIViewController*)viewController {
    if ([[[self options] objectForKey:@"cropping"] boolValue]) {
        RSKImageCropViewController *imageCropVC = [[RSKImageCropViewController alloc] initWithImage:image cropMode:RSKImageCropModeCustom];
        
        imageCropVC.avoidEmptySpaceAroundImage = YES;
        imageCropVC.dataSource = self;
        imageCropVC.delegate = self;
        
        [viewController dismissViewControllerAnimated:YES completion:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[self getRootVC] presentViewController:imageCropVC animated:YES completion:nil];
            });
        }];
    } else {
        NSData *data = UIImageJPEGRepresentation(image, 1);
        NSString *filePath = [self persistFile:data];
        if (filePath == nil) {
            self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
            return;
        }
        
        self.resolve(@{
                       @"path": filePath,
                       @"width": @(image.size.width),
                       @"height": @(image.size.height),
                       @"mime": @"image/jpeg",
                       @"size": [NSNumber numberWithUnsignedInteger:data.length],
                       @"data": [[self.options objectForKey:@"includeBase64"] boolValue] ? [data base64EncodedStringWithOptions:0] : [NSNull null],
                       });
        [viewController dismissViewControllerAnimated:YES completion:nil];
    }
}

#pragma mark - CustomCropModeDelegates

// Returns a custom rect for the mask.
- (CGRect)imageCropViewControllerCustomMaskRect:
(RSKImageCropViewController *)controller {
    CGSize maskSize = CGSizeMake(
                                 [[self.options objectForKey:@"width"] intValue],
                                 [[self.options objectForKey:@"height"] intValue]);
    
    CGFloat viewWidth = CGRectGetWidth(controller.view.frame);
    CGFloat viewHeight = CGRectGetHeight(controller.view.frame);
    
    CGRect maskRect = CGRectMake((viewWidth - maskSize.width) * 0.5f,
                                 (viewHeight - maskSize.height) * 0.5f,
                                 maskSize.width, maskSize.height);
    
    return maskRect;
}

// if provided width or height is bigger than screen w/h,
// then we should scale draw area
- (CGRect) scaleRect:(RSKImageCropViewController *)controller {
    CGRect rect = controller.maskRect;
    CGFloat viewWidth = CGRectGetWidth(controller.view.frame);
    CGFloat viewHeight = CGRectGetHeight(controller.view.frame);
    
    double scaleFactor = fmin(viewWidth / rect.size.width, viewHeight / rect.size.height);
    rect.size.width *= scaleFactor;
    rect.size.height *= scaleFactor;
    rect.origin.x = (viewWidth - rect.size.width) / 2;
    rect.origin.y = (viewHeight - rect.size.height) / 2;
    
    return rect;
}

// Returns a custom path for the mask.
- (UIBezierPath *)imageCropViewControllerCustomMaskPath:
(RSKImageCropViewController *)controller {
    CGRect rect = [self scaleRect:controller];
    UIBezierPath *path = [UIBezierPath bezierPathWithRoundedRect:rect
                                               byRoundingCorners:UIRectCornerAllCorners
                                                     cornerRadii:CGSizeMake(0, 0)];
    return path;
}

// Returns a custom rect in which the image can be moved.
- (CGRect)imageCropViewControllerCustomMovementRect:
(RSKImageCropViewController *)controller {
    return [self scaleRect:controller];
}

#pragma mark - CropFinishDelegate

// Crop image has been canceled.
- (void)imageCropViewControllerDidCancelCrop:
(RSKImageCropViewController *)controller {
    self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    [controller dismissViewControllerAnimated:YES completion:nil];
}

// The original image has been cropped.
- (void)imageCropViewController:(RSKImageCropViewController *)controller
                   didCropImage:(UIImage *)croppedImage
                  usingCropRect:(CGRect)cropRect {
    
    // we have correct rect, but not correct dimensions
    // so resize image
    CGSize resizedImageSize = CGSizeMake([[[self options] objectForKey:@"width"] intValue], [[[self options] objectForKey:@"height"] intValue]);
    UIImage *resizedImage = [croppedImage resizedImageToFitInSize:resizedImageSize scaleIfSmaller:YES];
    NSData *data = UIImageJPEGRepresentation(resizedImage, 1);
    
    NSString *filePath = [self persistFile:data];
    if (filePath == nil) {
        self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
        return;
    }
    
    self.resolve(@{
                   @"path": filePath,
                   @"width": @(resizedImage.size.width),
                   @"height": @(resizedImage.size.height),
                   @"mime": @"image/jpeg",
                   @"size": [NSNumber numberWithUnsignedInteger:data.length],
                   @"data": [[self.options objectForKey:@"includeBase64"] boolValue] ? [data base64EncodedStringWithOptions:0] : [NSNull null],
                   });
    
    [controller dismissViewControllerAnimated:YES completion:nil];
}

// at the moment it is not possible to upload image by reading PHAsset
// we are saving image and saving it to the tmp location where we are allowed to access image later
- (NSString*) persistFile:(NSData*)data {
    // create temp file
    NSString *filePath = [NSTemporaryDirectory() stringByAppendingString:[[NSUUID UUID] UUIDString]];
    filePath = [filePath stringByAppendingString:@".jpg"];
    
    // save cropped file
    BOOL status = [data writeToFile:filePath atomically:YES];
    if (!status) {
        return nil;
    }
    
    return filePath;
}

// The original image has been cropped. Additionally provides a rotation angle
// used to produce image.
- (void)imageCropViewController:(RSKImageCropViewController *)controller
                   didCropImage:(UIImage *)croppedImage
                  usingCropRect:(CGRect)cropRect
                  rotationAngle:(CGFloat)rotationAngle {
    [self imageCropViewController:controller didCropImage:croppedImage usingCropRect:cropRect];
}

@end
