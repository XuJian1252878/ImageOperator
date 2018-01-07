package com.example.multi_image_selector.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.multi_image_selector.MultiImageSelectorFragment;
import com.example.multi_image_selector.R;
import com.example.multi_image_selector.entity.Image;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/1/6.
 */

public class ImageGridAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;
    private boolean mShowCamera = true;

    // 单选模式下不显示图片右上角的复选框，多选模式下才显示
    private boolean mShowSelectIndicator = true;

    // 当前已经被选择的图片列表
    private List<Image> mSelectedImages = new ArrayList<>();
    // 系统中全部的图片列表
    private List<Image> mImages = new ArrayList<>();

    // 设置每一个网格当前的宽度
    private int mGridWidth;

    private LayoutInflater mInflater;

    public ImageGridAdapter(Context context, boolean showCamera, int column) {
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mShowCamera = showCamera;

        // 获取当前屏幕的宽度信息
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int width = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            width = size.x;
        } else {
            width = wm.getDefaultDisplay().getWidth();
        }
        // 从而获得每一个图片网格控件的宽度（根据指定的列数）
        mGridWidth = width / column;
    }

    /**
     * 单选模式下不显示图片右上角的复选框，多选模式下才显示。默认是多选模式
     * @param showSelectIndicator
     */
    public void showSelectIndicator(boolean showSelectIndicator) {
        mShowSelectIndicator = showSelectIndicator;
    }

    public void setShowCamera(boolean showCamera) {
        if (mShowCamera == showCamera) {
            return;
        }

        mShowCamera = showCamera;
        // 数据集发生改变，通知GridView控件重绘
        notifyDataSetChanged();
    }

    public boolean isShowCamera() {
        return mShowCamera;
    }

    /**
     * 当用户选择GridView里面的某一个图片时候的操作，可能是选择也有可能是取消选择
     * @param image
     */
    public void select(Image image) {
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
        } else {
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置初始选择的图片数据
     * @param defaultSelectedImageList
     */
    public void setDefaultSelected(ArrayList<String> defaultSelectedImageList) {
        for (String path : defaultSelectedImageList) {
            Image image = getImageByPath(path);
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
        // 设置完成初始图片数据之后需要 更新控件
        if (mSelectedImages.size() >= 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * 当用户选择切换图片文件夹的时候需要对图片文件夹进行更新
     * @param images
     */
    public void setData(List<Image> images) {
        mSelectedImages.clear();

        if (images != null && images.size() > 0) {
            mImages = images;
        } else {
            mImages.clear();
        }

        notifyDataSetChanged();
    }

    private Image getImageByPath(String path) {
        if (mImages != null && mImages.size() >= 0) {
            for (Image image: mImages) {
                if (image.getPath().equals(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return mShowCamera ? mImages.size() + 1: mImages.size();
    }

    @Override
    public Image getItem(int position) {
        if (mShowCamera) {
            if (position == 0) {
                return null;
            }
            return mImages.get(position - 1);
        } else {
            return mImages.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (mShowCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (isShowCamera()) {
            // 如果显示摄像头的情况下
            if (position == 0) {
                view = mInflater.inflate(R.layout.mis_list_item_camera, parent, false);
                return view;
            }
        }

        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.mis_list_item_image, parent, false);
            holder = new ViewHolder(view);
        } else {
            // 取出缓存的view holder信息
            holder = (ViewHolder)view.getTag();
        }

        holder.bindData(getItem(position));
        return view;
    }

    class ViewHolder {
        ImageView image;
        ImageView indicator;
        View mask;

        ViewHolder(View view) {
            image = view.findViewById(R.id.image);
            indicator = view.findViewById(R.id.checkmark);
            mask = view.findViewById(R.id.mask);

            // 将每一个子view中的每一个控件先缓存下来，因此利用convertView，就不必每一次都findViewById，提高GridView的效率
            view.setTag(this);
        }

        void bindData(Image data) {
            if (data == null) {
                return;
            }
            // 处理单选和多选状态
            if (mShowSelectIndicator) {
                indicator.setVisibility(View.VISIBLE);
                if (mSelectedImages.contains(data)) {
                    // 设置该图片选中
                    indicator.setImageResource(R.drawable.mis_btn_selected);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    // 图片没有被选中
                    indicator.setImageResource(R.drawable.mis_btn_unselected);
                    mask.setVisibility(View.GONE);
                }
            } else {
                // 单选状态下不显示复选框
                indicator.setVisibility(View.GONE);
            }

            // 显示图片
            File imageFile = new File(data.getPath());
            if (imageFile.exists()) {
                Picasso.with(mContext)
                        .load(imageFile)
                        .placeholder(R.drawable.mis_default_error)
                        .tag(MultiImageSelectorFragment.TAG)
                        .resize(mGridWidth, mGridWidth)
                        .centerCrop()
                        .into(image);
            } else {
                image.setImageResource(R.drawable.mis_default_error);
            }

        }
    }
}
