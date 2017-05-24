package com.reactnative.ivpusic.imagepicker.imageloader;

import okhttp3.OkHttpClient;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;


import com.reactnative.ivpusic.imagepicker.ScaleTypeFillCenterInside;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.reactnative.ivpusic.imagepicker.AlbumListActivity;
import com.reactnative.ivpusic.imagepicker.ImageLoader;
import com.reactnative.ivpusic.imagepicker.cache.CacheManager;



/**
 * Created by Martin on 2017/1/17.
 */

public class FrescoImageLoader implements ImageLoader {

  public FrescoImageLoader() {
    init();
  }

  private void init() {
    DiskCacheConfig diskCacheConfig =
        DiskCacheConfig
            .newBuilder(AlbumListActivity.getAppContext())
            .setBaseDirectoryPath(
                CacheManager.getInstance().getImageCache()
                    .getDirectory())
            .build();
    ImagePipelineConfig config =
        OkHttpImagePipelineConfigFactory
            .newBuilder(
                AlbumListActivity.getAppContext(),
                new OkHttpClient.Builder().build())
            .setDownsampleEnabled(true).setMainDiskCacheConfig(diskCacheConfig)
            .build();
    Fresco.initialize(AlbumListActivity.getAppContext(), config);
  }

  @Override
  public void bindImage(ImageView photoImageView, Uri uri, int width, int height) {
    DraweeView draweeView = (DraweeView) photoImageView;
    final ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);
    if (width > 0 && height > 0) {
      requestBuilder.setResizeOptions(new ResizeOptions(width, height));
    }
    ImageRequest imageRequest = requestBuilder.build();
    DraweeController controller = Fresco.newDraweeControllerBuilder()
        .setOldController(draweeView.getController())
        .setImageRequest(imageRequest).build();
    draweeView.setController(controller);
  }

  @Override
  public void bindImage(ImageView imageView, Uri uri) {
    bindImage(imageView, uri, 0, 0);
  }

  @Override
  public ImageView createImageView(Context context) {
    SimpleDraweeView simpleDraweeView = new SimpleDraweeView(context);
    return simpleDraweeView;
  }

  @Override
  public ImageView createFakeImageView(Context context) {
    SimpleDraweeView fakeImage = new SimpleDraweeView(context);
    fakeImage.getHierarchy().setActualImageScaleType(ScaleTypeFillCenterInside.INSTANCE);
    return fakeImage;
  }
}
