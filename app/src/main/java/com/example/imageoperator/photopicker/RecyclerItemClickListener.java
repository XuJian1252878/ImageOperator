package com.example.imageoperator.photopicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by xujian on 2018/1/8.
 */

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener mListener;
    GestureDetector mGestureDetector;

    // 定义接口供外部具体操作
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    // java的回调机制，接口交给外面的类来实现，由外面的类自定义需要的操作
    public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true; // 如果是单击事件的话，那么返回true
            }
        });
    }

    // 拦截Touch事件的地方，要是拦截成功，touch事件将不会往下传递
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        // 获得当前
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        // 注意 mGestureDetector 拦截事件的方法
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            /**
             * mGestureDetector.onTouchEvent(e) 这里将MotionEvent传递给了mGestureDetector，由其判断是否为单击事件，
             * 如果为单击事件，那么 mGestureDetector 返回true（之前的定义），将进行消息拦截。
             */

            /**
             * getChildLayoutPosition
             * Return the adapter position of the given child view as of the latest completed layout pass.
             * This position may not be equal to Item's adapter position if there are pending changes
             * in the adapter which have not been reflected to the layout yet.
             *
             * getChildAdapterPosition (adapter中的更新可能会没有及时同步到layout中，所以有了这两种方法)
             * Return the adapter position that the given child view corresponds to.
             */
            mListener.onItemClick(childView, rv.getChildLayoutPosition(childView));
            return true;
        }
        return false;
    }

    // 处理touch事件的地方
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
