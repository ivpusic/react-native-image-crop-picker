package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoListActivity extends AppCompatActivity {

    private ImageView back;
    private TextView title;
    private TextView cancel;

    private GridView gridView;
    private List<Photo> photoList;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_list);

        back = (ImageView) findViewById(R.id.photo_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = (TextView) findViewById(R.id.photo_title);
        cancel = (TextView) findViewById(R.id.photo_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        int width = (int) this.getWindowManager().getDefaultDisplay().getWidth() / 4;

        photoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(photoList, PhotoListActivity.this, width);
        gridView = (GridView) findViewById(R.id.photo_list);
        gridView.setAdapter(photoAdapter);
        //gridView.setAdapter(new ImageAdapter(PhotoListActivity.this,width));
        gridView.setColumnWidth(width);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(PhotoListActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });

        //ArrayList<String> mlist = getAllShownImagesPath(this);

        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
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

        if (cur.moveToFirst()) {
            String id;
            String bucket;
            String date;
            //String data;
            String thumbId;

            int idColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
            int bucketColumn = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            //int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            int thumbIdColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails._ID);

            int i = 0;

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                //data = cur.getString(dataColumn);
                id = cur.getString(idColumn);
                thumbId = cur.getString(thumbIdColumn);

                String cover = images + "/" + Integer.parseInt(thumbId);
                //Uri coverUri = Uri.parse(cover);
                //cover = getThumbnail(coverUri);

                Photo photo = new Photo();
                photo.cover = cover;
                photoList.add(photo);
                i++;

            } while (cur.moveToNext());

            photoAdapter.photoList = photoList;
            photoAdapter.notifyDataSetChanged();

        }

    }


    public static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

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

    public class Photo {

        private String cover;

        private Uri uri;

        public void setCover(String cover) {
            this.cover = cover;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }

        public String getCover() {
            return cover;
        }

        public Uri getUri() {
            return uri;
        }
    }

    public class PhotoAdapter extends BaseAdapter {

        public int columnWidth;
        public Activity activity;
        public List<Photo> photoList;
        public LayoutInflater inflater;


        public PhotoAdapter(List<Photo> photoList, Activity activity, int columnWidth) {
            this.columnWidth = columnWidth;
            this.photoList = photoList;
            this.activity = activity;
            this.inflater = LayoutInflater.from(activity);
        }

        @Override
        public int getCount() {
            return photoList == null ? 0 : photoList.size();
        }

        @Override
        public Photo getItem(int position) {
            return photoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.photo_list_item, null);
            view.setLayoutParams(new GridView.LayoutParams(columnWidth, columnWidth));
            view.setPadding(3, 3, 0, 0);
            Photo photo = getItem(position);
            ImageView cover = (ImageView) view.findViewById(R.id.photo_cover);
            try {
                cover.setImageBitmap(getBitmapFormUri(activity, Uri.parse(photo.getCover())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return view;
        }
    }


    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 200f;//这里设置高度为800f
        float ww = 200f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

}
