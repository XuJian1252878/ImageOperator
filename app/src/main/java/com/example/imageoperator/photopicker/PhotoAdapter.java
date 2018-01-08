package com.example.imageoperator.photopicker;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.photopicker.R;
import com.example.photopicker.utils.AndroidLifecycleUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/1/8.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<String> photoPaths = new ArrayList<>();
    private LayoutInflater inflater;

    private Context mContext;

    final static int TYPE_ADD = 1;
    final static int TYPE_PHOTO = 2;

    final static int MAX = 9;

    public PhotoAdapter(List<String> photoPaths, Context mContext) {
        this.photoPaths = photoPaths;
        this.mContext = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private View vSelected;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            vSelected = itemView.findViewById(R.id.v_selected);
            if (vSelected != null) {
                // 默认不显示图片已被选中的标签
                vSelected.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case TYPE_ADD:
                itemView = inflater.inflate(com.example.imageoperator.R.layout.photo_picker_item_add, parent, false);
                break;
            case TYPE_PHOTO:
                itemView = inflater.inflate(R.layout.__picker_item_photo, parent, false);
                break;
            default:
                break;
        }
        return new PhotoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PHOTO) {
            Uri uri = Uri.fromFile(new File(photoPaths.get(position)));

            // 图片选择界面的activity被关闭之后才能在主界面上 显示图片
            boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(holder.ivPhoto.getContext());

            if (canLoadImage) {
                RequestOptions options = new RequestOptions();
                options.centerCrop()
                        .placeholder(R.drawable.__picker_ic_photo_black_48dp)  // 大图未加载出时候显示的替代小图
                        .error(R.drawable.__picker_ic_broken_image_black_48dp);  // 图片不能显示，而显示的代替的错误图标
                Glide.with(mContext)
                        .load(uri)
                        .apply(options)
                        .thumbnail(0.1f)
                        .into(holder.ivPhoto);
            }
        }
    }

    @Override
    public int getItemCount() {
        int count = photoPaths.size() + 1;
        if (count > MAX) {
            // 对于已经选择满了需要的图片信息，通过将count设置为需要图片的最大数量，直接让添加符号不显示
            count = MAX;
        }
        return count;
    }

    // 返回对应位置的item对应的type
    @Override
    public int getItemViewType(int position) {
        return (position == photoPaths.size() && position != MAX) ? TYPE_ADD : TYPE_PHOTO;
    }


}
