package com.reactnative.ivpusic.imagepicker.fragment;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.reactnative.ivpusic.imagepicker.R;
import com.reactnative.ivpusic.imagepicker.SImagePicker;
import com.reactnative.ivpusic.imagepicker.util.GlideUtil;
import com.reactnative.ivpusic.imagepicker.util.PhotoMetadataUtils;
import com.reactnative.ivpusic.imagepicker.widget.subsamplingview.ImageSource;
import com.reactnative.ivpusic.imagepicker.widget.subsamplingview.SubsamplingScaleImageView;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class PreviewItemFragment extends Fragment {

    private static final String ARGS_ITEM = "args_item";

    public static PreviewItemFragment newInstance(Uri uri) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGS_ITEM, uri.toString());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final String uri = getArguments().getString(ARGS_ITEM);
        if (uri == null) {
            return;
        }

//        ImageViewTouch image = (ImageViewTouch)view.findViewById(R.id.image_view);
//        image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) view.findViewById(R.id.image_view);
        imageView.setImage(ImageSource.uri(uri));
//
//        try {
//            Point size = PhotoMetadataUtils.getBitmapSize(Uri.parse(uri), getActivity());
//            if (size != null)
//                GlideUtil.loadImage(getContext(), size.x, size.y, image, Uri.parse(uri));
//            else {
//                GlideUtil.loadImage(getContext(), image, Uri.parse(uri));
//            }
//        } catch(NullPointerException e) {
//            e.printStackTrace();
//        }
    }

//    public void resetView() {
//        if (getView() != null) {
//            ((ImageViewTouch) getView().findViewById(R.id.image_view)).resetMatrix();
//        }
//    }
}
