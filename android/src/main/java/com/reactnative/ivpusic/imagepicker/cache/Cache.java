package com.reactnative.ivpusic.imagepicker.cache;

import com.reactnative.ivpusic.imagepicker.util.FileUtil;

import java.io.File;

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
