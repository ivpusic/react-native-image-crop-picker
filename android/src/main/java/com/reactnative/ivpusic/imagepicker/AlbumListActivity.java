package com.reactnative.ivpusic.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.imnjh.imagepicker.SImagePicker;

import java.util.ArrayList;
import java.util.List;

public class AlbumListActivity extends AppCompatActivity {

    private TextView title;
    private TextView cancel;
    private ListView listView;

    private List<Album> albumList;
    private AlbumAdapter albumAdapter;

    public static final int REQUEST_CODE_IMAGE = 101;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);



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
                    int count = bucketCount.get(bucketCount.size()-1);
                    bucketCount.set(bucketCount.size()-1,count+1);
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
            View view = inflater.inflate(R.layout.albums_list_item, null);
            Album album = getItem(position);
            SimpleDraweeView cover = (SimpleDraweeView) view.findViewById(R.id.album_cover);
            TextView name = (TextView) view.findViewById(R.id.album_name);
            TextView count = (TextView) view.findViewById(R.id.album_count);
            cover.setImageURI(album.getCover());
            name.setText(album.getName());
            count.setText(""+album.getCount());

            return view;

        }
    }
}
