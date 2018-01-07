package com.example.multi_image_selector.entity;

import android.text.TextUtils;

/**
 * Created by xujian on 2018/1/4.
 */

public class Image {

    private String path;
    private String name;
    private long time;

    public Image(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Image)) {
            return false;
        }

        Image other = (Image)obj;
        // 如果文件路径完全相同，那么这两个Image完全相同
        return TextUtils.equals(this.path, other.path);
    }
}
