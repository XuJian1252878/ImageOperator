package com.example.multi_image_selector.util;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

/**
 * 获取屏幕参数信息
 * Created by xujian on 2018/1/6.
 */

public class ScreenUtils {

    /**
     * 获取当前屏幕的大小信息
     * @param context
     * @return
     */
    public static Point getScreenSize(Context context) {
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        // 存储当前屏幕的长宽信息
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            int width = display.getWidth();
            int height = display.getHeight();
            size.set(width, height);
        }

        return size;
    }

}
