


@Observed
export class Rectangle {
  left: number;
  top: number;
  right: number;
  bottom: number;

  constructor(left: number, top: number, right: number, bottom: number){
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  width(): number {
    return this.right - this.left;
  }

  height(): number {
    return this.bottom - this.top;
  }

  centerX(): number {
    return this.left + this.width() / 2;
  }

  centerY(): number {
    return this.top + this.height() / 2;
  }

  roundLeft(): number {
    return Math.floor(vp2px(this.left));
  }

  roundRight(): number {
    return Math.floor(vp2px(this.right));
  }

  roundTop(): number {
    return Math.floor(vp2px(this.top));
  }

  roundBottom(): number {
    return Math.floor(vp2px(this.bottom));
  }

  roundWidth(): number {
    return this.roundRight() - this.roundLeft();
  }

  roundHeight(): number {
    return this.roundBottom() - this.roundTop();
  }

  scale(ratio: number, originPointX: number, originPointY: number): Rectangle {
    let left = originPointX + (this.left - originPointX) * ratio;
    let top = originPointY + (this.top - originPointY) * ratio;
    let right = originPointX + (this.right - originPointX) * ratio;
    let bottom = originPointY + (this.bottom - originPointY) * ratio;
    return new Rectangle (left, top, right, bottom);
  }

  translate(offsetX: number, offsetY: number): Rectangle {
    let left = this.left + offsetX;
    let top = this.top + offsetY;
    let right = this.right + offsetX;
    let bottom = this.bottom + offsetY;
    return new Rectangle (left, top, right, bottom);
  }

  clone(): Rectangle {
    return new Rectangle(this.left, this.top, this.right, this.bottom);
  }

  toString() {
    return 'Rect[' + this.left + ',' + this.top + ',' + this.right + ',' + this.bottom + ']';
  }
}

@Observed
export class TextDrawStatus {
  public currentTab: string = 'text';
  public play_pos: number = 0;
  public fixWidth: number = 0;
  public fixHeight: number = 0;
  public currentTextUid: string = '';
  public trimerMove: boolean = false;
  public identifyStatus: number = -1
  public identifyResult: number = -1
}