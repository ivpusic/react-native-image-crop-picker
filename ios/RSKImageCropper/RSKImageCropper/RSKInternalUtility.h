//
//  RSKInternalUtility.h
//  RSKImageCropperExample
//
//  Created by Ruslan Skorb on 9/5/15.
//  Copyright (c) 2015 Ruslan Skorb. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 Returns a localized version of the string designated by the specified key and residing in the RSKImageCropper table.
 
 @param key The key for a string in the RSKImageCropper table.
 @param comment The comment to place above the key-value pair in the strings file.
 
 @return A localized version of the string designated by key in the RSKImageCropper table.
 */
FOUNDATION_EXPORT NSString * RSKLocalizedString(NSString *key, NSString *comment);

@interface RSKInternalUtility : NSObject

/**
 Returns the NSBundle object for returning localized strings.
 
 @return The NSBundle object for returning localized strings.
 
 @discussion We assume a convention of a bundle named RSKImageCropperStrings.bundle, otherwise we
 return the bundle associated with the RSKInternalUtility class.
 */
+ (NSBundle *)bundleForStrings;

@end
