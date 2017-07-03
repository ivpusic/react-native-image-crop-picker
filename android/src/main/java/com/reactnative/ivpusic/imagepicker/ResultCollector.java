package com.reactnative.ivpusic.imagepicker;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ipusic on 12/28/16.
 */

public class ResultCollector {
  private Promise promise;
  private int maxCount;
  private boolean multiple;
  private AtomicInteger waitCounter;
  private AtomicInteger filedCounter;
  private WritableArray arrayResult;
  private boolean resultSent = false;

  public ResultCollector(Promise promise, boolean multiple) {
    this.promise = promise;
    this.multiple = multiple;

    if (multiple) {
      this.arrayResult = new WritableNativeArray();
      setMaxCount(1);
    }
  }

  // if user has provided "multiple" option, we will wait for X number of result to come,
  // and also return result as an array
  public void setMaxCount(int waitCount) {
    this.maxCount = waitCount;
    this.waitCounter = new AtomicInteger(0);
    this.filedCounter = new AtomicInteger(0);
  }

  public synchronized void notifySuccess(WritableMap result) {
    if (resultSent) {
      Log.w("image-crop-picker", "Skipping result, already sent...");
    }

    if (multiple) {
      arrayResult.pushMap(result);
      int currentCount = waitCounter.addAndGet(1);

      if (currentCount == maxCount) {
        resultSent = true;
        promise.resolve(arrayResult);
      }
    } else {
      resultSent = true;
      promise.resolve(result);
    }
  }

  public synchronized void notifyProblem(String code, String message) {
    if (resultSent) {
      Log.w("image-crop-picker", "Skipping result, already sent...");
    }

    Log.e("image-crop-picker", "pick failed. " + message);

    if (multiple) {
      int currentCount = waitCounter.addAndGet(1);
      int filedCount = filedCounter.addAndGet(1);
      if (currentCount == maxCount) { // all processed
        resultSent = true;
        if (filedCount == maxCount) { // all failed
          promise.reject(code, message);
        } else {
          promise.resolve(arrayResult);
        }
      }
    } else {
      resultSent = true;
      promise.reject(code, message);
    }
  }

  public synchronized void notifyProblem(String code, Throwable throwable) {
    if (resultSent) {
      Log.w("image-crop-picker", "Skipping result, already sent...");
    }

    Log.e("image-crop-picker", "pick failed. " + throwable.getMessage());

    if (multiple) {
      int currentCount = waitCounter.addAndGet(1);
      int filedCount = filedCounter.addAndGet(1);
      if (currentCount == maxCount) { // all processed
        resultSent = true;
        if (filedCount == maxCount) { // all failed
          promise.reject(code, throwable);
        } else {
          promise.resolve(arrayResult);
        }
      }
    } else {
      resultSent = true;
      promise.reject(code, throwable);
    }
  }
}
