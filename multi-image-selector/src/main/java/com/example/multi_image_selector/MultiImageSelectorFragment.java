package com.example.multi_image_selector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.multi_image_selector.adapter.FolderAdapter;
import com.example.multi_image_selector.adapter.ImageGridAdapter;
import com.example.multi_image_selector.entity.Folder;
import com.example.multi_image_selector.entity.Image;
import com.example.multi_image_selector.util.FileUtils;
import com.example.multi_image_selector.util.ScreenUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/1/5.
 */

public class MultiImageSelectorFragment extends Fragment {

    public static final String TAG = "MultiImageSelectorFragment";

    private static final String FILE_PROVIDER_﻿AUTHORITY = "com.example.photopicker.fileprovider";

    private static final int REQUEST_STORAGE_WRITE_ACCESS_PERMISSION = 110;
    private static final int REQUEST_CAMERA = 100;

    // Single choice
    public static final int MODE_SINGLE = 0;
    // Multi choice
    public static final int MODE_MULTI = 1;

    // loaders
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;

    // 图片路径列表
    private ArrayList<String> mResultImageList = new ArrayList<>();
    // 图片文件夹路径列表
    private ArrayList<Folder> mResultFolderList = new ArrayList<>();

    // 图片GridView的adapter
    private ImageGridAdapter mImageAdapter;
    // 图片文件夹的adapter
    private FolderAdapter mFolderAdapter;

    // 显示在图片GridView下方的文件夹按钮
    private View mPopupAnchorView;
    // 显示图片文件夹的popupListView信息
    private ListPopupWindow mFolderPopupWindow;
    // 设置Loader中是否已经加载了图片文件夹的信息
    private boolean hasFolderGened = false;
    // 显示 图片文件夹类别的 TextView
    private TextView mCategoryText;
    // 显示具体图片文件的GridView
    private GridView mGridView;

    // 用于存储用于用于拍照的所产生的图片临时文件
    private File mTmpFile;

    // 用户存储拍照产生的图片文件的key
    private static final String KEY_TEMP_FILE = "key_temp_file";

    /** Max image size，int，*/
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /** Select mode，{@link #MODE_MULTI} by default */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /** Whether show camera，true by default */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /** Original data set */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";

    /** 在对应activity中实现的接口，与activty进行通信 */
    public interface Callback {
        void onSingleImageSelected(String path);
        void onImageSelected(String path);
        void onImageUnselected(String path);
        void onCameraShot(File imageFile);
    }
    private Callback mCallback;

    // 获取从Activity中setArguments传递过来的参数
    private boolean isShowCamera() {
        // 默认显示拍照的图标信息
        return getArguments() == null || getArguments().getBoolean(EXTRA_SHOW_CAMERA, true);
    }

    /**
     * 获取从activity传递过来的setArguments参数信息
     * @return
     */
    private int selectMode() {
        return getArguments() == null ? MODE_MULTI : getArguments().getInt(EXTRA_SELECT_MODE);
    }

    private int selectImageCount() {
        return getArguments() == null ? 9 : getArguments().getInt(EXTRA_SELECT_COUNT);
    }

    // 当Fragment与Activity建立关联的时候调用
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 检查与该fragment关联的activity是不是实现了该fragment的接口
        try {
            mCallback = (Callback)context;
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }
    }

    // 为fragment创建关联视图的时候调用
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 这里返回的是子view，即在fragment所在的布局，如果最后一个参数设置为true，那么将会返回一整个父布局，这不是想要的
        return inflater.inflate(R.layout.mis_fragment_multi_image, container, false);
    }

    // 在onCreateView返回之后马上被调用，这时候　fragment 还没有与它的父控件绑定。主要就是为Fragment上的布局控件设定监听事件
    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but
     * before any saved state has been restored in to the view. This gives subclasses a chance to
     * initialize themselves once they know their view hierarchy has been completely created.
     * The fragment's view hierarchy is not however attached to its parent at this point.
     * */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取当前是单选还是多选的图片选择模式
        final int mode = selectMode();
        if (mode ==  MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(EXTRA_DEFAULT_SELECTED_LIST);
            if (tmp != null && tmp.size() > 0) {
                mResultImageList = tmp;
            }
        }

        // 设置 显示图片信息 activity 的adapter
        mImageAdapter = new ImageGridAdapter(getActivity(), isShowCamera(), 3);
        // 如果是双选模式，那么在grid view的图片上显示indicator
        mImageAdapter.showSelectIndicator(mode == MODE_MULTI);

        mPopupAnchorView = view.findViewById(R.id.footer);
        mCategoryText = view.findViewById(R.id.category_btn);
        mCategoryText.setText(R.string.mis_folder_all);
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFolderPopupWindow == null) {
                    createPopupFolderList(); // 创建关于图片文件夹的popupListWindow
                }

                // 如果当前的 ListPopupWindow 处于开启状态，那么关闭
                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
//                    int index = mFolderAdapter.getSelectIndex();
//                    index = index == 0 ? index : index - 1;
//                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        // 对于ListView GridView 这些视图的adapter来说，adapter决定的事视图的呈现逻辑（点击了某一项之后视图的呈现逻辑）
        // 而ListView GridView 这些视图绑定的listener 是用来处理点击的业务逻辑，有这两点的区别
        mGridView = view.findViewById(R.id.grid);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isShowCamera()) {
                    if (position == 0) {
                        // 调用拍照界面
                        showCameraAction();
                    } else {
                        Image image = (Image)parent.getAdapter().getItem(position);
                        selectImageFromGrid(image, mode);
                    }
                } else {
                    Image image = (Image)parent.getAdapter().getItem(position);
                    selectImageFromGrid(image, mode);
                }
            }
        });

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /**
                 * 用于为当前请求分配标签，用于统一管理相关的请求，参数是一个object对象，可以传入任意Object的子类。
                 * 与这个方法有关的有Picasso类中的cancelTag(Object)、pauseTag(Object)、resumeTag(Object)，
                 * 分别用于对设置了对应tag的请求进行取消、暂停、重新启动操作。
                 适用这个方法的典型情景是，在列表视图中，如果快速进行滑动，则完全可以在滑动的时候暂停所有网络请求，
                 在停止滑动的时候再次开始请求，实现这种需求的方法就是利用同一个tag在适当的地方调用resumeTag和pauseTag。
                 */
                // SCROLL_STATE_FLING The user had previously been scrolling using touch and had performed a fling.
                // SCROLL_STATE_IDLE The view is not scrolling.
                // SCROLL_STATE_TOUCH_SCROLL The user is scrolling using touch, and their finger is still on the screen
                if (scrollState == SCROLL_STATE_FLING) {
                    Picasso.with(view.getContext()).pauseTag(TAG);
                } else {
                    Picasso.with(view.getContext()).resumeTag(TAG);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        // 创建文件夹的adapter
        mFolderAdapter = new FolderAdapter(getActivity());
    }

    // 确保与fragment相关联的activity一定已经创建完毕的时候调用
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 初始化 fragment中的数据信息
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }


    // Called when all saved state has been restored into the view hierarchy of the fragment.
    // This is called after onActivityCreated(Bundle) and before onStart().
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTmpFile = (File)savedInstanceState.getSerializable(KEY_TEMP_FILE);
        }

    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
     * in a new instance of its process is restarted. If a new instance of the fragment later needs
     * to be created, the data you place in the Bundle here will be available in the Bundle given
     * to onCreate(Bundle), onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_TEMP_FILE, mTmpFile);
    }

    // onConfigurationChanged事件并不是只有屏幕方向改变才可以触发，其他的一些系统设置改变也可以触发，比如打开或者隐藏键盘。
    // 当我们的屏幕方向发生改变时，就可以触发onConfigurationChanged事件。
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // 如果屏幕大小或者方向发生改变的时候，关闭文件夹按钮
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }
    }

    // 拍照之后跳回图片选择的activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    if (mTmpFile != null) {
                        if (mCallback != null) {
                            mCallback.onCameraShot(mTmpFile);
                        }
                    }
                } else {
                    while (mTmpFile != null && mTmpFile.exists()) {
                        boolean success = mTmpFile.delete();
                        if (success) {
                            mTmpFile = null;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 当用户点击选择界面图片时候的回调函数
     * @param image
     * @param mode
     */
    private void selectImageFromGrid(Image image, int mode) {
        // Activity与Fragment 已经选择的图片数据的连接点
        if (image != null) {
            if (MODE_MULTI == mode) {
                // 多选的图片状态
                if (mResultImageList.contains(image.getPath())) {
                    mResultImageList.remove(image.getPath());
                    // 取消选择图片时候的回调
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.getPath());
                    }
                } else {
                    if (mResultImageList.size() >= selectImageCount()) {
                        Toast.makeText(getActivity(), R.string.mis_msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mResultImageList.add(image.getPath());
                    // 选择一张图片的时候发生的回调
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.getPath());
                    }
                }
                mImageAdapter.select(image);
            } else if (MODE_SINGLE == mode) {

                if (mCallback == null) {
                    mCallback.onSingleImageSelected(image.getPath());
                }
            }
        }
    }

    // 开启手机的摄像头拍照
    private void showCameraAction() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getString(R.string.mis_permission_rationale_write_storage),
                    REQUEST_STORAGE_WRITE_ACCESS_PERMISSION);
        } else {
            // 启动相机程序
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            /**
             * If multiple activities are found to satisfy the intent, the one with the highest
             * priority will be used. If there are multiple activities with the same priority,
             * the system will either pick the best activity based on user preference, or resolve
             * to a system class that will allow the user to pick an activity and forward from there.
             * 查找有没有合适的activity来响应这个intent
             * */
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                try {
                    mTmpFile = FileUtils.createTmpFile(getActivity());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (mTmpFile != null && mTmpFile.exists()) {

                    /**
                     * ﻿判断设备运行的系统是否小于Android7.0，从7.0的系统开始，认为直接使用本地真实路径的Uri是不安全的，
                     * 会抛出FileUriExposedException异常。因此应该使用FileProvider，一种特殊的ContentProvider
                     * （提供与Content Provider类似的机制来对数据进行保护），可以选择性的将封装过的Uri共享给外部，
                     * 提高应用的安全性。
                     */
                    Uri tmpFileUri;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        tmpFileUri = Uri.fromFile(mTmpFile);
                    } else {
                        tmpFileUri = FileProvider.getUriForFile(getActivity(), FILE_PROVIDER_﻿AUTHORITY, mTmpFile);
                    }

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tmpFileUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else {
                    Toast.makeText(getActivity(), R.string.mis_error_image_not_exist, Toast.LENGTH_SHORT).show();
                }
            } else {
                // 提示手机上没有相机程序
                Toast.makeText(getActivity(), R.string.mis_msg_no_camera, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 关于请求权限
    private void requestPermission(final String permission, String rationale, final int requestCode) {
        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.mis_permission_dialog_title)
                    .setMessage(rationale)
                    .setPositiveButton(R.string.mis_permission_dialog_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, requestCode);
                        }
                    })
                    .setNegativeButton(R.string.mis_permission_dialog_cancel, null)
                    .create()
                    .show();
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
    }

    // 申请权限的回调函数
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode) {
            case REQUEST_STORAGE_WRITE_ACCESS_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请通过，打开拍照程序
                    showCameraAction();
                }
                break;
            default:
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 创建关于图片文件夹的popupListWindow
    private void createPopupFolderList() {
        Point point = ScreenUtils.getScreenSize(getActivity());
        int width = point.x;
        // popupList的高度占屏幕的 4.5 / 8.0
        int height = (int)(point.y * (4.5f / 8.0f));

        mFolderPopupWindow = new ListPopupWindow(getActivity());
        // 设置背景图片
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        // 设置popupWindow中内容的宽度
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height);
        // Sets the popup's anchor view. This popup will always be positioned relative to the anchor view when shown.
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        /**
         * Set whether this window should be modal when shown.
         * If a popup window is modal, it will receive all touch and key input.
         * If the user touches outside the popup window's content area the popup window will be dismissed.
         */
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 设置当前被点击的是哪一个文件夹
                mFolderAdapter.setSelectIndex(position);
                final AdapterView adapterView = parent;
                // 当前选择的文件夹下标
                final int index = position;

                // 还是在本线程线程执行任务
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // 关闭文件夹的window
                        mFolderPopupWindow.dismiss();

                        if (index == 0) {
                            // 当前点击的是所有文件文件夹选项
                            /**
                             * 当您使用 initLoader() 时，它将使用含有指定 ID 的现有加载器（如有）。
                             * 如果没有，则它会创建一个。但有时，您想舍弃这些旧数据并重新开始。
                             * 要舍弃旧数据，请使用 restartLoader()。
                             * 例如，当用户的查询更改时，此 SearchView.OnQueryTextListener 实现将重启加载器。
                             * 加载器需要重启，以便它能够使用修订后的搜索过滤器执行新查询：
                             */
                            // 因为ImageAdapter中的Images存储的是当前文件夹下的图片文件信息，所以当从某一个
                            // 文件夹下切换到所有文件夹下的时候。需要重新机器上的所有文件夹信息
                            getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            mCategoryText.setText(R.string.mis_folder_all);
                            if (isShowCamera()) {
                                mImageAdapter.setShowCamera(true);
                            } else {
                                mImageAdapter.setShowCamera(false);
                            }
                        } else {
                            // 用户点击的是具体的某一个子文件夹
                            Folder folder = (Folder) adapterView.getAdapter().getItem(index);
                            if (null != folder) {
                                // GridView中显示的将会是 该文件夹下的图片信息
                                mImageAdapter.setData(folder.getImages());
                                mCategoryText.setText(folder.getName());
                                if (mResultImageList != null && mResultFolderList.size() > 0) {
                                    mImageAdapter.setDefaultSelected(mResultImageList);
                                }
                            }
                            // 显示子文件夹的图片的时候，不提供拍照的功能
                            mImageAdapter.setShowCamera(false);
                        }
                        // 显示对应的文件夹图片之前，首先把图片GridView滑动到最顶端
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);
            }
        });
    }

    // 关于使用MediaStore.Images.Media 这个ContentProvider来选择图片的enum类型
    private enum ImageProjectionEnum {
        _ID(0, MediaStore.Images.Media._ID),
        SIZE(1, MediaStore.Images.Media.SIZE),
        MIME_TYPE(2, MediaStore.Images.Media.MIME_TYPE),
        DATE_ADDED(3, MediaStore.Images.Media.DATE_ADDED),
        DISPLAY_NAME(4, MediaStore.Images.Media.DISPLAY_NAME),
        DATA(5, MediaStore.Images.Media.DATA);

        private int id;
        private String name;

        ImageProjectionEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public static String[] getFields() {
            List<String> fields = new ArrayList<>();
            for (ImageProjectionEnum item: ImageProjectionEnum.values()) {
                fields.add(item.getName());
            }
            return fields.toArray(new String[0]);
        }
    }

    // 通过LoaderManager.LoaderCallbacks接口可以很轻松的实现异步加载数据到Fragment或Activity 中，
    // Loaders提供了回调机制onLoadFinished()通知最终的运行结果。
    // Loader对于并发可以用过Loader管理器统一管理，所以更适合批量处理多个异步任务(当然内部仍然是多线程)。
    // 支持异步加载数据;监控其数据源并在内容变化时传递新结果。
    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        // 针对指定的 ID 进行实例化并返回新的 Loader
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            // 该方法将检查是否已存在由该 ID 指定的加载器，如果没有，创建id对应的加载器（加载器的使用）
            CursorLoader cursorLoader = null;
            if (id == LOADER_ALL) {
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // The content:// style URI for the "primary" external storage volume.
                        ImageProjectionEnum.getFields(),
                        ImageProjectionEnum.SIZE.getName() + " > 0 AND " +
                                ImageProjectionEnum.MIME_TYPE.getName() + " = ? OR " +
                                ImageProjectionEnum.MIME_TYPE.getName() + " = ?",
                        new String[] {"image/jpeg", "image/png"},
                        ImageProjectionEnum.DATE_ADDED.getName() + " DESC");
            } else if (id == LOADER_CATEGORY) {
                cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ImageProjectionEnum.getFields(),
                        ImageProjectionEnum.SIZE + " > 0 AND " +
                                ImageProjectionEnum.DATA + " like '%" +
                                args.getString("path") + "%'",
                        null,
                        ImageProjectionEnum.DATE_ADDED + " DESC");
            }
            return cursorLoader;
        }

        // 将在先前创建的加载器完成加载时调用
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                if (data.getCount() > 0) {
                    List<Image> images = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(ImageProjectionEnum.DATA.getName()));
                        String name = data.getString(data.getColumnIndexOrThrow(ImageProjectionEnum.DISPLAY_NAME.getName()));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(ImageProjectionEnum.DATE_ADDED.getName()));

                        if (!fileExist(path)) {
                            continue;
                        }

                        Image image = null;
                        if (!TextUtils.isEmpty(name)) {
                            image = new Image(path, name, dateTime);
                            images.add(image);
                        }

                        if (!hasFolderGened) {
                            // 收集所有的图片文件夹信息
                            File folderFile = new File(path).getParentFile(); // 获得图片文件的上一级目录
                            if (folderFile != null && folderFile.exists()) {
                                String folderFilePath = folderFile.getAbsolutePath();
                                Folder folder = getFolderByPath(folderFilePath); // 文件夹信息可能存在
                                if (folder == null) {
                                    // 是一个新出现的文件夹
                                    folder = new Folder();
                                    folder.setName(folderFile.getName()); // 获得文件夹的名称
                                    folder.setPath(folderFilePath);
                                    folder.setCover(image);
                                    List<Image> imageList = new ArrayList<>();
                                    imageList.add(image);
                                    folder.setImages(imageList);
                                    mResultFolderList.add(folder);
                                } else {
                                    // 记录对应文件夹下的图片信息
                                    folder.getImages().add(image);
                                }
                            }
                        }

                    } while (data.moveToNext());

                    // 可能是全部的图片信息，或者是某一个图片文件夹下的文件信息
                    mImageAdapter.setData(images);
                    // 如果用户已经选择了相应的图片信息
                    if (mResultImageList != null && mResultImageList.size() > 0) {
                        mImageAdapter.setDefaultSelected(mResultImageList);
                    }

                    // 如果还没有产生过文件夹信息
                    if (!hasFolderGened) {
                        mFolderAdapter.setData(mResultFolderList);
                        hasFolderGened = true;
                    }
                }
            }
        }

        // 将在先前创建的加载器重置且其数据因此不可用时调用
        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }

        // 判断指定路径的文件是否存在
        private boolean fileExist(String path) {
            if (!TextUtils.isEmpty(path)) {
                return new File(path).exists();
            }
            return false;
        }

        // 通过文件夹的路径构建Folder类
        private Folder getFolderByPath(String path) {
            if (mResultFolderList != null) {
                for (Folder folder: mResultFolderList) {
                    if (TextUtils.equals(folder.getPath(), path)) {
                        return folder;
                    }
                }
            }
            return null;
        }
    };
}
