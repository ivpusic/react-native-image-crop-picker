package com.imnjh.imagepicker.adapter;

import java.util.ArrayList;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

public class CommonHeaderFooterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int TYPE_HEADER_VIEW = Integer.MIN_VALUE;
  private static final int TYPE_FOOTER_VIEW = Integer.MIN_VALUE + 1;

  private RecyclerView.Adapter<RecyclerView.ViewHolder> mInnerAdapter;

  private ArrayList<View> mHeaderViews = new ArrayList<>();
  private ArrayList<View> mFooterViews = new ArrayList<>();

  private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {

    @Override
    public void onChanged() {
      super.onChanged();
      notifyDataSetChanged();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      super.onItemRangeChanged(positionStart, itemCount);
      notifyItemRangeChanged(positionStart + getHeaderViewsCount(), itemCount);
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      super.onItemRangeInserted(positionStart, itemCount);
      notifyItemRangeInserted(positionStart + getHeaderViewsCount(), itemCount);
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      super.onItemRangeRemoved(positionStart, itemCount);
      notifyItemRangeRemoved(positionStart + getHeaderViewsCount(), itemCount);
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      super.onItemRangeMoved(fromPosition, toPosition, itemCount);
      int headerViewsCountCount = getHeaderViewsCount();
      notifyItemRangeChanged(fromPosition + headerViewsCountCount, toPosition
          + headerViewsCountCount + itemCount);
    }
  };

  public CommonHeaderFooterAdapter() {}

  public CommonHeaderFooterAdapter(RecyclerView.Adapter innerAdapter) {
    setAdapter(innerAdapter);
  }

  public void setAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {

    if (adapter != null) {
      if (!(adapter instanceof RecyclerView.Adapter))
        throw new RuntimeException("your adapter must be a RecyclerView.Adapter");
    }

    if (mInnerAdapter != null) {
      notifyItemRangeRemoved(getHeaderViewsCount(), mInnerAdapter.getItemCount());
      mInnerAdapter.unregisterAdapterDataObserver(mDataObserver);
    }

    this.mInnerAdapter = adapter;
    mInnerAdapter.registerAdapterDataObserver(mDataObserver);
    notifyItemRangeInserted(getHeaderViewsCount(), mInnerAdapter.getItemCount());
  }

  public RecyclerView.Adapter getInnerAdapter() {
    return mInnerAdapter;
  }

  public void addHeaderView(View header) {

    if (header == null) {
      throw new RuntimeException("header is null");
    }

    mHeaderViews.add(header);
    this.notifyDataSetChanged();
  }

  public void addFooterView(View footer) {

    if (footer == null) {
      throw new RuntimeException("footer is null");
    }

    mFooterViews.add(footer);
    this.notifyDataSetChanged();
  }

  /**
   * 返回第一个 FoView
   * 
   * @return
   */
  public View getFooterView() {
    return getFooterViewsCount() > 0 ? mFooterViews.get(0) : null;
  }

  /**
   * 返回第一个 HeaderView
   * 
   * @return
   */
  public View getHeaderView() {
    return getHeaderViewsCount() > 0 ? mHeaderViews.get(0) : null;
  }

  public void removeHeaderView(View view) {
    mHeaderViews.remove(view);
    this.notifyDataSetChanged();
  }

  public void removeFooterView(View view) {
    mFooterViews.remove(view);
    this.notifyDataSetChanged();
  }

  public int getHeaderViewsCount() {
    return mHeaderViews.size();
  }

  public int getFooterViewsCount() {
    return mFooterViews.size();
  }

  public boolean isHeader(int position) {
    return getHeaderViewsCount() > 0 && position == 0;
  }

  public boolean isFooter(int position) {
    int lastPosition = getItemCount() - 1;
    return getFooterViewsCount() > 0 && position == lastPosition;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    int headerViewsCountCount = getHeaderViewsCount();
    if (viewType < TYPE_HEADER_VIEW + headerViewsCountCount) {
      return new ViewHolder(mHeaderViews.get(viewType - TYPE_HEADER_VIEW));
    } else if (viewType >= TYPE_FOOTER_VIEW && viewType < Integer.MAX_VALUE / 2) {
      return new ViewHolder(mFooterViews.get(viewType - TYPE_FOOTER_VIEW));
    } else {
      return mInnerAdapter.onCreateViewHolder(parent, viewType - Integer.MAX_VALUE / 2);
    }
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    int headerViewsCountCount = getHeaderViewsCount();
    if (position >= headerViewsCountCount
        && position < headerViewsCountCount + mInnerAdapter.getItemCount()) {
      mInnerAdapter.onBindViewHolder(holder, position - headerViewsCountCount);
    } else {
      ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
      if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
        ((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
      }
    }
  }

  @Override
  public int getItemCount() {
    return getHeaderViewsCount() + getFooterViewsCount() + mInnerAdapter.getItemCount();
  }

  @Override
  public int getItemViewType(int position) {
    int innerCount = mInnerAdapter.getItemCount();
    int headerViewsCountCount = getHeaderViewsCount();
    if (position < headerViewsCountCount) {
      return TYPE_HEADER_VIEW + position;
    } else if (headerViewsCountCount <= position && position < headerViewsCountCount + innerCount) {

      int innerItemViewType = mInnerAdapter.getItemViewType(position - headerViewsCountCount);
      if (innerItemViewType >= Integer.MAX_VALUE / 2) {
        throw new IllegalArgumentException(
            "your adapter's return value of getViewTypeCount() must < Integer.MAX_VALUE / 2");
      }
      return innerItemViewType + Integer.MAX_VALUE / 2;
    } else {
      return TYPE_FOOTER_VIEW + position - headerViewsCountCount - innerCount;
    }
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View itemView) {
      super(itemView);
    }
  }
}
