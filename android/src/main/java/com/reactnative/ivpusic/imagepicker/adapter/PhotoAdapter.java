package com.reactnative.ivpusic.imagepicker.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reactnative.ivpusic.imagepicker.PhotoLoadListener;
import com.reactnative.ivpusic.imagepicker.PickerConfig;
import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.SImagePicker;
import com.reactnative.ivpusic.imagepicker.imageloader.FrescoImageLoader;
import com.reactnative.ivpusic.imagepicker.model.Photo;
import com.reactnative.ivpusic.imagepicker.util.SystemUtil;
import com.reactnative.ivpusic.imagepicker.util.UriUtil;
import com.reactnative.ivpusic.imagepicker.widget.SquareRelativeLayout;

import java.util.ArrayList;

/**
 * Created by Martin on 2017/1/17.
 */
public class PhotoAdapter extends BaseRecycleCursorAdapter<RecyclerView.ViewHolder> {
    private int maxCount = 1;

    private final LayoutInflater layoutInflater;
    private final int photoSize;
    private ArrayList<String> selectedPhoto;
    private OnPhotoActionListener actionListener;
    private int mode;

    private boolean isCovered = false;

    public PhotoAdapter(Context context, Cursor c, @SImagePicker.PickMode int mode, int rowCount) {
        super(context, c);
        this.layoutInflater = LayoutInflater.from(context);
        this.photoSize = SystemUtil.displaySize.x / rowCount;
        this.selectedPhoto = new ArrayList<>();
        this.mode = mode;
    }

    public void setActionListener(OnPhotoActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder originHolder, Cursor cursor) {

        final PhotoViewHolder holder = (PhotoViewHolder) originHolder;

        final Photo photo = Photo.fromCursor(cursor);

        if (selectedPhoto.size() >= this.maxCount && !selectedPhoto.contains(photo.getFilePath()) && isCovered) {
            holder.coverView.setVisibility(View.VISIBLE);
        } else {
            holder.coverView.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();
        PickerConfig pickerConfig = SImagePicker.getPickerConfig();
        if (pickerConfig == null) {
            pickerConfig = new PickerConfig.Builder().setAppContext(this.mContext)
                    .setImageLoader(new FrescoImageLoader())
                    .build();
            SImagePicker.init(pickerConfig);
        }
        pickerConfig.getImageLoader().bindImage(holder.photoCell.photo,
                new Uri.Builder().scheme(UriUtil.LOCAL_FILE_SCHEME).path(photo.getFilePath()).build(),
                photoSize,
                photoSize);

        holder.photoCell.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 点击单图 预览
                 */
                actionListener.onPreview(position, photo, originHolder.itemView);
            }
        });
        if (mode == SImagePicker.MODE_IMAGE) {
            holder.photoCell.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //          PhotoAdapter.this.onCheckStateChange(holder.photoCell, photo);
                    PhotoAdapter.this.onCheckStateChange(holder, photo);
                }
            });
        } else if (mode == SImagePicker.MODE_AVATAR) {
            holder.photoCell.checkBox.setVisibility(View.INVISIBLE);
        }

        if (selectedPhoto.contains(photo.getFilePath())) {
            holder.photoCell.checkBox
                    .setText(String.valueOf(selectedPhoto.indexOf(photo.getFilePath()) + 1));
            holder.photoCell.checkBox.setChecked(true, false);
            //      holder.photoCell.photo.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            holder.photoCell.checkBox.setChecked(false, false);
            //      holder.photoCell.photo.clearColorFilter();
        }

        holder.photoCell.setTag(photo.getFilePath());
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.picker_photo_item, parent, false);
        SquareRelativeLayout photoCell = (SquareRelativeLayout) itemView.findViewById(R.id.photo_cell);
        try {
            photoCell.setPhotoView(SImagePicker.getPickerConfig().getImageLoader()
                    .createImageView(parent.getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new PhotoViewHolder(itemView);
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {

        SquareRelativeLayout photoCell;
        View coverView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photoCell = (SquareRelativeLayout) itemView.findViewById(R.id.photo_cell);
            coverView = itemView.findViewById(R.id.photo_cover);
            coverView.setClickable(true);
        }
    }

    private void onCheckStateChange(PhotoViewHolder holder, Photo photo) {
        if (isCountOver() && !selectedPhoto.contains(photo.getFilePath())) {

            return;
        }
        if (selectedPhoto.contains(photo.getFilePath())) {
            selectedPhoto.remove(photo.getFilePath());
            if (isCovered) {
                isCovered = false;
                notifyDataSetChanged();
            }
            holder.photoCell.checkBox.setChecked(false, true);
            holder.photoCell.photo.clearColorFilter();
            if (actionListener != null) {
                actionListener.onDeselect(photo.getFilePath());
            }
        } else {
            selectedPhoto.add(photo.getFilePath());
            holder.photoCell.checkBox.setText(String.valueOf(selectedPhoto.size()));
            holder.photoCell.checkBox.setChecked(true, true);

            if (actionListener != null) {
                actionListener.onSelect(photo.getFilePath());
            }
            if (isCountOver()) {
                isCovered = true;
                this.notifyDataSetChanged();
            }
        }
    }

    private void onCheckStateChange(SquareRelativeLayout photoCell, Photo photo) {
        if (isCountOver() && !selectedPhoto.contains(photo.getFilePath())) {
            //      showMaxDialog(mContext, maxCount);
            return;
        }
        if (selectedPhoto.contains(photo.getFilePath())) {
            selectedPhoto.remove(photo.getFilePath());
            photoCell.checkBox.setChecked(false, true);
            photoCell.photo.clearColorFilter();
            if (actionListener != null) {
                actionListener.onDeselect(photo.getFilePath());
            }
        } else {
            selectedPhoto.add(photo.getFilePath());
            if (isCountOver()) {

                showMaxDialog(mContext, maxCount);
            }
            photoCell.checkBox.setText(String.valueOf(selectedPhoto.size()));
            photoCell.checkBox.setChecked(true, true);
            //取消选中图片时 图片变灰色的效果
            //      photoCell.photo.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
            if (actionListener != null) {
                actionListener.onSelect(photo.getFilePath());
            }
        }
    }


    public boolean isCountOver() {
        return selectedPhoto.size() >= maxCount;
    }

    public interface OnPhotoActionListener {
        void onSelect(String filePath);

        void onDeselect(String filePath);

        void onPreview(int position, Photo photo, View view);
    }

    public ArrayList<String> getSelectedPhoto() {
        return selectedPhoto;
    }

    public void setIsCovered(boolean isCovered) {
        this.isCovered = isCovered;
    }

    public void setSelectedPhoto(ArrayList<String> selectedPhoto) {
        this.selectedPhoto = selectedPhoto;
        notifyDataSetChanged();
    }

    public void getAllPhoto(final PhotoLoadListener photoLoadListener) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ArrayList<Uri> result = new ArrayList<>();
                    mCursor.moveToPosition(-1);
                    while (mCursor.moveToNext()) {
                        result.add(new Uri.Builder().scheme(UriUtil.LOCAL_FILE_SCHEME).path(
                                Photo.fromCursor(mCursor).getFilePath()).build());
                    }
                    SystemUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (photoLoadListener != null) {
                                photoLoadListener.onLoadComplete(result);
                            }
                        }
                    });
                } catch (Exception e) {
                    SystemUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (photoLoadListener != null) {
                                photoLoadListener.onLoadError();
                            }
                        }
                    });
                }
                return null;
            }
        }.execute();
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    private static void showMaxDialog(Context context, int max) {
        new AlertDialog.Builder(context)
                .setMessage(context.getResources().getString(R.string.error_maximun_nine_photos, max))
                .setPositiveButton(R.string.general_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    public void cancelSelectedPhoto() {
        if (selectedPhoto != null && selectedPhoto.size() > 0) {
            selectedPhoto.clear();
            notifyDataSetChanged();
        }
    }
}
