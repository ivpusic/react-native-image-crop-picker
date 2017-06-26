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
  private int waitCount;
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
    }
  }

  // if user has provided "multiple" option, we will wait for X number of result to come,
  // and also return result as an array
  public void setWaitCount(int waitCount) {
    this.waitCount = waitCount;
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

      if (currentCount == waitCount) {
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
      if (currentCount == waitCount) { // all processed
        resultSent = true;
        if (filedCount == waitCount) { // all failed
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
      if (currentCount == waitCount) { // all processed
        resultSent = true;
        if (filedCount == waitCount) { // all failed
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
