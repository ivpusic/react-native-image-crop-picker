import image from '@ohos.multimedia.image';
import Logger from '../Logger';
import display from '@ohos.display';
import router from '@ohos.router';
import {BusinessError} from '@ohos.base';
import { Rectangle } from '../utils/types';
import { Event } from '../utils/jul';
import { Action, DownPos, DragObj } from '../utils/CropModel';
import { Constants } from '../utils/Constants';
import {getPixelMap} from '../utils/DecodeAndEncodeUtil';
import {RotateType} from '../viewmodel/viewAndModel';
import { encode } from '../utils/EncodeUtil';
let hotspotsWidth: number = 18;
const PHONE_MIN_CROP: number = 43;
const TAG: string = 'lkImageEdit';


@Extend(Row)
function rowStyle() {
  .width('100%')
  .height(1)
  .backgroundColor('#FFFFFF')
  .opacity(0.5)
}

@Extend(Row)
function rowStyle1(hasSplitLine: boolean) {
  .width('100%')
  .height(0.5)
  .backgroundColor('#FFFFFF')
  .opacity(hasSplitLine ? 0.5 : 0)
}

@Extend(Row)
function rowStyle2(hasMoreSplitLine: boolean) {
  .width('100%')
  .height(0.5)
  .backgroundColor('#FFFFFF')
  .opacity(hasMoreSplitLine ? 0.2 : 0)
}

@Extend(Column)
function columnStyle() {
  .width(1)
  .height('100%')
  .backgroundColor('#FFFFFF')
  .opacity(0.5)
}

@Extend(Column)
function columnStyle1(hasSplitLine: boolean) {
  .width(0.5)
  .height('100%')
  .backgroundColor('#FFFFFF')
  .opacity(hasSplitLine ? 0.5 : 0)
}

@Extend(Column)
function columnStyle2(hasMoreSplitLine: boolean) {
  .width(0.5)
  .height('100%')
  .backgroundColor('#FFFFFF')
  .opacity(hasMoreSplitLine ? 0.2 : 0)
}

@Extend(Image)
function iconStyle() {
  .size({ width: 24, height: 24})
  .fillColor('#FFFFFF')
  .opacity(0.9)
}

@Extend(Row)
function bottomIconStyle() {
  .size({ width: '20%', height: '100%'})
  .justifyContent(FlexAlign.Center)
}

@Extend(Text)
function textStyle() {
  .size({ width: '20%', height: '100%'})
  .textAlign(TextAlign.Center)
}

@Entry
@Component
export struct ImageEditInfo {

  @State filePath:string = AppStorage.get('filePath') as string;
  @State cropperRotate:string = AppStorage.get('cropperRotate') as string;
  setting: RenderingContextSettings = new RenderingContextSettings(true);
  context: CanvasRenderingContext2D = new CanvasRenderingContext2D(this.setting);
  @Provide('isPixelMapChange') @Watch('flushPixelMap') isPixelMapChange: boolean = false;
  @State title: string = AppStorage.get('title') as string;
  @State cancel: string = AppStorage.get('cancelText') as string;
  @State cancelTextColor: string = AppStorage.get('cancelTextColor') as string;
  @State choose: string = AppStorage.get('chooseText') as string;
  @State chooseTextColor: string = AppStorage.get('chooseTextColor') as string;
  @State uri: string = '';
  @State icon?: image.PixelMap = undefined;
  @State imageWith: number = 0;
  @State imageHeight: number = 0;

  @State screenWidth: number = 0;
  @State screenHeight: number = 0;
  DEFAULT_MASK_STYLE: string = 'rgba(0, 0, 0, 0.3)';
  @State clipSize: Rectangle = new Rectangle(0, 0, 0, 0);
  @State maxClipSize: Rectangle = new Rectangle(0, 0, 0, 0);
  @State display: Rectangle = new Rectangle(0, 0, 0, 0);
  @State crop: number = 0;
  @State hasSplitLine: boolean = true;
  @State clipVisible: boolean = true;
  @State imgOffSetX: number = 0;
  @State imgOffSetY: number = 0;
  @State imgScale: number = 1;
  @State currentScale: number = 1;
  @State preOffsetX: number = 0;
  @State preOffsetY: number = 0;
  @State clipRatio: number = 0;
  @State imageFlag: boolean = true;
  @State angle: number = 0;
  private minSize = PHONE_MIN_CROP // 最小宽高
  private dragObj: DragObj = new DragObj(false);
  private clb:number =1
  pixelInit(path: string) {
    this.angle = 0;
    this.screenWidth = px2vp(display.getDefaultDisplaySync().width);
    this.screenHeight = px2vp(display.getDefaultDisplaySync().height);
    this.uri = path;
    getPixelMap(this.uri)
      .then((pixelMap: image.PixelMap) => {
        if (pixelMap) {
          this.isPixelMapChange = !this.isPixelMapChange;
          this.icon = pixelMap;
          pixelMap.getImageInfo().then((imageInfo) => {
            this.imageHeight = imageInfo.size == undefined ? 0 : imageInfo.size?.height;
            this.imageWith = imageInfo.size == undefined ? 0 : imageInfo.size?.width;
            this.initCropBox(this.imageWith, this.imageHeight, this.screenWidth, this.screenHeight);
          })
        }
      })
  }

  resetImg(): void {
    this.imgScale = 1;
    this.currentScale = 1;
    this.preOffsetX = 0;
    this.preOffsetY = 0;
  }

  initCropBox(imageWidth: number, imageHeight: number, screenWidth: number, screenHeight: number) {
    if (imageWidth > imageHeight) {
      this.clb = imageWidth / (screenWidth - hotspotsWidth * 2);
      let imgHeight = Math.round(imageHeight / this.clb);
      let imgWidth = (screenHeight / 2 - imgHeight / 2);
      let top = imgWidth - 56;
      let right = screenWidth - hotspotsWidth;
      let bottom = imgHeight + top;
      this.clipSize = new Rectangle(hotspotsWidth, top, right, bottom);
    } else {
      let imgHeight = screenHeight - 56 * 2 - hotspotsWidth * 2;
      this.clb = imageHeight / imgHeight
      let imgWidth = Math.round(imageWidth / this.clb);
      let left = screenWidth / 2 - imgWidth / 2;
      let right = imgWidth + left;
      let bottom = imgHeight + hotspotsWidth;
      this.clipSize = new Rectangle(left, hotspotsWidth, right, bottom);
    }
    this.maxClipSize = this.clipSize;
    this.display = new Rectangle(0, 0, this.screenWidth, this.screenHeight - 56 * 2);
  }

  aboutToAppear() {
    this.pixelInit(this.filePath);
    let arrayData = ['filePath', 'title', 'chooseText', 'chooseTextColor', 'cancelText', 'cancelTextColor', 'cropperRotate'];
    this.initData(arrayData);
  }

  initData(arrayData : Array<string>) {
    for (let i = 0; i < arrayData.length; i++) {
      AppStorage.setOrCreate(arrayData[i], '');
    }
  }

  @Builder
  CropTitleBar() {
    Text(this.title)
      .fontColor(Color.White)
      .fontSize(20)
      .textAlign(TextAlign.Center)
      .width('100%')
      .height(56)
      .position({x: 0, y : 0})
      .backgroundColor(Color.Black)
  }

  @Builder
  CropImageArea() {
    Stack() {
      Image(this.icon)
        .objectFit(ImageFit.Contain)
        .position({x: this.imgOffSetX, y: this.imgOffSetY})
        .scale({ x: this.imgScale, y: this.imgScale})
        .padding(hotspotsWidth)
        .width('100%')
        .height('100%')

      Row()
        .height(this.clipSize.height())
        .width(this.clipSize.left - this.display.left)
        .position({x:this.display.left,y:this.clipSize.top})
        .backgroundColor(this.DEFAULT_MASK_STYLE)

      Row()
        .height(this.clipSize.top - this.display.top)
        .width(this.display.width())
        .position({x:this.display.left,y:this.display.top})
        .backgroundColor(this.DEFAULT_MASK_STYLE)

      Row()
        .height(this.clipSize.height())
        .width(this.display.right - this.clipSize.right)
        .position({x:this.clipSize.right,y:this.clipSize.top})
        .backgroundColor(this.DEFAULT_MASK_STYLE)

      Row()
        .height(this.display.bottom - this.clipSize.bottom)
        .width(this.display.width())
        .position({x:this.display.left,y:this.clipSize.bottom})
        .backgroundColor(this.DEFAULT_MASK_STYLE)

      RectangleSizer({
        clipSize: this.clipSize,
        onDrag: this.onDragStartFun,
        clipVisible: this.clipVisible,
        hasSplitLine: this.hasSplitLine
      })
    }
    .height(this.screenHeight - 56 * 2)
    .width('100%')
    .backgroundColor(Color.Black)
    .onTouch(this.touchHandler)
    .gesture(GestureGroup(GestureMode.Exclusive,
      PinchGesture({ fingers: Constants.DOUBLE_NUMBER})
        .onActionUpdate((event?: GestureEvent) => {
          if (event) {
            this.imgScale = this.currentScale * event.scale;
          }
        })
        .onActionEnd(() => {
          if (this.imgScale < 1) {
            this.resetImg();
            this.imgOffSetX = 0;
            this.imgOffSetY = 0;
          } else {
            this.currentScale = this.imgScale;
          }
        }),
      PanGesture()
        .onActionStart(() => {
          this.preOffsetX = this.imgOffSetX;
          this.preOffsetY = this.imgOffSetY;
        })
        .onActionUpdate((event?: GestureEvent) => {
          if (event && this.imageFlag ) {
            this.imgOffSetX = this.preOffsetX + event.offsetX;
            this.imgOffSetY = this.preOffsetY + event.offsetY;
            Logger.info(TAG, "into flushPixelMapChange x : " + this.imgOffSetX + " y : " + this.imgOffSetY)
          }
        })
    ))
  }


  @Builder
  BottomToolbar() {
    Flex({ direction: FlexDirection.Row, justifyContent: FlexAlign.Center }) {
      Text(this.cancel)
        .textStyle()
        .fontColor(this.cancelTextColor)
        .onClick(async () => {
          router.back()
        });
      Row() {
        Image($r('app.media.ic_anti_clockwise'))
          .iconStyle()
          .visibility(this.cropperRotate === 'true' ? Visibility.Hidden : Visibility.Visible)
          .onClick(async () => {
            this.rotateImage(RotateType.ANTI_CLOCK);
          });
      }
      .bottomIconStyle();
      Row() {
        Image($r('app.media.ic_reset'))
          .iconStyle()
          .onClick(() => {
            this.pixelInit(this.uri);
          });
      }
      .bottomIconStyle();
      Row() {
        Image($r('app.media.ic_clockwise'))
          .iconStyle()
          .visibility(this.cropperRotate === 'true' ? Visibility.Hidden : Visibility.Visible)
          .onClick(() => {
            this.rotateImage(RotateType.CLOCKWISE);
          });
      }
      .bottomIconStyle();
      Text(this.choose)
        .textStyle()
        .fontColor(this.chooseTextColor)
        .onClick(()=>{
          if (this.icon) {
            let xOff = this.imageWith * (this.imgScale - 1) / 2;
            let yOff = this.imageHeight * (this.imgScale - 1) / 2;
            let region: image.Region = { x: 0, y: 0, size: { height: this.imageHeight, width: this.imageWith } };
            region.x = (this.clipSize.left - this.maxClipSize.left) / this.imgScale * this.clb + (xOff/this.imgScale) - this.imgOffSetX / this.imgScale * this.clb;
            region.size.width = this.imageWith - region.x - (this.maxClipSize.right - this.clipSize.right) / this.imgScale * this.clb
              - (xOff/this.imgScale) - this.imgOffSetX / this.imgScale * this.clb;
            region.y = (this.clipSize.top - this.maxClipSize.top) / this.imgScale * this.clb + (yOff/this.imgScale) - this.imgOffSetY / this.imgScale * this.clb;
            region.size.height = this.imageHeight - region.y - (this.maxClipSize.bottom - this.clipSize.bottom) / this.imgScale * this.clb
              - (yOff/this.imgScale) - this.imgOffSetY / this.imgScale * this.clb;
            this.icon.crop(region, async (err: BusinessError) => {
              if (err != undefined) {
                console.error("Failed to crop pixelmap.");
                return;
              } else {
                let imgPath = await encode(this, this.icon);
                AppStorage.setOrCreate('cropImagePath', imgPath)
                router.back()
              }
            })
          }
        })
    }
    .width('100%')
    .height(56)
    .position({x: 0, y : (this.screenHeight - 56)})
    .backgroundColor(Color.Black)
  }

  rotateImage(rotateType: RotateType) {
    Logger.info(TAG, "into rotateImage rotateType : " +rotateType)
    if (rotateType === RotateType.CLOCKWISE) {
      if (!this.icon) {
        Logger.info(TAG, "into rotateImage return")
        return;
      }
      try {
        this.icon.rotate(Constants.CLOCK_WISE)
          .then(() => {
            this.angle = this.angle + Constants.CLOCK_WISE;
            this.flushPixelMapChange(this.angle);
          })
      } catch (error) {
      }
    }
    if (rotateType === RotateType.ANTI_CLOCK) {
      if (!this.icon) {
        Logger.info(TAG, "into rotateImage return")
        return;
      }
      try {
        this.icon.rotate(Constants.ANTI_CLOCK)
          .then(() => {
            this.angle = this.angle + Constants.ANTI_CLOCK;
            this.flushPixelMapChange(this.angle);
          })
      } catch (error) {
      }
    }
  }

  flushPixelMapChange(angle : number) {
    this.isPixelMapChange = !this.isPixelMapChange;
  }


  flushPixelMap() {
    const temp = this.icon;
    this.icon = undefined;
    this.icon = temp;
  }

  onDragStartFun = (event: TouchEvent, left: boolean, top: boolean, right: boolean, bottom: boolean, multiCrop: boolean): void => {
    let downPos = new DownPos(this.clipSize.left, this.clipSize.top, this.clipSize.bottom, this.clipSize.right);
    let action = new Action(left, top, right, bottom);
    this.dragObj = new DragObj(true, event.touches[0].screenX, event.touches[0].screenY, action, downPos, multiCrop);
  }

  private endImageDrag(): void {
    let crop: Rectangle = new Rectangle(this.clipSize.left, this.clipSize.top, this.clipSize.right, this.clipSize.bottom);

    // 裁剪框和图片间的偏移量
    let extraLeft = 0;
    let extraTop = 0;
    let extraRight = 0;
    let extraBottom = 0;

    // 图片实际的宽高
    let actualHeight = 0;
    let actualWidth = 0;

    // 图片偏移量
    let imgOffSetX = this.imgOffSetX;
    let imgOffSetY = this.imgOffSetY;

    // 图片缩放偏移量
    let scaleOffsetX = 0;
    let scaleOffsetY = 0;

    // 根据图片宽高比计算实际的图片宽高和偏移量
    // 计算图片的实际宽高和实际偏移量
    if (this.imageWith > this.imageHeight) {
      actualHeight = Math.round(this.imageHeight / (this.imageWith / (this.screenWidth - hotspotsWidth * 2) ));
      actualWidth = (this.screenHeight / 2 - actualHeight / 2);
      extraLeft = hotspotsWidth;
      extraTop = actualWidth - 56;
      extraRight = this.screenWidth - hotspotsWidth;
      extraBottom = actualHeight + extraTop;
    } else {
      actualHeight = (this.screenHeight - 56 * 2 - hotspotsWidth * 2);
      actualWidth = Math.round(this.imageWith / (this.imageHeight / actualHeight));
      extraLeft = this.screenWidth / 2 - actualWidth / 2;
      extraTop = hotspotsWidth;
      extraRight = actualWidth + extraLeft;
      extraBottom = actualHeight + hotspotsWidth;
    }

    // 计算缩放偏移量
    scaleOffsetX = this.imgScale > 1 ? 0- ((extraRight - extraLeft) * this.imgScale - (extraRight - extraLeft)) / 2 : 0;
    scaleOffsetY = this.imgScale > 1 ? 0- ((extraBottom - extraTop) * this.imgScale - (extraBottom - extraTop)) / 2 : 0;

    // 计算图片四个边缘的位置
    const imageRight = this.imgOffSetX + actualWidth;
    const imageBottom = this.imgOffSetY + actualHeight;

    // 计算裁剪框四个边缘的位置
    const cropRight = crop.right;
    const cropBottom = crop.bottom;

    // 判断图片左边缘是否在裁剪框内
    if (this.imgOffSetX + scaleOffsetX > crop.left - extraLeft) {
      // 图片左边缘已经被拖动到裁剪框内，需要还原位置
      imgOffSetX = crop.left - extraLeft - scaleOffsetX;
    }

    // 判断图片上边缘是否在裁剪框内
    if (this.imgOffSetY + scaleOffsetY > crop.top - extraTop) {
      // 图片上边缘已经被拖动到裁剪框内，需要还原位置
      imgOffSetY = crop.top - extraTop - scaleOffsetY;
    }

    // 判断图片右边缘是否在裁剪框内
    if (imageRight - scaleOffsetX < cropRight - extraLeft) {
      imgOffSetX = cropRight - extraRight + scaleOffsetX;
    }

    // 判断图片下边缘是否在裁剪框内
    if (imageBottom - scaleOffsetY < cropBottom - extraTop) {
      // 图片下边缘已经被拖动到裁剪框内，需要还原位置
      imgOffSetY = cropBottom - extraBottom + scaleOffsetY;
    }
    this.imgOffSetX = imgOffSetX;
    this.imgOffSetY = imgOffSetY;
  }

  touchHandler: (event: TouchEvent | undefined) => void = (event: TouchEvent | undefined) : void => {
    if (!event) {
      return;
    }

    if (event.type === TouchType.Up) {
      this.dragObj.dragging = false;
      this.endImageDrag();
    }

    if (event.type === TouchType.Down) {
      Logger.info(TAG, "Down")
    }

    if (event.type === TouchType.Move) {
      if (!this.dragObj.dragging) {
        this.imageFlag =true;
        return;
      }
      this.imageFlag =false;
      let touch = event.touches[0];
      let delX: number = touch.screenX - this.dragObj.x;
      let delY: number = touch.screenY - this.dragObj.y;
      // 裁剪和缩放平移收拾互斥

      let newPosition = this.clipSize.clone();

      let direction = this.dragObj.action;
      if (this.dragObj.multiCrop) {
        this.getMultiCropRect(delX, delY, newPosition, direction, event.pressure);
      } else {
        this.getSingleCropRect(delX, delY, newPosition, direction, event.pressure);
      }
    }
  }

  getMultiCropRect(delX: number, delY: number, newPosition: Rectangle, direction: Action, pressure: number) {
    if (direction.left) {
      newPosition.left = this.dragObj.downPos.left + delX;
      let width = this.clipSize.right - newPosition.left;
      if (this.clipRatio > 0) {
        let height = width / this.clipRatio;
        if (height <= this.minSize) {
          newPosition.left = this.clipSize.right - this.minSize * this.clipRatio;
        }
      } else if (width <= this.minSize) {
        newPosition.left = this.clipSize.right - this.minSize;
      }
      if (newPosition.left < this.maxClipSize.left) {
        newPosition.left = this.maxClipSize.left;
      }
    }
    if (direction.top) {
      newPosition.top = this.dragObj.downPos.top + delY;
      let height = this.clipSize.bottom - newPosition.top;
      if (this.clipRatio > 0) {
        let width = height * this.clipRatio;
        if (width <= this.minSize) {
          newPosition.top = this.clipSize.bottom - this.minSize / this.clipRatio;
        }
      } else if (height <= this.minSize) {
        newPosition.top = this.clipSize.bottom - this.minSize;
      }
      if (newPosition.top < this.maxClipSize.top) {
        newPosition.top = this.maxClipSize.top;
      }
    }
    if (direction.right) {
      newPosition.right = this.dragObj.downPos.right + delX;
      let width = newPosition.right - this.clipSize.left;
      if (this.clipRatio > 0) {
        let height = width / this.clipRatio;
        if (height <= this.minSize) {
          newPosition.right = this.clipSize.left + this.minSize * this.clipRatio;
        }
      } else if (width <= this.minSize) {
        newPosition.right = this.clipSize.left + this.minSize;
      }
      if (newPosition.right > this.maxClipSize.right) {
        newPosition.right = this.maxClipSize.right;
      }
    }
    if (direction.bottom) {
      newPosition.bottom = this.dragObj.downPos.bottom + delY;
      let height = newPosition.bottom - this.clipSize.top;
      if (this.clipRatio > 0) {
        let width = height * this.clipRatio;
        if (width <= this.minSize) {
          newPosition.bottom = this.clipSize.top + this.minSize / this.clipRatio;
        }
      } else if (height <= this.minSize) {
        newPosition.bottom = this.clipSize.top + this.minSize;
      }
      if (newPosition.bottom > this.maxClipSize.bottom) {
        newPosition.bottom = this.maxClipSize.bottom;
      }
    }
    this.clipSize = new Rectangle(newPosition.left, newPosition.top, newPosition.right, newPosition.bottom);
  }

  getSingleCropRect(delX: number, delY: number, newPosition: Rectangle, direction: Action, pressure: number) {
    if (direction.left) {
      let newX = this.dragObj.downPos.left + delX;
      newX = newX < this.dragObj.downPos.right - this.minSize ? newX : this.dragObj.downPos.right - this.minSize
      if (this.clipRatio > 0) {
        let width = this.clipSize.right - newX;
        let height = width / this.clipRatio;
        newPosition.top = this.clipSize.top - (height - this.clipSize.height()) / 2;
        newPosition.bottom = this.clipSize.bottom + (height - this.clipSize.height()) / 2;
        if (height <= this.minSize) {
          newX = this.clipSize.right - this.minSize * this.clipRatio;
        }
      }
      newPosition.left = newX;
      if (newPosition.left < this.maxClipSize.left) {
        newPosition.left = this.maxClipSize.left;
      }
    } else if (direction.right) {
      let newX = this.dragObj.downPos.right + delX;
      if (newX < this.dragObj.downPos.left + this.minSize) {
        newX = this.dragObj.downPos.left + this.minSize;
      }
      if (this.clipRatio > 0) {
        let width = newX - this.clipSize.left;
        let height = width / this.clipRatio;
        newPosition.top = this.clipSize.top - (height - this.clipSize.height()) / 2;
        newPosition.bottom = this.clipSize.bottom + (height - this.clipSize.height()) / 2;
        if (height <= this.minSize) {
          newX = this.minSize * this.clipRatio + this.clipSize.left;
        }
      }
      newPosition.right = newX;
      if (newPosition.right > this.maxClipSize.right) {
        newPosition.right = this.maxClipSize.right;
      }
    } else if (direction.top) {
      let newY = this.dragObj.downPos.top + delY;
      newY = newY < this.dragObj.downPos.bottom - this.minSize ? newY : this.dragObj.downPos.bottom - this.minSize
      if (this.clipRatio > 0) {
        let height = this.clipSize.bottom - newY;
        let width = height * this.clipRatio;
        newPosition.left = this.clipSize.left - (width - this.clipSize.width()) / 2;
        newPosition.right = this.clipSize.right + (width - this.clipSize.width()) / 2;
        if (width <= this.minSize) {
          newY = this.clipSize.bottom - this.minSize / this.clipRatio;
        }
      }
      newPosition.top = newY;
      if (newPosition.top < this.maxClipSize.top) {
        newPosition.top = this.maxClipSize.top;
      }
    } else if (direction.bottom) {
      let newY = this.dragObj.downPos.bottom + delY;
      if (newY < this.dragObj.downPos.top + this.minSize) {
        newY = this.dragObj.downPos.top + this.minSize;
      }
      if (this.clipRatio > 0) {
        let height = newY - this.clipSize.top;
        let width = height * this.clipRatio;
        newPosition.left = this.clipSize.left - (width - this.clipSize.width()) / 2;
        newPosition.right = this.clipSize.right + (width - this.clipSize.width()) / 2;
        if (width <= this.minSize) {
          newY = this.minSize / this.clipRatio + this.clipSize.top;
        }
      }
      newPosition.bottom = newY;
      if (newPosition.bottom > this.maxClipSize.bottom) {
        newPosition.bottom = this.maxClipSize.bottom;
      }
    }
  this.clipSize = new Rectangle(newPosition.left, newPosition.top, newPosition.right, newPosition.bottom);

  }

  build() {
    Stack(){
      this.CropImageArea();
      this.CropTitleBar();
      this.BottomToolbar();
    }
    .width('100%')
    .height('100%')
  }
}


@Component
export struct RectangleSizer {
  @Prop clipSize: Rectangle = new Rectangle(0, 0, 0, 0);
  public onDrag: (event: TouchEvent, left: boolean, top: boolean, right: boolean, bottom: boolean, multiCrop: boolean) => void = Event
  @Prop clipVisible: boolean = true;
  @State isScrolling: boolean = false;
  @State isTouched: boolean = false;
  @Prop hasSplitLine: boolean = true;
  @State showCropFrame: boolean = AppStorage.get('showCropFrame') as boolean;
  @State showCropGuidelines: boolean = AppStorage.get('showCropGuidelines') as boolean;
  @Prop isScreenPortrait: boolean = false;

  build() {
    Stack() {
      Flex( { direction: FlexDirection.Column, justifyContent: FlexAlign.SpaceBetween }) {
        Row().rowStyle().id('petalClip_frostedGlass_top')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_1')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_2')
        Row().rowStyle1(this.hasSplitLine || this.isScrolling).id('petalClip_frostedGlass_row_9_1')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_3')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_4')
        Row().rowStyle1(this.hasSplitLine || this.isScrolling).id('petalClip_frostedGlass_row_9_2')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_5')
        Row().rowStyle2(this.isScrolling).id('petalClip_frostedGlass_row_81_6')
        Row().rowStyle().id('petalClip_frostedGlass_bottom')
      }
      .visibility(this.showCropGuidelines ? Visibility.Visible : Visibility.Hidden)
      .padding(hotspotsWidth)

      Flex({ direction: FlexDirection.Row, justifyContent: FlexAlign.SpaceBetween }) {
        Column().columnStyle().id('petalClip_frostedGlass_left')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_1')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_2')
        Column()
          .columnStyle1(this.hasSplitLine || this.isScrolling)
          .id('petalClip_frostedGlass_column_9_1')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_3')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_4')
        Column()
          .columnStyle1(this.hasSplitLine || this.isScrolling)
          .id('petalClip_frostedGlass_column_9_2')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_5')
        Column().columnStyle2(this.isScrolling).id('petalClip_frostedGlass_column_81_6')
        Column().columnStyle().id('petalClip_frostedGlass_right')
      }
      .visibility(this.showCropGuidelines ? Visibility.Visible : Visibility.Hidden)
      .padding(hotspotsWidth)

      Flex({ direction: FlexDirection.Column, justifyContent: FlexAlign.SpaceBetween }) {
        Flex( { direction: FlexDirection.Row, justifyContent: FlexAlign.SpaceBetween }) {
          Row()
            .width(20)
            .height(20)
            .border({ width: { top: 3, left: 3 }, color: { top: '#FFFFFF', left: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_leftTop')
          Row()
            .width(20)
            .height(20)
            .border({ width: { top: 3 }, color: { top: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_topMiddle')
          Row()
            .width(20)
            .height(20)
            .border({ width: { top: 3, right: 3 }, color: { top: '#FFFFFF', right: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_rightTop')
        }
        .height(Math.min(20, this.clipSize.height() / 3 + 4)).clip(true)

        Flex( { direction: FlexDirection.Row, justifyContent: FlexAlign.SpaceBetween }) {
          Row()
            .width(20)
            .height(20)
            .border({ width: { left: 3 }, color: { left: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_leftMiddle')
          Row()
            .width(20)
            .height(20)
            .border({ width: { right: 3 }, color: { right: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_rightMiddle')
        }
        .height(Math.min(20, this.clipSize.height() / 3 + 1)).clip(true)

        Flex( { direction: FlexDirection.Row, justifyContent: FlexAlign.SpaceBetween }) {
          Row()
            .width(20)
            .height(Math.min(20, this.clipSize.height() / 3 + 4))
            .border({ width: { bottom: 3, left: 3 }, color: { bottom: '#FFFFFF', left: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_leftBottom')
          Row()
            .width(20)
            .height(Math.min(20, this.clipSize.height() / 3 + 4))
            .border({ width: { bottom: 3 }, color: { bottom: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_MiddleBottom')
          Row()
            .width(20)
            .height(Math.min(20, this.clipSize.height() / 3 + 4))
            .border({ width: { bottom: 3, right: 3 }, color: { bottom: '#FFFFFF', right: '#FFFFFF' } })
            .id('petalClip_frostedGlass_rect_rightBottom')
        }
      }
      .visibility(this.showCropFrame ? Visibility.Visible : Visibility.Hidden)
      .padding(12)
    }
    .width(this.clipSize.width() + hotspotsWidth * 2)
    .height(this.clipSize.height() + hotspotsWidth * 2)
    .visibility(this.clipVisible ? Visibility.Visible : Visibility.Hidden)
    .position({
      x: this.clipSize.left - hotspotsWidth,
      y: this.clipSize.top - hotspotsWidth,
    })
    .onTouch((event?: TouchEvent) => {
      if (!event) {
        return;
      }
      if (this.isScrolling) {
        return;
      }
      if (event.touches.length >= 2) {
        return;
      }
      let touch = event.touches[0];
      let eventX = touch.x;
      let eventY = touch.y;
      if (event.type == TouchType.Down) {
        if (this.isTouched) {
          return;
        }
        this.isTouched = true;
        let eventLeft = eventX < (2 * hotspotsWidth);
        let eventTop = eventY < (2 * hotspotsWidth);
        let eventRight = eventX > this.clipSize.width();
        let eventBottom = eventY > this.clipSize.height();

        let eventCount = (eventLeft ? 1 : 0) + (eventTop ? 1 : 0) + (eventRight ? 1 : 0) + (eventBottom ? 1 : 0);
        if (eventCount > 0) {
          this.onDrag(event, eventLeft, eventTop, eventRight, eventBottom, eventCount > 1);
        }
      }
      if (event.type === TouchType.Up) {
        this.isTouched = false;
      }
    })

  }
}