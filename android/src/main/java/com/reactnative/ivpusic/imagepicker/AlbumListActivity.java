package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.reactnative.ivpusic.imagepicker.activity.BasePickerActivity;
import com.reactnative.ivpusic.imagepicker.activity.PhotoPickerActivity;
import com.reactnative.ivpusic.imagepicker.adapter.AlbumListAdapter;
import com.reactnative.ivpusic.imagepicker.imageloader.FrescoImageLoader;
import com.reactnative.ivpusic.imagepicker.model.AlbumListData;
import com.reactnative.ivpusic.imagepicker.util.UriUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择相册
 */
public class AlbumListActivity extends BasePickerActivity {

    private TextView title;
    private TextView cancel;
    private ListView listView;

    private List<AlbumListData> albumList;
    private AlbumListAdapter albumAdapter;

    public static final int REQUEST_CODE_IMAGE = 101;
    public static final int REQUEST_CODE_IMAGE_SELECTED = 102;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE_IMAGE) {
            List<String> images = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            ArrayList<Uri> uris = UriUtil.getUris(this, images);
            Intent uriIntent = new Intent();
            uriIntent.putParcelableArrayListExtra(PhotoPickerActivity.PARAM_DATA, uris);
            setResult(Activity.RESULT_OK, uriIntent);
            finish();
        } else if (resultCode == REQUEST_CODE_IMAGE_SELECTED) {
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_album_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
                .setImageLoader(new FrescoImageLoader())
                .build());
        cancel = (TextView) findViewById(R.id.album_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        albumList = new ArrayList<>();

        albumAdapter = new AlbumListAdapter(albumList, AlbumListActivity.this);

        final Boolean multiple = this.getIntent().getBooleanExtra("multiple", true);
        final int maxFiles = this.getIntent().getIntExtra("maxFiles", 9);

        listView = (ListView) findViewById(R.id.album_list);
        listView.setAdapter(albumAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlbumListData album = albumList.get(position);
                //加载相册
                SImagePicker
                        .from(AlbumListActivity.this)
                        .albumName(album.getName())
                        .bucketId(album.getBucketId())
                        .maxCount(multiple ? maxFiles : 1)
                        .rowCount(4)
                        .pickMode(SImagePicker.MODE_IMAGE)
                        .fileInterceptor(null)
                        .forResult(REQUEST_CODE_IMAGE);

            }
        });

        initData();
    }

    private void initData() {
        String[] projection = new String[]{
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

        //        Log.i("ListingImages"," query count=" + cur.getCount());

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

            int i = 0;

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

                String cover = images + "/" + Integer.parseInt(thumbId);
                //Uri coverUri = Uri.parse(cover);
                //cover = getThumbnail(coverUri);

                //                Log.i("chen","bucketId:"+bucketId);

                if (!bucketIdList.contains(bucketId)) {
                    bucketList.add(bucket);
                    bucketCover.add(cover);
                    bucketCount.add(1);
                    bucketIdList.add(bucketId);
                } else {
                    int index = bucketIdList.indexOf(bucketId);
                    int count = bucketCount.get(index);
                    bucketCount.set(index, count + 1);
                    //Log.e("Picker", "Title : " + bucket + "  size : " + count);

                }

                i++;

                // Do something with the values.
                //                Log.i("ListingImages", " bucket=" + bucket + "  date_taken=" + date + "  uri=" + cover);

            } while (cur.moveToNext());


            for (i = 0; i < bucketList.size(); i++) {
                AlbumListData album = new AlbumListData();
                album.setCover(bucketCover.get(i));
                album.setName(bucketList.get(i));
                album.setCount(bucketCount.get(i));
                album.setBucketId(bucketIdList.get(i));
                albumList.add(album);
            }

            albumAdapter.albumList = albumList;
            albumAdapter.notifyDataSetChanged();
        }
        cur.close();
    }


    public String getThumbnail(Uri imageUri) {
        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(
                getContentResolver(), imageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();//**EDIT**
            String uriString = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            cursor.close();
            return uriString;
        } else {
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

        String[] projection = {MediaStore.MediaColumns.DATA,
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
        cursor.close();
        return listOfAllImages;
    }

}
