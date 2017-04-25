package com.imnjh.imagepicker.adapter;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.widget.FilterQueryProvider;

/**
 * Created by Martin on 2017/1/17.
 */

public abstract class BaseRecycleCursorAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> {

  /**
   * Call when bind view with the cursor
   *
   * @param holder RecyclerView.ViewHolder
   * @param cursor The cursor from which to get the data. The cursor is already
   *          moved to the correct position.
   */
  public abstract void onBindViewHolder(VH holder, Cursor cursor);

  protected boolean mDataValid;
  protected Cursor mCursor;
  protected Context mContext;
  protected int mRowIDColumn;
  protected ChangeObserver mChangeObserver;
  protected DataSetObserver mDataSetObserver;
  protected FilterQueryProvider mFilterQueryProvider;

  /**
   * If set the adapter will register a content observer on the cursor and will call
   * {@link #onContentChanged()} when a notification comes in. Be careful when
   * using this flag: you will need to unset the current Cursor from the adapter
   * to avoid leaks due to its registered observers. This flag is not needed
   * when using a CursorAdapter with a {@link android.content.CursorLoader}.
   */
  public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

  /**
   * Constructor that flags always FLAG_REGISTER_CONTENT_OBSERVER.
   *
   * @param c The cursor from which to get the data.
   * @param context The context
   *          This option is discouraged, as it results in Cursor queries
   *          being performed on the application's UI thread and thus can cause poor
   *          responsiveness or even Application Not Responding errors. As an alternative,
   *          use {@link android.app.LoaderManager} with a {@link android.content.CursorLoader}.
   */
  public BaseRecycleCursorAdapter(Context context, Cursor c) {
    init(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
  }

  void init(Context context, Cursor c, int flags) {
    boolean cursorPresent = c != null;
    mCursor = c;
    mDataValid = cursorPresent;
    mContext = context;
    mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
    if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
      mChangeObserver = new ChangeObserver();
      mDataSetObserver = new CursorDataSetObserver();
    } else {
      mChangeObserver = null;
      mDataSetObserver = null;
    }

    if (cursorPresent) {
      if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
      if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
    }

    setHasStableIds(true);// 这个地方要注意一下，需要将表关联ID设置为true
  }

  /**
   * Returns the cursor.
   *
   * @return the cursor.
   */
  public Cursor getCursor() {
    return mCursor;
  }

  /**
   * @see android.support.v7.widget.RecyclerView.Adapter#getItemCount()
   */
  @Override
  public int getItemCount() {
    if (mDataValid && mCursor != null) {
      return mCursor.getCount();
    } else {
      return 0;
    }
  }

  public Object getItem(int position) {
    if (mDataValid && mCursor != null) {
      mCursor.moveToPosition(position);
      return mCursor;
    } else {
      return null;
    }
  }

  /**
   * @param position Adapter position to query
   * @see android.support.v7.widget.RecyclerView.Adapter#getItemId(int)
   */
  @Override
  public long getItemId(int position) {
    if (mDataValid && mCursor != null) {
      if (mCursor.moveToPosition(position)) {
        return mCursor.getLong(mRowIDColumn);
      } else {
        return 0;
      }
    } else {
      return 0;
    }
  }

  @Override
  public void onBindViewHolder(VH holder, int position) {
    if (!mDataValid) {
      throw new IllegalStateException("this should only be called when the cursor is valid");
    }
    if (!mCursor.moveToPosition(position)) {
      throw new IllegalStateException("couldn't move cursor to position " + position);
    }
    onBindViewHolder(holder, mCursor);
  }

  /**
   * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
   * closed.
   *
   * @param cursor The new cursor to be used
   */
  public void changeCursor(Cursor cursor) {
    Cursor old = swapCursor(cursor);
    if (old != null) {
      old.close();
    }
  }

  /**
   * Swap in a new Cursor, returning the old Cursor. Unlike {@link #changeCursor(Cursor)}, the
   * returned old Cursor is <em>not</em> closed.
   *
   * @param newCursor The new cursor to be used.
   * @return Returns the previously set Cursor, or null if there wasa not one.
   *         If the given new Cursor is the same instance is the previously set
   *         Cursor, null is also returned.
   */
  public Cursor swapCursor(Cursor newCursor) {
    if (newCursor == mCursor) {
      return null;
    }
    Cursor oldCursor = mCursor;
    if (oldCursor != null) {
      if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
      if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
    }
    mCursor = newCursor;
    if (newCursor != null) {
      if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
      if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
      mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
      mDataValid = true;
      // notify the observers about the new cursor
      notifyDataSetChanged();
    } else {
      mRowIDColumn = -1;
      mDataValid = false;
      // notify the observers about the lack of a data set
      // There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
      notifyDataSetChanged();
    }
    return oldCursor;
  }

  /**
   * <p>
   * Converts the cursor into a CharSequence. Subclasses should override this method to convert
   * their results. The default implementation returns an empty String for null values or the
   * default String representation of the value.
   * </p>
   *
   * @param cursor the cursor to convert to a CharSequence
   * @return a CharSequence representing the value
   */
  public CharSequence convertToString(Cursor cursor) {
    return cursor == null ? "" : cursor.toString();
  }



  /**
   * Called when the {@link ContentObserver} on the cursor receives a change
   * notification.
   * The default implementation provides the auto-requery logic, but may be overridden by
   * sub classes.
   *
   * @see ContentObserver#onChange(boolean)
   */
  protected void onContentChanged() {

  }

  private class ChangeObserver extends ContentObserver {
    public ChangeObserver() {
      super(new Handler());
    }

    @Override
    public boolean deliverSelfNotifications() {
      return true;
    }

    @Override
    public void onChange(boolean selfChange) {
      onContentChanged();
    }
  }

  private class CursorDataSetObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      mDataValid = true;
      notifyDataSetChanged();
    }

    @Override
    public void onInvalidated() {
      mDataValid = false;
      // There is no notifyDataSetInvalidated() method in RecyclerView.Adapter
      notifyDataSetChanged();
    }
  }
}
