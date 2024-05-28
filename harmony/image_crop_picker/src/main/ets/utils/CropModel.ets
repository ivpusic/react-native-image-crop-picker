


export class CropScaleClass {
  value: number;
  ratio: string;
  cropIndex: number;
  text: string | Resource;
  key: string = '';

  constructor(value: number, ratio: string, cropIndex: number, text: string | Resource, key?: string) {
    this.value = value;
    this.ratio = ratio;
    this.cropIndex = cropIndex;
    this.text = text;
    key && (this.key = key);
  }
}

export class DownPos{
  left: number
  top: number
  bottom: number
  right: number

  constructor(left: number, top: number, bottom: number, right: number) {
    this.left = left;
    this.top = top;
    this.bottom = bottom;
    this.right = right;
  }
}

export class DragObj{
  x: number
  y: number
  dragging: boolean
  action: Action
  downPos: DownPos
  multiCrop: boolean

  constructor(dragging: boolean, x: number = 0, y: number = 0,
              action: Action = new Action(false, false, false, false),
              downPos: DownPos = new DownPos(0, 0, 0, 0), multiCrop: boolean = false) {
    this.dragging = dragging;
    this.x = x;
    this.y = y;
    this.action = action;
    this.downPos = downPos;
    this.multiCrop = multiCrop;
  }
}

export class Action {
  left: boolean
  top: boolean
  right: boolean
  bottom: boolean

  constructor(left: boolean, top: boolean, right: boolean, bottom: boolean) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }
}

export enum CropType {
  FREE_CROP,
  FIXED_RATIO,
  FRAME_CROP
}