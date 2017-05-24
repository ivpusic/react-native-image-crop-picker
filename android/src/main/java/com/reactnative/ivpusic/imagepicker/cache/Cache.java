package com.reactnative.ivpusic.imagepicker.cache;

import java.io.File;

import com.reactnative.ivpusic.imagepicker.util.FileUtil;

/**
 * Created by Martin on 2017/1/17.
 */
public abstract class Cache {

  public abstract boolean exist(String fileName);

  public abstract String getAbsolutePath(String fileName);

  public abstract File getDirectory();

  public abstract boolean deleteCacheItem(String fileName);

  public void clear() {
    FileUtil.deleteDirectory(getDirectory());
  }
}
