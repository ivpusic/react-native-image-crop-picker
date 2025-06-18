package com.reactnative.ivpusic.imagepicker;

import android.util.Size;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoCompressionOptions {

    public static float DEFAULT_FPS = 30.0f;
    public static int DEFAULT_AUDIO_BITRATE = 42_000;

    private String codec;
    private Size size;

    VideoCompressionOptions(String compressVideoPreset) {
        parse(compressVideoPreset);
    }

    public String getCodec() {
        return codec;
    }

    public Size getSize() {
        return size;
    }

    /**
     * Parses strings like "<CODEC><WIDTH>x<HEIGHT>", e.g. "HEVC1920x1080".
     *
     * @param compressVideoPreset the input string to parse
     * @return a {@link VideoCompressionOptions} instance containing codec, width and height
     * @throws IllegalArgumentException if the string does not match the expected pattern
     */
    private void parse(String compressVideoPreset) {
        // 1) codec  2) first number  3) second number
        Pattern p = Pattern.compile("^([A-Za-z0-9]+?)(\\d{3,5})x(\\d{3,5})$");
        Matcher m = p.matcher(compressVideoPreset);

        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid format: " + compressVideoPreset);
        }

        this.codec = m.group(1);

        int n1 = Integer.parseInt(m.group(2));   // first numeric token
        int n2 = Integer.parseInt(m.group(3));   // second numeric token

        /* Handling portrait logic */
        int width, height;
        width  = n2;
        height = n1;


        this.size = new Size(width, height);     // android.util.Size
    }
}
