package com.example.multi_image_selector.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by xujian on 2018/1/5.
 */

/** 提供一个正方形的布局，供在选择图片的时候 显示图片使用，正方形布局的边长由宽度决定 */

public class SquareFrameLayout extends FrameLayout {
    public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // 控制该控件以正方形的形式展示，以测量出来的宽的长度为基准
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
