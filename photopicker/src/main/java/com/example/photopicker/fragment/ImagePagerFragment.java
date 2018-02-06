package com.example.photopicker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by xujian on 2018/2/6.
 */

public class ImagePagerFragment extends Fragment {

    public final static String ARG_PATH = "PATHS";
    public final static String ARG_CURRENT_ITEM = "ARG_CURRENT_ITEM";

    public static ImagePagerFragment newInstance(List<String> paths, int currentItem) {
        ImagePagerFragment f = new ImagePagerFragment();

        Bundle args = new Bundle();
        args.putStringArray(ARG_PATH, paths.toArray(new String[paths.size()]));
        args.putInt(ARG_CURRENT_ITEM, currentItem);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
