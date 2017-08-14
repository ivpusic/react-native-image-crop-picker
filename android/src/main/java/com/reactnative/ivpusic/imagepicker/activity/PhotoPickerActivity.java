package com.reactnative.ivpusic.imagepicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.reactnative.ivpusic.imagepicker.AlbumListActivity;
import com.reactnative.ivpusic.imagepicker.CapturePhotoHelper;
import com.reactnative.ivpusic.imagepicker.FileChooseInterceptor;
import com.reactnative.ivpusic.imagepicker.PhotoLoadListener;
import com.reactnative.ivpusic.imagepicker.PickerAction;
import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.SImagePicker;
import com.reactnative.ivpusic.imagepicker.adapter.PhotoAdapter;
import com.reactnative.ivpusic.imagepicker.control.AlbumController;
import com.reactnative.ivpusic.imagepicker.control.PhotoController;
import com.reactnative.ivpusic.imagepicker.model.Album;
import com.reactnative.ivpusic.imagepicker.model.Photo;
import com.reactnative.ivpusic.imagepicker.util.CollectionUtils;
import com.reactnative.ivpusic.imagepicker.util.UriUtil;
import com.reactnative.ivpusic.imagepicker.widget.GridInsetDecoration;
import com.reactnative.ivpusic.imagepicker.widget.PickerBottomLayout;
import com.reactnative.ivpusic.imagepicker.widget.SquareRelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Martin on 2017/1/17.
 * <p>
 * 选择照片
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

    private String albumName;
    private String bucketId;
    public static final String PARAM_ALBUM_NAME = "PARAM_ALBUM_NAME";
    public static final String PARAM_BUCKET_ID = "PARAM_BUCKET_ID";
    public static final String PARAM_SELECTED_URIS = "ARAM_SELECTED_URIS";
    public static final String PARAM_ALL_URIS = "PARAM_ALL_URIS";
    public static final String PARAM_DATA = "PARAM_DATA";
    public static final String PARAM_CURRENT_POSITION = "PARAM_CURRENT_POSITION";
    public static final String PARAM_FIRST_CLICKED_URI = "PARAM_FIRST_CLICKED_URI";
    public static final int PARAM_SELECTED_PREVIEW = 1019;

    private TextView tvPhotoTitle;//album name textview
    private TextView tvCancel;
    private ImageView ivBack;

    public static final int REQUEST_CODE_PICKER_PREVIEW = 100;
    public static final int REQUEST_CODE_CROP_IMAGE = 101;

    PickerBottomLayout bottomLayout;
    RecyclerView recyclerView;

    private GridLayoutManager layoutManager;
    private int maxCount;
    private int mode;
    private int rowCount;
    private boolean showCamera = false;
    private String avatarFilePath;
    private
    @StringRes
    int pickRes;
    private
    @StringRes
    int pickNumRes;
    private FileChooseInterceptor fileChooseInterceptor;
    private CapturePhotoHelper capturePhotoHelper;

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
                public void onPreview(final int position, final Photo photo, final View view) {
                    if (mode == SImagePicker.MODE_IMAGE) {
                        photoController.getAllPhoto(new PhotoLoadListener() {
                            @Override
                            public void onLoadComplete(ArrayList<Uri> photoUris) {
                                //TODO: Change
                                Uri firstUri = UriUtil.pathToUri(PhotoPickerActivity.this, Uri.fromFile(new File(photo.getFilePath())));
                                List<String> paths = photoController.getSelectedPhoto();
                                ArrayList<Uri> selectedUris = UriUtil.getUris(PhotoPickerActivity.this, paths);
                                ArrayList<Uri> allUris = UriUtil.getContentUris(PhotoPickerActivity.this, photoUris);
                                Intent intent = new Intent(PhotoPickerActivity.this, SelectedPreviewActivity.class);
                                intent.putExtra(PARAM_FIRST_CLICKED_URI, firstUri);
                                intent.putParcelableArrayListExtra(PARAM_SELECTED_URIS, selectedUris);
                                intent.putParcelableArrayListExtra(PARAM_ALL_URIS, allUris);
                                intent.putExtra(PARAM_CURRENT_POSITION, position);
                                PhotoPickerActivity.this.startActivityForResult(intent, PARAM_SELECTED_PREVIEW);
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
        albumName = getIntent().getStringExtra(PARAM_ALBUM_NAME);
        bucketId = getIntent().getStringExtra(PARAM_BUCKET_ID);
        initUI();
    }

    private void initUI() {
        bottomLayout = (PickerBottomLayout) findViewById(R.id.picker_bottom);
        bottomLayout.showSendNumber = maxCount > 1;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        ivBack = (ImageView) findViewById(R.id.photo_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvCancel = (TextView) findViewById(R.id.album_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoController.cancelSelectedPhoto();
                updateBottomBar();
            }
        });

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

        /**
         * 通过bucketId加载相册
         */
        photoController.loadAlbumPhoto(this, String.valueOf(bucketId));

        fileChooseInterceptor = getIntent().getParcelableExtra(PARAM_FILE_CHOOSE_INTERCEPTOR);
        ArrayList<String> selected = getIntent().getStringArrayListExtra(PARAM_SELECTED);
        if (!CollectionUtils.isEmpty(selected)) {
            photoController.setSelectedPhoto(selected);
        }
        pickRes = getIntent().getIntExtra(PARAM_CUSTOM_PICK_TEXT_RES, 0);
        bottomLayout.setCustomPickText(pickRes);
        updateBottomBar();

        tvPhotoTitle = (TextView) findViewById(R.id.photo_title);
        tvPhotoTitle.setText(albumName);
        albumController.onCreate(this, directorySelectListener);
        bottomLayout.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commit();
            }
        });

        bottomLayout.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preview();
            }
        });
    }

    private void preview() {
        List<String> images = photoController.getSelectedPhoto();
        if (images != null && images.size() > 0 && !images.isEmpty()) {
            Intent intent = new Intent(this, SelectedPreviewActivity.class);
            ArrayList<Uri> uris = new ArrayList<>();
            uris = UriUtil.getUris(this, images);
            intent.putParcelableArrayListExtra(PARAM_SELECTED_URIS, uris);
            startActivityForResult(intent, PARAM_SELECTED_PREVIEW);
        }
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
        finish();
    }

    private void commit() {
        if (!photoController.getSelectedPhoto().isEmpty()) {

            /**
             * 确定后 获取选中的photo的存储路径
             */
            List<String> photos = photoController.getSelectedPhoto();
            for (String photo : photos) {
                Log.e("Photo", photo);
            }
            setResultAndFinish(photoController.getSelectedPhoto(), AlbumListActivity.REQUEST_CODE_IMAGE);
        }
    }

    private void setResultAndFinish(ArrayList<String> selected, int resultCode) {
        if (fileChooseInterceptor != null
                && !fileChooseInterceptor.onFileChosen(this, selected, false, resultCode, this)) {
            // Prevent finish if interceptor returns false.
            return;
        }
        proceedResultAndFinish(selected, false, resultCode);
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
                //        bottomLayout.originalCheckbox.setChecked(selectOriginal);
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
        } else if (resultCode == PARAM_SELECTED_PREVIEW) {
            /**
             * 预览后 确定
             */
            setResult(AlbumListActivity.REQUEST_CODE_IMAGE_SELECTED, data);
            finish();
        } else if (resultCode == BasePreviewActivity.PARAM_SELECTED_RESULT) { // 预览后有选择的照片
            ArrayList<Uri> uris = data.getParcelableArrayListExtra(BasePreviewActivity.PARAM_SELECTED_URIS);
            ArrayList<String> paths = new ArrayList<>();
            for (Uri uri : uris) {
                String path = UriUtil.getRealFilePath(PhotoPickerActivity.this, uri);
                paths.add(path);
            }
            photoController.setSelectedPhoto(paths);
            this.updateBottomBar();
        } else if (resultCode == BasePreviewActivity.PARAM_SELECTED_RESULT_NULL) { // 预览后没有选择的照片
            photoController.setSelectedPhoto(new ArrayList<String>());
            this.updateBottomBar();
        }
    }

    private void updateBottomBar() {
        if (mode == SImagePicker.MODE_IMAGE) {
            bottomLayout.updateSelectedCount(photoController.getSelectedPhoto().size());
        } else if (mode == SImagePicker.MODE_AVATAR) {
            bottomLayout.setVisibility(View.GONE);
        }
    }

    private void refreshCheckbox() {
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        int lastVisible = layoutManager.findLastVisibleItemPosition();
        for (int i = firstVisible; i <= lastVisible; i++) {
            View view = layoutManager.findViewByPosition(i);
            if (view instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) view;
                if (frameLayout != null) {
                    SquareRelativeLayout item = (SquareRelativeLayout) frameLayout.findViewById(R.id.photo_cell);
                    if (item != null) {
                        String photoPath = (String) item.getTag();
                        if (photoController.getSelectedPhoto().contains(photoPath)) {
                            item.checkBox.setText(String.valueOf(photoController.getSelectedPhoto().indexOf(photoPath) + 1));
                            item.checkBox.refresh(false);
                        }
                    }
                }
            }
        }
    }
}
