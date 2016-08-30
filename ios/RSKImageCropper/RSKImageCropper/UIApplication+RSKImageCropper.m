//
// UIApplication+RSKImageCropper.m
//
// Copyright (c) 2015 Ruslan Skorb, http://ruslanskorb.com/
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

#import "UIApplication+RSKImageCropper.h"
#import <objc/runtime.h>

static IMP rsk_sharedApplicationOriginalImplementation;

@implementation UIApplication (RSKImageCropper)

+ (void)load
{
    // When you build an extension based on an Xcode template, you get an extension bundle that ends in .appex.
    // https://developer.apple.com/library/ios/documentation/General/Conceptual/ExtensibilityPG/ExtensionCreation.html
    if (![[[NSBundle mainBundle] bundlePath] hasSuffix:@".appex"]) {
        Method sharedApplicationMethod = class_getClassMethod([UIApplication class], @selector(sharedApplication));
        if (sharedApplicationMethod != NULL) {
            IMP sharedApplicationMethodImplementation = method_getImplementation(sharedApplicationMethod);
            Method rsk_sharedApplicationMethod = class_getClassMethod([UIApplication class], @selector(rsk_sharedApplication));
            rsk_sharedApplicationOriginalImplementation = method_setImplementation(rsk_sharedApplicationMethod, sharedApplicationMethodImplementation);
        }
    }
}

+ (UIApplication *)rsk_sharedApplication
{
    return nil;
}

+ (IMP)rsk_sharedApplicationOriginalImplementaion
{
    return rsk_sharedApplicationOriginalImplementation;
}

@end
