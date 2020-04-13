//
//  Scale.m
//  imageCropPicker
//
//  Created by AP on 13/07/2019.
//  Copyright © 2019 Ivan Pusic. All rights reserved.
//

#import "Scale.h"

@implementation Scale

+ (CGSize) scaleWidth:(CGFloat)width
            andHeight:(CGFloat)height
             maxWidth:(CGFloat)maxWidth
            maxHeight:(CGFloat)maxHeight {
    
    CGFloat scaleFactor = ((maxWidth / width) < (maxHeight / height)) ? (maxWidth / width) : (maxHeight / height);
    
    int newWidth = width * scaleFactor;
    int newHeight = height * scaleFactor;

    return CGSizeMake(newWidth, newHeight);
}

+ (CGSize) scaleWidth:(CGFloat)width
            andHeight:(CGFloat)height
            maxPixels:(CGFloat)maxPixels {

    CGFloat currentPixels = width * height;
    CGFloat scale = 1.0;
    if (currentPixels > maxPixels && currentPixels > 0 && maxPixels > 0) {
        scale = 1.0 / sqrt(currentPixels / maxPixels);
    }
    
    CGFloat newWidth = (int)(width * scale);
    CGFloat newHeight = (int)(height * scale);

    return CGSizeMake(newWidth, newHeight);
}

@end
