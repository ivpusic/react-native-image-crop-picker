package com.reactnative.ivpusic.imagepicker.patch30277;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

public class PickerModule30277Workaround implements ReactInstanceManager.ReactInstanceEventListener {

    private @Nullable
    ActivityResultData mActivityResultData;
    private @Nullable
    WeakReference<ReactActivity> mReactActivity;


    public void onActivityResultTriggered(ReactInstanceManager reactInstanceManager, int requestCode, int resultCode, Intent data) {
        if (reactInstanceManager.getCurrentReactContext() == null) {
            mActivityResultData = new ActivityResultData(requestCode, resultCode, data);
        } else {
            mActivityResultData = null;
        }
    }

    public void onActivityResume(ReactActivity activity, ReactInstanceManager reactInstanceManager) {
        if (mActivityResultData != null) {
            mReactActivity = new WeakReference<>(activity);
        }
        reactInstanceManager.addReactInstanceEventListener(this);
    }

    public void onActivityStop(ReactInstanceManager reactInstanceManager) {
        reactInstanceManager.removeReactInstanceEventListener(this);
    }

    @Override
    public void onReactContextInitialized(ReactContext context) {
        if (mActivityResultData != null && mReactActivity != null && mReactActivity.get() != null) {
            context.onActivityResult(mReactActivity.get(), mActivityResultData.requestCode, mActivityResultData.resultCode, mActivityResultData.data);
        }
    }
}

class ActivityResultData {

    public int requestCode;
    public int resultCode;
    public Intent data;

    public ActivityResultData(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}
