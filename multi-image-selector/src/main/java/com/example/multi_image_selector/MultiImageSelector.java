package com.example.multi_image_selector;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * 图片选择器
 * Created by xujian on 2018/1/4.
 */

public class MultiImageSelector {
    public static final String EXTRA_RESULT = MultiImageSelectorActivity.EXTRA_RESULT;

    // 是否在图片选择器中显示 照相图标（开启照相功能）
    private boolean mShowCamera = true;
    // 一次最多能选择的照片数量
    private int mMaxCount = 9;
    // 默认情况下是选择多张图片
    private int mMode = MultiImageSelectorActivity.MODE_MULTI;

    // 已经选择的图片路径list
    private ArrayList<String> mOriginData;
    private static MultiImageSelector sSelector;

    private MultiImageSelector() {
    }

    private MultiImageSelector(Context context) {
    }


    public static MultiImageSelector create(Context context) {
        if (sSelector == null) {
            sSelector = new MultiImageSelector(context);
        }
        return sSelector;
    }

    public static MultiImageSelector create() {
        if (sSelector == null) {
            sSelector = new MultiImageSelector();
        }
        return sSelector;
    }

    // 设置是否显示照相机图标（是否显示照相机的功能）
    public MultiImageSelector showCamera(boolean show) {
        mShowCamera = show;
        return sSelector;
    }

    // 设置一次可以选择的最大图片数量
    public MultiImageSelector count(int count) {
        mMaxCount = count;
        return sSelector;
    }

    // 设置当前图片是单项选择的方式
    public MultiImageSelector single() {
        mMode = MultiImageSelectorActivity.MODE_SINGLE;
        return sSelector;
    }

    // 设置当前图片是双向选择的方式
    public MultiImageSelector multi() {
        mMode = MultiImageSelectorActivity.MODE_MULTI;
        return sSelector;
    }

    // 设置当前已经选择的图片列表
    public MultiImageSelector origin(ArrayList<String> images) {
        mOriginData = images;
        return sSelector;
    }

    // 关于重新启动一个activity的参数设置，打开选择图片的activity
    private Intent createIntent(Context context) {
        Intent intent = new Intent(context, MultiImageSelectorActivity.class);
        // 设置是否开启照相功能
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, mShowCamera);
        // 设置最多能选择的图片数量
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, mMaxCount);
        // 设置初始的图片路径数组
        if (mOriginData != null) {
            intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mOriginData);
        }
        // 设置图片是单选还是多选操作
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, mMode);
        return intent;
    }

    // 关于运行时权限的申请
    private boolean hasPermission(Context context, String permission ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // 读取外部存取的权限是在 API level 16这里加入的
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    // 根据设定的参数，启动图片选择的activity
    public void start(Activity activity, int requestCode) {
        Context context = activity;
        if (hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            activity.startActivityForResult(createIntent(context), requestCode);
        } else {
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

    // 根据设定的参数，启动图片选择的activity
    public void start(Fragment fragment, int requestCode) {
        Context context = fragment.getContext();
        if (hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            fragment.startActivityForResult(createIntent(context), requestCode);
        } else {
            Toast.makeText(context, R.string.mis_error_no_permission, Toast.LENGTH_SHORT).show();
        }
    }

}
