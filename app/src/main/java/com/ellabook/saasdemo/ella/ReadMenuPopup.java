package com.ellabook.saasdemo.ella;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.ellabook.saasdemo.R;
import com.ellabook.saasdemo.ReaderUtils;


/**
 * Created by tiandehua on 2020/11/3
 *
 * @description:
 */
class ReadMenuPopup {
    private PopupWindow popupWindow;

    ReadMenuPopup(Context context, final Callback<Integer> callback) {
        View view = View.inflate(context, R.layout.popup_reader_menu, null);
        view.findViewById(R.id.tvPlaySetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.callback(0);
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(false);
        popupWindow.setTouchable(true);
    }

    void show(final View belowView) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(belowView, Gravity.RIGHT | Gravity.TOP,
                        ReaderUtils.dip2px(belowView.getContext(), 24), ReaderUtils.dip2px(belowView.getContext(), 56));
            }
        });
    }
}
