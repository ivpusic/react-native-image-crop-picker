//
//  Scale.h
//  imageCropPicker
//
//  Created by AP on 13/07/2019.
//  Copyright © 2019 Ivan Pusic. All rights reserved.
//


#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
//#import <AVFoundation/AVFoundation.h>

@interface Scale : NSObject

+ (CGSize) scaleWidth:(CGFloat)width
            andHeight:(CGFloat)height
             maxWidth:(CGFloat)maxWidth
            maxHeight:(CGFloat)maxHeight;
    

+ (CGSize) scaleWidth:(CGFloat)width
            andHeight:(CGFloat)height
            maxPixels:(CGFloat)maxPixels;

@end
