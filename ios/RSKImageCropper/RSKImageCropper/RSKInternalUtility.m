//
//  RSKInternalUtility.m
//  RSKImageCropperExample
//
//  Created by Ruslan Skorb on 9/5/15.
//  Copyright (c) 2015 Ruslan Skorb. All rights reserved.
//

#import "RSKInternalUtility.h"

NSString * RSKLocalizedString(NSString *key, NSString *comment)
{
    return [[[RSKInternalUtility class] bundleForStrings] localizedStringForKey:key value:key table:@"RSKImageCropper"];
}

@implementation RSKInternalUtility

+ (NSBundle *)bundleForStrings
{
    static NSBundle *bundle;
    
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSBundle *bundleForClass = [NSBundle bundleForClass:[self class]];
        NSString *stringsBundlePath = [bundleForClass pathForResource:@"RSKImageCropperStrings" ofType:@"bundle"];
        bundle = [NSBundle bundleWithPath:stringsBundlePath] ?: bundleForClass;
    });
    
    return bundle;
}

@end
