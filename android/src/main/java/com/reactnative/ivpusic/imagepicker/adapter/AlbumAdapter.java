package com.reactnative.ivpusic.imagepicker.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.model.Album;


/**
 * Created by Martin on 2017/1/17.
 */
public class AlbumAdapter extends CursorAdapter {

  private final LayoutInflater layoutInflater;

  public AlbumAdapter(Context context, Cursor c) {
    super(context, c, false);
    layoutInflater = LayoutInflater.from(context);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = layoutInflater.inflate(R.layout.album_list_item, parent, false);
    AlbumViewHolder albumViewHolder = new AlbumViewHolder(view);
    view.setTag(albumViewHolder);
    return view;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if (!getCursor().moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    if (convertView == null) {
      convertView =
          LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_selected,
              parent, false);
    }
    TextView albumName = (TextView) convertView.findViewById(android.R.id.text1);
    Album album = Album.valueOf(getCursor());
    albumName.setText(album.getDisplayName());
    return convertView;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    AlbumViewHolder viewHolder = (AlbumViewHolder) view.getTag();
    Album album = Album.valueOf(cursor);
    viewHolder.albumTitle.setText(album.getDisplayName());
    viewHolder.photoCount.setText(context.getResources().getString(R.string.bracket_num, album.getCount()));

  }


  static class AlbumViewHolder {

    public TextView albumTitle;
    public TextView photoCount;
    public AlbumViewHolder(View itemView) {
      albumTitle = (TextView) itemView.findViewById(R.id.album_name);
      photoCount = (TextView) itemView.findViewById(R.id.photo_count);

    }
  }
}
