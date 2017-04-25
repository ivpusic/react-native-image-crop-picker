package com.imnjh.imagepicker.util;

import java.util.Collection;
import java.util.List;

/**
 * Created by Martin on 2017/1/17.
 */
public class CollectionUtils {

  private CollectionUtils() {}

  public static boolean isEmpty(Collection collection) {
    return collection == null || collection.isEmpty();
  }

  public static void removeRange(List list, int start, int count) {
    for (int i = start + count - 1; i >= start; i--) {
      list.remove(i);
    }
  }

  public static boolean containsAny(Collection all, Collection keys) {
    for (Object key : keys) {
      if (all.contains(key)) {
        return true;
      }
    }
    return false;
  }
}
