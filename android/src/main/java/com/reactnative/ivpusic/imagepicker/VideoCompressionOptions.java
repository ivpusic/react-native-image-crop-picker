package com.reactnative.ivpusic.imagepicker;

import android.util.Size;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoCompressionOptions {

    private String codec;
    private String audioCodec;
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
        // Pattern: 1) codec = one or more alphanumerics (non-greedy)
        //          2) width  = 3–5 digits
        //          3) height = 3–5 digits
        Pattern p = Pattern.compile("^([A-Za-z0-9]+?)(\\d{3,5}x\\d{3,5})$");
        Matcher m = p.matcher(compressVideoPreset);

        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid format: " + compressVideoPreset);
        }

        this.codec = m.group(1);
        this.size = Size.parseSize(m.group(2));
    }
}
