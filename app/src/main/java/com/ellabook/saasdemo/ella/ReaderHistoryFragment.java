package com.ellabook.saasdemo.ella;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.ellabook.saasdemo.R;
import com.ellabook.saasdemo.ReaderUtils;
import com.ellabook.saassdk.IEllaReaderControl;

/**
 * Created by tiandehua on 2020/8/17
 *
 * @description:
 */
public class ReaderHistoryFragment extends Fragment {

    private Callback<Integer> callback;
    private int lastPageIndex;
    private Handler mHandler = new Handler();
    private IEllaReaderControl control;

    public void setCallback( int pageIndex, Callback<Integer> callback) {
        this.lastPageIndex = pageIndex;
        this.callback = callback;
    }

    public void setEllaReaderControl(IEllaReaderControl _control) {
        this.control = _control;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //暂停阅读器
        if(control != null) {
            control.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(control != null) {
            control.resume();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_read_history_tips, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //do nothing
                //防止点击穿透，单纯的设置clickable属性有些定制系统不生效
            }
        });

        view.findViewById(R.id.bt_read_history_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.callback(lastPageIndex);
                }

                dismiss();
            }
        });

        view.findViewById(R.id.bt_read_history_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 3000);

    }

    private void dismiss() {
        mHandler.removeCallbacksAndMessages(null);
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag("reader_history") == null) {
            ReaderUtils.LogI("fragment", "ReaderHistoryFragment popup start... fm="+fragmentManager+", content=" + android.R.id.content);
            fragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, this, "reader_history")
                    .commit();
            ReaderUtils.LogI("fragment", "ReaderHistoryFragment popup stop...");
        }
    }
}
