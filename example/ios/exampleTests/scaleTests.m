//
//  scaleTests.m
//  example
//
//  Created by AP on 14/07/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <XCTest/XCTest.h>
#import "Scale.h"

@interface scaleTests : XCTestCase

@end

@implementation scaleTests

- (void)testScaleByMaxDimensions {
  CGSize scaled = [Scale scaleWidth:(CGFloat)4032 andHeight:3024 maxWidth:2560 maxHeight:2560];
  XCTAssertEqual(scaled.width, (double)2560);
  XCTAssertEqual(scaled.height, (double)1920);
}

- (void)testScaleByMaxPixels {
  CGSize scaled = [Scale scaleWidth:(CGFloat)4032 andHeight:3024 maxPixels:5000000];
  XCTAssertEqual((int)scaled.width, (int)2581);
  XCTAssertEqual((int)scaled.height, (int)1936);
}

@end
