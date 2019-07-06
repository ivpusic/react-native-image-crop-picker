package com.reactnative.ivpusic.imagepicker;

import android.support.v4.content.FileProvider;

/**
 * Providing a custom {@code FileProvider} prevents manifest {@code <provider>} name collisions.
 *
 * See https://developer.android.com/guide/topics/manifest/provider-element.html for details.
 */
public class CropFileProvider extends FileProvider {
    // This class intentionally left blank.
}
