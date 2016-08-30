//
//  QBAlbumsViewController.h
//  QBImagePicker
//
//  Created by Katsuma Tanaka on 2015/04/03.
//  Copyright (c) 2015 Katsuma Tanaka. All rights reserved.
//

#import <UIKit/UIKit.h>

@class QBImagePickerController;

@interface QBAlbumsViewController : UITableViewController

@property (nonatomic, weak) QBImagePickerController *imagePickerController;

@end
