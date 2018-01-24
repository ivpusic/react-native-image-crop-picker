package com.reactnative.ivpusic.imagepicker.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.model.AlbumListData;
import com.reactnative.ivpusic.imagepicker.util.BitmapUtil;
import com.reactnative.ivpusic.imagepicker.util.UriUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by song on 2018/1/24.
 */

public class AlbumListAdapter extends BaseAdapter {

    private final DisplayMetrics mDisplayMetrics;
    public List<AlbumListData> albumList;
    public LayoutInflater inflater;
    private Context mContext;

    public AlbumListAdapter(List<AlbumListData> albumList, Context context) {
        this.mContext = context;
        this.albumList = albumList;
        this.inflater = LayoutInflater.from(context);
        mDisplayMetrics = context.getResources().getDisplayMetrics();
    }

    @Override
    public int getCount() {
        return albumList == null ? 0 : albumList.size();
    }

    @Override
    public AlbumListData getItem(int position) {
        return albumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.albums_list_item, null);
            holder = new ViewHolder();
            holder.cover = (ImageView) convertView.findViewById(R.id.album_cover);
            holder.name = (TextView) convertView.findViewById(R.id.album_name);
            holder.count = (TextView) convertView.findViewById(R.id.album_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AlbumListData album = getItem(position);
        AlbumListData.ImageSize imageSize = album.getImageSize();
        if(imageSize ==null) {
            imageSize = getImageViewSize(holder.cover);
            album.setImageSize(imageSize);
        }
        Bitmap bitmap = BitmapUtil.decodeSampleBitmapFromPath(
                UriUtil.getRealFilePath(mContext,
                        Uri.parse(album.getCover())), imageSize.width, imageSize.height);
        holder.cover.setImageBitmap(bitmap);
        holder.name.setText(album.getName());
        holder.count.setText("" + album.getCount());

        return convertView;
    }

    /**
     * 获取ImageView的宽高
     *
     * @param imageView
     * @return
     */
    private AlbumListData.ImageSize getImageViewSize(ImageView imageView) {
        AlbumListData.ImageSize imageSize = new AlbumListData.ImageSize();


        ViewGroup.LayoutParams lp = imageView.getLayoutParams();

        int width = imageView.getWidth();//获取imageView的实际宽度
        if (width <= 0) {
            width = lp.width; //获取imageView在layout中声明的宽度
        }
        if (width <= 0) {
            //            width = imageView.getMaxWidth();//检查最大值
            width = getImageViewFieldValue(imageView, "mMaxWidth");
        }
        if (width <= 0) {
            width = mDisplayMetrics.widthPixels;
        }


        int height = imageView.getHeight();//获取imageView的实际宽度
        if (height <= 0) {
            height = lp.height; //获取imageView在layout中声明的宽度
        }
        if (height <= 0) {
            //            height = imageView.getMaxHeight();//检查最大值
            height = getImageViewFieldValue(imageView, "mMaxHeight");
        }
        if (height <= 0) {
            height = mDisplayMetrics.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = height;
        return imageSize;
    }

    /**
     * 通过反射获取imageview的某个属性值
     *
     * @param object
     * @param fieldName
     * @return
     */
    private int getImageViewFieldValue(Object object, String fieldName) {
        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    static class ViewHolder {
        private ImageView cover;
        private TextView name;
        private TextView count;
    }

}
