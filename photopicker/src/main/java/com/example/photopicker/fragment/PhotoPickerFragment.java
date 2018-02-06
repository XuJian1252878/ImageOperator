package com.example.photopicker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.photopicker.R;

import java.util.ArrayList;

import static com.example.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.example.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static com.example.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.example.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_GIF;

/**
 * Created by xujian on 2018/1/9.
 */

public class PhotoPickerFragment extends Fragment {

    //目录弹出框的一次最多显示的目录数目
    public static int COUNT_MAX = 4;

    public static PhotoPickerFragment newInstance(boolean showCamera, boolean showGif,
                                                  boolean previewEnable, int column, int maxCount,
                                                  ArrayList<String> originalPhotos) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_SHOW_CAMERA, showCamera);
        args.putBoolean(EXTRA_SHOW_GIF, showGif);
        args.putBoolean(EXTRA_PREVIEW_ENABLED, previewEnable);
        args.putInt(EXTRA_GRID_COLUMN, column);
        args.putInt(EXTRA_MAX_COUNT, maxCount);
        args.putStringArrayList(EXTRA_ORIGINAL_PHOTOS, originalPhotos);

        PhotoPickerFragment fragment = new PhotoPickerFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Fragment具有属性retainInstance，默认值为false。当设备旋转时，fragment会随托管activity一起销毁并重建。
         */
        setRetainInstance(true);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);
        return rootView;
    }
}
