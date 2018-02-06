package com.example.photopicker;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.photopicker.fragment.PhotoPickerFragment;

import java.util.ArrayList;

import static com.example.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static com.example.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static com.example.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static com.example.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static com.example.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static com.example.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static com.example.photopicker.PhotoPicker.EXTRA_SHOW_GIF;

public class PhotoPickerActivity extends AppCompatActivity {

    private final static String PHOTO_PICKER_FRAGMENT_TAG = "tag";
    private PhotoPickerFragment pickerFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /**
         * Set the Z-axis elevation of the action bar in pixels.
         * The action bar's elevation is the distance it is placed from its parent surface.
         * Higher values are closer to the user.
         */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 设置action bar的仰角
            actionBar.setElevation(25);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        // activity数据获取
        boolean showCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);  // 取不到值的话指定一个默认值
        boolean showGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, true);
        boolean previewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        int maxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        int columnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        ArrayList<String> originalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);


        // 启动PhotoPickerFragment
        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag(PHOTO_PICKER_FRAGMENT_TAG);
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment.newInstance(showCamera, showGif, previewEnabled, columnNumber, maxCount, originalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, PHOTO_PICKER_FRAGMENT_TAG)
                    .commit();
            // 您需要把多次提交操作的同一个时间点一起执行，则使用 executePendingTransactions()
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
