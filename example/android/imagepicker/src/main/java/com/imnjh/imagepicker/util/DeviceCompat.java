package com.imnjh.imagepicker.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import android.os.Build;
import android.os.Environment;


/**
 * Created by Martin on 2017/1/17.
 */
public final class DeviceCompat {

  private static final String MANUFACTURE_NAME_SONY = "sony";

  public enum ROM {
    SONY,
    OTHERS
  }

  private static ROM rom;

  private static BuildProperties prop;

  static {
    try {
      prop = BuildProperties.newInstance();
      if (MANUFACTURE_NAME_SONY.equalsIgnoreCase(Build.MANUFACTURER)) {
        rom = ROM.SONY;
      } else {
        rom = ROM.OTHERS;
      }
    } catch (final IOException e) {
      rom = ROM.OTHERS;
    }
  }

  private DeviceCompat() {}

  public static ROM getROM() {
    return rom;
  }

  private static class BuildProperties {

    private final Properties properties;

    private BuildProperties() throws IOException {
      properties = new Properties();
      try {
        properties
            .load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        for (Object key : properties.keySet()) {
          if (key instanceof String) {
            String keyStr = (String) key;
            LogUtils.d("properties", keyStr + " : " + properties.getProperty(keyStr));
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public int size() {
      return properties.size();
    }

    public static BuildProperties newInstance() throws IOException {
      return new BuildProperties();
    }

  }
}
