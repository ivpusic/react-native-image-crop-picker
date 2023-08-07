//
//  QBAlbumsViewController.m
//  QBImagePicker
//
//  Created by Katsuma Tanaka on 2015/04/03.
//  Copyright (c) 2015 Katsuma Tanaka. All rights reserved.
//

#import "QBAlbumsViewController.h"
#import <Photos/Photos.h>
#import <PhotosUI/PhotosUI.h>

// Views
#import "QBAlbumCell.h"

// ViewControllers
#import "QBImagePickerController.h"
#import "QBAssetsViewController.h"

static CGSize CGSizeScale(CGSize size, CGFloat scale) {
    return CGSizeMake(size.width * scale, size.height * scale);
}

static bool isLimitedPermission() {
    if (@available(iOS 14, *)) {
        PHAuthorizationStatus accessLevel = [PHPhotoLibrary authorizationStatusForAccessLevel: PHAccessLevelReadWrite];
        return accessLevel == PHAuthorizationStatusLimited;
    }
    return false;
}

static bool isDarkMode() {
    if (@available(iOS 13.0, *)) {
        UITraitCollection *current = UITraitCollection.currentTraitCollection;
        return current.userInterfaceStyle == UIUserInterfaceStyleDark;
    }
    return false;
}

@interface QBImagePickerController (Private)

@property (nonatomic, strong) NSBundle *assetBundle;

@end

@interface QBAlbumsViewController () <PHPhotoLibraryChangeObserver>

@property (nonatomic, strong) IBOutlet UIBarButtonItem *doneButton;

@property (nonatomic, copy) NSArray *fetchResults;
@property (nonatomic, copy) NSArray *assetCollections;

@end

@implementation QBAlbumsViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setUpToolbarItems];
    
    // Fetch user albums and smart albums
    PHFetchResult *smartAlbums = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeSmartAlbum subtype:PHAssetCollectionSubtypeAny options:nil];
    PHFetchResult *userAlbums = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum subtype:PHAssetCollectionSubtypeAny options:nil];
    self.fetchResults = @[smartAlbums, userAlbums];
    
    [self updateAssetCollections];
    
    // Register observer
    [[PHPhotoLibrary sharedPhotoLibrary] registerChangeObserver:self];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    // Configure navigation item
    self.navigationItem.title = NSLocalizedStringFromTableInBundle(@"albums.title", @"QBImagePicker", self.imagePickerController.assetBundle, nil);
    self.navigationItem.prompt = self.imagePickerController.prompt;
    
    // Show/hide 'Done' button
    if (self.imagePickerController.allowsMultipleSelection) {
        [self.navigationItem setRightBarButtonItem:self.doneButton animated:NO];
    } else {
        [self.navigationItem setRightBarButtonItem:nil animated:NO];
    }
    
    [self updateControlState];
    [self updateSelectionInfo];
}

- (void)dealloc
{
    // Deregister observer
    [[PHPhotoLibrary sharedPhotoLibrary] unregisterChangeObserver:self];
}


#pragma mark - Storyboard

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    QBAssetsViewController *assetsViewController = segue.destinationViewController;
    assetsViewController.imagePickerController = self.imagePickerController;
    assetsViewController.assetCollection = self.assetCollections[self.tableView.indexPathForSelectedRow.row];
}


#pragma mark - Actions

- (IBAction)cancel:(id)sender
{
    if ([self.imagePickerController.delegate respondsToSelector:@selector(qb_imagePickerControllerDidCancel:)]) {
        [self.imagePickerController.delegate qb_imagePickerControllerDidCancel:self.imagePickerController];
    }
}

- (IBAction)done:(id)sender
{
    if ([self.imagePickerController.delegate respondsToSelector:@selector(qb_imagePickerController:didFinishPickingAssets:)]) {
        [self.imagePickerController.delegate qb_imagePickerController:self.imagePickerController
                                               didFinishPickingAssets:self.imagePickerController.selectedAssets.array];
    }
}


#pragma mark - Toolbar

- (void)setUpToolbarItems
{
    // Space
    UIBarButtonItem *leftSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:NULL];
    UIBarButtonItem *rightSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:NULL];
    
    // Info label
    NSDictionary *attributes = @{ NSForegroundColorAttributeName: [UIColor blackColor] };
    UIBarButtonItem *infoButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:NULL];
    infoButtonItem.enabled = NO;
    [infoButtonItem setTitleTextAttributes:attributes forState:UIControlStateNormal];
    [infoButtonItem setTitleTextAttributes:attributes forState:UIControlStateDisabled];
    
    self.toolbarItems = @[leftSpace, infoButtonItem, rightSpace];
}

- (void)updateSelectionInfo
{
    NSMutableOrderedSet *selectedAssets = self.imagePickerController.selectedAssets;
    
    if (selectedAssets.count > 0) {
        NSBundle *bundle = self.imagePickerController.assetBundle;
        NSString *format;
        if (selectedAssets.count > 1) {
            format = NSLocalizedStringFromTableInBundle(@"assets.toolbar.items-selected", @"QBImagePicker", bundle, nil);
        } else {
            format = NSLocalizedStringFromTableInBundle(@"assets.toolbar.item-selected", @"QBImagePicker", bundle, nil);
        }
        
        NSString *title = [NSString stringWithFormat:format, selectedAssets.count];
        [(UIBarButtonItem *)self.toolbarItems[1] setTitle:title];
    } else {
        [(UIBarButtonItem *)self.toolbarItems[1] setTitle:@""];
    }
}


#pragma mark - Fetching Asset Collections

- (void)updateAssetCollections
{
    // Filter albums
    NSArray *assetCollectionSubtypes = self.imagePickerController.assetCollectionSubtypes;
    NSMutableDictionary *smartAlbums = [NSMutableDictionary dictionaryWithCapacity:assetCollectionSubtypes.count];
    NSMutableArray *userAlbums = [NSMutableArray array];
    
    for (PHFetchResult *fetchResult in self.fetchResults) {
        [fetchResult enumerateObjectsUsingBlock:^(PHAssetCollection *assetCollection, NSUInteger index, BOOL *stop) {
            PHAssetCollectionSubtype subtype = assetCollection.assetCollectionSubtype;
            
            if (subtype == PHAssetCollectionSubtypeAlbumRegular) {
                [userAlbums addObject:assetCollection];
            } else if ([assetCollectionSubtypes containsObject:@(subtype)]) {
                if (!smartAlbums[@(subtype)]) {
                    smartAlbums[@(subtype)] = [NSMutableArray array];
                }
                [smartAlbums[@(subtype)] addObject:assetCollection];
            }
        }];
    }
    
    NSMutableArray *assetCollections = [NSMutableArray array];

    // Fetch smart albums
    for (NSNumber *assetCollectionSubtype in assetCollectionSubtypes) {
        NSArray *collections = smartAlbums[assetCollectionSubtype];
        
        if (collections) {
            [assetCollections addObjectsFromArray:collections];
        }
    }
    
    // Fetch user albums
    [userAlbums enumerateObjectsUsingBlock:^(PHAssetCollection *assetCollection, NSUInteger index, BOOL *stop) {
        [assetCollections addObject:assetCollection];
    }];
    
    self.assetCollections = assetCollections;
}

- (UIImage *)placeholderImageWithSize:(CGSize)size
{
    UIGraphicsBeginImageContext(size);
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    UIColor *backgroundColor = [UIColor colorWithRed:(239.0 / 255.0) green:(239.0 / 255.0) blue:(244.0 / 255.0) alpha:1.0];
    UIColor *iconColor = [UIColor colorWithRed:(179.0 / 255.0) green:(179.0 / 255.0) blue:(182.0 / 255.0) alpha:1.0];
    
    // Background
    CGContextSetFillColorWithColor(context, [backgroundColor CGColor]);
    CGContextFillRect(context, CGRectMake(0, 0, size.width, size.height));
    
    // Icon (back)
    CGRect backIconRect = CGRectMake(size.width * (16.0 / 68.0),
                                     size.height * (20.0 / 68.0),
                                     size.width * (32.0 / 68.0),
                                     size.height * (24.0 / 68.0));
    
    CGContextSetFillColorWithColor(context, [iconColor CGColor]);
    CGContextFillRect(context, backIconRect);
    
    CGContextSetFillColorWithColor(context, [backgroundColor CGColor]);
    CGContextFillRect(context, CGRectInset(backIconRect, 1.0, 1.0));
    
    // Icon (front)
    CGRect frontIconRect = CGRectMake(size.width * (20.0 / 68.0),
                                      size.height * (24.0 / 68.0),
                                      size.width * (32.0 / 68.0),
                                      size.height * (24.0 / 68.0));
    
    CGContextSetFillColorWithColor(context, [backgroundColor CGColor]);
    CGContextFillRect(context, CGRectInset(frontIconRect, -1.0, -1.0));
    
    CGContextSetFillColorWithColor(context, [iconColor CGColor]);
    CGContextFillRect(context, frontIconRect);
    
    CGContextSetFillColorWithColor(context, [backgroundColor CGColor]);
    CGContextFillRect(context, CGRectInset(frontIconRect, 1.0, 1.0));
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}


#pragma mark - Checking for Selection Limit

- (BOOL)isMinimumSelectionLimitFulfilled
{
    return (self.imagePickerController.minimumNumberOfSelection <= self.imagePickerController.selectedAssets.count);
}

- (BOOL)isMaximumSelectionLimitReached
{
    NSUInteger minimumNumberOfSelection = MAX(1, self.imagePickerController.minimumNumberOfSelection);
    
    if (minimumNumberOfSelection <= self.imagePickerController.maximumNumberOfSelection) {
        return (self.imagePickerController.maximumNumberOfSelection <= self.imagePickerController.selectedAssets.count);
    }
    
    return NO;
}

- (void)updateControlState
{
    self.doneButton.enabled = [self isMinimumSelectionLimitFulfilled];
}

#pragma mark - Manage Limited Permission

-(void)managePermissionAction:(id)sender
{
    UIAlertController *actionSheet = [UIAlertController alertControllerWithTitle:NSLocalizedStringFromTableInBundle(@"permission.title", @"QBImagePicker", self.imagePickerController.assetBundle, nil) message:nil preferredStyle:UIAlertControllerStyleActionSheet];
    actionSheet.popoverPresentationController.sourceView = sender;

    [actionSheet addAction:[UIAlertAction actionWithTitle:NSLocalizedStringFromTableInBundle(@"permission.cancel", @"QBImagePicker", self.imagePickerController.assetBundle, nil) style:UIAlertActionStyleCancel handler:nil]];

    [actionSheet addAction:[UIAlertAction actionWithTitle:NSLocalizedStringFromTableInBundle(@"permission.choose_more", @"QBImagePicker", self.imagePickerController.assetBundle, nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        // Open limited permission picker.
        if (@available(iOS 14, *)) {
            [[PHPhotoLibrary sharedPhotoLibrary] presentLimitedLibraryPickerFromViewController:self];
        }
    }]];

    [actionSheet addAction:[UIAlertAction actionWithTitle:NSLocalizedStringFromTableInBundle(@"permission.change_settings", @"QBImagePicker", self.imagePickerController.assetBundle, nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction *action) {
        // Open application settings.
        UIApplication *application = [UIApplication sharedApplication];
        NSURL *URL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
        [application openURL:URL options:@{} completionHandler:nil];
    }]];

    // Present action sheet.
    [self presentViewController:actionSheet animated:YES completion:nil];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return isLimitedPermission() ? 2 : 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (isLimitedPermission() && section == 0) {
        return 1;
    }
    return self.assetCollections.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (isLimitedPermission()) {
        if (indexPath.section == 0) {
            CGRect frame = [tableView rectForRowAtIndexPath:indexPath];
            UITableViewCell *cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:nil];
            cell.tag = indexPath.row;
            cell.selectionStyle = UITableViewCellSelectionStyleNone;
            UIButton *manageButton = [UIButton buttonWithType:UIButtonTypeSystem];
            if (isDarkMode()) {
                [cell setBackgroundColor:[UIColor colorWithRed: 0.03 green: 0.03 blue: 0.03 alpha: 1.00]];
                [manageButton setBackgroundColor:[UIColor colorWithRed: 0.13 green: 0.13 blue: 0.13 alpha: 1.00]];
                [manageButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
            } else {
                [cell setBackgroundColor:[UIColor colorWithRed: 0.95 green: 0.95 blue: 0.95 alpha: 1.00]];
                [manageButton setBackgroundColor:[UIColor colorWithRed: 0.85 green: 0.85 blue: 0.85 alpha: 1.00]];
                [manageButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
            }
            [manageButton addTarget:self action:@selector(managePermissionAction:) forControlEvents:UIControlEventTouchUpInside];
            manageButton.layer.cornerRadius = 12;
            manageButton.contentEdgeInsets = UIEdgeInsetsMake(4, 12, 4, 12);
            [manageButton setTitle:NSLocalizedStringFromTableInBundle(@"permission.manage", @"QBImagePicker", self.imagePickerController.assetBundle, nil) forState:UIControlStateNormal];
            [manageButton sizeToFit];
            [cell setAccessoryView:manageButton];
            
            UILabel *helpText = [[UILabel alloc] initWithFrame:CGRectMake(16,0,cell.contentView.frame.size.width - manageButton.frame.size.width + 24, frame.size.height)];
            helpText.font = [UIFont systemFontOfSize:13];
            [helpText setNumberOfLines:2];
            helpText.text = NSLocalizedStringFromTableInBundle(@"permission.help", @"QBImagePicker", self.imagePickerController.assetBundle, nil);
            
            [cell.contentView addSubview:helpText];

            return cell;
        }
    }
    
    QBAlbumCell *cell = [tableView dequeueReusableCellWithIdentifier:@"AlbumCell" forIndexPath:indexPath];
    cell.tag = indexPath.row;
    cell.borderWidth = 1.0 / [[UIScreen mainScreen] scale];
    
    // Thumbnail
    PHAssetCollection *assetCollection = self.assetCollections[indexPath.row];
    
    PHFetchOptions *options = [PHFetchOptions new];
    
    switch (self.imagePickerController.mediaType) {
        case QBImagePickerMediaTypeImage:
            options.predicate = [NSPredicate predicateWithFormat:@"mediaType == %ld", PHAssetMediaTypeImage];
            break;
            
        case QBImagePickerMediaTypeVideo:
            options.predicate = [NSPredicate predicateWithFormat:@"mediaType == %ld", PHAssetMediaTypeVideo];
            break;
            
        default:
            break;
    }
    
    PHFetchResult *fetchResult = [PHAsset fetchAssetsInAssetCollection:assetCollection options:options];
    PHImageManager *imageManager = [PHImageManager defaultManager];
    
    if (fetchResult.count >= 3) {
        cell.imageView3.hidden = NO;
        
        [imageManager requestImageForAsset:fetchResult[fetchResult.count - 3]
                                targetSize:CGSizeScale(cell.imageView3.frame.size, [[UIScreen mainScreen] scale])
                               contentMode:PHImageContentModeAspectFill
                                   options:nil
                             resultHandler:^(UIImage *result, NSDictionary *info) {
                                 if (cell.tag == indexPath.row) {
                                     cell.imageView3.image = result;
                                 }
                             }];
    } else {
        cell.imageView3.hidden = YES;
    }
    
    if (fetchResult.count >= 2) {
        cell.imageView2.hidden = NO;
        
        [imageManager requestImageForAsset:fetchResult[fetchResult.count - 2]
                                targetSize:CGSizeScale(cell.imageView2.frame.size, [[UIScreen mainScreen] scale])
                               contentMode:PHImageContentModeAspectFill
                                   options:nil
                             resultHandler:^(UIImage *result, NSDictionary *info) {
                                 if (cell.tag == indexPath.row) {
                                     cell.imageView2.image = result;
                                 }
                             }];
    } else {
        cell.imageView2.hidden = YES;
    }
    
    if (fetchResult.count >= 1) {
        [imageManager requestImageForAsset:fetchResult[fetchResult.count - 1]
                                targetSize:CGSizeScale(cell.imageView1.frame.size, [[UIScreen mainScreen] scale])
                               contentMode:PHImageContentModeAspectFill
                                   options:nil
                             resultHandler:^(UIImage *result, NSDictionary *info) {
                                 if (cell.tag == indexPath.row) {
                                     cell.imageView1.image = result;
                                 }
                             }];
    }
    
    if (fetchResult.count == 0) {
        cell.imageView3.hidden = NO;
        cell.imageView2.hidden = NO;
        
        // Set placeholder image
        UIImage *placeholderImage = [self placeholderImageWithSize:cell.imageView1.frame.size];
        cell.imageView1.image = placeholderImage;
        cell.imageView2.image = placeholderImage;
        cell.imageView3.image = placeholderImage;
    }
    
    // Album title
    cell.titleLabel.text = assetCollection.localizedTitle;
    
    // Number of photos
    cell.countLabel.text = [NSString stringWithFormat:@"%lu", (long)fetchResult.count];
    
    return cell;
}


#pragma mark - PHPhotoLibraryChangeObserver

- (void)photoLibraryDidChange:(PHChange *)changeInstance
{
    dispatch_async(dispatch_get_main_queue(), ^{
        // Update fetch results
        NSMutableArray *fetchResults = [self.fetchResults mutableCopy];
        
        [self.fetchResults enumerateObjectsUsingBlock:^(PHFetchResult *fetchResult, NSUInteger index, BOOL *stop) {
            PHFetchResultChangeDetails *changeDetails = [changeInstance changeDetailsForFetchResult:fetchResult];
            
            if (changeDetails) {
                [fetchResults replaceObjectAtIndex:index withObject:changeDetails.fetchResultAfterChanges];
            }
        }];
        
        if (![self.fetchResults isEqualToArray:fetchResults]) {
            self.fetchResults = fetchResults;
            
            // Reload albums
            [self updateAssetCollections];
            [self.tableView reloadData];
        }
    });
}

@end
