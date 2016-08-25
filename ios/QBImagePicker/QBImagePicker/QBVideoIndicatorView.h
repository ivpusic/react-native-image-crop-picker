//
//  QBVideoIndicatorView.h
//  QBImagePicker
//
//  Created by Katsuma Tanaka on 2015/04/04.
//  Copyright (c) 2015 Katsuma Tanaka. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "QBVideoIconView.h"
#import "QBSlomoIconView.h"

@interface QBVideoIndicatorView : UIView

@property (nonatomic, weak) IBOutlet UILabel *timeLabel;
@property (nonatomic, weak) IBOutlet QBVideoIconView *videoIcon;
@property (nonatomic, weak) IBOutlet QBSlomoIconView *slomoIcon;


@end
