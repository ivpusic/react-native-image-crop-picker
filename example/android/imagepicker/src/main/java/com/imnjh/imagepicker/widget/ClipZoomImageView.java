package com.imnjh.imagepicker.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by Martin on 2017/1/17.
 */
public class ClipZoomImageView extends ImageView implements
    OnScaleGestureListener, OnTouchListener,
    ViewTreeObserver.OnGlobalLayoutListener {
  public static float SCALE_MAX = 4.0f;
  private static float SCALE_MID = 2.0f;

  private float initScale = 1.0f;
  private boolean once = true;

  private final float[] matrixValues = new float[9];

  private ScaleGestureDetector scaleGestureDetector = null;
  private final Matrix scaleMatrix = new Matrix();

  private GestureDetector gestureDetector;
  private boolean isAutoScale;

  private int touchSlop;

  private float mLastX;
  private float mLastY;

  private boolean isCanDrag;
  private int lastPointerCount;
  private int horizontalPadding;


  public ClipZoomImageView(Context context) {
    this(context, null);
  }

  public ClipZoomImageView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setScaleType(ScaleType.MATRIX);
    gestureDetector = new GestureDetector(context,
        new SimpleOnGestureListener() {

          @Override
          public boolean onDoubleTap(MotionEvent e) {
            if (isAutoScale == true)
              return true;

            float x = e.getX();
            float y = e.getY();
            if (getScale() < SCALE_MID) {
              ClipZoomImageView.this.postDelayed(
                  new AutoScaleRunnable(SCALE_MID, x, y), 16);
              isAutoScale = true;
            } else {
              ClipZoomImageView.this.postDelayed(
                  new AutoScaleRunnable(initScale, x, y), 16);
              isAutoScale = true;
            }

            return true;
          }
        });
    scaleGestureDetector = new ScaleGestureDetector(context, this);
    this.setOnTouchListener(this);
  }

  private class AutoScaleRunnable implements Runnable {
    static final float BIGGER = 1.07f;
    static final float SMALLER = 0.93f;
    private float targetScale;
    private float tmpScale;

    private float x;
    private float y;

    public AutoScaleRunnable(float targetScale, float x, float y) {
      this.targetScale = targetScale;
      this.x = x;
      this.y = y;
      if (getScale() < this.targetScale) {
        tmpScale = BIGGER;
      } else {
        tmpScale = SMALLER;
      }

    }

    @Override
    public void run() {
      scaleMatrix.postScale(tmpScale, tmpScale, x, y);
      checkBorder();
      setImageMatrix(scaleMatrix);

      final float currentScale = getScale();
      if (((tmpScale > 1f) && (currentScale < targetScale))
          || ((tmpScale < 1f) && (targetScale < currentScale))) {
        ClipZoomImageView.this.postDelayed(this, 16);
      } else {
        final float deltaScale = targetScale / currentScale;
        scaleMatrix.postScale(deltaScale, deltaScale, x, y);
        checkBorder();
        setImageMatrix(scaleMatrix);
        isAutoScale = false;
      }

    }
  }

  @Override
  public boolean onScale(ScaleGestureDetector detector) {
    float scale = getScale();
    float scaleFactor = detector.getScaleFactor();

    if (getDrawable() == null)
      return true;

    if ((scale < SCALE_MAX && scaleFactor > 1.0f)
        || (scale > initScale && scaleFactor < 1.0f)) {
      if (scaleFactor * scale < initScale) {
        scaleFactor = initScale / scale;
      }
      if (scaleFactor * scale > SCALE_MAX) {
        scaleFactor = SCALE_MAX / scale;
      }
      scaleMatrix.postScale(scaleFactor, scaleFactor,
          detector.getFocusX(), detector.getFocusY());
      checkBorder();
      setImageMatrix(scaleMatrix);
    }
    return true;
  }

  private RectF getMatrixRectF() {
    Matrix matrix = scaleMatrix;
    RectF rect = new RectF();
    Drawable d = getDrawable();
    if (null != d) {
      rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
      matrix.mapRect(rect);
    }
    return rect;
  }

  @Override
  public boolean onScaleBegin(ScaleGestureDetector detector) {
    return true;
  }

  @Override
  public void onScaleEnd(ScaleGestureDetector detector) {}

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if (gestureDetector.onTouchEvent(event))
      return true;
    scaleGestureDetector.onTouchEvent(event);

    float x = 0, y = 0;
    final int pointerCount = event.getPointerCount();
    for (int i = 0; i < pointerCount; i++) {
      x += event.getX(i);
      y += event.getY(i);
    }
    x = x / pointerCount;
    y = y / pointerCount;

    if (pointerCount != lastPointerCount) {
      isCanDrag = false;
      mLastX = x;
      mLastY = y;
    }

    lastPointerCount = pointerCount;
    switch (event.getAction()) {
      case MotionEvent.ACTION_MOVE:
        float dx = x - mLastX;
        float dy = y - mLastY;

        if (!isCanDrag) {
          isCanDrag = isCanDrag(dx, dy);
        }
        if (isCanDrag) {
          if (getDrawable() != null) {

            RectF rectF = getMatrixRectF();
            if (rectF.width() <= getWidth() - horizontalPadding * 2) {
              dx = 0;
            }
            if (rectF.height() <= getHeight() - getHVerticalPadding()
                * 2) {
              dy = 0;
            }
            scaleMatrix.postTranslate(dx, dy);
            checkBorder();
            setImageMatrix(scaleMatrix);
          }
        }
        mLastX = x;
        mLastY = y;
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        lastPointerCount = 0;
        break;
    }

    return true;
  }

  public final float getScale() {
    scaleMatrix.getValues(matrixValues);
    return matrixValues[Matrix.MSCALE_X];
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getViewTreeObserver().addOnGlobalLayoutListener(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    getViewTreeObserver().removeGlobalOnLayoutListener(this);
  }

  @Override
  public void onGlobalLayout() {
    if (once) {
      Drawable d = getDrawable();
      if (d == null)
        return;
      int width = getWidth();
      int height = getHeight();
      int drawableW = d.getIntrinsicWidth();
      int drawableH = d.getIntrinsicHeight();
      float scale = 1.0f;

      int frameSize = getWidth() - horizontalPadding * 2;

      if (drawableW > frameSize && drawableH < frameSize) {
        scale = 1.0f * frameSize / drawableH;
      } else if (drawableH > frameSize && drawableW < frameSize) {
        scale = 1.0f * frameSize / drawableW;
      } else if (drawableW > frameSize && drawableH > frameSize) {
        float scaleW = frameSize * 1.0f / drawableW;
        float scaleH = frameSize * 1.0f / drawableH;
        scale = Math.max(scaleW, scaleH);
      }

      if (drawableW < frameSize && drawableH > frameSize) {
        scale = 1.0f * frameSize / drawableW;
      } else if (drawableH < frameSize && drawableW > frameSize) {
        scale = 1.0f * frameSize / drawableH;
      } else if (drawableW < frameSize && drawableH < frameSize) {
        float scaleW = 1.0f * frameSize / drawableW;
        float scaleH = 1.0f * frameSize / drawableH;
        scale = Math.max(scaleW, scaleH);
      }

      initScale = scale;
      SCALE_MID = initScale * 2;
      SCALE_MAX = initScale * 4;
      scaleMatrix.postTranslate((width - drawableW) / 2,
          (height - drawableH) / 2);
      scaleMatrix.postScale(scale, scale, getWidth() / 2,
          getHeight() / 2);

      setImageMatrix(scaleMatrix);
      once = false;
    }
  }

  public Bitmap clip() {
    Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(),
        Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    draw(canvas);
    return Bitmap.createBitmap(bitmap, horizontalPadding,
        getHVerticalPadding(), getWidth() - 2 * horizontalPadding,
        getWidth() - 2 * horizontalPadding);
  }

  private void checkBorder() {
    RectF rect = getMatrixRectF();
    float deltaX = 0;
    float deltaY = 0;

    int width = getWidth();
    int height = getHeight();

    if (rect.width() + 0.01 >= width - 2 * horizontalPadding) {
      if (rect.left > horizontalPadding) {
        deltaX = -rect.left + horizontalPadding;
      }

      if (rect.right < width - horizontalPadding) {
        deltaX = width - horizontalPadding - rect.right;
      }
    }

    if (rect.height() >= height - 2 * getHVerticalPadding()) {
      if (rect.top > getHVerticalPadding()) {
        deltaY = -rect.top + getHVerticalPadding();
      }

      if (rect.bottom < height - getHVerticalPadding()) {
        deltaY = height - getHVerticalPadding() - rect.bottom;
      }
    }

    scaleMatrix.postTranslate(deltaX, deltaY);
  }

  private boolean isCanDrag(float dx, float dy) {
    return Math.sqrt((dx * dx) + (dy * dy)) >= touchSlop;
  }

  public void setHorizontalPadding(int mHorizontalPadding) {
    this.horizontalPadding = mHorizontalPadding;
  }

  private int getHVerticalPadding() {
    return (getHeight() - (getWidth() - 2 * horizontalPadding)) / 2;
  }
}
