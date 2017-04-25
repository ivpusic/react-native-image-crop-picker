package com.imnjh.imagepicker.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.os.Build;

import com.imnjh.imagepicker.BuildConfig;


/**
 * Created by Martin on 2017/1/17.
 */
public class LogUtils {

  private static final boolean DEBUG = BuildConfig.DEBUG;
  private static String tagPrefix = "";
  private static int MAX_MESSAGE_LENGTH = 4000; // adb logcat -g

  private LogUtils() {}

  public static void i(String tag, String msg, Object... args) {
    if (DEBUG) {
      println(android.util.Log.INFO, tag, formatString(msg, args), null);
    }
  }

  public static void v(String tag, String msg, Object... args) {
    if (DEBUG) {
      println(android.util.Log.VERBOSE, tag, formatString(msg, args), null);
    }
  }

  public static void d(String tag, String msg, Object... args) {
    if (DEBUG) {
      println(android.util.Log.DEBUG, tag, formatString(msg, args), null);
    }
  }

  public static void w(String tag, String msg, Object... args) {
    if (DEBUG) {
      println(android.util.Log.WARN, tag, formatString(msg, args), null);
    }
  }

  public static void w(String tag, Throwable th) {
    if (DEBUG) {
      println(android.util.Log.WARN, tag, "", th);
    }
  }

  public static void e(String tag, String msg, Object... args) {
    if (DEBUG) {
      println(android.util.Log.ERROR, tag, formatString(msg, args), null);
    }
  }

  public static void e(String tag, String msg, Throwable th) {
    if (DEBUG) {
      println(android.util.Log.ERROR, tag, msg, th);
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtf(String tag, String msg, Object... args) {
    if (DEBUG) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, formatString(msg, args));
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtf(String tag, String msg, Throwable th) {
    if (DEBUG) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, msg, th);
      }
    }
  }

  @TargetApi(Build.VERSION_CODES.FROYO)
  public static void wtfStack(String tag, String msg, Throwable th) {
    if (DEBUG) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
        android.util.Log.wtf(tagPrefix + tag, formatString(msg));
        android.util.Log.wtf(tagPrefix + tag, android.util.Log.getStackTraceString(th));
      }
    }
  }

  public static void printStackTrace(Throwable th) {
    if (DEBUG && th != null) {
      th.printStackTrace();
    }
  }

  public static String getStackTraceString(Throwable th) {
    return android.util.Log.getStackTraceString(th);
  }

  private static String formatString(String msg, Object... args) {
    if (args.length > 0) {
      return String.format(msg, args);
    } else {
      return msg;
    }
  }

  private static void println(int priority, String tag, String message, Throwable t) {
    if (!DEBUG) {
      return;
    }
    if (message == null || message.length() == 0) {
      return;
    }
    if (t != null) {
      message += "\n" + android.util.Log.getStackTraceString(t);
    }
    if (message.length() < MAX_MESSAGE_LENGTH) {
      android.util.Log.println(priority, tagPrefix + tag, message);
    } else {
      // It's rare that the message will be this large, so we're ok with the perf hit of splitting
      // and calling Log.println N times. It's possible but unlikely that a single line will be
      // longer than 4000 characters: we're explicitly ignoring this case here.
      String[] lines = message.split("\n");
      for (String line : lines) {
        android.util.Log.println(priority, tagPrefix + tag, line);
      }
    }
  }

  private static List<String> splitLongString(String str) {
    if (str == null) {
      return Collections.emptyList();
    }

    List<String> result = new ArrayList<String>(str.length() / MAX_MESSAGE_LENGTH + 1);
    for (int offset = 0; offset < str.length();) {
      int size = Math.min(MAX_MESSAGE_LENGTH, str.length() - offset);
      result.add(str.substring(offset, offset + size));
      offset += size;
    }

    return result;
  }

  private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$\\d+$");

  public static String defaultTag() {
    String tag = new Throwable().getStackTrace()[5].getClassName();
    Matcher m = ANONYMOUS_CLASS.matcher(tag);
    if (m.find()) {
      tag = m.replaceAll("");
    }
    return tag.substring(tag.lastIndexOf('.') + 1);
  }

  public static String tag(Class clazz) {
    return clazz.getSimpleName();
  }

  public static void setTagPrefix(String prefix) {
    tagPrefix = prefix;
  }

}
