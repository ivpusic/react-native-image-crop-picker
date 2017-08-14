package com.reactnative.ivpusic.imagepicker.cache;

import android.content.Context;
import android.os.Environment;

import com.reactnative.ivpusic.imagepicker.AlbumListActivity;
import com.reactnative.ivpusic.imagepicker.util.FileUtil;

import java.io.File;


/**
 * Created by Martin on 2017/1/17.
 */
public class InnerCache extends Cache {

    private File innerCache;

    public InnerCache() {
        innerCache = getCacheDirCreateIfNotExist();
    }

    private File getCacheDirCreateIfNotExist() {
        File cachePath = new File(getInnerCacheDir(AlbumListActivity.getAppContext()));
        if (!cachePath.isDirectory()) {
            try {
                cachePath.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                new File(cachePath, ".nomedia").createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cachePath;
    }

    private String getInnerCacheDir(Context context) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }


    public boolean exist(String fileName) {
        String path = innerCache + File.separator + fileName;
        return FileUtil.exist(path);
    }

    @Override
    public String getAbsolutePath(String fileName) {
        return getDirectory().getAbsolutePath() + File.separator + fileName;
    }

    @Override
    public File getDirectory() {
        return getCacheDirCreateIfNotExist();
    }

    @Override
    public boolean deleteCacheItem(String fileName) {
        String filePath = getAbsolutePath(fileName);
        return FileUtil.deleteFile(filePath);
    }
}
