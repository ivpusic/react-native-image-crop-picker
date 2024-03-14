// Copyright © 2021 Ivan Pusic. All rights reserved.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol WindowProvider <NSObject>

@property (nonatomic, readonly) UIWindow *window;

@end

NS_ASSUME_NONNULL_END
