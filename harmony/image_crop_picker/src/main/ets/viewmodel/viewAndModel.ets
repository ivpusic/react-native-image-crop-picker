
export class RegionItem {
  x: number;
  y: number;
  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }
}

export class IconStatus {
  normal: Resource;
  chosen: Resource;
  constructor(normal: Resource, chosen: Resource) {
    this.normal = normal;
    this.chosen = chosen;
  }
}

export const menuIconList: Array<IconStatus> = [
  new IconStatus($r('app.media.ic_crop'), $r('app.media.ic_crop_filled')),
  new IconStatus($r('app.media.ic_rotate'), $r('app.media.ic_rotate_filled')),
]

export const cropIconChangeList: Array<IconStatus> = [
  new IconStatus($r('app.media.ic_original'), $r('app.media.ic_original_filled')),
  new IconStatus($r('app.media.ic_one2one'), $r('app.media.ic_one2one_filled')),
  new IconStatus($r('app.media.ic_four2three'), $r('app.media.ic_four2three_filled')),
  new IconStatus($r('app.media.ic_sixteen2nine'), $r('app.media.ic_sixteen2nine_filled'))
]

export enum CropType {
  ORIGINAL_IMAGE,
  SQUARE,
  BANNER,
  RECTANGLE
}

export enum RotateType {
  CLOCKWISE,
  ANTI_CLOCK
}

export enum MainTabId {
  CROP,
  ROTATE
}