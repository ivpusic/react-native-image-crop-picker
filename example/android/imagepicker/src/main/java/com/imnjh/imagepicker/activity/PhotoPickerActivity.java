package com.imnjh.imagepicker.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.imnjh.imagepicker.CapturePhotoHelper;
import com.imnjh.imagepicker.FileChooseInterceptor;
import com.imnjh.imagepicker.PhotoLoadListener;
import com.imnjh.imagepicker.PickerAction;
import com.imnjh.imagepicker.R;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.adapter.PhotoAdapter;
import com.imnjh.imagepicker.control.AlbumController;
import com.imnjh.imagepicker.control.PhotoController;
import com.imnjh.imagepicker.model.Album;
import com.imnjh.imagepicker.model.Photo;
import com.imnjh.imagepicker.util.CollectionUtils;
import com.imnjh.imagepicker.util.FileUtil;
import com.imnjh.imagepicker.widget.GridInsetDecoration;
import com.imnjh.imagepicker.widget.PickerBottomLayout;
import com.imnjh.imagepicker.widget.SquareRelativeLayout;


/**
 * Created by Martin on 2017/1/17.
 */
public class PhotoPickerActivity extends BasePickerActivity implements PickerAction {

  public static final String EXTRA_RESULT_SELECTION = "EXTRA_RESULT_SELECTION";
  public static final String EXTRA_RESULT_ORIGINAL = "EXTRA_RESULT_ORIGINAL";

  public static final String PARAM_MODE = "PARAM_MODE";
  public static final String PARAM_MAX_COUNT = "PARAM_MAX_COUNT";
  public static final String PARAM_SELECTED = "PARAM_SELECTED";
  public static final String PARAM_ROW_COUNT = "PARAM_ROW_COUNT";
  public static final String PARAM_SHOW_CAMERA = "PARAM_SHOW_CAMERA";
  public static final String PARAM_CUSTOM_PICK_TEXT_RES = "PARAM_CUSTOM_PICK_TEXT_RES";
  public static final String PARAM_FILE_CHOOSE_INTERCEPTOR = "PARAM_FILE_CHOOSE_INTERCEPTOR";

  public static final int REQUEST_CODE_PICKER_PREVIEW = 100;
  public static final int REQUEST_CODE_CROP_IMAGE = 101;

  PickerBottomLayout bottomLayout;
  RecyclerView recyclerView;
  Toolbar toolbar;

  private GridLayoutManager layoutManager;
  private int maxCount;
  private int mode;
  private int rowCount;
  private boolean showCamera = false;
  private String avatarFilePath;
  private @StringRes int pickRes;
  private @StringRes int pickNumRes;
  private FileChooseInterceptor fileChooseInterceptor;
  private CapturePhotoHelper capturePhotoHelper;

  private AppCompatSpinner albumSpinner;
  private final PhotoController photoController = new PhotoController();
  private final AlbumController albumController = new AlbumController();
  private final AlbumController.OnDirectorySelectListener directorySelectListener =
      new AlbumController.OnDirectorySelectListener() {
        @Override
        public void onSelect(Album album) {
          photoController.resetLoad(album);
        }

        @Override
        public void onReset(Album album) {
          photoController.load(album);
        }
      };

  private final PhotoAdapter.OnPhotoActionListener selectionChangeListener =
      new PhotoAdapter.OnPhotoActionListener() {

        @Override
        public void onSelect(String filePath) {
          updateBottomBar();
        }

        @Override
        public void onDeselect(String filePath) {
          updateBottomBar();
          refreshCheckbox();
        }

        @Override
        public void onPreview(final int position, Photo photo, final View view) {
          if (mode == SImagePicker.MODE_IMAGE) {
            photoController.getAllPhoto(new PhotoLoadListener() {
              @Override
              public void onLoadComplete(ArrayList<Uri> photoUris) {
                if (!CollectionUtils.isEmpty(photoUris)) {
                  PickerPreviewActivity.startPicturePreviewFromPicker(PhotoPickerActivity.this,
                      photoUris, photoController.getSelectedPhoto(), position,
                      bottomLayout.originalCheckbox.isChecked(), maxCount, rowCount,
                      fileChooseInterceptor,
                      pickRes, pickNumRes,
                      PickerPreviewActivity.AnchorInfo.newInstance(view),
                      REQUEST_CODE_PICKER_PREVIEW);
                }
              }

              @Override
              public void onLoadError() {

              }
            });
          } else if (mode == SImagePicker.MODE_AVATAR) {
            CropImageActivity.startImageCrop(PhotoPickerActivity.this, photo.getFilePath(),
                REQUEST_CODE_CROP_IMAGE, avatarFilePath);
          }
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mode = getIntent().getIntExtra(PARAM_MODE, SImagePicker.MODE_IMAGE);
    maxCount = getIntent().getIntExtra(PARAM_MAX_COUNT, 1);
    avatarFilePath = getIntent().getStringExtra(CropImageActivity.PARAM_AVATAR_PATH);
    rowCount = getIntent().getIntExtra(PARAM_ROW_COUNT, 4);
    showCamera = getIntent().getBooleanExtra(PARAM_SHOW_CAMERA, false);
    initUI();
  }

  private void initUI() {
    bottomLayout = (PickerBottomLayout) findViewById(R.id.picker_bottom);
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    if (SImagePicker.getPickerConfig().getToolbarColor() != 0) {
      toolbar.setBackgroundColor(SImagePicker.getPickerConfig().getToolbarColor());
    }
    recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onBackPressed();
      }
    });
    toolbar.setNavigationIcon(R.drawable.ic_general_cancel_left);
    layoutManager = new GridLayoutManager(this, rowCount);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new GridInsetDecoration());
    if (!showCamera) {
      photoController.onCreate(this, recyclerView, selectionChangeListener, maxCount, rowCount,
          mode);
    } else {
      capturePhotoHelper = new CapturePhotoHelper(this);
      photoController.onCreate(this, recyclerView, selectionChangeListener, maxCount, rowCount,
          mode, capturePhotoHelper);
    }
    photoController.loadAllPhoto(this);

    fileChooseInterceptor = getIntent().getParcelableExtra(PARAM_FILE_CHOOSE_INTERCEPTOR);
    ArrayList<String> selected = getIntent().getStringArrayListExtra(PARAM_SELECTED);
    if (!CollectionUtils.isEmpty(selected)) {
      photoController.setSelectedPhoto(selected);
    }
    pickRes = getIntent().getIntExtra(PARAM_CUSTOM_PICK_TEXT_RES, 0);
    bottomLayout.setCustomPickText(pickRes);
    updateBottomBar();

    albumSpinner =
        (AppCompatSpinner) LayoutInflater.from(this).inflate(R.layout.common_toolbar_spinner,
            toolbar, false);
    toolbar.addView(albumSpinner);
    albumController.onCreate(this, albumSpinner, directorySelectListener);
    albumController.loadAlbums();
    bottomLayout.send.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        commit();
      }
    });
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.activity_photo_picker;
  }


  @Override
  protected void onDestroy() {
    albumController.onDestroy();
    photoController.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    setResultAndFinish(photoController.getSelectedPhoto(),
        bottomLayout.originalCheckbox.isChecked(), Activity.RESULT_CANCELED);
  }

  private void commit() {
    if (!photoController.getSelectedPhoto().isEmpty()) {
      setResultAndFinish(photoController.getSelectedPhoto(),
          bottomLayout.originalCheckbox.isChecked(), Activity.RESULT_OK);
    }
  }

  private void setResultAndFinish(ArrayList<String> selected, boolean original, int resultCode) {
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
    intent.putStringArrayListExtra(EXTRA_RESULT_SELECTION, selected);
    intent.putExtra(EXTRA_RESULT_ORIGINAL, original);
    setResult(resultCode, intent);
    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICKER_PREVIEW) {
      if (data != null) {
        ArrayList<String> selected =
            data.getStringArrayListExtra(PickerPreviewActivity.KEY_SELECTED);
        boolean selectOriginal =
            data.getBooleanExtra(PickerPreviewActivity.KEY_SELECTED_ORIGINAL, false);
        bottomLayout.originalCheckbox.setChecked(selectOriginal);
        if (resultCode == Activity.RESULT_CANCELED) {
          photoController.setSelectedPhoto(selected);
          updateBottomBar();
        } else if (resultCode == Activity.RESULT_OK) {
          setResultAndFinish(selected, selectOriginal, Activity.RESULT_OK);
        }
      }
    } else if (requestCode == REQUEST_CODE_CROP_IMAGE) {
      if (resultCode == Activity.RESULT_OK) {
        String path = data.getStringExtra(CropImageActivity.RESULT_PATH);
        ArrayList<String> result = new ArrayList<>();
        result.add(path);
        setResultAndFinish(result, true, Activity.RESULT_OK);
      }
    } else if (requestCode == CapturePhotoHelper.CAPTURE_PHOTO_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_CANCELED) {
        if (capturePhotoHelper.getPhoto() != null && capturePhotoHelper.getPhoto().exists()) {
          capturePhotoHelper.getPhoto().delete();
        }
      } else if (resultCode == Activity.RESULT_OK) {
        if (mode == SImagePicker.MODE_AVATAR) {
          File photoFile = capturePhotoHelper.getPhoto();
          if (photoFile != null) {
            CropImageActivity.startImageCrop(this, photoFile.getAbsolutePath(),
                REQUEST_CODE_CROP_IMAGE,
                avatarFilePath);
          }
        } else {
          File photoFile = capturePhotoHelper.getPhoto();
          ArrayList<String> result = new ArrayList<>();
          result.add(photoFile.getAbsolutePath());
          setResultAndFinish(result, true, Activity.RESULT_OK);
        }
      }
    }
  }

  private void updateBottomBar() {
    if (mode == SImagePicker.MODE_IMAGE) {
      bottomLayout.updateSelectedCount(photoController.getSelectedPhoto().size());
      if (CollectionUtils.isEmpty(photoController.getSelectedPhoto())) {
        bottomLayout.updateSelectedSize(null);
      } else {
        bottomLayout.updateSelectedSize(FileUtil.getFilesSize(PhotoPickerActivity.this,
            photoController.getSelectedPhoto()));
      }
    } else if (mode == SImagePicker.MODE_AVATAR) {
      bottomLayout.setVisibility(View.GONE);
    }
  }

  private void refreshCheckbox() {
    int firstVisible = layoutManager.findFirstVisibleItemPosition();
    int lastVisible = layoutManager.findLastVisibleItemPosition();
    for (int i = firstVisible; i <= lastVisible; i++) {
      View view = layoutManager.findViewByPosition(i);
      if (view instanceof SquareRelativeLayout) {
        SquareRelativeLayout item = (SquareRelativeLayout) view;
        if (item != null) {
          String photoPath = (String) item.getTag();
          if (photoController.getSelectedPhoto().contains(photoPath)) {
            item.checkBox.setText(String.valueOf(photoController.getSelectedPhoto()
                .indexOf(photoPath) + 1));
            item.checkBox.refresh(false);
          }
        }
      }
    }
  }
}
