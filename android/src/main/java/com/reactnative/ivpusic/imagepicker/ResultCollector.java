package com.reactnative.ivpusic.imagepicker;

import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ipusic on 12/28/16.
 */

class ResultCollector {
    private Promise promise;
    private int waitCount;
    private boolean multiple;
    private AtomicInteger waitCounter;
    private WritableArray arrayResult;
    private boolean resultSent;


    synchronized void setup(Promise promise, boolean multiple) {
        this.promise = promise;
        this.multiple = multiple;

        this.resultSent = false;
        this.waitCount = 0;
        this.waitCounter = new AtomicInteger(0);

        if (multiple) {
            this.arrayResult = new WritableNativeArray();
        }
    }

    // if user has provided "multiple" option, we will wait for X number of result to come,
    // and also return result as an array
    synchronized void setWaitCount(int waitCount) {
        this.waitCount = waitCount;
        this.waitCounter = new AtomicInteger(0);
    }

    synchronized private boolean isRequestValid() {
        if (resultSent) {
            Log.w("image-crop-picker", "Skipping result, already sent...");
            return false;
        }

        if (promise == null) {
            Log.w("image-crop-picker", "Trying to notify success but promise is not set");
            return false;
        }

        return true;
    }

    synchronized void notifySuccessBySortingPriority(WritableMap result) {
        if (!isRequestValid()) {
            return;
        }

        if (multiple) {
           arrayResult.pushMap(result);
            int currentCount = waitCounter.addAndGet(1);

            if (currentCount == waitCount) {
                ArrayList derivedResult = arrayResult.toArrayList();

                for(int i = 0; i< derivedResult.size(); i++){
                  for(int j = i+ 1; j < derivedResult.size(); j++){

                     HashMap iM = (HashMap) derivedResult.get(i);
                     Object val = iM.get("PRIORITY");
                     int iPriority = (int)((double)val);
                     HashMap jM = (HashMap) derivedResult.get(j);
                     Object val2 = jM.get("PRIORITY");
                     int jPriority =  (int)((double)val2);
                     HashMap holder;
                     if(jPriority < iPriority){
                       holder = (HashMap) derivedResult.get(j);
                       derivedResult.set(i ,derivedResult.get(j) );
                       derivedResult.set(j ,holder);
                     }
                  }
                }
                promise.resolve(arrayResult);
                resultSent = true;
            }
        } else {
            promise.resolve(result);
            resultSent = true;
        }
    }

  synchronized void notifySuccess(WritableMap result) {
    if (!isRequestValid()) {
      return;
    }

    if (multiple) {
      arrayResult.pushMap(result);
      int currentCount = waitCounter.addAndGet(1);

      if (currentCount == waitCount) {
        promise.resolve(arrayResult);
        resultSent = true;
      }
    } else {
      promise.resolve(result);
      resultSent = true;
    }
  }

    synchronized void notifyProblem(String code, String message) {
        if (!isRequestValid()) {
            return;
        }

        Log.e("image-crop-picker", "Promise rejected. " + message);
        promise.reject(code, message);
        resultSent = true;
    }

    synchronized void notifyProblem(String code, Throwable throwable) {
        if (!isRequestValid()) {
            return;
        }

        Log.e("image-crop-picker", "Promise rejected. " + throwable.getMessage());
        promise.reject(code, throwable);
        resultSent = true;
    }
}
