package com.imnjh.imagepicker.activity;

import java.io.File;
import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.imnjh.imagepicker.FileChooseInterceptor;
import com.imnjh.imagepicker.PickerAction;
import com.imnjh.imagepicker.R;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.util.FileUtil;
import com.imnjh.imagepicker.util.ImageUtil;
import com.imnjh.imagepicker.util.SystemUtil;
import com.imnjh.imagepicker.widget.CheckBox;
import com.imnjh.imagepicker.widget.PickerBottomLayout;
import com.imnjh.imagepicker.widget.PicturePreviewPageView;
import com.imnjh.imagepicker.widget.PreviewViewPager;
import com.imnjh.imagepicker.widget.subsamplingview.ImageSource;
import com.imnjh.imagepicker.widget.subsamplingview.OnImageEventListener;

/**
 * Created by Martin on 2017/1/17.
 */
public class PickerPreviewActivity extends BasePickerActivity implements PickerAction {

  public static final String KEY_URIS = "picture_uri";
  public static final String KEY_SELECTED = "picture_selected";
  public static final String KEY_SELECTED_ORIGINAL = "select_original";
  public static final String KEY_CURRENT_POSITION = "current_position";
  public static final String KEY_MAX_COUNT = "max_count";
  public static final String KEY_ROW_COUNT = "row_count";
  public static final String KEY_ANCHOR_INFO = "anchor_info";
  public static final String KEY_FILE_CHOOSE_INTERCEPTOR = "file_choose_interceptor";
  public static final String PARAM_CUSTOM_PICK_TEXT_RES = "PARAM_CUSTOM_PICK_TEXT_RES";

  private static final int STATE_FULLSCREEN = 0;
  private static final int STATE_SHOW_MENU = 1;

  private static final long IMAGE_SHOW_UP_DURATION = 280L;
  private static final long IMAGE_DISMISS_DURATION = 230L;

  private boolean initImageLoaded;

  PreviewViewPager viewPager;
  CheckBox checkBox;
  PickerBottomLayout previewBottomLayout;
  Toolbar toolbar;
  ImageView fakeImage;
  FrameLayout containerView;
  View navView;
  TextView titleView;


  private ArrayList<Uri> uris = new ArrayList<>();
  private ArrayList<String> selected = new ArrayList<>();
  private int max = 9;
  private int rowCount = 4;
  private boolean selectOriginal = false;
  private String title;
  private int currentState = STATE_SHOW_MENU;
  private FileChooseInterceptor fileChooseInterceptor;
  private PreviewAdapter previewAdapter;


  private boolean enterAnimationRunning;
  private boolean exitAnimationRunning;
  private ValueAnimator enterAnimator;
  private ValueAnimator exitAnimator;

  private int initPosition;
  private int toolbarHeight;
  private int bottomLayoutHeight;

  private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
      checkBox.setChecked(selected.contains(uris.get(position).getPath()), false);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initUI();
  }

  /**
   * 
   * @param activityContext
   * @param pictures
   * @param currentPosition
   */
  public static void startPicturePreviewFromPicker(Activity activityContext,
      ArrayList<Uri> pictures, ArrayList<String> selected, int currentPosition,
      boolean selectOriginal, int maxCount, int rowCount,
      FileChooseInterceptor fileChooseInterceptor,
      @StringRes int pickRes, @StringRes int pickNumRes, AnchorInfo anchorInfo,
      int requestCode) {
    Intent intent = new Intent(activityContext, PickerPreviewActivity.class);
    intent.putExtra(KEY_CURRENT_POSITION, currentPosition);
    intent.putParcelableArrayListExtra(KEY_URIS, pictures);
    intent.putStringArrayListExtra(KEY_SELECTED, selected);
    intent.putExtra(KEY_SELECTED_ORIGINAL, selectOriginal);
    intent.putExtra(KEY_MAX_COUNT, maxCount);
    intent.putExtra(KEY_ROW_COUNT, rowCount);
    intent.putExtra(KEY_FILE_CHOOSE_INTERCEPTOR, fileChooseInterceptor);
    intent.putExtra(PhotoPickerActivity.PARAM_CUSTOM_PICK_TEXT_RES, pickRes);
    intent.putExtra(KEY_ANCHOR_INFO, anchorInfo);
    activityContext.startActivityForResult(intent, requestCode);
  }

  public static class AnchorInfo implements Parcelable {

    public int left;
    public int top;
    public int width;
    public int height;

    public static AnchorInfo newInstance(View view) {
      final AnchorInfo anchorInfo = new AnchorInfo();
      final int[] loc = new int[2];
      view.getLocationOnScreen(loc);
      anchorInfo.left = loc[0];
      anchorInfo.top = loc[1];
      anchorInfo.width = view.getWidth();
      anchorInfo.height = view.getHeight();
      return anchorInfo;
    }

    private AnchorInfo() {}

    private FrameLayout.LayoutParams genInitLayoutParams() {
      final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
      lp.leftMargin = left;
      lp.topMargin = SystemUtil.getSdkVersionInt() >= Build.VERSION_CODES.KITKAT
          ? top
          : top - SystemUtil.statusBarHeight;
      return lp;
    }

    protected AnchorInfo(Parcel in) {
      left = in.readInt();
      top = in.readInt();
      width = in.readInt();
      height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(left);
      dest.writeInt(top);
      dest.writeInt(width);
      dest.writeInt(height);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    public static final Creator<AnchorInfo> CREATOR = new Creator<AnchorInfo>() {
      @Override
      public AnchorInfo createFromParcel(Parcel in) {
        return new AnchorInfo(in);
      }

      @Override
      public AnchorInfo[] newArray(int size) {
        return new AnchorInfo[size];
      }
    };
  }



  private void initUI() {
    toolbarHeight =
        getResources().getDimensionPixelSize(R.dimen.toolbar_height) + SystemUtil.statusBarHeight;
    bottomLayoutHeight = getResources().getDimensionPixelSize(R.dimen.bottombar_height);
    navView = findViewById(R.id.nav_icon);
    containerView = (FrameLayout) findViewById(R.id.container);
    fakeImage = SImagePicker.getPickerConfig().getImageLoader().createFakeImageView(this);
    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(0, 0);
    containerView.addView(fakeImage);
    viewPager = (PreviewViewPager) findViewById(R.id.viewpager);
    checkBox = (CheckBox) findViewById(R.id.checkbox);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    titleView = (TextView) findViewById(R.id.title);
    if (SImagePicker.getPickerConfig().getToolbarColor() != 0) {
      toolbar.setBackgroundColor(SImagePicker.getPickerConfig().getToolbarColor());
    }
    previewBottomLayout = (PickerBottomLayout) findViewById(R.id.picker_bottom);
    FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
    layoutParams2.height += SystemUtil.statusBarHeight;
    toolbar.setLayoutParams(layoutParams2);
    navView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    max = getIntent().getIntExtra(KEY_MAX_COUNT, 9);
    rowCount = getIntent().getIntExtra(KEY_ROW_COUNT, 4);
    fileChooseInterceptor = getIntent().getParcelableExtra(KEY_FILE_CHOOSE_INTERCEPTOR);
    ArrayList<Uri> uriParam = getIntent().getParcelableArrayListExtra(KEY_URIS);
    ArrayList<String> selectedParam = getIntent().getStringArrayListExtra(KEY_SELECTED);
    selectOriginal = getIntent().getBooleanExtra(KEY_SELECTED_ORIGINAL, false);
    initPosition = getIntent().getIntExtra(KEY_CURRENT_POSITION, 0);
    if (selectedParam != null) {
      selected.addAll(selectedParam);
    }
    if (uriParam != null) {
      uris.addAll(uriParam);
    }
    checkBox.setChecked(selected.contains(uris.get(initPosition).getPath()), false);
    previewAdapter = new PreviewAdapter();
    viewPager.setAdapter(previewAdapter);
    viewPager.addOnPageChangeListener(pageChangeListener);
    viewPager.setCurrentItem(initPosition);
    checkBox.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String filePath = uris.get(viewPager.getCurrentItem()).getPath();
        if (isCountOver() && !selected.contains(filePath)) {
          showMaxDialog(PickerPreviewActivity.this, max);
          return;
        }
        checkBox.setChecked(!checkBox.isChecked(), true);
        toggleSelectPhoto(filePath);
      }
    });
    startEnterAnimation(uris.get(viewPager.getCurrentItem()));

    previewBottomLayout.originalCheckbox.setChecked(selectOriginal);
    previewBottomLayout.send.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        send();
      }
    });
    updateTitle();
    int pickTextRes = getIntent().getIntExtra(PARAM_CUSTOM_PICK_TEXT_RES, 0);
    previewBottomLayout.setCustomPickText(pickTextRes);
    updateBottomBar();
  }

  private void startEnterAnimation(Uri enterImgUri) {
    hideMenu();
    final AnchorInfo anchorInfo = getIntent().getParcelableExtra(KEY_ANCHOR_INFO);
    if (anchorInfo == null || enterImgUri == null) {
      containerView.setAlpha(0f);
      containerView.animate().alpha(1f)
          .setDuration(IMAGE_SHOW_UP_DURATION)
          .setInterpolator(new AccelerateDecelerateInterpolator())
          .setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
          }).start();
      return;
    }
    fakeImage.setVisibility(View.VISIBLE);
    viewPager.setVisibility(View.INVISIBLE);
    viewPager.setScrollEnabled(false);
    containerView.setBackgroundColor(Color.TRANSPARENT);

    SImagePicker
        .getPickerConfig()
        .getImageLoader()
        .bindImage(fakeImage, enterImgUri, SystemUtil.displaySize.x / rowCount,
            SystemUtil.displaySize.x / rowCount);

    final FrameLayout.LayoutParams initLP = anchorInfo.genInitLayoutParams();
    fakeImage.setLayoutParams(initLP);

    final int finalWidth;
    final int finalHeight;
    final int finalMarginLeft;
    final int finalMarginTop;

    final int windowHeight = SystemUtil.getSdkVersionInt() >= Build.VERSION_CODES.KITKAT
        ? SystemUtil.displayMetrics.heightPixels
        : SystemUtil.displayMetrics.heightPixels - SystemUtil.statusBarHeight;
    finalWidth = SystemUtil.displayMetrics.widthPixels;
    finalHeight = windowHeight;
    finalMarginLeft = 0;
    finalMarginTop = 0;

    enterAnimator = ValueAnimator.ofPropertyValuesHolder(
        PropertyValuesHolder.ofInt("marginLeft", initLP.leftMargin, finalMarginLeft),
        PropertyValuesHolder.ofInt("marginTop", initLP.topMargin, finalMarginTop),
        PropertyValuesHolder.ofInt("width", initLP.width, finalWidth),
        PropertyValuesHolder.ofInt("height", initLP.height, finalHeight));
    enterAnimator.setDuration(IMAGE_SHOW_UP_DURATION);
    enterAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    enterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fakeImage.getLayoutParams();
        lp.width = (int) animation.getAnimatedValue("width");
        lp.height = (int) animation.getAnimatedValue("height");
        lp.topMargin = (int) animation.getAnimatedValue("marginTop");
        lp.leftMargin = (int) animation.getAnimatedValue("marginLeft");
        fakeImage.requestLayout();

        containerView
            .setBackgroundColor(ImageUtil.adjustAlpha(Color.BLACK, animation.getAnimatedFraction()));
      }

    });
    enterAnimator.addListener(new AnimatorListenerAdapter() {

      @Override
      public void onAnimationEnd(Animator animation) {
        if (initImageLoaded) {
          fakeImage.setVisibility(View.GONE);
        }
        viewPager.setVisibility(View.VISIBLE);
        enterAnimationRunning = false;
      }

    });
    enterAnimator.start();
    enterAnimationRunning = true;
  }

  private void startExitAnimation() {
    if (exitAnimationRunning) {
      return;
    }
    exitAnimator = ValueAnimator.ofPropertyValuesHolder(
        PropertyValuesHolder.ofFloat("alpha", 1f, 0f),
        PropertyValuesHolder.ofFloat("scale", 1f, 0.7f));
    exitAnimator.setDuration(IMAGE_DISMISS_DURATION);
    exitAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    exitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        viewPager.setAlpha((Float) animation.getAnimatedValue("alpha"));
        final float scale = (float) animation.getAnimatedValue("scale");
        viewPager.setScaleX(scale);
        viewPager.setScaleY(scale);
        containerView.setBackgroundColor(
            ImageUtil.adjustAlpha(Color.BLACK, (1 - animation.getAnimatedFraction())));
      }

    });
    exitAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        if (fakeImage.getVisibility() == View.VISIBLE) {
          fakeImage.setVisibility(View.GONE);
        }
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        exitAnimationRunning = false;
        setResultAndFinish(Activity.RESULT_CANCELED);
      }
    });
    exitAnimator.start();
    exitAnimationRunning = true;
  }


  @Override
  protected int getLayoutResId() {
    return R.layout.activity_picker_preview;
  }

  private View.OnClickListener photoTapListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      toggleStateChange();
    }
  };



  class PreviewAdapter extends PagerAdapter {

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
      final PicturePreviewPageView pageView = new PicturePreviewPageView(container.getContext());
      pageView.setMaxScale(15);
      pageView.setOnClickListener(photoTapListener);
      pageView.getOriginImageView().setOnImageEventListener(new OnImageEventListener() {

        @Override
        public void onImageLoaded(int width, int height) {
          if (isFinishing()) {
            return;
          }
          if (position == initPosition) {
            initImageLoaded = true;
            toggleStateChange();
            if (!enterAnimationRunning && fakeImage.getVisibility() == View.VISIBLE) {
              fakeImage.setVisibility(View.GONE);
              viewPager.setScrollEnabled(true);
            }
          }
        }
      });
      Uri curUriInfo = uris.get(position);
      File file = new File(curUriInfo.getPath());
      pageView.setOriginImage(ImageSource.uri(file.getAbsolutePath()));
      pageView.setBackgroundColor(Color.TRANSPARENT);
      container.addView(pageView,
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      pageView.setTag(position);
      return pageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      container.removeView((View) object);
    }

    @Override
    public int getCount() {
      return uris.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == object;
    }

  }


  private void toggleStateChange() {
    if (currentState == STATE_SHOW_MENU) {
      hideTitleBar();
      previewBottomLayout.hide();
      contentView.setSystemUiVisibility(View.INVISIBLE);
      currentState = STATE_FULLSCREEN;
    } else {
      showTitleBar();
      previewBottomLayout.show();
      contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
      currentState = STATE_SHOW_MENU;
    }
  }

  private void hideMenu() {
    toolbar.setTranslationY(-toolbarHeight);
    previewBottomLayout.setTranslationY(bottomLayoutHeight);
    contentView.setSystemUiVisibility(View.INVISIBLE);
    currentState = STATE_FULLSCREEN;
  }


  private void hideTitleBar() {
    toolbar.animate().translationY(-toolbar.getHeight())
        .setInterpolator(new AccelerateInterpolator(2));
  }

  private void showTitleBar() {
    toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
  }

  private void toggleSelectPhoto(String filePath) {
    for (String existItem : selected) {
      if (TextUtils.equals(existItem, filePath)) {
        selected.remove(existItem);
        updateTitle();
        updateBottomBar();
        return;
      }
    }
    selected.add(filePath);
    updateTitle();
    updateBottomBar();
  }

  @Override
  public void onBackPressed() {
    if (enterAnimationRunning) {
      return;
    }
    if (exitAnimationRunning) {
      return;
    }
    startExitAnimation();
  }

  private void updateTitle() {
    titleView.setText(selected.size() + "/" + max);
  }

  @Override
  protected void onDestroy() {
    viewPager.removeOnPageChangeListener(pageChangeListener);
    super.onDestroy();
  }

  private void setResultAndFinish(int resultCode) {
    boolean original = previewBottomLayout.originalCheckbox.isChecked();
    if (fileChooseInterceptor != null
        && !fileChooseInterceptor.onFileChosen(this, selected, original, resultCode, this)) {
      // Prevent finish if interceptor returns false.
      return;
    }
    proceedResultAndFinish(selected, original, resultCode);
  }

  @Override
  public void proceedResultAndFinish(ArrayList<String> selected, boolean original, int resultCode) {
    Intent intent = new Intent();
    intent.putStringArrayListExtra(KEY_SELECTED, selected);
    intent.putExtra(KEY_SELECTED_ORIGINAL, original);
    setResult(resultCode, intent);
    finish();
  }

  private void updateBottomBar() {
    if (selected.isEmpty()) {
      previewBottomLayout.updateSelectedSize(null);
    } else {
      previewBottomLayout.updateSelectedSize(FileUtil.getFilesSize(this, selected));
    }
    previewBottomLayout.updateSelectedCount(selected.size());
  }

  private void send() {
    setResultAndFinish(Activity.RESULT_OK);
  }

  private boolean isCountOver() {
    return selected.size() >= max;
  }

  private static void showMaxDialog(Context context, int max) {
    new AlertDialog.Builder(context)
        .setMessage(context.getResources().getString(R.string.error_maximun_nine_photos, max))
        .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        }).show();
  }
}
