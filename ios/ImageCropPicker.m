//
//  ImageManager.m
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "ImageCropPicker.h"

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

RCT_EXPORT_METHOD(openPicker:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    self.resolve = resolve;
    self.reject = reject;
    self.options = [NSMutableDictionary dictionaryWithDictionary:self.defaultOptions];
    for (NSString *key in options.keyEnumerator) {
        [self.options setValue:options[key] forKey:key];
    }
    
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
            
            UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
            dispatch_async(dispatch_get_main_queue(), ^{
                if (root.presentedViewController) {
                    [root.presentedViewController presentViewController:imagePickerController animated:YES completion:nil];
                } else {
                    [root presentViewController:imagePickerController animated:YES completion:nil];
                }
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
                 
                 NSMutableDictionary *object = [NSMutableDictionary
                                               dictionaryWithDictionary:@{
                                                                          @"path": filePath,
                                                                          @"width": @(asset.pixelWidth),
                                                                          @"height": @(asset.pixelHeight),
                                                                          @"mime": @"image/jpeg",
                                                                          @"size": [NSNumber numberWithUnsignedInteger:data.length]}];
  
                 
                 if ([[[self options] objectForKey:@"includeBase64"] boolValue]) {
                     NSString *dataString = [data base64EncodedStringWithOptions:0];

                     object[@"data"] = dataString;
                 }
                 
                 [images addObject:[object copy]];
             }];
        }
        
        self.resolve(images);
        [imagePickerController dismissViewControllerAnimated:YES completion:nil];
    } else {
        PHAsset *asset = [assets objectAtIndex:0];
        
        [manager
         requestImageDataForAsset:asset
         options:nil
         resultHandler:^(NSData *imageData, NSString *dataUTI,
                         UIImageOrientation orientation,
                         NSDictionary *info) {
             
             if ([[[self options] objectForKey:@"cropping"] boolValue]) {
                 UIImage *image = [UIImage imageWithData:imageData];
                 RSKImageCropViewController *imageCropVC = [[RSKImageCropViewController alloc] initWithImage:image cropMode:RSKImageCropModeCustom];
                 
                 imageCropVC.avoidEmptySpaceAroundImage = YES;
                 imageCropVC.dataSource = self;
                 imageCropVC.delegate = self;
                 
                 [imagePickerController dismissViewControllerAnimated:YES completion:^{
                     UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
                     dispatch_async(dispatch_get_main_queue(), ^{
                         if (root.presentedViewController) {
                             [root.presentedViewController presentViewController:imageCropVC animated:YES completion:nil];
                         } else {
                             [root presentViewController:imageCropVC animated:YES completion:nil];
                         }
                     });
                 }];
             } else {
                 UIImage *image = [UIImage imageWithData:imageData];
                 NSData *data = UIImageJPEGRepresentation(image, 1);
                 NSString *filePath = [self persistFile:data];
                 if (filePath == nil) {
                     self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
                     return;
                 }
                 
                 NSMutableDictionary *object = [NSMutableDictionary
                                                dictionaryWithDictionary:@{
                                                                           @"path": filePath,
                                                                           @"width": @(asset.pixelWidth),
                                                                           @"height": @(asset.pixelHeight),
                                                                           @"mime": @"image/jpeg",
                                                                           @"size": [NSNumber numberWithUnsignedInteger:data.length]}];
                 
                 
                 if ([[[self options] objectForKey:@"includeBase64"] boolValue]) {
                     NSString *dataString = [data base64EncodedStringWithOptions:0];
                     
                     object[@"data"] = dataString;
                 }
                 
                 self.resolve([object copy]);
                 [imagePickerController dismissViewControllerAnimated:YES completion:nil];
             }
         }];
    }
}

- (void)qb_imagePickerControllerDidCancel:(QBImagePickerController *)imagePickerController {
    self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    [imagePickerController dismissViewControllerAnimated:YES completion:nil];
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
    
    NSMutableDictionary *image = [NSMutableDictionary
                                   dictionaryWithDictionary:@{
                                                              @"path": filePath,
                                                              @"width": @(resizedImage.size.width),
                                                              @"height": @(resizedImage.size.height),
                                                              @"mime": @"image/jpeg",
                                                              @"size": [NSNumber numberWithUnsignedInteger:data.length]}];
    
    
    if ([[[self options] objectForKey:@"includeBase64"] boolValue]) {
        NSString *dataString = [data base64EncodedStringWithOptions:0];
        
        image[@"data"] = dataString;
    }
    
    self.resolve([image copy]);
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
