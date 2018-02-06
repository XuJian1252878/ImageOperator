package com.example.photopicker.utils;

import com.example.photopicker.entity.PhotoDirectory;

import java.util.List;

/**
 * Created by xujian on 2018/2/5.
 */

public class MediaStoreHelper {

    public final static int INDEX_ALL_PHOTOS = 0;

    public interface PhotosResultCallback {
        void onResultCallback(List<PhotoDirectory> directories);
    }


}
