package com.example.multi_image_selector.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.multi_image_selector.R;
import com.example.multi_image_selector.entity.Folder;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xujian on 2018/1/6.
 */

public class FolderAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private List<Folder> mFolders = new ArrayList<>();

    private int mImageSize;
    // 上一次选中的文件夹的下标
    private int mLastSelected = 0;

    public FolderAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.mis_folder_cover_size);
    }

    /**
     * 设置当前拥有图片的文件夹的信息
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 获得当前手机下的所有图片信息
     * @return
     */
    private int getTotalImageSize() {
        int result = 0;
        if (mFolders != null && mFolders.size() > 0) {
            for (Folder folder: mFolders) {
                result += folder.getImages().size();
            }
        }
        return result;
    }

    /**
     * 设置用户当前选择的文件夹的下标
     * @param index
     */
    public void setSelectIndex(int index) {
        if (mLastSelected == index) {
            return;
        }
        mLastSelected = index;
        // 触发控件更新的操作
        notifyDataSetChanged();
    }

    /**
     * 获得上次用户选择的文件夹下标信息
     * @return
     */
    public int getSelectIndex() {
        return mLastSelected;
    }

    @Override
    public int getCount() {
        // 因为多了一个所有图片文件夹的选项
        return mFolders.size() + 1;
    }

    @Override
    public Folder getItem(int position) {
        if (position == 0) {
            return null;
        }
        return mFolders.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.mis_list_item_folder, parent, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (position == 0) {
            // 默认关于图片的第一项就是 当前手机中的所有图片文件信息
            holder.name.setText(R.string.mis_folder_all);
            holder.path.setText("/sdcard");
            holder.size.setText(String.format("%d%s", getTotalImageSize(),
                    mContext.getResources().getString(R.string.mis_photo_unit)));

            if (mFolders.size() > 0) {
                // 设置所有图片这一个图片文件夹选项，这个文件夹选项比较特殊
                // 这个多出来的所有图片文件夹 用第一个文件夹的cover图片 来显示
                Folder folder = mFolders.get(0);
                if (folder != null) {
                    Picasso.with(mContext)
                            .load(new File(folder.getCover().getPath()))
                            .error(R.drawable.mis_default_error)
                            .resizeDimen(R.dimen.mis_folder_cover_size, R.dimen.mis_folder_cover_size)
                            .centerCrop()
                            .into(holder.cover);
                } else {
                    holder.cover.setImageResource(R.drawable.mis_default_error);
                }
            }
        } else {
            holder.bindData(getItem(position));
        }

        if (mLastSelected == position) {
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.indicator.setVisibility(View.GONE);
        }

        return view;
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView path;
        TextView size;
        ImageView indicator;

        public ViewHolder(View view) {
            cover = view.findViewById(R.id.cover);
            name = view.findViewById(R.id.name);
            path = view.findViewById(R.id.path);
            size = view.findViewById(R.id.size);
            indicator = view.findViewById(R.id.indicator);

            view.setTag(this);
        }

        void bindData(Folder data) {
            if (data == null) {
                return;
            }

            name.setText(data.getName());
            path.setText(data.getPath());
            // 设置该文件夹下图片的总数信息
            if (data.getImages() != null) {
                size.setText(String.format("%d%s", data.getImages().size(), mContext.getResources().getString(R.string.mis_photo_unit)));
            } else {
                size.setText("*" + mContext.getResources().getString(R.string.mis_photo_unit));
            }

            // 设置文件夹的封面图片
            if (data.getCover() != null) {
                // 显示图片
                Picasso.with(mContext)
                        .load(new File(data.getCover().getPath()))
                        .placeholder(R.drawable.mis_default_error)
                        .resizeDimen(R.dimen.mis_folder_cover_size, R.dimen.mis_folder_cover_size)
                        .centerCrop()
                        .into(cover);
            } else {
                cover.setImageResource(R.drawable.mis_default_error);
            }
        }
    }
}
