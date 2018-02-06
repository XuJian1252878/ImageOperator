package com.example.photopicker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.photopicker.PhotoPickerActivity;
import com.example.photopicker.R;
import com.example.photopicker.adapter.PhotoGridAdapter;
import com.example.photopicker.adapter.PopupDirectoryListAdapter;
import com.example.photopicker.entity.PhotoDirectory;
import com.example.photopicker.event.OnPhotoClickListener;
import com.example.photopicker.utils.ImageCaptureManager;
import com.example.photopicker.utils.MediaStoreHelper;

import java.util.ArrayList;
import java.util.List;

import static com.example.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
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

    private int column; // 默认显示的是多少列

    private ImageCaptureManager captureManager;

    // 所有photos的路径
    private List<PhotoDirectory> directories;
    // 已选的照片
    private ArrayList<String> originalPhotos;

    private RequestManager mGlideRequestManager;
    private ListPopupWindow listPopupWindow;

    private PhotoGridAdapter photoGridAdapter;
    private PopupDirectoryListAdapter listAdapter;

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

        mGlideRequestManager = Glide.with(this);

        directories = new ArrayList<>();
        originalPhotos = getArguments().getStringArrayList(EXTRA_ORIGINAL_PHOTOS);

        column = getArguments().getInt(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        boolean showCamera = getArguments().getBoolean(EXTRA_SHOW_CAMERA, true);
        boolean previewEnable = getArguments().getBoolean(EXTRA_PREVIEW_ENABLED, true);

        photoGridAdapter = new PhotoGridAdapter(getActivity(), mGlideRequestManager, directories, originalPhotos, column);
        photoGridAdapter.setShowCamera(showCamera);
        photoGridAdapter.setPreviewEnable(previewEnable);

        listAdapter = new PopupDirectoryListAdapter(directories, mGlideRequestManager);

        Bundle mediaStoreArgs = new Bundle();

        boolean showGif = getArguments().getBoolean(EXTRA_SHOW_GIF);
        mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, showGif);
        // 这里进行loaderManger的load操作
        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                // dirs 是已经从MediaStore中取出的图片信息
                directories.clear();
                directories.addAll(dirs); // 填充入全部的数据
                // 通知RecyclerView和ListView更新数据
                photoGridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
                adjustHeight(); // 调整listPopupWindow的高度
            }
        });

        captureManager = new ImageCaptureManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.__picker_fragment_photo_picker, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(column, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);

        final Button btSwitchDirectory = rootView.findViewById(R.id.category_btn);

        listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(rootView.findViewById(R.id.category_footer));
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();

                PhotoDirectory directory = directories.get(position);
                btSwitchDirectory.setText(directory.getName());

                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

        // 设置照片被点击时的事件信息
        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                int index = showCamera ? position - 1 : position;

                List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

                ImagePagerFragment imagePagerFragment = ImagePagerFragment.newInstance(photos, index);
                // 两个fragment通过寄宿的activity通信
                ((PhotoPickerActivity)getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof PhotoPickerActivity) {
            PhotoPickerActivity photoPickerActivity = (PhotoPickerActivity)getActivity();
            photoPickerActivity.updateTitleDoneItem();
        }
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }

    public void adjustHeight() {
        if (listAdapter == null) {
            return;
        }
        int count = listAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;
        if (listPopupWindow != null) {
            listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
        }
    }
}
