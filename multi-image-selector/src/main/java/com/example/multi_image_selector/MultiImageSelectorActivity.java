package com.example.multi_image_selector;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;

/**
 * 显示图片选择的Activity，包括一个ActionBar以及一个FrameLayout。然后这个FrameLayout里面将会有一个显示 各种图片
 * 的GridView以及打开所有图片文件夹的RelativeLayout
 */
public class MultiImageSelectorActivity extends AppCompatActivity implements MultiImageSelectorFragment.Callback {

    // 一次只选择一张图片
    public static final int MODE_SINGLE = 0;
    // 一次选择多张图片
    public static final int MODE_MULTI = 1;

    /** Result data set，ArrayList&lt;String&gt; */
    public static final String EXTRA_RESULT = "select_result";

    /** Whether show camera，true by default */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";

    /** Original data set 一开始被选择的图片路径列表 */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

    /** Max image size，int，{@link #DEFAULT_IMAGE_SIZE} by default */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";

    /** Select mode，{@link #MODE_MULTI} by default */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";

    /** Default image size 一次最多选择多少张图片 */
    private static final int DEFAULT_IMAGE_SIZE = 9;

    private ArrayList<String> resultList = new ArrayList<>();
    private Button mSubmitButton;
    private int mDefaultCount = DEFAULT_IMAGE_SIZE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.MIS_NO_ACTIONBAR);
        setContentView(R.layout.mis_activity_default);

        // 设置状态栏的颜色，android5.0引入，之前不支持修改状态栏的颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        // 设置自定义的actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // 获得当前的actionbar实例
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示toolbar上的返回按钮（返回按钮就叫HomeAsUp）
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 接收客户端传过来的关于图片选择的intent信息
        Intent intent = getIntent();
        // 获得客户端需要的最多图片的数量，默认是9
        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, DEFAULT_IMAGE_SIZE);
        // 获得当前图片的选择模式，单选或者多选；默认是多选的选择模式
        int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        // 获得是否支持选择图片的时候照相，默认支持
        boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        // 客户端有传入一些选择图片列表的情况
        if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
        }

        // 关于用户提交选择的照片信息
        mSubmitButton = findViewById(R.id.commit);
        if (mode == MODE_MULTI) {
            updateDoneText(resultList);
            // 将提交按钮设置为可见
            mSubmitButton.setVisibility(View.VISIBLE);
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 用户完成选择图片的操作，提交已选择的图片
                    if (resultList != null && resultList.size() > 0) {
                        Intent data = new Intent();
                        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                        // 因为是使用startActivityForResult启动的图片选择的Activity，所以使用setResult设置返回的结果
                        setResult(RESULT_OK, data);
                    } else {
                        // 用户没有选择图片就返回的情况
                        setResult(RESULT_CANCELED);
                    }
                    // 返回调用的activity
                    finish();
                }
            });
        } else {
            // 如果是图片的单选模式，不显示点击提交按钮。
            mSubmitButton.setVisibility(View.GONE);
        }


        // 启动加载图片信息的具体的Fragment
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_COUNT, mDefaultCount);
            bundle.putInt(MultiImageSelectorFragment.EXTRA_SELECT_MODE, mode);
            bundle.putBoolean(MultiImageSelectorFragment.EXTRA_SHOW_CAMERA, isShow);
            bundle.putStringArrayList(MultiImageSelectorFragment.EXTRA_DEFAULT_SELECTED_LIST, resultList);

            // 启动对应的Fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.image_grid, Fragment.instantiate(this, MultiImageSelectorFragment.class.getName(), bundle));
            fragmentTransaction.commit();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 如果HomeAsUp按钮被点击，那么说明用户取消了选择图片的操作，直接返回
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                // boolean	boolean Return false to allow normal menu processing to proceed,
                // true to consume it here.
                return true;  // 在这里消费HomeAsUp的点击事件
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 更新图片选择界面右上角 提交所选择的图片 的按钮上的数字
     * %1$s(%2$d/%3$d)
     * @param resultList
     */
    private void updateDoneText(ArrayList<String> resultList) {
        int size = 0;
        if (resultList == null || resultList.size() <= 0) {
            // 当还没有选择图片的情况下，提交按钮上显示的文字
//            mSubmitButton.setText(R.string.mis_action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        // 设置button上的文字
        mSubmitButton.setText(getString(R.string.mis_action_button_string,
                getString(R.string.mis_action_done), size, mDefaultCount));
    }

    // 在单选模式下 某张图片被选择之后 发生的回调（直接跳回调用者界面）
    // 单选模式下，界面右上角的提交按钮隐藏，点击图片直接返回调用客户端
    @Override
    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    // 多选模式下图片被选择时候的操作
    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        // 更新提交按钮上的文字信息
        updateDoneText(resultList);
    }

    // 多选模式下图片被取消选择时候的操作
    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
        }
        updateDoneText(resultList);
    }

    // 调用拍照按钮的时候
    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            // 通知系统扫描 指定的图片文件
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));

            Intent data = new Intent();
            // 获取绝度路径
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }
}
