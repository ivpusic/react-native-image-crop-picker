//
//  Compression.m
//  imageCropPicker
//
//  Created by Ivan Pusic on 12/24/16.
//  Copyright © 2016 Ivan Pusic. All rights reserved.
//

#import "Compression.h"
#import "Scale.h"

@implementation Compression

- (instancetype)init {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] initWithDictionary:@{
                                                                                 @"640x480": AVAssetExportPreset640x480,
                                                                                 @"960x540": AVAssetExportPreset960x540,
                                                                                 @"1280x720": AVAssetExportPreset1280x720,
                                                                                 @"1920x1080": AVAssetExportPreset1920x1080,
                                                                                 @"LowQuality": AVAssetExportPresetLowQuality,
                                                                                 @"MediumQuality": AVAssetExportPresetMediumQuality,
                                                                                 @"HighestQuality": AVAssetExportPresetHighestQuality,
                                                                                 @"Passthrough": AVAssetExportPresetPassthrough,
                                                                                 }];
    
    if (@available(iOS 9.0, *)) {
        [dic addEntriesFromDictionary:@{@"3840x2160": AVAssetExportPreset3840x2160}];
    } else {
        // Fallback on earlier versions
    }
    
    self.exportPresets = dic;
    
    return self;
}

- (ImageResult*) compressImage:(UIImage*)image
                  toDimensions:(CGSize)dimensions
                    intoResult:(ImageResult*)result {

    NSLog(@"image-crop-picker: scaling image width x height: %i x %i -> %i x %i", (int)image.size.width, (int)image.size.height, (int)dimensions.width, (int)dimensions.height);
    
    UIGraphicsBeginImageContext(dimensions);
    [image drawInRect:CGRectMake(0, 0, dimensions.width, dimensions.height)];
    UIImage *resizedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    result.width = [NSNumber numberWithFloat:dimensions.width];
    result.height = [NSNumber numberWithFloat:dimensions.height];
    result.image = resizedImage;
    return result;
}

- (NSInteger) getPixelCountForImage:(UIImage*)image {
    return image.size.width * image.size.height;
}

- (ImageResult*) compressImage:(UIImage*)image
                   withOptions:(NSDictionary*)options {
    
    ImageResult *result = [[ImageResult alloc] init];
    result.width = @(image.size.width);
    result.height = @(image.size.height);
    result.image = image;
    result.mime = @"image/jpeg";
    
    NSNumber *compressImageMaxWidth = [options valueForKey:@"compressImageMaxWidth"];
    NSNumber *compressImageMaxHeight = [options valueForKey:@"compressImageMaxHeight"];
    NSNumber *compressImageMaxPixels = [options valueForKey:@"compressImageMaxPixels"];
    if (!compressImageMaxPixels) compressImageMaxPixels = 0;
    
    if (compressImageMaxPixels > 0) {
        NSLog(@"image-crop-picker: scaling image to max pixels: %@", compressImageMaxPixels);
        BOOL shouldResizeImage = [self getPixelCountForImage:image] > [compressImageMaxPixels intValue];
        if (shouldResizeImage) {
            CGSize dimensions = [Scale scaleWidth:image.size.width andHeight:image.size.height maxPixels:[compressImageMaxPixels floatValue]];
            [self compressImage:image toDimensions:dimensions intoResult:result];
        }
    } else {
        NSLog(@"image-crop-picker: scaling image to max width/height: %@/%@", compressImageMaxWidth, compressImageMaxHeight);
        // determine if it is necessary to resize image
        BOOL shouldResizeWidth = (compressImageMaxWidth != nil && [compressImageMaxWidth floatValue] < image.size.width);
        BOOL shouldResizeHeight = (compressImageMaxHeight != nil && [compressImageMaxHeight floatValue] < image.size.height);
        
        if (shouldResizeWidth || shouldResizeHeight) {
            CGFloat maxWidth = compressImageMaxWidth != nil ? [compressImageMaxWidth floatValue] : image.size.width;
            CGFloat maxHeight = compressImageMaxHeight != nil ? [compressImageMaxHeight floatValue] : image.size.height;
            
            CGSize dimensions = [Scale scaleWidth:image.size.width andHeight:image.size.height maxWidth:maxWidth maxHeight:maxHeight];
            [self compressImage:image toDimensions:dimensions intoResult:result];
        }
    }
    
    // parse desired image quality
    NSNumber *compressQuality = [options valueForKey:@"compressImageQuality"];
    if (compressQuality == nil) {
        compressQuality = [NSNumber numberWithFloat:0.8];
    }
    
    NSLog(@"image-crop-picker: compressing image with image quality: %@", compressQuality);

    // convert image to jpeg representation
    result.data = UIImageJPEGRepresentation(result.image, [compressQuality floatValue]);
    
    return result;
}

- (void)compressVideo:(NSURL*)inputURL
            outputURL:(NSURL*)outputURL
          withOptions:(NSDictionary*)options
              handler:(void (^)(AVAssetExportSession*))handler {
    
    NSString *presetKey = [options valueForKey:@"compressVideoPreset"];
    if (presetKey == nil) {
        presetKey = @"MediumQuality";
    }
    
    NSString *preset = [self.exportPresets valueForKey:presetKey];
    if (preset == nil) {
        preset = AVAssetExportPresetMediumQuality;
    }
    
    [[NSFileManager defaultManager] removeItemAtURL:outputURL error:nil];
    AVURLAsset *asset = [AVURLAsset URLAssetWithURL:inputURL options:nil];
    AVAssetExportSession *exportSession = [[AVAssetExportSession alloc] initWithAsset:asset presetName:preset];
    exportSession.shouldOptimizeForNetworkUse = YES;
    exportSession.outputURL = outputURL;
    exportSession.outputFileType = AVFileTypeMPEG4;
    
    [exportSession exportAsynchronouslyWithCompletionHandler:^(void) {
        handler(exportSession);
    }];
}

@end
