package com.reactnative.ivpusic.imagepicker;

import androidx.exifinterface.media.ExifInterface;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static androidx.exifinterface.media.ExifInterface.*;

class ExifExtractor {

    static WritableMap extract(String path, String compressedPath, boolean copyExif) throws IOException {
        WritableMap exifData = new WritableNativeMap();

        List<String> attributes = getAttributes();

        ExifInterface exif = new ExifInterface(path);
        ExifInterface compressedExif = new ExifInterface(compressedPath);

        String compressedWidth = compressedExif.getAttribute(TAG_IMAGE_WIDTH);
        String compressedHeight = compressedExif.getAttribute(TAG_IMAGE_LENGTH);

        try {
            GeoDegree geoDegree = new GeoDegree(exif);
            if (geoDegree.getLatitude() != null && geoDegree.getLongitude() != null) {
                exifData.putDouble("Latitude", geoDegree.getLatitude());
                exifData.putDouble("Longitude", geoDegree.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String attribute : attributes) {
            String value = exif.getAttribute(attribute);

            if (copyExif) {
                String compressedValue = compressedExif.getAttribute(attribute);

                if (attribute == TAG_PIXEL_X_DIMENSION) {
                    compressedExif.setAttribute(attribute, compressedWidth);
                } else if (attribute == TAG_PIXEL_Y_DIMENSION) {
                    compressedExif.setAttribute(attribute, compressedHeight);
                } else if (attribute != TAG_ORIENTATION && compressedValue == null) {
                    compressedExif.setAttribute(attribute, value);
                }
            }

            exifData.putString(attribute, value);
        }

        if (copyExif) {
            compressedExif.saveAttributes();
        }

        return exifData;
    }

    private static List<String> getAttributes() {
        return new ArrayList<>(Arrays.asList(
                TAG_APERTURE_VALUE,
                TAG_ARTIST,
                TAG_BITS_PER_SAMPLE,
                TAG_BODY_SERIAL_NUMBER,
                TAG_BRIGHTNESS_VALUE,
                TAG_CAMARA_OWNER_NAME,
                TAG_CFA_PATTERN,
                TAG_COLOR_SPACE,
                TAG_COMPONENTS_CONFIGURATION,
                TAG_COMPRESSED_BITS_PER_PIXEL,
                TAG_COMPRESSION,
                TAG_CONTRAST,
                TAG_COPYRIGHT,
                TAG_CUSTOM_RENDERED,
                TAG_DATETIME,
                TAG_DATETIME_DIGITIZED,
                TAG_DATETIME_ORIGINAL,
                TAG_DEFAULT_CROP_SIZE,
                TAG_DEVICE_SETTING_DESCRIPTION,
                TAG_DIGITAL_ZOOM_RATIO,
                TAG_DNG_VERSION,
                TAG_EXIF_VERSION,
                TAG_EXPOSURE_BIAS_VALUE,
                TAG_EXPOSURE_INDEX,
                TAG_EXPOSURE_MODE,
                TAG_EXPOSURE_PROGRAM,
                TAG_EXPOSURE_TIME,
                TAG_F_NUMBER,
                TAG_FILE_SOURCE,
                TAG_FLASH,
                TAG_FLASH_ENERGY,
                TAG_FLASHPIX_VERSION,
                TAG_FOCAL_LENGTH,
                TAG_FOCAL_LENGTH_IN_35MM_FILM,
                TAG_FOCAL_PLANE_RESOLUTION_UNIT,
                TAG_FOCAL_PLANE_X_RESOLUTION,
                TAG_FOCAL_PLANE_Y_RESOLUTION,
                TAG_GAIN_CONTROL,
                TAG_GAMMA,
                TAG_GPS_ALTITUDE,
                TAG_GPS_ALTITUDE_REF,
                TAG_GPS_AREA_INFORMATION,
                TAG_GPS_DATESTAMP,
                TAG_GPS_DEST_BEARING,
                TAG_GPS_DEST_BEARING_REF,
                TAG_GPS_DEST_DISTANCE,
                TAG_GPS_DEST_DISTANCE_REF,
                TAG_GPS_DEST_LATITUDE,
                TAG_GPS_DEST_LATITUDE_REF,
                TAG_GPS_DEST_LONGITUDE,
                TAG_GPS_DEST_LONGITUDE_REF,
                TAG_GPS_DIFFERENTIAL,
                TAG_GPS_DOP,
                TAG_GPS_H_POSITIONING_ERROR,
                TAG_GPS_IMG_DIRECTION,
                TAG_GPS_IMG_DIRECTION_REF,
                TAG_GPS_LATITUDE,
                TAG_GPS_LATITUDE_REF,
                TAG_GPS_LONGITUDE,
                TAG_GPS_LONGITUDE_REF,
                TAG_GPS_MAP_DATUM,
                TAG_GPS_MEASURE_MODE,
                TAG_GPS_PROCESSING_METHOD,
                TAG_GPS_SATELLITES,
                TAG_GPS_SPEED,
                TAG_GPS_SPEED_REF,
                TAG_GPS_STATUS,
                TAG_GPS_TIMESTAMP,
                TAG_GPS_TRACK,
                TAG_GPS_TRACK_REF,
                TAG_GPS_VERSION_ID,
                TAG_IMAGE_DESCRIPTION,
                TAG_IMAGE_LENGTH,
                TAG_IMAGE_UNIQUE_ID,
                TAG_IMAGE_WIDTH,
                TAG_INTEROPERABILITY_INDEX,
                TAG_ISO_SPEED,
                TAG_ISO_SPEED_LATITUDE_YYY,
                TAG_ISO_SPEED_LATITUDE_ZZZ,
                TAG_JPEG_INTERCHANGE_FORMAT,
                TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
                TAG_LENS_MAKE,
                TAG_LENS_MODEL,
                TAG_LENS_SERIAL_NUMBER,
                TAG_LENS_SPECIFICATION,
                TAG_LIGHT_SOURCE,
                TAG_MAKE,
                TAG_MAKER_NOTE,
                TAG_MAX_APERTURE_VALUE,
                TAG_METERING_MODE,
                TAG_MODEL,
                TAG_NEW_SUBFILE_TYPE,
                TAG_OECF,
                TAG_ORF_ASPECT_FRAME,
                TAG_ORF_PREVIEW_IMAGE_LENGTH,
                TAG_ORF_PREVIEW_IMAGE_START,
                TAG_ORF_THUMBNAIL_IMAGE,
                TAG_ORIENTATION,
                TAG_PHOTOGRAPHIC_SENSITIVITY,
                TAG_PHOTOMETRIC_INTERPRETATION,
                TAG_PIXEL_X_DIMENSION,
                TAG_PIXEL_Y_DIMENSION,
                TAG_PLANAR_CONFIGURATION,
                TAG_PRIMARY_CHROMATICITIES,
                TAG_RECOMMENDED_EXPOSURE_INDEX,
                TAG_REFERENCE_BLACK_WHITE,
                TAG_RELATED_SOUND_FILE,
                TAG_RESOLUTION_UNIT,
                TAG_ROWS_PER_STRIP,
                TAG_RW2_ISO,
                TAG_RW2_JPG_FROM_RAW,
                TAG_RW2_SENSOR_BOTTOM_BORDER,
                TAG_RW2_SENSOR_LEFT_BORDER,
                TAG_RW2_SENSOR_RIGHT_BORDER,
                TAG_RW2_SENSOR_TOP_BORDER,
                TAG_SAMPLES_PER_PIXEL,
                TAG_SATURATION,
                TAG_SCENE_CAPTURE_TYPE,
                TAG_SCENE_TYPE,
                TAG_SENSING_METHOD,
                TAG_SENSITIVITY_TYPE,
                TAG_SHARPNESS,
                TAG_SHUTTER_SPEED_VALUE,
                TAG_SOFTWARE,
                TAG_SPATIAL_FREQUENCY_RESPONSE,
                TAG_SPECTRAL_SENSITIVITY,
                TAG_STANDARD_OUTPUT_SENSITIVITY,
                TAG_STRIP_BYTE_COUNTS,
                TAG_STRIP_OFFSETS,
                TAG_SUBFILE_TYPE,
                TAG_SUBJECT_AREA,
                TAG_SUBJECT_DISTANCE,
                TAG_SUBJECT_DISTANCE_RANGE,
                TAG_SUBJECT_LOCATION,
                TAG_SUBSEC_TIME,
                TAG_SUBSEC_TIME_DIGITIZED,
                TAG_SUBSEC_TIME_ORIGINAL,
                TAG_THUMBNAIL_IMAGE_LENGTH,
                TAG_THUMBNAIL_IMAGE_WIDTH,
                TAG_TRANSFER_FUNCTION,
                TAG_USER_COMMENT,
                TAG_WHITE_BALANCE,
                TAG_WHITE_POINT,
                TAG_X_RESOLUTION,
                TAG_Y_CB_CR_COEFFICIENTS,
                TAG_Y_CB_CR_POSITIONING,
                TAG_Y_CB_CR_SUB_SAMPLING,
                TAG_Y_RESOLUTION
        ));
    }
}
