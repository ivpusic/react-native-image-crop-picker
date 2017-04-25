package com.imnjh.imagepicker.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.imnjh.imagepicker.util.SystemUtil;

/**
 * Created by Martin on 2017/1/17.
 */
public class GridInsetDecoration extends RecyclerView.ItemDecoration {

  private int offset;

  public GridInsetDecoration() {
    offset = SystemUtil.dp(1);
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    GridLayoutManager.LayoutParams layoutParams =
        (GridLayoutManager.LayoutParams) view.getLayoutParams();
    int position = layoutParams.getViewAdapterPosition();
    if (position == RecyclerView.NO_POSITION) {
      outRect.set(0, 0, 0, 0);
      return;
    }
    outRect.set(offset, offset, offset, offset);
  }
}
