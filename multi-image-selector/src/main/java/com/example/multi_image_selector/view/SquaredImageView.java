package com.example.multi_image_selector.view;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * Created by xujian on 2018/1/5.
 */

/** 显示供选择的图片的控件，都是以正方形的形式展示，正方形的边长以当前的宽作为基准 */

public class SquaredImageView extends AppCompatImageView {
    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 控制该控件以正方形的形式展示，以测量出来的宽的长度为基准
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
