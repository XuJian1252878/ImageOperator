package com.example.multi_image_selector.entity;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by xujian on 2018/1/4.
 */

public class Folder {

    private String name;
    private String path;
    // 图片文件夹的显示封面
    private Image cover;
    // 图片文件夹下包含的图片信息
    private List<Image> images;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Folder)) {
            return false;
        }

        Folder other = (Folder)obj;
        return TextUtils.equals(this.path, other.path);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Image getCover() {
        return cover;
    }

    public void setCover(Image cover) {
        this.cover = cover;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
}
