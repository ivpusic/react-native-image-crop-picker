package com.imnjh.imagepicker.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.format.Formatter;

/**
 * Created by Martin on 2017/1/17.
 */
public class FileUtil {

  /**
   * @param filePath
   * @return
   */
  public static boolean deleteFile(String filePath) {
    File file = new File(filePath);
    return deleteFile(file);
  }

  /**
   * @param file
   * @return
   */
  public static boolean deleteFile(File file) {
    if (file.exists()) {
      return file.delete();
    }
    return true;
  }


  /**
   * @param dirPath
   * @return
   */
  public static boolean deleteDirectory(String dirPath) {
    return deleteDirectory(new File(dirPath));
  }

  /**
   * @param dir
   * @return
   */
  public static boolean deleteDirectory(File dir) {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      if (files != null) {
        for (File file : files) {
          deleteDirectory(file);
        }
      }
    }
    return deleteFile(dir);
  }

  /**
   * @param filePath
   * @return
   */
  public static boolean exist(String filePath) {
    File file = new File(filePath);
    return file.exists();
  }

  /**
   * get file size which is human readable
   *
   * @param context
   * @param files
   * @return
   */
  public static String getFilesSize(Context context, List<String> files) {
    long result = 0;
    for (String filePath : files) {
      File file = new File(filePath);
      result += file.length();
    }
    return Formatter.formatFileSize(context, result);
  }


  public static void copyFile(File source, File dest) {
    FileChannel inputChannel = null;
    FileChannel outputChannel = null;
    try {
      inputChannel = new FileInputStream(source).getChannel();
      outputChannel = new FileOutputStream(dest).getChannel();
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputChannel != null) {
          inputChannel.close();
        }
        if (outputChannel != null) {
          outputChannel.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void closeSilently(@Nullable Closeable c) {
    if (c == null) {
      return;
    }
    try {
      c.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static String inputStream2String(InputStream is) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int i = -1;
    try {
      while ((i = is.read()) != -1) {
        baos.write(i);
      }
    } catch (IOException e) {}
    return baos.toString();
  }
}
