//
//  ImageManager.m
//
//  Created by Ivan Pusic on 5/4/16.
//  Copyright © 2016 Facebook. All rights reserved.
//

#import "ImageCropPicker.h"
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVAsset.h>
#import <MobileCoreServices/MobileCoreServices.h>
#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_KEY @"E_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR"
#define ERROR_PICKER_CANNOT_RUN_CAMERA_ON_SIMULATOR_MSG @"Cannot run camera on simulator"

#define ERROR_PICKER_NO_CAMERA_PERMISSION_KEY @"E_PICKER_NO_CAMERA_PERMISSION"
#define ERROR_PICKER_NO_CAMERA_PERMISSION_MSG @"User did not grant camera permission."

#define ERROR_PICKER_UNAUTHORIZED_KEY @"E_PERMISSION_MISSING"
#define ERROR_PICKER_UNAUTHORIZED_MSG @"Cannot access images. Please allow access if you want to be able to select images."

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

@implementation ImageResult
@end

@interface LabeledCropView : RSKImageCropViewController {
}
@property NSString *toolbarTitle;
@property UILabel *_moveAndScaleLabel;
- (UILabel *)moveAndScaleLabel;
@end

@implementation LabeledCropView
- (UILabel *)moveAndScaleLabel
{
    if (!self._moveAndScaleLabel) {
        self._moveAndScaleLabel = [[UILabel alloc] init];
        self._moveAndScaleLabel.backgroundColor = [UIColor clearColor];
        self._moveAndScaleLabel.text = self.toolbarTitle;
        self._moveAndScaleLabel.textColor = [UIColor whiteColor];
        self._moveAndScaleLabel.translatesAutoresizingMaskIntoConstraints = NO;
        self._moveAndScaleLabel.opaque = NO;
    }
    return self._moveAndScaleLabel;
}
@end

@implementation ImageCropPicker

RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (instancetype)init
{
    if (self = [super init]) {
        self.arrImageProcessId = [NSMutableArray array];
        self.arrQuality  = @[@"HighQuality", @"MediumQuality", @"LowQuality"];
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
                                @"compressImageQuality": @1,
                                @"compressVideoPreset": @"MediumQuality",
                                @"loadingLabelText": @"Processing assets...",
                                @"mediaType": @"any",
                                @"showsSelectedCount": @YES
                                };
        self.compression = [[Compression alloc] init];
    }
    
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (void (^ __nullable)(void))waitAnimationEnd:(void (^ __nullable)(void))completion {
    if ([[self.options objectForKey:@"waitAnimationEnd"] boolValue]) {
        return completion;
    }
    
    if (completion != nil) {
        completion();
    }
    
    return nil;
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
    self.currentSelectionMode = CAMERA;
    
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
        
        NSString *mediaType = [self.options objectForKey:@"mediaType"];
        if ([[self.options objectForKey:@"useFrontCamera"] boolValue]) {
            picker.cameraDevice = UIImagePickerControllerCameraDeviceFront;
        }

        if ([mediaType isEqualToString:@"video"]) {
            
            
            NSString *presetKey = [self.options valueForKey:@"compressVideoPreset"];
            if (!presetKey) {
                presetKey = @"MediumQuality";
            }
            
            int item = [self.arrQuality indexOfObject:presetKey];
            switch (item) {
                case 0:
                    picker.videoQuality = UIImagePickerControllerQualityTypeHigh;
                    break;
                case 1:
                    picker.videoQuality = UIImagePickerControllerQualityTypeMedium;
                    break;
                case 2:
                    picker.videoQuality = UIImagePickerControllerQualityTypeLow;
                    break;
                default:
                    picker.videoQuality = UIImagePickerControllerQualityTypeMedium;
                    break;
            }
            picker.mediaTypes = @[(NSString *)kUTTypeMovie];
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [[self getRootVC] presentViewController:picker animated:YES completion:nil];
        });
    }];
#endif
}

- (void)viewDidLoad {
    [self viewDidLoad];
}

- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info {
    UIImage *chosenImage = [info objectForKey:UIImagePickerControllerOriginalImage];
    
    NSDictionary *exif;
    if([[self.options objectForKey:@"includeExif"] boolValue]) {
        exif = [info objectForKey:UIImagePickerControllerMediaMetadata];
    }

    NSURL *fileURL = info[UIImagePickerControllerMediaURL];
    NSString *mediaType = [self.options objectForKey:@"mediaType"];
    if ([mediaType isEqualToString:@"video"]) {
        NSString *fileExtension = [fileURL pathExtension];
        NSString *UTI = (__bridge_transfer NSString *)UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)fileExtension, NULL);
        NSString *contentType = (__bridge_transfer NSString *)UTTypeCopyPreferredTagWithClass((__bridge CFStringRef)UTI, kUTTagClassMIMEType);
        AVURLAsset *avAsset = [AVURLAsset URLAssetWithURL:fileURL options:nil];
        NSArray *compatiblePresets = [AVAssetExportSession exportPresetsCompatibleWithAsset:avAsset];
        [self getVideoAssetFromCamera:avAsset fileExtension:fileExtension mimeType:contentType  completion:^(NSDictionary* video) {
            dispatch_async(dispatch_get_main_queue(), ^{
                
                [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                    if (video != nil) {
                        self.resolve(video);
                    } else {
                        self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                    }
                }]];
            });
        }];
        
        
    }
    else {
        if([[self.options objectForKey:@"includeExif"] boolValue]) {
            exif = [info objectForKey:UIImagePickerControllerMediaMetadata];
        }
    NSData* imageData = UIImageJPEGRepresentation(chosenImage, 1);
    [self processSingleImagePick:imageData withExif:exif withViewController:picker withSourceURL:self.croppingFile[@"sourceURL"] withLocalIdentifier:self.croppingFile[@"localIdentifier"] withFilename:self.croppingFile[@"filename"] withCreationDate:self.croppingFile[@"creationDate"] withModificationDate:self.croppingFile[@"modificationDate"]];
    }
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
        self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    }]];
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
        BOOL deleted = [[NSFileManager defaultManager] removeItemAtPath:[NSString stringWithFormat:@"%@%@", tmpDirectoryPath, file] error:NULL];
        
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
    
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        if (status != PHAuthorizationStatusAuthorized) {
            self.reject(ERROR_PICKER_UNAUTHORIZED_KEY, ERROR_PICKER_UNAUTHORIZED_MSG, nil);
            return;
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            // init picker
            QBImagePickerController *imagePickerController =
            [QBImagePickerController new];
            imagePickerController.delegate = self;
            imagePickerController.allowsMultipleSelection = [[self.options objectForKey:@"multiple"] boolValue];
            imagePickerController.minimumNumberOfSelection = abs([[self.options objectForKey:@"minFiles"] intValue]);
            imagePickerController.maximumNumberOfSelection = abs([[self.options objectForKey:@"maxFiles"] intValue]);
            imagePickerController.showsNumberOfSelectedAssets = [[self.options objectForKey:@"showsSelectedCount"] boolValue];
            
            NSArray *smartAlbums = [self.options objectForKey:@"smartAlbums"];
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
                    if ([albums objectForKey:smartAlbum] != nil) {
                        [albumsToShow addObject:[albums objectForKey:smartAlbum]];
                    }
                }
                imagePickerController.assetCollectionSubtypes = albumsToShow;
            }
            
            if ([[self.options objectForKey:@"cropping"] boolValue]) {
                imagePickerController.mediaType = QBImagePickerMediaTypeImage;
            } else {
                NSString *mediaType = [self.options objectForKey:@"mediaType"];
                
                if ([mediaType isEqualToString:@"any"]) {
                    imagePickerController.mediaType = QBImagePickerMediaTypeAny;
                } else if ([mediaType isEqualToString:@"photo"]) {
                    imagePickerController.mediaType = QBImagePickerMediaTypeImage;
                } else {
                    imagePickerController.mediaType = QBImagePickerMediaTypeVideo;
                }
                
            }
            
            [[self getRootVC] presentViewController:imagePickerController animated:YES completion:nil];
        });
    }];
}

RCT_EXPORT_METHOD(openCropper:(NSDictionary *)options
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    [self setConfiguration:options resolver:resolve rejecter:reject];
    self.currentSelectionMode = CROPPING;
    
    NSString *path = [options objectForKey:@"path"];
    
    [self.bridge.imageLoader loadImageWithURLRequest:[RCTConvert NSURLRequest:path] callback:^(NSError *error, UIImage *image) {
        if (error) {
            self.reject(ERROR_CROPPER_IMAGE_NOT_FOUND_KEY, ERROR_CROPPER_IMAGE_NOT_FOUND_MSG, nil);
        } else {
            [self startCropping:[image fixOrientation]];
        }
    }];
}

- (void)startCropping:(UIImage *)image {
    LabeledCropView *imageCropVC = [[LabeledCropView alloc] initWithImage:image];
    if ([[[self options] objectForKey:@"cropperCircleOverlay"] boolValue]) {
        imageCropVC.cropMode = RSKImageCropModeCircle;
    } else {
        imageCropVC.cropMode = RSKImageCropModeCustom;
    }
    imageCropVC.toolbarTitle = [[self options] objectForKey:@"cropperToolbarTitle"];
    imageCropVC.avoidEmptySpaceAroundImage = YES;
    imageCropVC.dataSource = self;
    imageCropVC.delegate = self;
    [imageCropVC setModalPresentationStyle:UIModalPresentationCustom];
    [imageCropVC setModalTransitionStyle:UIModalTransitionStyleCrossDissolve];
    dispatch_async(dispatch_get_main_queue(), ^{
        [[self getRootVC] presentViewController:imageCropVC animated:YES completion:nil];
    });
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
        if (self.loadingLabel == nil) {
            self.loadingLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 115, 130, 22)];
            self.loadingLabel.backgroundColor = [UIColor clearColor];
            self.loadingLabel.textColor = [UIColor whiteColor];
            self.loadingLabel.adjustsFontSizeToFitWidth = YES;
            CGPoint loadingLabelLocation = loadingView.center;
            loadingLabelLocation.y += [activityView bounds].size.height;
            self.loadingLabel.center = loadingLabelLocation;
            self.loadingLabel.textAlignment = NSTextAlignmentCenter;
        }
        self.loadingLabel.text = [self.options objectForKey:@"loadingLabelText"];
        [self.loadingLabel setFont:[UIFont boldSystemFontOfSize:18]];
        [loadingView addSubview:self.loadingLabel];
        
        // PS :- Created Cancel Button      
        
        self.btnCancel = [[UIButton alloc]initWithFrame:CGRectMake(0, loadingView.frame.size.height-50, loadingView.frame.size.width, 50)];
        
        [self.btnCancel addTarget:self action:@selector(btnCancelAction:) forControlEvents:UIControlEventTouchUpInside];
        
        [self.btnCancel setTitle:@"Cancel" forState:UIControlStateNormal];
        [self.btnCancel setTitle:@"Cancel" forState:UIControlStateSelected];
        [self.btnCancel setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        self.btnCancel.backgroundColor = [UIColor whiteColor];
        [loadingView addSubview:self.btnCancel];
        
        // show all
        [mainView addSubview:loadingView];
        [activityView startAnimating];
        
        handler(activityView, loadingView);
    });
}



-(IBAction)btnCancelAction:(id)sender{
    for (NSNumber *num in self.arrImageProcessId) {
        [self.manager cancelImageRequest:[num integerValue]];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *vc =   [self getRootVC] ;
        [vc dismissViewControllerAnimated:true completion:^{
            self.manager = nil;
            [self.arrImageProcessId removeAllObjects];
            self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
        }];
    });
    
}


- (void) getVideoAssetFromCamera:(AVURLAsset*)avAsset fileExtension:(NSString *)fileExtension mimeType:(NSString *)mimetype completion:(void (^)(NSDictionary* image))completion {
    @try {
        
        
        NSArray *compatiblePresets = [AVAssetExportSession exportPresetsCompatibleWithAsset:avAsset];
        
        if ([compatiblePresets containsObject:AVAssetExportPresetLowQuality])
        {
            AVAssetExportSession *exportSession = [[AVAssetExportSession alloc]initWithAsset:avAsset presetName:AVAssetExportPresetPassthrough];
            // save to temp directory
            
            NSString *strFileName = [[[NSUUID UUID] UUIDString] stringByAppendingString:[NSString stringWithFormat:@".%@",fileExtension]];
            NSString *filePath = [[self getTmpDirectory] stringByAppendingString:strFileName];
            
            
            NSURL *outputURL = [NSURL fileURLWithPath:filePath];
            
            exportSession.outputURL = outputURL;
            NSLog(@"videopath of your  file = %@",filePath);  // PATH OF YOUR FILE
            exportSession.outputFileType = AVFileTypeMPEG4;
            
            [exportSession exportAsynchronouslyWithCompletionHandler:^{
                
                if ([exportSession status] == AVAssetExportSessionStatusCompleted) {
                    AVAsset *compressedAsset = [AVAsset assetWithURL:outputURL];
                    
                    AVAssetTrack *videoTrack = [[compressedAsset tracksWithMediaType:AVMediaTypeVideo] firstObject];
                    NSNumber *filesize = nil;
                    [outputURL getResourceValue:&filesize forKey:NSURLFileSizeKey error:nil];
                    NSDictionary *video = [self createAttachmentResponse:[outputURL absoluteString]
                                                                withExif:nil
                                                           withSourceURL:nil
                                                     withLocalIdentifier:@""
                                                            withFilename:strFileName
                                                               withWidth:[NSNumber numberWithFloat:videoTrack.naturalSize.width]
                                                              withHeight:[NSNumber numberWithFloat:videoTrack.naturalSize.height]
                                                                withMime:mimetype
                                                                withSize:filesize
                                                                withData:nil
                                                                withRect:CGRectNull
                                                        withCreationDate:[NSDate date]
                                                    withModificationDate:[NSDate date]];
                    completion(video);
                }
                
            }];
            
        }
    } @catch(NSException *e) {
        
    }
}

- (void) getVideoAsset:(PHAsset*)forAsset completion:(void (^)(NSDictionary* image))completion {
    NSString *loadingLabelTextTemp = [self.options objectForKey:@"loadingLabelText"];
    self.manager = [PHImageManager defaultManager];
    PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
    options.version = PHVideoRequestOptionsVersionCurrent;
    // This is for videos stored in iCloud
    options.networkAccessAllowed = YES;
    options.deliveryMode = PHVideoRequestOptionsDeliveryModeFastFormat;
    // Only dispatched when asset is in iCloud
    options.progressHandler = ^(double progress, NSError *error, BOOL *stop, NSDictionary *info){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.loadingLabel setText:@"Downloading from iCloud..."];
        });
    };
    
    
    // PS :- get Video File Name with Extension and Mime Type from PHAsset Object
    NSString *mimeType = @"";
    NSString *filename = [forAsset valueForKey:@"filename"];
    CFStringRef UTI = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)[filename pathExtension], NULL);
    CFStringRef MIMETYPE = UTTypeCopyPreferredTagWithClass(UTI, kUTTagClassMIMEType);
    
    CFRelease(UTI);
    if (MIMETYPE) {
        mimeType = (NSString *)CFBridgingRelease(MIMETYPE);
    }
    
    NSString *strFileName = filename;
    NSString *filePath = [[self getTmpDirectory] stringByAppendingString:strFileName];
    NSURL *outputURL = [NSURL fileURLWithPath:filePath];
    // Get compression presets
    NSString *presetKey = [self.options valueForKey:@"compressVideoPreset"];
    if (presetKey == nil) {
        presetKey = @"MediumQuality";
    }
    NSString *preset = [[self.compression exportPresets] valueForKey:presetKey];
    if (preset == nil) {
        preset = AVAssetExportPresetMediumQuality;
    }
    
    PHImageRequestID imageRequest = [self.manager requestExportSessionForVideo:forAsset
                                                                       options:options
                                                                  exportPreset:preset
                                                                 resultHandler:^(AVAssetExportSession * _Nullable exportSession, NSDictionary * _Nullable info) {
                                                                     dispatch_async(dispatch_get_main_queue(), ^{
                                                                         [self.loadingLabel setText:loadingLabelTextTemp];
                                                                     });
                                                                     exportSession.shouldOptimizeForNetworkUse = YES;
                                                                     exportSession.outputURL = outputURL;
                                                                     exportSession.outputFileType = AVFileTypeMPEG4;
                                                                     [exportSession exportAsynchronouslyWithCompletionHandler:^{
                                                                         if (exportSession.status == AVAssetExportSessionStatusCompleted) {
                                                                             AVAsset *compressedAsset = [AVAsset assetWithURL:outputURL];
                                                                             AVAssetTrack *videoTrack = [[compressedAsset tracksWithMediaType:AVMediaTypeVideo] firstObject];
                                                                             NSNumber *filesize = nil;
                                                                             [outputURL getResourceValue:&filesize forKey:NSURLFileSizeKey error:nil];
                                                                             NSDictionary *video = [self createAttachmentResponse:[outputURL absoluteString]
                                                                                                                         withExif:nil
                                                                                                                    withSourceURL:nil
                                                                                                              withLocalIdentifier:forAsset.localIdentifier
                                                                                                                     withFilename:[forAsset valueForKey:@"filename"]
                                                                                                                        withWidth:[NSNumber numberWithFloat:videoTrack.naturalSize.width]
                                                                                                                       withHeight:[NSNumber numberWithFloat:videoTrack.naturalSize.height]
                                                                                                                         withMime:mimeType                                                                                    withSize:filesize
                                                                                                                         withData:nil
                                                                                                                         withRect:CGRectNull
                                                                                                                 withCreationDate:forAsset.creationDate
                                                                                                             withModificationDate:forAsset.modificationDate];
                                                                             completion(video);
                                                                         }
                                                                     }];
                                                                 }];
    [self.arrImageProcessId addObject:[NSNumber numberWithInt: imageRequest]];
}

- (NSDictionary*) createAttachmentResponse:(NSString*)filePath withExif:(NSDictionary*) exif withSourceURL:(NSString*)sourceURL withLocalIdentifier:(NSString*)localIdentifier withFilename:(NSString*)filename withWidth:(NSNumber*)width withHeight:(NSNumber*)height withMime:(NSString*)mime withSize:(NSNumber*)size withData:(NSString*)data withRect:(CGRect)cropRect withCreationDate:(NSDate*)creationDate withModificationDate:(NSDate*)modificationDate {
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
    }
    return @"image/jpeg";
}

- (NSString *)determineExtensionFromImageData:(NSData *)data {
    uint8_t c;
    [data getBytes:&c length:1];
    
    switch (c) {
        case 0xFF:
            return @".jpg";
        case 0x89:
            return @".png";
        case 0x47:
            return @".gif";
        case 0x49:
        case 0x4D:
            return @".tiff";
    }
    return @".jpg";
}

static ImageResult * getResizedCompressedImageIfNeedded(ImageCropPicker *object, NSData *imageData) {
    UIImage *imgT = [UIImage imageWithData:imageData];
    
    NSNumber *compressQuality = [object.options valueForKey:@"compressImageQuality"];
    Boolean isLossless = (compressQuality == nil || [compressQuality floatValue] == 1);
    
    NSNumber *maxWidth = [object.options valueForKey:@"compressImageMaxWidth"];
    Boolean useOriginalWidth = (maxWidth == nil || [maxWidth integerValue] >= imgT.size.width);
    
    NSNumber *maxHeight = [object.options valueForKey:@"compressImageMaxHeight"];
    Boolean useOriginalHeight = (maxHeight == nil || [maxHeight integerValue] >= imgT.size.height);
    NSString* mime = [object determineMimeTypeFromImageData:imageData];
    ImageResult *imageResult = [[ImageResult alloc] init];
    if ([mime isEqualToString:@"image/gif"] || (isLossless && useOriginalWidth && useOriginalHeight)) {
        // Use original, unmodified image
        imageResult.data = imageData;
        imageResult.width = @(imgT.size.width);
        imageResult.height = @(imgT.size.height);
        imageResult.mime = mime;
        imageResult.image = imgT;
    } else {
        imageResult = [object.compression compressImage:[imgT fixOrientation] withOptions:object.options];
    }
    return imageResult;
}

- (void)qb_imagePickerController:
(QBImagePickerController *)imagePickerController
          didFinishPickingAssets:(NSArray *)assets {
    
    self.manager = [PHImageManager defaultManager];

    PHImageRequestOptions* options = [[PHImageRequestOptions alloc] init];
    options.synchronous = NO;
    options.networkAccessAllowed = YES;
    
    if ([[[self options] objectForKey:@"multiple"] boolValue]) {
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
                                    [self.arrImageProcessId removeAllObjects];
                                    self.resolve(selections);
                                    
                                }]];
                                return;
                            }
                        });
                    }];
                } else {
                    [self.manager
                     requestImageDataForAsset:phAsset
                     options:options
                     resultHandler:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                         
                         NSURL *sourceURL = [info objectForKey:@"PHImageFileURLKey"];
                         
                         dispatch_async(dispatch_get_main_queue(), ^{
                             [lock lock];
                             @autoreleasepool {
                                 ImageResult * imageResult = getResizedCompressedImageIfNeedded(self, imageData);
                                 
                                 NSString *filePath = @"";
                                 if([[self.options objectForKey:@"writeTempFile"] boolValue]) {
                                     
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
                                 if([[self.options objectForKey:@"includeExif"] boolValue]) {
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
                                                                             withSize:[NSNumber numberWithUnsignedInteger:imageResult.data.length]
                                                                             withData:[[self.options objectForKey:@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0]: nil
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
                                     [self.arrImageProcessId removeAllObjects];
                                     self.resolve(selections);
                                     
                                 }]];
                                 return;
                             }
                         });
                     }];
                }
            }
        }];
    } else {
        PHAsset *phAsset = [assets objectAtIndex:0];
        
        [self showActivityIndicator:^(UIActivityIndicatorView *indicatorView, UIView *overlayView) {
            if (phAsset.mediaType == PHAssetMediaTypeVideo) {
                [self getVideoAsset:phAsset completion:^(NSDictionary* video) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [indicatorView stopAnimating];
                        [overlayView removeFromSuperview];
                        [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
                            if (video != nil) {
                                [self.arrImageProcessId removeAllObjects];
                                self.resolve(video);
                            } else {
                                self.reject(ERROR_CANNOT_PROCESS_VIDEO_KEY, ERROR_CANNOT_PROCESS_VIDEO_MSG, nil);
                            }
                        }]];
                    });
                }];
            } else {
                [self.manager
                 requestImageDataForAsset:phAsset
                 options:options
                 resultHandler:^(NSData *imageData, NSString *dataUTI,
                                 UIImageOrientation orientation,
                                 NSDictionary *info) {
                     NSURL *sourceURL = [info objectForKey:@"PHImageFileURLKey"];
                     NSDictionary* exif;
                     if([[self.options objectForKey:@"includeExif"] boolValue]) {
                         exif = [[CIImage imageWithData:imageData] properties];
                     }
                     
                     dispatch_async(dispatch_get_main_queue(), ^{
                         [indicatorView stopAnimating];
                         [overlayView removeFromSuperview];
                         [self.arrImageProcessId removeAllObjects];   
                         [self processSingleImagePick:imageData

                                             withExif: exif
                                   withViewController:imagePickerController
                                        withSourceURL:[sourceURL absoluteString]
                                  withLocalIdentifier:phAsset.localIdentifier
                                         withFilename:[phAsset valueForKey:@"filename"]
                                     withCreationDate:phAsset.creationDate
                                 withModificationDate:phAsset.modificationDate];
                     });
                 }];
            }
        }];
    }
}

- (void)qb_imagePickerControllerDidCancel:(QBImagePickerController *)imagePickerController {
    [imagePickerController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
        self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
    }]];
}

// when user selected single image, with camera or from photo gallery,
// this method will take care of attaching image metadata, and sending image to cropping controller
// or to user directly

- (void) processSingleImagePick:(NSData*)imageData withExif:(NSDictionary*) exif withViewController:(UIViewController*)viewController withSourceURL:(NSString*)sourceURL withLocalIdentifier:(NSString*)localIdentifier withFilename:(NSString*)filename withCreationDate:(NSDate*)creationDate withModificationDate:(NSDate*)modificationDate {
    UIImage* image = [UIImage imageWithData:imageData];

    if (image == nil) {
        [viewController dismissViewControllerAnimated:YES completion:[self waitAnimationEnd:^{
            self.reject(ERROR_PICKER_NO_DATA_KEY, ERROR_PICKER_NO_DATA_MSG, nil);
        }]];
        return;
    }
    
    NSLog(@"id: %@ filename: %@", localIdentifier, filename);
    
    if ([[[self options] objectForKey:@"cropping"] boolValue]) {
        self.croppingFile = [[NSMutableDictionary alloc] init];
        self.croppingFile[@"sourceURL"] = sourceURL;
        self.croppingFile[@"localIdentifier"] = localIdentifier;
        self.croppingFile[@"filename"] = filename;
        self.croppingFile[@"creationDate"] = creationDate;
        self.croppingFile[@"modifcationDate"] = modificationDate;
        NSLog(@"CroppingFile %@", self.croppingFile);
        
        [self startCropping:[image fixOrientation]];
    } else {
        
        ImageResult * imageResult = getResizedCompressedImageIfNeedded(self, imageData);

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
                                               withSize:[NSNumber numberWithUnsignedInteger:imageResult.data.length]
                                               withData:[[self.options objectForKey:@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                               withRect:CGRectNull
                                       withCreationDate:creationDate
                                   withModificationDate:modificationDate
                          ]);
        }]];
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
    [self dismissCropper:controller selectionDone:NO completion:[self waitAnimationEnd:^{
        if (self.currentSelectionMode == CROPPING) {
            self.reject(ERROR_PICKER_CANCEL_KEY, ERROR_PICKER_CANCEL_MSG, nil);
        }
    }]];
}

- (void) dismissCropper:(RSKImageCropViewController*)controller selectionDone:(BOOL)selectionDone completion:(void (^)())completion {
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

// The original image has been cropped.
- (void)imageCropViewController:(RSKImageCropViewController *)controller
                   didCropImage:(UIImage *)croppedImage
                  usingCropRect:(CGRect)cropRect {
    
    // we have correct rect, but not correct dimensions
    // so resize image
    CGSize resizedImageSize = CGSizeMake([[[self options] objectForKey:@"width"] intValue], [[[self options] objectForKey:@"height"] intValue]);
    UIImage *resizedImage = [croppedImage resizedImageToFitInSize:resizedImageSize scaleIfSmaller:YES];
    ImageResult *imageResult = [self.compression compressImage:resizedImage withOptions:self.options];
    
    NSString *filePath = [self persistFile:imageResult.data];
    if (filePath == nil) {
        [self dismissCropper:controller selectionDone:YES completion:[self waitAnimationEnd:^{
            self.reject(ERROR_CANNOT_SAVE_IMAGE_KEY, ERROR_CANNOT_SAVE_IMAGE_MSG, nil);
        }]];
        return;
    }
    
    NSDictionary* exif = nil;
    if([[self.options objectForKey:@"includeExif"] boolValue]) {
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
                                           withSize:[NSNumber numberWithUnsignedInteger:imageResult.data.length]
                                           withData:[[self.options objectForKey:@"includeBase64"] boolValue] ? [imageResult.data base64EncodedStringWithOptions:0] : nil
                                           withRect:cropRect
                                   withCreationDate:self.croppingFile[@"creationDate"]
                               withModificationDate:self.croppingFile[@"modificationDate"]
                      ]);
    }]];
}

// at the moment it is not possible to upload image by reading PHAsset
// we are saving image and saving it to the tmp location where we are allowed to access image later
- (NSString*) persistFile:(NSData*)data {
    // create temp file
    NSString *tmpDirFullPath = [self getTmpDirectory];
    NSString *filePath = [tmpDirFullPath stringByAppendingString:[[NSUUID UUID] UUIDString]];
    NSString* ext = [self determineExtensionFromImageData:data];
    filePath = [filePath stringByAppendingString:ext];
    
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



+ (NSDictionary *)cgRectToDictionary:(CGRect)rect {
    return @{
             @"x": [NSNumber numberWithFloat: rect.origin.x],
             @"y": [NSNumber numberWithFloat: rect.origin.y],
             @"width": [NSNumber numberWithFloat: CGRectGetWidth(rect)],
             @"height": [NSNumber numberWithFloat: CGRectGetHeight(rect)]
             };
}

@end
