package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;

import com.facebook.cache.common.CacheKey;
import com.facebook.common.references.CloseableReference;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;
import com.reactnative.ivpusic.imagepicker.activity.PhotoPickerActivity;
import com.reactnative.ivpusic.imagepicker.imageloader.FrescoImageLoader;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AlbumListActivity extends AppCompatActivity {

    private static Activity instance;

    public static Context getAppContext() {
        return instance;
    }

    private TextView title;
    private TextView cancel;
    private ListView listView;

    private List<Album> albumList;
    private AlbumAdapter albumAdapter;

    public static final int REQUEST_CODE_IMAGE = 101;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE_IMAGE) {
            List<String> images = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            for (String path : images) {
                Log.e("Path : ", path);
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        instance = this;

        Fresco.initialize(this);

        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
                .setImageLoader(new FrescoImageLoader())
                .setToolbaseColor(getColor(R.color.colorPrimary))
                .build());

        cancel = (TextView) findViewById(R.id.album_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        albumList = new ArrayList<>();

        albumAdapter = new AlbumAdapter(albumList,AlbumListActivity.this);

        listView = (ListView) findViewById(R.id.album_list);

        listView.setAdapter(albumAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Log.i("chen",""+position);

//                Intent intent = new Intent(AlbumListActivity.this,PhotoListActivity.class);
//                startActivity(intent);

                Album album = albumList.get(position);
                SImagePicker
                        .from(AlbumListActivity.this)
                        .albumName(album.getName())
                        .bucketId(album.getBucketId())
                        .maxCount(9)
                        .rowCount(4)
                        .pickMode(SImagePicker.MODE_IMAGE)
                        .fileInterceptor(null)
                        .forResult(REQUEST_CODE_IMAGE);

            }
        });

        //ArrayList<String> mlist = getAllShownImagesPath(this);
        //if (mlist.size()>0)
            //Log.i("chen",mlist.get(0));

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Thumbnails._ID,
        };

        // content:// style URI for the "primary" external storage volume
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Make the query.
        Cursor cur = managedQuery(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );

        Log.i("ListingImages"," query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String id;
            String bucketId;
            String bucket;
            String date;
            //String data;
            String thumbId;

            int idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
            int bucketIdColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            //int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            int thumbIdColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails._ID);

            int i=0;

            ArrayList<String> bucketList = new ArrayList<>();
            ArrayList<String> bucketCover = new ArrayList<>();
            ArrayList<Integer> bucketCount = new ArrayList<>();
            ArrayList<String> bucketIdList = new ArrayList<>();

            do {
                // Get the field values
                id = cur.getString(idColumn);
                bucketId = cur.getString(bucketIdColumn);
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                //data = cur.getString(dataColumn);
                thumbId = cur.getString(thumbIdColumn);

                String cover = images+"/"+Integer.parseInt(thumbId);
                //Uri coverUri = Uri.parse(cover);
                //cover = getThumbnail(coverUri);

                Log.i("chen","bucketId:"+bucketId);

                if (!bucketList.contains(bucket)) {
                    bucketList.add(bucket);
                    bucketCover.add(cover);
                    bucketCount.add(1);
                    bucketIdList.add(bucketId);
                }else{
                    int index = bucketList.indexOf(bucket);
                    int count = bucketCount.get(index);
                    bucketCount.set(index,count+1);
                    //Log.e("Picker", "Title : " + bucket + "  size : " + count);

                }

                i++;

                // Do something with the values.
                Log.i("ListingImages", " bucket=" + bucket + "  date_taken=" + date + "  uri=" + cover);

            } while (cur.moveToNext());


            for (i=0;i<bucketList.size();i++){
                Album album = new Album();
                album.setCover(bucketCover.get(i));
                album.setName(bucketList.get(i));
                album.setCount(bucketCount.get(i));
                album.setBucketId(bucketIdList.get(i));
                albumList.add(album);
            }

            albumAdapter.albumList = albumList;
            albumAdapter.notifyDataSetChanged();

        }

    }



    public String getThumbnail(Uri imageUri){
        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(
                getContentResolver(), imageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null );
        if( cursor != null && cursor.getCount() > 0 ) {
            cursor.moveToFirst();//**EDIT**
            String uriString = cursor.getString( cursor.getColumnIndex( MediaStore.Images.Thumbnails.DATA ) );
            return uriString;
        }else{
            return null;
        }
    }

    public static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }


    public class Album {
        private String name;
        private int count;
        private String cover;
        private String bucketId;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getCover() {
            return cover;
        }

        public void setBucketId(String bucketId) {
            this.bucketId = bucketId;
        }

        public String getBucketId() {
            return bucketId;
        }
    }

    public class AlbumAdapter extends BaseAdapter {

        public List<Album> albumList;
        public LayoutInflater inflater;

        public AlbumAdapter(List<Album> albumList,Context context){
            this.albumList = albumList;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return albumList==null?0:albumList.size();
        }

        @Override
        public Album getItem(int position) {
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
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }
            Album album = getItem(position);
            ImageSize imageSize = getImageViewSize(holder.cover);
            Bitmap bitmap = decodeSampleBitmapFromPath(getRealFilePath(AlbumListActivity.this,
                    Uri.parse(album.getCover())), imageSize.width,imageSize.height);
            holder.cover.setImageBitmap(bitmap);
            holder.name.setText(album.getName());
            holder.count.setText(""+album.getCount());

            return convertView;
        }

        //通过Uri获取图片的实际存储路径
        public String getRealFilePath( final Context context, final Uri uri ) {
            if ( null == uri ) return null;
            final String scheme = uri.getScheme();
            String data = null;
            if ( scheme == null )
                data = uri.getPath();
            else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
                data = uri.getPath();
            } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
                Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
                if ( null != cursor ) {
                    if ( cursor.moveToFirst() ) {
                        int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                        if ( index > -1 ) {
                            data = cursor.getString( index );
                        }
                    }
                    cursor.close();
                }
            }
            return data;
        }


        //根据图片需要显示的宽和高对图片进行压缩
        protected Bitmap decodeSampleBitmapFromPath(String path, int width, int height) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(path,options);
            return bitmap;
        }


        //根据需求的宽和高以及图片实际的宽和高计算SampleSize
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            int width = options.outWidth;
            int height = options.outHeight;

            int inSampleSize = 1;

            if(width > reqWidth || height > reqHeight) {
                int widthRadio = Math.round(width * 1.0f/reqWidth);
                int heightRadio = Math.round(height * 1.0f/reqHeight);

                inSampleSize = Math.max(widthRadio, heightRadio);
            }

            return inSampleSize;
        }


        private ImageSize getImageViewSize(ImageView imageView) {
            ImageSize imageSize = new ImageSize();

            DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

            ViewGroup.LayoutParams lp = imageView.getLayoutParams();

            int width = imageView.getWidth();//获取imageView的实际宽度
            if(width <= 0) {
                width = lp.width; //获取imageView在layout中声明的宽度
            }
            if(width <= 0) {
//            width = imageView.getMaxWidth();//检查最大值
                width = getImageViewFieldValue(imageView, "mMaxWidth");
            }
            if (width <= 0) {
                width = displayMetrics.widthPixels;
            }


            int height = imageView.getHeight();//获取imageView的实际宽度
            if(height <= 0) {
                height = lp.height; //获取imageView在layout中声明的宽度
            }
            if(height <= 0) {
//            height = imageView.getMaxHeight();//检查最大值
                height = getImageViewFieldValue(imageView, "mMaxHeight");
            }
            if (height <= 0) {
                height = displayMetrics.heightPixels;
            }
            imageSize.width = width;
            imageSize.height = height;
            return imageSize;
        }

        /**
         * 通过反射获取imageview的某个属性值
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


        class ViewHolder {
            private ImageView cover;
//            private SimpleDraweeView cover;
            private TextView name;
            private TextView count;
        }

        class ImageSize {
            private int width;
            private int height;
        }

    }
}
