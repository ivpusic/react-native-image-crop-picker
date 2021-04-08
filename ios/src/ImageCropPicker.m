//
//  ImageManager.m
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import <TOCropViewController/TOCropViewController.h>
#import <PhotosUI/PhotosUI.h>

#if __has_include("QBImagePicker.h")
#import "QBImagePicker.h"
#elif __has_include(<QBImagePickerController/QBImagePickerController.h>)
#import <QBImagePickerController/QBImagePickerController.h>
#elif __has_include("QBImagePickerController.h") // local QBImagePickerController subspec
#import "QBImagePickerController.h"
#else
#import
#import "QBImagePicker/QBImagePicker.h"
#endif

#import "ImageCropPicker.h"

#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_KEY @"E_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR"
#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_MSG @"Cannot run camera on simulator"

#define ERROR_NO_CAMERA_PERMISSION_KEY @"E_NO_CAMERA_PERMISSION"
#define ERROR_NO_CAMERA_PERMISSION_MSG @"User did not grant camera permission."

#define ERROR_NO_LIBRARY_PERMISSION_KEY @"E_NO_LIBRARY_PERMISSION"
#define ERROR_NO_LIBRARY_PERMISSION_MSG @"User did not grant library permission."

#define ERROR_PICKER_CANCEL_KEY @"E_PICKER_CANCELLED"
#define ERROR_PICKER_CANCEL_MSG @"User cancelled image selection"

#define ERROR_PICKER_NO_DATA_KEY @"E_NO_IMAGE_DATA_FOUND"
#define ERROR_PICKER_NO_DATA_MSG @"Cannot find image data"

#define ERROR_CROPPER_IMAGE_NOT_FOUND_KEY @"E_CROPPER_IMAGE_NOT_FOUND"
#define ERROR_CROPPER_IMAGE_NOT_FOUND_MSG @"Can't find the image at the specified path"

#define ERROR_CLEANUP_ERROR_KEY @"E_ERROR_WHILE_CLEANING_FILES"
#define ERROR_CLEANUP_ERROR_MSG @"Error while cleaning up tmp files"

#define ERROR_CANNOT_SAVE_IMAGE_KEY @"E_CANNOT_SAVE_IMAGE"
#define ERROR_CANNOT_SAVE_IMAGE_MSG @"Cannot save image. Unable to write to tmp location."

#define ERROR_CANNOT_PROCESS_VIDEO_KEY @"E_CANNOT_PROCESS_VIDEO"
#define ERROR_CANNOT_PROCESS_VIDEO_MSG @"Cannot process video data"

#import "UIImage+Resize.h"
#import "UIImage+Extension.h"
#import "Compression.h"

@implementation ImageResult
@end

@interface ImageCropPicker(UIImagePickerControllerDelegate) <UIImagePickerControllerDelegate>
@end

@interface ImageCropPicker(QBImagePickerControllerDelegate) <QBImagePickerControllerDelegate>
@end

@interface ImageCropPicker(TOCropViewControllerDelegate) <TOCropViewControllerDelegate>
@end

@interface ImageCropPicker(PHPickerViewControllerDelegate) <PHPickerViewControllerDelegate>
@end

@implementation ImageCropPicker

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (instancetype)init {
    if (self = [super init]) {
        self.defaultOptions = @{
            @"multiple": @NO,
            @"cropping": @NO,
            @"cropperCircleOverlay": @NO,
            @"writeTempFile": @YES,
            @"includeBase64": @NO,
            @"includeExif": @NO,
            @"compressVideo": @YES,
            @"minFiles": @1,
            @"maxFiles": @5,
            @"width": @200,
            @"waitAnimationEnd": @YES,
            @"height": @200,
            @"useFrontCamera": @NO,
            @"avoidEmptySpaceAroundImage": @YES,
            @"compressImageQuality": @0.8,
            @"compressVideoPreset": @"MediumQuality",
            @"loadingLabelText": @"Processing assets...",
            @"mediaType": @"any",
            @"showsSelectedCount": @YES,
            @"forceJpg": @NO,
            @"sortOrder": @"none",
            @"cropperCancelText": @"Cancel",
            @"cropperChooseText": @"Choose",
            @"cropperRotateButtonsHidden": @NO
        };
        self.compression = [[Compression alloc] init];
    }
    
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (void (^ __nullable)(void))waitAnimationEnd:(void (^ __nullable)(void))completion {
    if ([self.options[@"waitAnimationEnd"] boolValue]) {
        return completion;
    }
    
    if (completion != nil) {
        completion();
    }
    
    return nil;
}

- (void)checkCameraPermissions:(void(^)(BOOL granted))callback {
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
    return RCTPresentedViewController();
}

RCT_EXPORT_METHOD(openCamera:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    [self setConfiguration:options resolver:resolve rejecter:reject];
    self.currentSelectionMode = CAMERA;
    
#if TARGET_IPHONE_SIMULATOR
    self.reject(ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_KEY, ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_MSG, nil);
    return;
#else
    [self checkCameraPermissions:^(BOOL granted) {
        if (!granted) {
            self.reject(ERROR_NO_CAMERA_PERMISSION_KEY, ERROR_NO_CAMERA_PERMISSION_MSG, nil);
            return;
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            UIImagePickerController *picker = [[UIImagePickerController alloc] init];
            picker.delegate = self;
            picker.allowsEditing = NO;
            picker.sourceType = UIImagePickerControllerSourceTypeCamera;
            
            NSString *mediaType = self.options[@"mediaType"];
            
            if ([mediaType isEqualToString:@"video"]) {
                NSArray *availableTypes = [UIImagePickerController availableMediaTypesForSourceType:UIImagePickerControllerSourceTypeCamera];
                
                if ([availableTypes containsObject:(NSString *)kUTTypeMovie]) {
                    picker.mediaTypes = @[(NSString *) kUTTypeMovie];
                    picker.videoQuality = UIImagePickerControllerQualityTypeHigh;
                }
            }
            
            if ([self.options[@"useFrontCamera"] boolValue]) {
                picker.cameraDevice = UIImagePickerControllerCameraDeviceFront;
            }
            
            [[self getRootVC] presentViewController:picker animated:YES completion:nil];
        });
    }];
#endif
}

- (NSString*) getTmpDirectory {
    NSString *TMP_DIRECTORY = @"react-native-image-crop-picker/";
    NSString *tmpFullPath = [NSTemporaryDirectory() stringByAppendingString:TMP_DIRECTORY];
    
    BOOL isDir;
    BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:tmpFullPath isDirectory:&isDir];
    if (!exists) {
        [[NSFileManager defaultManager] createDirectoryAtPath: tmpFullPath
                                  withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    return tmpFullPath;
}

- (BOOL)cleanTmpDirectory {
    NSString* tmpDirectoryPath = [self getTmpDirectory];
    NSArray* tmpDirectory = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:tmpDirectoryPath error:NULL];
    
    for (NSString *file in tmpDirectory) {
        BOOL deleted = [[NSFileManager defaultManager] removeItemAtPath:[NSString stringWithFormat:@"%@%@", tmpDirectoryPath, file]
                                                                  error:NULL];
        if (!deleted) {
            return NO;
        }
    }
    
    return YES;
}

RCT_EXPORT_METHOD(cleanSingle:(NSString *) path
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    BOOL deleted = [[NSFileManager defaultManager] removeItemAtPath:path error:NULL];
    
    if (!deleted) {
        reject(ERROR_CLEANUP_ERROR_KEY, ERROR_CLEANUP_ERROR_MSG, nil);
    } else {
        resolve(nil);
    }
}

RCT_REMAP_METHOD(clean, resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {
    if (![self cleanTmpDirectory]) {
        reject(ERROR_CLEANUP_ERROR_KEY, ERROR_CLEANUP_ERROR_MSG, nil);
    } else {
        resolve(nil);
    }
}

RCT_EXPORT_METHOD(openPicker:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self setConfiguration:options resolver:resolve rejecter:reject];
    self.currentSelectionMode = PICKER;

    if (@available(iOS 14, *)) {
        if (![self.options[@"cropperCircleOverlay"] boolValue] && ![self.options[@"cropping"] boolValue]) {
            PHPickerConfiguration *configuration = [self makeConfiguration];
            PHPickerViewController *controller = [[PHPickerViewController alloc] initWithConfiguration:configuration];
            controller.delegate = self;
            dispatch_async(dispatch_get_main_queue(), ^{
                [[self getRootVC] presentViewController:controller animated:YES completion:nil];
            });
            return;
        }
    }

    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        if (status != PHAuthorizationStatusAuthorized) {
            self.reject(ERROR_NO_LIBRARY_PERMISSION_KEY, ERROR_NO_LIBRARY_PERMISSION_MSG, nil);
            return;
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            // init picker
            QBImagePickerController *imagePickerController = [QBImagePickerController new];
            imagePickerController.delegate = self;
            imagePickerController.allowsMultipleSelection = [self.options[@"multiple"] boolValue];
            imagePickerController.minimumNumberOfSelection = abs([self.options[@"minFiles"] intValue]);
            imagePickerController.maximumNumberOfSelection = abs([self.options[@"maxFiles"] intValue]);
            imagePickerController.showsNumberOfSelectedAssets = [self.options[@"showsSelectedCount"] boolValue];
            imagePickerController.sortOrder = self.options[@"sortOrder"];
            
            NSArray *smartAlbums = self.options[@"smartAlbums"];
            if (smartAlbums != nil) {
                NSDictionary *albums = @{
                    //user albums
                    @"Regular" : @(PHAssetCollectionSubtypeAlbumRegular),
                    @"SyncedEvent" : @(PHAssetCollectionSubtypeAlbumSyncedEvent),
                    @"SyncedFaces" : @(PHAssetCollectionSubtypeAlbumSyncedFaces),
                    @"SyncedAlbum" : @(PHAssetCollectionSubtypeAlbumSyncedAlbum),
                    @"Imported" : @(PHAssetCollectionSubtypeAlbumImported),
                    
                    //cloud albums
                    @"PhotoStream" : @(PHAssetCollectionSubtypeAlbumMyPhotoStream),
                    @"CloudShared" : @(PHAssetCollectionSubtypeAlbumCloudShared),
                    
                    //smart albums
                    @"Generic" : @(PHAssetCollectionSubtypeSmartAlbumGeneric),
                    @"Panoramas" : @(PHAssetCollectionSubtypeSmartAlbumPanoramas),
                    @"Videos" : @(PHAssetCollectionSubtypeSmartAlbumVideos),
                    @"Favorites" : @(PHAssetCollectionSubtypeSmartAlbumFavorites),
                    @"Timelapses" : @(PHAssetCollectionSubtypeSmartAlbumTimelapses),
                    @"AllHidden" : @(PHAssetCollectionSubtypeSmartAlbumAllHidden),
                    @"RecentlyAdded" : @(PHAssetCollectionSubtypeSmartAlbumRecentlyAdded),
                    @"Bursts" : @(PHAssetCollectionSubtypeSmartAlbumBursts),
                    @"SlomoVideos" : @(PHAssetCollectionSubtypeSmartAlbumSlomoVideos),
                    @"UserLibrary" : @(PHAssetCollectionSubtypeSmartAlbumUserLibrary),
                    @"SelfPortraits" : @(PHAssetCollectionSubtypeSmartAlbumSelfPortraits),
                    @"Screenshots" : @(PHAssetCollectionSubtypeSmartAlbumScreenshots),
                    @"DepthEffect" : @(PHAssetCollectionSubtypeSmartAlbumDepthEffect),
                    @"LivePhotos" : @(PHAssetCollectionSubtypeSmartAlbumLivePhotos),
                    @"Animated" : @(PHAssetCollectionSubtypeSmartAlbumAnimated),
                    @"LongExposure" : @(PHAssetCollectionSubtypeSmartAlbumLongExposures),
                };
                
                NSMutableArray *albumsToShow = [NSMutableArray arrayWithCapacity:smartAlbums.count];
                for (NSString* smartAlbum in smartAlbums) {
                    if (albums[smartAlbum] != nil) {
                        [albumsToShow addObject:albums[smartAlbum]];
                    }
                }
                imagePickerController.assetCollectionSubtypes = albumsToShow;
            }
            
            if ([self.options[@"cropping"] boolValue]) {
                imagePickerController.mediaType = QBImagePickerMediaTypeImage;
            } else {
                NSString *mediaType = self.options[@"mediaType"];
                
                if ([mediaType isEqualToString:@"photo"]) {
                    imagePickerController.mediaType = QBImagePickerMediaTypeImage;
                } else if ([mediaType isEqualToString:@"video"]) {
                    imagePickerController.mediaType = QBImagePickerMediaTypeVideo;
                } else {
                    imagePickerController.mediaType = QBImagePickerMediaTypeAny;
                }
            }
            
            [imagePickerController setModalPresentationStyle: UIModalPresentationFullScreen];
            [[self getRootVC] presentViewController:imagePickerController animated:YES completion:nil];
        });
    }];
}

RCT_EXPORT_METHOD(openCropper:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    [self setConfiguration:options resolver:resolve rejecter:reject];
    self.currentSelectionMode = CROPPING;
    
    NSString *path = options[@"path"];
    
    [[self.bridge moduleForName:@"ImageLoader" lazilyLoadIfNecessary:YES] loadImageWithURLRequest:[RCTConvert NSURLRequest:path]
                                                                                         callback:^(NSError *error, UIImage *image) {
        if (error) {
            self.reject(ERROR_CROPPER_IMAGE_NOT_FOUND_KEY, ERROR_CROPPER_IMAGE_NOT_FOUND_MSG, nil);
        } else {
            [self cropImage:[image fixOrientation]];
        }
    }];
}

- (PHPickerConfiguration *)makeConfiguration API_AVAILABLE(ios(14)){
    PHPickerConfiguration *configuration = [[PHPickerConfiguration alloc] init];
    configuration.selectionLimit = [self.options[@"multiple"] boolValue] ? 0 : 1;

    NSString *mediaType = self.options[@"mediaType"];
    if ([mediaType isEqualToString:@"video"] && ![self.options[@"cropping"] boolValue]) {
        configuration.filter = [PHPickerFilter videosFilter];
    } else {
        configuration.filter = [PHPickerFilter imagesFilter];
    }

    return configuration;
}

- (void)showActivityIndicator:(void (^)(UIActivityIndicatorView*, UIView*))handler {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *mainView = [[self getRootVC] view];
        
        // create overlay
        UIView *loadingView = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
        loadingView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.5];
        loadingView.clipsToBounds = YES;
        
        // create loading spinner
        UIActivityIndicatorView *activityView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        activityView.frame = CGRectMake(65, 40, activityView.bounds.size.width, activityView.bounds.size.height);
        activityView.center = loadingView.center;
        [loadingView addSubview:activityView];
        
        // create message
        UILabel *loadingLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 115, 130, 22)];
        loadingLabel.backgroundColor = [UIColor clearColor];
        loadingLabel.textColor = [UIColor whiteColor];
        loadingLabel.adjustsFontSizeToFitWidth = YES;
        CGPoint loadingLabelLocation = loadingView.center;
        loadingLabelLocation.y += [activityView bounds].size.height;
        loadingLabel.center = loadingLabelLocation;
        loadingLabel.textAlignment = NSTextAlignmentCenter;
        loadingLabel.text = self.options[@"loadingLabelText"];
        [loadingLabel setFont:[UIFont boldSystemFontOfSize:18]];
        [loadingView addSubview:loadingLabel];
        
        // show all
        [mainView addSubview:loadingView];
        [activityView startAnimating];
        
        handler(activityView, loadingView);
    });
}

- (void) handleVideo:(AVAsset*)asset
        withFileName:(NSString*)fileName
 withLocalIdentifier:(NSString*)localIdentifier
          completion:(void (^)(NSDictionary* image))completion {
    NSURL *sourceURL = [(AVURLAsset *)asset URL];
    
    // create temp file
    NSString *tmpDirFullPath = [self getTmpDirectory];
    NSString *filePath = [tmpDirFullPath stringByAppendingString:[[NSUUID UUID] UUIDString]];
    filePath = [filePath stringByAppendingString:@".mp4"];
    NSURL *outputURL = [NSURL fileURLWithPath:filePath];
    
    [self.compression compressVideo:sourceURL outputURL:outputURL withOptions:self.options handler:^(AVAssetExportSession *exportSession) {
        if (exportSession.status == AVAssetExportSessionStatusCompleted) {
            AVAsset *compressedAsset = [AVAsset assetWithURL:outputURL];
            AVAssetTrack *track = [[compressedAsset tracksWithMediaType:AVMediaTypeVideo] firstObject];
            
            NSNumber *fileSizeValue = nil;
            [outputURL getResourceValue:&fileSizeValue
                                 forKey:NSURLFileSizeKey
                                  error:nil];
            
            AVURLAsset *durationFromUrl = [AVURLAsset assetWithURL:outputURL];
            CMTime time = [durationFromUrl duration];
            int milliseconds = ceil(time.value/time.timescale) * 1000;
            
            completion([self createAttachmentResponse:[outputURL absoluteString]
                                             withExif:nil
                                        withSourceURL:[sourceURL absoluteString]
                                  withLocalIdentifier:localIdentifier
                                         withFilename:fileName
                                            withWidth:@(track.naturalSize.width)
                                           withHeight:@(track.naturalSize.height)
                                             withMime:@"video/mp4"
                                             withSize:fileSizeValue
                                         withDuration:@(milliseconds)
                                             withData:nil
                                             withRect:CGRectNull
                                     withCreationDate:nil
                                 withModificationDate:nil
                        ]);
        } else {
            completion(nil);
        }
    }];
}

- (void) getVideoAsset:(PHAsset*)forAsset completion:(void (^)(NSDictionary* image))completion {
    PHImageManager *manager = [PHImageManager defaultManager];
    PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
    options.version = PHVideoRequestOptionsVersionOriginal;
    options.networkAccessAllowed = YES;
    options.deliveryMode = PHVideoRequestOptionsDeliveryModeHighQualityFormat;
    
    [manager requestAVAssetForVideo:forAsset
                            options:options
                      resultHandler:^(AVAsset * asset, AVAudioMix * audioMix, NSDictionary *info) {
        [self handleVideo:asset
             withFileName:[forAsset valueForKey:@"filename"]
      withLocalIdentifier:forAsset.localIdentifier
               completion:completion
         ];
    }];
}

- (NSDictionary*) createAttachmentResponse:(NSString*)filePath
                                  withExif:(NSDictionary*) exif
                             withSourceURL:(NSString*)sourceURL
                       withLocalIdentifier:(NSString*)localIdentifier
                              withFilename:(NSString*)filename
                                 withWidth:(NSNumber*)width
                                withHeight:(NSNumber*)height
                                  withMime:(NSString*)mime
                                  withSize:(NSNumber*)size
                              withDuration:(NSNumber*)duration
                                  withData:(NSString*)data
                                  withRect:(CGRect)cropRect
                          withCreationDate:(NSDate*)creationDate
                      withModificationDate:(NSDate*)modificationDate {
    return @{
        @"path": (filePath && ![filePath isEqualToString:(@"")]) ? filePath : [NSNull null],
        @"sourceURL": (sourceURL) ? sourceURL : [NSNull null],
        @"localIdentifier": (localIdentifier) ? localIdentifier : [NSNull null],
        @"filename": (filename) ? filename : [NSNull null],
        @"width": width,
        @"height": height,
        @"mime": mime,
        @"size": size,
        @"data": (data) ? data : [NSNull null],
        @"exif": (exif) ? exif : [NSNull null],
        @"cropRect": CGRectIsNull(cropRect) ? [NSNull null] : [ImageCropPicker cgRectToDictionary:cropRect],
        @"creationDate": (creationDate) ? [NSString stringWithFormat:@"%.0f", [creationDate timeIntervalSince1970]] : [NSNull null],
        @"modificationDate": (modificationDate) ? [NSString stringWithFormat:@"%.0f", [modificationDate timeIntervalSince1970]] : [NSNull null],
        @"duration": (duration) ? duration : [NSNull null]
    };
}

// See https://stackoverflow.com/questions/4147311/finding-image-type-from-nsdata-or-uiimage
- (NSString *)determineMimeTypeFromImageData:(NSData *)data {
    uint8_t c;
    [data getBytes:&c length:1];
    
    switch (c) {
        case 0xFF:
            return @"image/jpeg";
        case 0x89:
            return @"image/png";
        case 0x47:
            return @"image/gif";
        case 0x49:
        case 0x4D:
            return @"image/tiff";
        case 0x00:
            return @"image/heic";
    }
    return @"";
}

// when user selected single image, with camera or from photo gallery,
// this method will take care of attaching image metadata, and sending image to cropping controller
// or to user directly
- (void) processSingleImagePick:(UIImage*)image
                       withExif:(NSDictionary*)exif
             withViewController:(UIViewController*)viewController
                  withSourceURL:(NSString*)sourceURL
            withLocalIdentifier:(NSString*)localIdentifier
                   withFilename:(NSString*)filename
               withCreationDate:(NSDate*)creationDate
           withModificationDate:(NSDate*)modificationDate {
    
    if (image == nil) {
        [viewController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
            self.reject(ERROR_PICKER_NO_DATA_KEY, ERROR_PICKER_NO_DATA_MSG, nil);
        }]];
        return;
    }
    
    NSLog(@"id: %@ filename: %@", localIdentifier, filename);
    
    if ([self.options[@"cropping"] boolValue]) {
        self.croppingFile = [[NSMutableDictionary alloc] init];
        self.croppingFile[@"sourceURL"] = sourceURL;
        self.croppingFile[@"localIdentifier"] = localIdentifier;
        self.croppingFile[@"filename"] = filename;
        self.croppingFile[@"creationDate"] = creationDate;
        self.croppingFile[@"modifcationDate"] = modificationDate;
        NSLog(@"CroppingFile %@", self.croppingFile);
        
        [self cropImage:[image fixOrientation]];
    } else {
        ImageResult *imageResult = [self.compression compressImage:[image fixOrientation]  withOptions:self.options];
        NSString *filePath = [self persistFile:imageResult.data];
        if (filePath == nil) {
            [viewController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
            }]];
            return;
        }
        
        // Wait for viewController to dismiss before resolving, or we lose the ability to display
        // Alert.alert in the .then() handler.
        [viewController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
            self.resolve([self createAttachmentResponse:filePath
                                               withExif:exif
                                          withSourceURL:sourceURL
                                    withLocalIdentifier:localIdentifier
                                           withFilename:filename
                                              withWidth:imageResult.width
                                             withHeight:imageResult.height
                                               withMime:imageResult.mime
                                               withSize:@(imageResult.data.length)
                                           withDuration: nil
                                               withData:[self.options[@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                               withRect:CGRectNull
                                       withCreationDate:creationDate
                                   withModificationDate:modificationDate
                          ]);
        }]];
    }
}

// at the moment it is not possible to upload image by reading PHAsset
// we are saving image and saving it to the tmp location where we are allowed to access image later
- (NSString*) persistFile:(NSData*)data {
    // create temp file
    NSString *tmpDirFullPath = [self getTmpDirectory];
    NSString *filePath = [tmpDirFullPath stringByAppendingString:[[NSUUID UUID] UUIDString]];
    filePath = [filePath stringByAppendingString:@".jpg"];
    
    // save cropped file
    BOOL status = [data writeToFile:filePath atomically:YES];
    if (!status) {
        return nil;
    }
    
    return filePath;
}

+ (NSDictionary *)cgRectToDictionary:(CGRect)rect {
    return @{
        @"x": @(rect.origin.x),
        @"y": @(rect.origin.y),
        @"width": @(CGRectGetWidth(rect)),
        @"height": @(CGRectGetHeight(rect))
    };
}

#pragma mark - TOCCropViewController Implementation
- (void)cropImage:(UIImage *)image {
    dispatch_async(dispatch_get_main_queue(), ^{
        TOCropViewController *cropVC;
        if ([self.options[@"cropperCircleOverlay"] boolValue]) {
            cropVC = [[TOCropViewController alloc] initWithCroppingStyle:TOCropViewCroppingStyleCircular image:image];
        } else {
            cropVC = [[TOCropViewController alloc] initWithImage:image];
            CGFloat widthRatio = [self.options[@"width"] floatValue];
            CGFloat heightRatio = [self.options[@"height"] floatValue];
            if (widthRatio > 0 && heightRatio > 0){
                CGSize aspectRatio = CGSizeMake(widthRatio, heightRatio);
                cropVC.customAspectRatio = aspectRatio;
                
            }
            cropVC.aspectRatioLockEnabled = ![self.options[@"freeStyleCropEnabled"] boolValue];
            cropVC.resetAspectRatioEnabled = !cropVC.aspectRatioLockEnabled;
        }
        
        cropVC.title = self.options[@"cropperToolbarTitle"];
        cropVC.delegate = self;
        
        cropVC.doneButtonTitle = self.options[@"cropperChooseText"];
        cropVC.cancelButtonTitle = self.options[@"cropperCancelText"];
        cropVC.rotateButtonsHidden = [self.options[@"cropperRotateButtonsHidden"] boolValue];
        
        cropVC.modalPresentationStyle = UIModalPresentationFullScreen;
        
        [[self getRootVC] presentViewController:cropVC animated:FALSE completion:nil];
    });
}

@end

@implementation ImageCropPicker(UIImagePickerControllerDelegate)

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    NSString* mediaType = info[UIImagePickerControllerMediaType];

    if (CFStringCompare ((__bridge CFStringRef) mediaType, kUTTypeMovie, 0) == kCFCompareEqualTo) {
        NSURL *url = info[UIImagePickerControllerMediaURL];
        AVURLAsset *asset = [AVURLAsset assetWithURL:url];
        NSString *fileName = [[asset.URL path] lastPathComponent];

        [self handleVideo:asset
             withFileName:fileName
      withLocalIdentifier:nil
               completion:^(NSDictionary* video) {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (video == nil) {
                    [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                        self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                    }]];
                    return;
                }

                [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                    self.resolve(video);
                }]];
            });
        }];
    } else {
        UIImage *chosenImage = info[UIImagePickerControllerOriginalImage];

        NSDictionary *exif;
        if([self.options[@"includeExif"] boolValue]) {
            exif = info[UIImagePickerControllerMediaMetadata];
        }

        [self processSingleImagePick:chosenImage
                            withExif:exif
                  withViewController:picker
                       withSourceURL:self.croppingFile[@"sourceURL"]
                 withLocalIdentifier:self.croppingFile[@"localIdentifier"]
                        withFilename:self.croppingFile[@"filename"]
                    withCreationDate:self.croppingFile[@"creationDate"]
                withModificationDate:self.croppingFile[@"modificationDate"]];
    }
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
        self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    }]];
}

@end

@implementation ImageCropPicker(TOCropViewControllerDelegate)

- (void)cropViewController:(TOCropViewController *)cropViewController
            didCropToImage:(UIImage *)image
                  withRect:(CGRect)cropRect
                     angle:(NSInteger)angle {
    [self imageCropViewController:cropViewController didCropImage:image usingCropRect:cropRect];
}

- (void)cropViewController:(TOCropViewController *)cropViewController didFinishCancelled:(BOOL)cancelled {
    [self dismissCropper:cropViewController selectionDone:NO completion:[self waitAnimationEnd:^{
        if (self.currentSelectionMode == CROPPING) {
            self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
        }
    }]];
}

// The original image has been cropped.
- (void)imageCropViewController:(UIViewController *)controller
                   didCropImage:(UIImage *)croppedImage
                  usingCropRect:(CGRect)cropRect {

    // we have correct rect, but not correct dimensions
    // so resize image
    CGSize desiredImageSize = CGSizeMake([self.options[@"width"] intValue],
                                         [self.options[@"height"] intValue]);

    UIImage *resizedImage = [croppedImage resizedImageToFitInSize:desiredImageSize scaleIfSmaller:YES];
    ImageResult *imageResult = [self.compression compressImage:resizedImage withOptions:self.options];

    NSString *filePath = [self persistFile:imageResult.data];
    if (filePath == nil) {
        [self dismissCropper:controller selectionDone:YES completion:[self waitAnimationEnd:^{
            self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
        }]];
        return;
    }

    NSDictionary* exif = nil;
    if([self.options[@"includeExif"] boolValue]) {
        exif = [[CIImage imageWithData:imageResult.data] properties];
    }

    [self dismissCropper:controller selectionDone:YES completion:[self waitAnimationEnd:^{
        self.resolve([self createAttachmentResponse:filePath
                                           withExif: exif
                                      withSourceURL: self.croppingFile[@"sourceURL"]
                                withLocalIdentifier: self.croppingFile[@"localIdentifier"]
                                       withFilename: self.croppingFile[@"filename"]
                                          withWidth:imageResult.width
                                         withHeight:imageResult.height
                                           withMime:imageResult.mime
                                           withSize:@(imageResult.data.length)
                                       withDuration: nil
                                           withData:[self.options[@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                           withRect:cropRect
                                   withCreationDate:self.croppingFile[@"creationDate"]
                               withModificationDate:self.croppingFile[@"modificationDate"]
                      ]);
    }]];
}

- (void)dismissCropper:(UIViewController *)controller selectionDone:(BOOL)selectionDone completion:(void (^)(void))completion {
    switch (self.currentSelectionMode) {
        case CROPPING:
            [controller dismissViewControllerAnimated:YES completion:completion];
            break;
        case PICKER:
            if (selectionDone) {
                [controller.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:completion];
            } else {
                // if user opened picker, tried to crop image, and cancelled cropping
                // return him to the image selection instead of returning him to the app
                [controller.presentingViewController dismissViewControllerAnimated:YES completion:completion];
            }
            break;
        case CAMERA:
            [controller.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:completion];
            break;
    }
}

@end

@implementation ImageCropPicker(QBImagePickerControllerDelegate)

- (void)qb_imagePickerController:(QBImagePickerController *)imagePickerController
          didFinishPickingAssets:(NSArray *)assets {

    PHImageManager *manager = [PHImageManager defaultManager];
    PHImageRequestOptions* options = [[PHImageRequestOptions alloc] init];
    options.synchronous = NO;
    options.networkAccessAllowed = YES;

    if ([self.options[@"multiple"] boolValue]) {
        NSMutableArray *selections = [[NSMutableArray alloc] init];

        [self showActivityIndicator:^(UIActivityIndicatorView *indicatorView, UIView *overlayView) {
            NSLock *lock = [[NSLock alloc] init];
            __block int processed = 0;

            for (PHAsset *phAsset in assets) {
                if (phAsset.mediaType == PHAssetMediaTypeVideo) {
                    [self getVideoAsset:phAsset completion:^(NSDictionary* video) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [lock lock];

                            if (video == nil) {
                                [indicatorView stopAnimating];
                                [overlayView removeFromSuperview];
                                [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                    self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                                }]];
                                return;
                            }

                            [selections addObject:video];
                            processed++;
                            [lock unlock];

                            if (processed == [assets count]) {
                                [indicatorView stopAnimating];
                                [overlayView removeFromSuperview];
                                [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                    self.resolve(selections);
                                }]];
                                return;
                            }
                        });
                    }];
                } else {
                    [phAsset requestContentEditingInputWithOptions:nil
                                                 completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                        [manager requestImageDataForAsset:phAsset
                                                  options:options
                                            resultHandler:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {

                            NSURL *sourceURL = contentEditingInput.fullSizeImageURL;

                            dispatch_async(dispatch_get_main_queue(), ^{
                                [lock lock];
                                @autoreleasepool {
                                    UIImage *image = [UIImage imageWithData:imageData];

                                    ImageResult *imageResult = [self makeResultFromImageData:imageData image:image];

                                    NSString *filePath = @"";
                                    if([self.options[@"writeTempFile"] boolValue]) {

                                        filePath = [self persistFile:imageResult.data];

                                        if (filePath == nil) {
                                            [indicatorView stopAnimating];
                                            [overlayView removeFromSuperview];
                                            [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                                self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
                                            }]];
                                            return;
                                        }
                                    }

                                    NSDictionary* exif = nil;
                                    if([self.options[@"includeExif"] boolValue]) {
                                        exif = [[CIImage imageWithData:imageData] properties];
                                    }

                                    [selections addObject:[self createAttachmentResponse:filePath
                                                                                withExif: exif
                                                                           withSourceURL:[sourceURL absoluteString]
                                                                     withLocalIdentifier: phAsset.localIdentifier
                                                                            withFilename: [phAsset valueForKey:@"filename"]
                                                                               withWidth:imageResult.width
                                                                              withHeight:imageResult.height
                                                                                withMime:imageResult.mime
                                                                                withSize:@(imageResult.data.length)
                                                                            withDuration: nil
                                                                                withData:[self.options[@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                                                                withRect:CGRectNull
                                                                        withCreationDate:phAsset.creationDate
                                                                    withModificationDate:phAsset.modificationDate
                                                           ]];
                                }
                                processed++;
                                [lock unlock];

                                if (processed == [assets count]) {

                                    [indicatorView stopAnimating];
                                    [overlayView removeFromSuperview];
                                    [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                        self.resolve(selections);
                                    }]];
                                    return;
                                }
                            });
                        }];
                    }];
                }
            }
        }];
    } else {
        PHAsset *phAsset = assets[0];

        [self showActivityIndicator:^(UIActivityIndicatorView *indicatorView, UIView *overlayView) {
            if (phAsset.mediaType == PHAssetMediaTypeVideo) {
                [self getVideoAsset:phAsset completion:^(NSDictionary* video) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [indicatorView stopAnimating];
                        [overlayView removeFromSuperview];
                        [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                            if (video != nil) {
                                self.resolve(video);
                            } else {
                                self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                            }
                        }]];
                    });
                }];
            } else {
                [phAsset requestContentEditingInputWithOptions:nil
                                             completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                    [manager requestImageDataForAsset:phAsset
                                              options:options
                                        resultHandler:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                        NSURL *sourceURL = contentEditingInput.fullSizeImageURL;
                        NSDictionary* exif;
                        if([self.options[@"includeExif"] boolValue]) {
                            exif = [[CIImage imageWithData:imageData] properties];
                        }

                        dispatch_async(dispatch_get_main_queue(), ^{
                            [indicatorView stopAnimating];
                            [overlayView removeFromSuperview];

                            [self processSingleImagePick:[UIImage imageWithData:imageData]
                                                withExif: exif
                                      withViewController:imagePickerController
                                           withSourceURL:[sourceURL absoluteString]
                                     withLocalIdentifier:phAsset.localIdentifier
                                            withFilename:[phAsset valueForKey:@"filename"]
                                        withCreationDate:phAsset.creationDate
                                    withModificationDate:phAsset.modificationDate];
                        });
                    }];
                }];
            }
        }];
    }
}

- (ImageResult *)makeResultFromImageData:(NSData *)data image:(UIImage *)image {
    Boolean forceJpg = [[self.options valueForKey:@"forceJpg"] boolValue];

    NSNumber *compressQuality = [self.options valueForKey:@"compressImageQuality"];
    Boolean isLossless = (compressQuality == nil || [compressQuality floatValue] >= 0.8);

    NSNumber *maxWidth = [self.options valueForKey:@"compressImageMaxWidth"];
    Boolean useOriginalWidth = (maxWidth == nil || [maxWidth integerValue] >= image.size.width);

    NSNumber *maxHeight = [self.options valueForKey:@"compressImageMaxHeight"];
    Boolean useOriginalHeight = (maxHeight == nil || [maxHeight integerValue] >= image.size.height);

    NSString *mimeType = [self determineMimeTypeFromImageData:data];
    Boolean isKnownMimeType = [mimeType length] > 0;

    ImageResult *imageResult = [[ImageResult alloc] init];
    if (isLossless && useOriginalWidth && useOriginalHeight && isKnownMimeType && !forceJpg) {
        // Use original, unmodified image
        imageResult.data = data;
        imageResult.width = @(image.size.width);
        imageResult.height = @(image.size.height);
        imageResult.mime = mimeType;
        imageResult.image = image;
    } else {
        imageResult = [self.compression compressImage:[image fixOrientation] withOptions:self.options];
    }
    return imageResult;
}

- (void)qb_imagePickerControllerDidCancel:(QBImagePickerController *)imagePickerController {
    [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
        self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    }]];
}

@end

@implementation ImageCropPicker(PHPickerViewControllerDelegate)

- (void)picker:(PHPickerViewController *)picker didFinishPicking:(NSArray<PHPickerResult *> *)results API_AVAILABLE(ios(14)){
    if (results.count == 0) {
        [picker dismissViewControllerAnimated:YES completion:^{
            self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
        }];
        return;
    }

    [self showActivityIndicator:^(UIActivityIndicatorView *indicatorView, UIView *overlayView) {
        NSMutableArray *selection = [[NSMutableArray alloc] initWithCapacity:results.count];

        NSLock *lock = [[NSLock alloc] init];
        __block NSUInteger processed = 0;

        for (PHPickerResult* result in results) {
            NSItemProvider *provider = result.itemProvider;

            if ([provider hasItemConformingToTypeIdentifier:@"public.movie"]) {
                [provider loadFileRepresentationForTypeIdentifier:@"public.movie"
                                                completionHandler:^(NSURL * _Nullable url, NSError * _Nullable error) {
                    [lock lock];

                    [self makeResponseForURL:url completion:^(NSDictionary *video) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            if (video == nil) {
                                [indicatorView stopAnimating];
                                [overlayView removeFromSuperview];
                                [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                    self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                                }]];
                                return;
                            }


                            [selection addObject:video];
                            processed++;
                            [lock unlock];

                            if (processed == results.count) {
                                [indicatorView stopAnimating];
                                [overlayView removeFromSuperview];
                                [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                    self.resolve(selection);
                                }]];
                            }
                        });
                    }];
                }];
            } else if ([provider canLoadObjectOfClass:[UIImage class]]) {
                NSString *identifier = provider.registeredTypeIdentifiers.firstObject;
                [provider loadFileRepresentationForTypeIdentifier:identifier
                                                completionHandler:^(NSURL * _Nullable url, NSError * _Nullable error) {
                    [lock lock];

                    NSURL *targetURL = [self copyFileFromURL:url];

                    NSDictionary *exif;
                    if ([self.options[@"includeExif"] boolValue]) {
                        exif = [self exifAndGPSDataFromURL:targetURL];
                    }
                    NSData *imageData = [[NSData alloc] initWithContentsOfFile:targetURL.path];
                    UIImage *image = [[UIImage alloc] initWithData:imageData];

                    ImageResult *imageResult = [self makeResultFromImageData:imageData image:image];

                    NSDictionary *attachment = [self createAttachmentResponse:targetURL.absoluteString
                                                                     withExif:exif
                                                                withSourceURL:targetURL.absoluteString
                                                          withLocalIdentifier:provider.suggestedName
                                                                 withFilename:url.lastPathComponent
                                                                    withWidth:imageResult.width
                                                                   withHeight:imageResult.height
                                                                     withMime:imageResult.mime
                                                                     withSize:@(imageResult.data.length)
                                                                 withDuration:nil
                                                                     withData:[self.options[@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                                                     withRect:CGRectNull
                                                             withCreationDate:[self dateForFileAtURL:url key:NSURLCreationDateKey]
                                                         withModificationDate:[self dateForFileAtURL:url key:NSURLContentModificationDateKey]];
                    [selection addObject:attachment];
                    processed++;
                    [lock unlock];

                    if (processed == results.count) {
                        dispatch_async(dispatch_get_main_queue(), ^{
                            [indicatorView stopAnimating];
                            [overlayView removeFromSuperview];
                            [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                                self.resolve(selection);
                            }]];
                        });
                    }
                }];
            }
        }
    }];
}

- (void)makeResponseForURL:(NSURL *)url completion:(void (^)(NSDictionary* video))completion {
    NSURL *targetURL = [self copyFileFromURL:url];
    AVURLAsset *asset = [AVURLAsset assetWithURL:targetURL];
    NSString *fileName = url.lastPathComponent;
    [self handleVideo:asset withFileName:fileName withLocalIdentifier:nil completion:completion];
}

- (NSURL *)copyFileFromURL:(NSURL *)url {
    NSString *path = [[NSTemporaryDirectory() stringByStandardizingPath] stringByAppendingPathComponent:url.lastPathComponent];
    NSURL *targetURL = [NSURL fileURLWithPath:path];

    [[NSFileManager defaultManager] copyItemAtURL:url toURL:targetURL error:nil];

    return targetURL;
}

- (NSDictionary *)exifAndGPSDataFromURL:(NSURL *)url {
    CGImageSourceRef ref = CGImageSourceCreateWithURL((__bridge CFURLRef) url, nil);
    NSDictionary *properties = CFBridgingRelease(CGImageSourceCopyPropertiesAtIndex(ref, 0, nil));

    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    [data addEntriesFromDictionary:properties[(NSString *)kCGImagePropertyExifDictionary]];
    [data addEntriesFromDictionary:properties[(NSString *)kCGImagePropertyGPSDictionary]];

    return [data copy];
}

- (NSDate *)dateForFileAtURL:(NSURL *)url key:(NSURLResourceKey)key {
    NSDate *date;
    [url getResourceValue:&date forKey:key error:nil];
    return date;
}

@end
