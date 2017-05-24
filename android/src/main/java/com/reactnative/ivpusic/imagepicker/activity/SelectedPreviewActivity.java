package com.reactnative.ivpusic.imagepicker.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by jack on 2017/5/8.
 */

public class SelectedPreviewActivity extends BasePreviewActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (allUris != null && allUris.size() > 0) {
            mAdapter.addAll(allUris);
            mPager.setCurrentItem(mCurrentPos);
            if (selectedUris.contains(firstUri)) {
                mCheckView.setText(String.valueOf(selectedUris.indexOf(firstUri) + 1));
                mCheckView.setChecked(true, false);
            }
            mAdapter.notifyDataSetChanged();
        }
        else if (selectedUris.size() >0 && selectedUris != null) {
            mAdapter.addAll(selectedUris);
            mAdapter.notifyDataSetChanged();
            mCheckView.setChecked(true, false);
            mCheckView.setText("1");
        }
        mPreviousPos = 0;
    }
}
