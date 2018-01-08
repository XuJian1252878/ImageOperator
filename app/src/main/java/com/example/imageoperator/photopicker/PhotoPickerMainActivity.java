package com.example.imageoperator.photopicker;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.example.imageoperator.R;
import com.example.photopicker.PhotoPicker;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class PhotoPickerMainActivity extends AppCompatActivity {

    private PhotoAdapter photoAdapter;
    private ArrayList<String> selectedPhotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 引入 Crashlytics 检测工具
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.photo_picker_activity);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        photoAdapter = new PhotoAdapter(selectedPhotos, this);

        // 设定 LayoutManager 设定水平方式滑动
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, OrientationHelper.VERTICAL));
        recyclerView.setAdapter(photoAdapter);


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(9)
                        .setGridColumnCount(4)
                        .start(PhotoPickerMainActivity.this);
            }
        });

        findViewById(R.id.button_no_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(7)
                        .setShowCamera(false)
                        .setPreviewEnabled(false)
                        .start(PhotoPickerMainActivity.this);
            }
        });

        findViewById(R.id.button_one_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(1)
                        .start(PhotoPickerMainActivity.this);
            }
        });

        findViewById(R.id.button_photo_gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setShowCamera(true)
                        .setShowGif(true)
                        .start(PhotoPickerMainActivity.this);
            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (photoAdapter.getItemViewType(position) == PhotoAdapter.TYPE_ADD) {
                    PhotoPicker.builder()
                            .setPhotoCount(PhotoAdapter.MAX)
                            .setShowCamera(true)
                            .setPreviewEnabled(false)
                            .setSelected(selectedPhotos)
                            .start(PhotoPickerMainActivity.this);
                } else {
                    // 图片预览的操作
                }
            }
        }));

        findViewById(R.id.button_crashlytics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forceCrash(v);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK || requestCode == PhotoPicker.REQUEST_CODE) {
            List<String> photos = null;

            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }

            selectedPhotos.clear();
            if (photos != null) {
                selectedPhotos.addAll(photos);
            }
            photoAdapter.notifyDataSetChanged();
        }
    }

    public void forceCrash(View view) {

        // TODO: Move this to where you establish a user session

        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier("12345");
        Crashlytics.setUserEmail("user@fabric.io");
        Crashlytics.setUserName("Test User");

        throw new RuntimeException("This is a crash");
    }
}
