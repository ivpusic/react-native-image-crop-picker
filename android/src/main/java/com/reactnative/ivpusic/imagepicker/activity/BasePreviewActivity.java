package com.reactnative.ivpusic.imagepicker.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.adapter.PreviewPagerAdapter;
import com.reactnative.ivpusic.imagepicker.widget.CheckBox;
import com.reactnative.ivpusic.imagepicker.widget.MultiPreviewViewPager;

import java.util.ArrayList;

import im.shimo.statusbarmanager.RNStatusbarManagerModule;


/**
 * Created by jack on 2017/5/8.
 */

public abstract class BasePreviewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    protected Uri firstUri;
    protected ArrayList<Uri> selectedUris;
    protected ArrayList<Uri> allUris;

    protected MultiPreviewViewPager mPager;
    protected PreviewPagerAdapter mAdapter;

    protected TextView mButtonApply;
    protected ImageView mBack;
    protected CheckBox mCheckView;

    protected int mPreviousPos = -1;
    protected int mCurrentPos = 0;
    public static final String PARAM_SELECTED_URIS = "PARAM_SELECTED_URIS";
    public static final int PARAM_SELECTED_RESULT = 1010;
    public static final int PARAM_SELECTED_RESULT_NULL = 1011;

    @Override
    public void onBackPressed() {
        if (selectedUris.size() > 0) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(PARAM_SELECTED_URIS, selectedUris);
            setResult(PARAM_SELECTED_RESULT, intent);
            finish();
        } else if (selectedUris.size() == 0) {
            setResult(PARAM_SELECTED_RESULT_NULL, new Intent());
            finish();
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RNStatusbarManagerModule.translucentStatusBar(this, true);
        View content = LayoutInflater.from(this).inflate(R.layout.activity_media_preview, null);
        setContentView(content);
        RNStatusbarManagerModule.steepStatusbarView(this, content,false);
        selectedUris = new ArrayList<>();
        allUris = new ArrayList<>();
        firstUri = Uri.parse("");
        selectedUris = getIntent().getParcelableArrayListExtra(PhotoPickerActivity.PARAM_SELECTED_URIS);
        allUris = getIntent().getParcelableArrayListExtra(PhotoPickerActivity.PARAM_ALL_URIS);
        firstUri = getIntent().getParcelableExtra(PhotoPickerActivity.PARAM_FIRST_CLICKED_URI);
        mCurrentPos = getIntent().getIntExtra(PhotoPickerActivity.PARAM_CURRENT_POSITION, 0);
        mButtonApply = (TextView) findViewById(R.id.button_apply);
        mButtonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUris != null && selectedUris.size() > 0) {
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(PhotoPickerActivity.PARAM_DATA, selectedUris);
                    setResult(PhotoPickerActivity.PARAM_SELECTED_PREVIEW, intent);
                    finish();
                }
            }
        });

        mBack = (ImageView) findViewById(R.id.preview_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPager = (MultiPreviewViewPager) findViewById(R.id.pager);
        mPager.addOnPageChangeListener(this);
        mPager.setAdapter(mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null));

        mCheckView = (CheckBox) findViewById(R.id.check_view);
        mCheckView.setBackgroundResource(R.drawable.mark_check);
        mCheckView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri item = mAdapter.getMediaItem(mPager.getCurrentItem());
                if (selectedUris.contains(item)) {
                    selectedUris.remove(item);
                    mCheckView.setChecked(false, false);
                    updateApplyButton();
                } else {
                    if (isMax()) {
                        Toast.makeText(BasePreviewActivity.this, "最多选择9张图片", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedUris.add(item);
                        mCheckView.setChecked(true, false);
                        mCheckView.setText(String.valueOf(selectedUris.indexOf(item) + 1));
                        updateApplyButton();
                    }
                }
            }
        });
        updateApplyButton();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
        if (mPreviousPos != -1 && mPreviousPos != position) {
            //            ((PreviewItemFragment) adapter.instantiateItem(mPager, mPreviousPos)).resetView();
            Uri item = adapter.getMediaItem(position);
            if (selectedUris.contains(item)) {
                mCheckView.setChecked(true, false);
                mCheckView.setText(String.valueOf(selectedUris.indexOf(item) + 1));
            } else {
                mCheckView.setChecked(false, false);
            }
        }
        mPreviousPos = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private boolean isMax() {
        return selectedUris.size() == 9;
    }

    private void updateApplyButton() {
        int selectedCount = selectedUris.size();
        if (selectedCount == 0) {
            mButtonApply.setTextColor(getResources().getColor(R.color.gray));
            mButtonApply.setText("确定");
            mButtonApply.setEnabled(false);
        } else {
            mButtonApply.setTextColor(getResources().getColor(R.color.color_48baf3));
            mButtonApply.setEnabled(true);
            mButtonApply.setText(getResources().getString(R.string.general_ok) + " "
                    + getResources().getString(R.string.bracket_num, selectedCount));
        }
    }
}
