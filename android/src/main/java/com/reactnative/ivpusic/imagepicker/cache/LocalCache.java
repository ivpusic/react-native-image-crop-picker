package com.reactnative.ivpusic.imagepicker.cache;

import java.io.File;
import java.io.IOException;


import com.reactnative.ivpusic.imagepicker.AlbumListActivity;
import com.reactnative.ivpusic.imagepicker.util.FileUtil;
import com.reactnative.ivpusic.imagepicker.util.LogUtils;
import com.reactnative.ivpusic.imagepicker.util.SystemUtil;

/**
 * Created by Martin on 2017/1/17.
 */
public final class LocalCache extends Cache {

  private final String dataName;

  private final String dataPath;

  /**
   * @param dataName cache directory name
   */
  public LocalCache(String dataName) {
    this.dataName = dataName;
    StringBuilder pathBuilder = new StringBuilder(getRushCachePath());
    pathBuilder.append(File.separatorChar);
    pathBuilder.append(getDataName());
    pathBuilder.append(File.separatorChar);
    dataPath = pathBuilder.toString();
    initCacheRoot(dataPath);
  }



  private String getRushCachePath() {
    File dataDir = SystemUtil.getStoreDir(AlbumListActivity.getAppContext());
    return getDirectoryCreateIfNotExist(
        dataDir.getPath() + File.separator + CacheManager.ROOT_STORE)
        .getPath();
  }

  private File getDirectoryCreateIfNotExist(String pathStr) {
    File file = new File(pathStr);
    if (!file.isDirectory()) {
      file.mkdirs();
    }
    return file;
  }

  @Override
  public File getDirectory() {
    return getDirectoryCreateIfNotExist(dataPath);
  }


  @Override
  public String getAbsolutePath(String fileName) {
    return getDirectory() + File.separator + fileName;
  }

  @Override
  public boolean deleteCacheItem(String fileName) {
    String filePath = getAbsolutePath(fileName);
    return FileUtil.deleteFile(filePath);
  }

  @Override
  public boolean exist(String relativePath) {
    String filePath = getAbsolutePath(relativePath);
    boolean ret = FileUtil.exist(filePath);
    return ret;
  }

  public String getDataName() {
    return dataName;
  }


  private void initCacheRoot(String root) {
    getDirectoryCreateIfNotExist(root);
    File ignoreFile = new File(root, ".nomedia");
    try {
      ignoreFile.createNewFile();
    } catch (IOException e) {
      LogUtils.e(LocalCache.class.getSimpleName(), "Failed to create ignore file.", e);
    }
  }

}
