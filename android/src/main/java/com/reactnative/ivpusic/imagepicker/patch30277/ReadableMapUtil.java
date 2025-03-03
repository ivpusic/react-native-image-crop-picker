package com.reactnative.ivpusic.imagepicker.patch30277;

import com.facebook.react.bridge.ReadableMap;

import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;

public class ReadableMapUtil {

    @Nullable
    public static String toString(ReadableMap map) {

        try {
            JSONObject options = MapUtil.toJSONObject(map);

            return options.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static ReadableMap toReadableMap(@Nullable String json) {
        if (json == null) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            return MapUtil.toWritableMap(MapUtil.toMap(jsonObject));
        } catch (JSONException err) {
            err.printStackTrace();
        }

        return null;
    }

}
