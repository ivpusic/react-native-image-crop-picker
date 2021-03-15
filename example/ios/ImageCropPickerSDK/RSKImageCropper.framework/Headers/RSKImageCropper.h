//
// RSKImageCropper.h
//
// Copyright (c) 2014-present Ruslan Skorb, http://ruslanskorb.com/
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

/**
 `RSKImageCropper` is an image cropper for iOS like in the Contacts app with support for landscape orientation.
 */

#import <Foundation/Foundation.h>

//! Project version number for RSKImageCropper.
FOUNDATION_EXPORT double RSKImageCropperVersionNumber;

//! Project version string for RSKImageCropper.
FOUNDATION_EXPORT const unsigned char RSKImageCropperVersionString[];

#import <RSKImageCropper/CGGeometry+RSKImageCropper.h>
#import <RSKImageCropper/RSKImageCropViewController.h>
#import <RSKImageCropper/RSKImageCropViewController+Protected.h>
#import <RSKImageCropper/RSKImageScrollView.h>
#import <RSKImageCropper/RSKInternalUtility.h>
#import <RSKImageCropper/RSKTouchView.h>
#import <RSKImageCropper/UIApplication+RSKImageCropper.h>
#import <RSKImageCropper/UIImage+RSKImageCropper.h>
