package com.reactnative.ivpusic.imagepicker.model;

/**
 * Created by song on 2018/1/24.
 */

public class AlbumListData {
    private String name;
    private int count;
    private String cover;
    private String bucketId;
    private ImageSize imageSize;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setImageSize(ImageSize imageSize) {
        this.imageSize = imageSize;
    }

    public ImageSize getImageSize() {
        return imageSize;
    }

    public static class ImageSize {
        public int width;
        public int height;
    }
}
