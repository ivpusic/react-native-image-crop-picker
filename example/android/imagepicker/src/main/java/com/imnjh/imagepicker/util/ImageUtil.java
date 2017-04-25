package com.imnjh.imagepicker.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;


/**
 * Created by Martin on 2017/1/17.
 */
public class ImageUtil {

  // Constants used for the Orientation Exif tag.
  public static final int ORIENTATION_UNDEFINED = 0;
  public static final int ORIENTATION_NORMAL = 1;
  public static final int ORIENTATION_FLIP_HORIZONTAL = 2; // left right reversed mirror
  public static final int ORIENTATION_ROTATE_180 = 3;
  public static final int ORIENTATION_FLIP_VERTICAL = 4; // upside down mirror
  public static final int ORIENTATION_TRANSPOSE = 5; // flipped about top-left <--> bottom-right
                                                     // axis
  public static final int ORIENTATION_ROTATE_90 = 6; // rotate 90 cw to right it
  public static final int ORIENTATION_TRANSVERSE = 7; // flipped about top-right <--> bottom-left
                                                      // axis
  public static final int ORIENTATION_ROTATE_270 = 8; // rotate 270 to right it

  public static Point computeCompressedSize(String path, double maxPixels) {
    final PointF originSize = getBmpSize(path);
    if (originSize.x == 0f && originSize.y == 0f) {
      return new Point(0, 0);
    } else {
      double scaleFactor = Math.sqrt(originSize.x * 1d * originSize.y / maxPixels);
      if (scaleFactor < 1d) {
        scaleFactor = 1d;
      }
      return new Point((int) (originSize.x / scaleFactor), (int) (originSize.y / scaleFactor));
    }
  }

  public static PointF getBmpSize(String path) {
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, bmOptions);

    try {
      ExifInterface exifInterface = new ExifInterface(path);
      if (bmOptions.outHeight == -1 || bmOptions.outWidth == -1) {
        bmOptions.outHeight = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH,
            ExifInterface.ORIENTATION_NORMAL);
        bmOptions.outWidth = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH,
            ExifInterface.ORIENTATION_NORMAL);
      }

      int orientationAttr =
          exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
              ExifInterface.ORIENTATION_NORMAL);
      if (orientationAttr == ORIENTATION_ROTATE_90 || orientationAttr == ORIENTATION_ROTATE_270) {
        return new PointF(Math.max(0F, bmOptions.outHeight), Math.max(0F, bmOptions.outWidth));
      } else {
        return new PointF(Math.max(0F, bmOptions.outWidth), Math.max(0F, bmOptions.outHeight));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new PointF(0F, 0F);
  }

  public static Bitmap loadBitmap(String path, int inSampleSize) {
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = false;
    bmOptions.inSampleSize = inSampleSize;
    String exifPath = path;
    Matrix matrix = null;
    if (exifPath != null) {
      ExifInterface exif;
      try {
        exif = new ExifInterface(exifPath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        matrix = new Matrix();
        switch (orientation) {
          case ExifInterface.ORIENTATION_ROTATE_90:
            matrix.postRotate(90);
            break;
          case ExifInterface.ORIENTATION_ROTATE_180:
            matrix.postRotate(180);
            break;
          case ExifInterface.ORIENTATION_ROTATE_270:
            matrix.postRotate(270);
            break;
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    Bitmap b = null;
    if (path != null) {
      try {
        b = BitmapFactory.decodeFile(path, bmOptions);
        if (b != null) {
          Bitmap newBitmap = createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
          if (newBitmap != b) {
            b.recycle();
            b = newBitmap;
          }
        }
      } catch (OutOfMemoryError e) {
        try {
          bmOptions.inSampleSize *= 2;
          b = BitmapFactory.decodeFile(path, bmOptions);
          if (b != null) {
            Bitmap newBitmap = createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
            if (newBitmap != b) {
              b.recycle();
              b = newBitmap;
            }
          }
        } catch (Throwable throwable) {
          throwable.printStackTrace();
        }
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
    return b;
  }

  public static Bitmap loadBitmap(String path, float expectedWidth, float expectedHeight,
      int rollbackSize) {
    final PointF originSize = getBmpSize(path);
    final float scaleFactor = expectedWidth == 0
        ? Math.max(originSize.x / rollbackSize, originSize.y / rollbackSize)
        : originSize.x / expectedWidth;
    int inSampleSize = scaleFactor < 1 ? 1 : (int) scaleFactor;
    return loadBitmap(path, inSampleSize);
  }


  private static void checkXYSign(int x, int y) {
    if (x < 0) {
      throw new IllegalArgumentException("x must be >= 0");
    }
    if (y < 0) {
      throw new IllegalArgumentException("y must be >= 0");
    }
  }

  private static void checkWidthHeight(int width, int height) {
    if (width <= 0) {
      throw new IllegalArgumentException("width must be > 0");
    }
    if (height <= 0) {
      throw new IllegalArgumentException("height must be > 0");
    }
  }

  public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m,
      boolean filter) {
    checkXYSign(x, y);
    checkWidthHeight(width, height);
    if (x + width > source.getWidth()) {
      throw new IllegalArgumentException("x + width must be <= bitmap.width()");
    }
    if (y + height > source.getHeight()) {
      throw new IllegalArgumentException("y + height must be <= bitmap.height()");
    }
    if (!source.isMutable() && x == 0 && y == 0 && width == source.getWidth()
        && height == source.getHeight() && (m == null || m.isIdentity())) {
      return source;
    }

    int neww = width;
    int newh = height;
    Canvas canvas = new Canvas();
    Bitmap bitmap;
    Paint paint;

    Rect srcR = new Rect(x, y, x + width, y + height);
    RectF dstR = new RectF(0, 0, width, height);

    Bitmap.Config newConfig = Bitmap.Config.ARGB_8888;
    final Bitmap.Config config = source.getConfig();
    if (config != null) {
      switch (config) {
        case RGB_565:
          newConfig = Bitmap.Config.RGB_565;
          break;
        case ALPHA_8:
          newConfig = Bitmap.Config.ALPHA_8;
          break;
        case ARGB_4444:
        case ARGB_8888:
        default:
          newConfig = Bitmap.Config.ARGB_8888;
          break;
      }
    }

    if (m == null || m.isIdentity()) {
      bitmap = createBitmap(neww, newh, newConfig);
      paint = null;
    } else {
      final boolean transformed = !m.rectStaysRect();
      RectF deviceR = new RectF();
      m.mapRect(deviceR, dstR);
      neww = Math.round(deviceR.width());
      newh = Math.round(deviceR.height());
      bitmap = createBitmap(neww, newh, transformed ? Bitmap.Config.ARGB_8888 : newConfig);
      canvas.translate(-deviceR.left, -deviceR.top);
      canvas.concat(m);
      paint = new Paint();
      paint.setFilterBitmap(filter);
      if (transformed) {
        paint.setAntiAlias(true);
      }
    }
    bitmap.setDensity(source.getDensity());
    bitmap.setHasAlpha(source.hasAlpha());
    if (Build.VERSION.SDK_INT >= 19) {
      bitmap.setPremultiplied(source.isPremultiplied());
    }
    canvas.setBitmap(bitmap);
    canvas.drawBitmap(source, srcR, dstR, paint);
    try {
      canvas.setBitmap(null);
    } catch (Exception e) {
      // don't promt, this will crash on 2.x
    }
    return bitmap;
  }


  public static Bitmap createBitmap(int width, int height, Bitmap.Config config) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, config);
    if (config == Bitmap.Config.ARGB_8888 || config == Bitmap.Config.ARGB_4444) {
      bitmap.eraseColor(Color.TRANSPARENT);
    }
    return bitmap;
  }


  public static String scaleAndSaveImage(String path, String cacheFilePath, Bitmap bitmap,
      int quality) {
    if (bitmap == null) {
      return null;
    }
    float photoW = bitmap.getWidth();
    float photoH = bitmap.getHeight();
    if (photoW == 0 || photoH == 0) {
      return null;
    }
    String fileName = getImageCacheFileName(path);
    scaleAndSaveImageInternal(cacheFilePath, bitmap, quality);
    return fileName;
  }

  private static void scaleAndSaveImageInternal(String filePath, Bitmap bitmap, int quality) {
    Bitmap scaledBitmap = bitmap;
    File file = new File(filePath);
    try {
      file.createNewFile();
      FileOutputStream stream = null;
      stream = new FileOutputStream(filePath);
      scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
      stream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (scaledBitmap != bitmap) {
      scaledBitmap.recycle();
    }
  }

  public static String getImageCacheFileName(String filePath) {
    if (!TextUtils.isEmpty(filePath)) {
      File file = new File(filePath);
      if (file.exists()) {
        return filePath.hashCode() + "_" + file.lastModified() + ".jpg";
      }
    }
    return filePath;
  }


  public static Bitmap decodeBitmap(Context context, Uri uri, int sampleSize) {
    InputStream is = null;
    try {
      is = context.getContentResolver().openInputStream(uri);
      BitmapFactory.Options option = new BitmapFactory.Options();
      option.inSampleSize = sampleSize;
      return BitmapFactory.decodeStream(is, null, option);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (OutOfMemoryError e) {
      e.printStackTrace();
    } finally {
      FileUtil.closeSilently(is);
    }
    return null;
  }

  public static boolean saveBitmap(Bitmap bitmap, String path, Bitmap.CompressFormat format,
      int quality) {
    File f = new File(path);
    if (f.exists()) {
      f.delete();
    }
    FileOutputStream fOut = null;
    try {
      f.createNewFile();
      fOut = new FileOutputStream(f);
      bitmap.compress(format, quality, fOut);
      fOut.flush();
      return true;
    } catch (IOException e1) {
      e1.printStackTrace();
      return false;
    } finally {
      FileUtil.closeSilently(fOut);
    }
  }

  public static Bitmap drawableToBitmap(Drawable drawable, int height, int width) {
    Bitmap bitmap;
    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if (bitmapDrawable.getBitmap() != null) {
        return bitmapDrawable.getBitmap();
      }
    }

    if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
      bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);
    } else {
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
          Bitmap.Config.ARGB_8888);
    }
    Canvas canvas = new Canvas(bitmap);
    canvas.drawARGB(0, 0, 0, 0);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public static Bitmap getCircleBitmap(Bitmap bm, int size) {
    Bitmap bitmap = ThumbnailUtils.extractThumbnail(bm, size, size);
    Bitmap output =
        Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    final int color = 0xffffffff;
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    final RectF rectF = new RectF(rect);

    paint.setAntiAlias(true);
    paint.setDither(true);
    paint.setFilterBitmap(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawOval(rectF, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);
    bm.recycle();
    return output;
  }

  public static int adjustAlpha(int color, float factor) {
    int alpha = Math.round(Color.alpha(color) * factor);
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return Color.argb(alpha, red, green, blue);
  }
}
