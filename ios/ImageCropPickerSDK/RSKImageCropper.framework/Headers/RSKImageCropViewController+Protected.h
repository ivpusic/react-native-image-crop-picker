//
// RSKImageCropViewController+Protected.h
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

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/**
 The methods in the RSKImageCropViewControllerProtectedMethods category
 typically should only be called by subclasses which are implementing new
 image crop view controllers. They may be overridden but must call super.
 */
@interface RSKImageCropViewController (RSKImageCropViewControllerProtectedMethods)

/**
 Asynchronously crops the original image in accordance with the current settings and tells the delegate that the original image will be / has been cropped.
 */
- (void)cropImage;

/**
 Tells the delegate that the crop has been canceled.
 */
- (void)cancelCrop;

/**
 Resets the rotation angle, the position and the zoom scale of the original image to the default values.
 
 @param animated Set this value to YES to animate the reset.
 */
- (void)reset:(BOOL)animated;

/**
 Sets the current rotation angle of the image in radians.
 
 @param rotationAngle The rotation angle of the image in radians.
 */
- (void)setRotationAngle:(CGFloat)rotationAngle;

/**
 Sets the current scale factor for the image.
 
 @param zoomScale The scale factor for the image.
 */
- (void)setZoomScale:(CGFloat)zoomScale;

@end

NS_ASSUME_NONNULL_END
