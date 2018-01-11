package com.reactnative.ivpusic.imagepicker.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import im.shimo.statusbarmanager.RNStatusbarManagerModule;


/**
 * Created by Martin on 2017/1/16.
 */

public abstract class BasePickerActivity extends AppCompatActivity {
    protected View contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RNStatusbarManagerModule.translucentStatusBar(this, true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (getLayoutResId() != 0) {
            contentView = inflater.inflate(getLayoutResId(), null, false);
        }
        if (contentView != null) {
            setContentView(contentView);
        }
        RNStatusbarManagerModule.steepStatusbarView(this, contentView,true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected abstract int getLayoutResId();
}
