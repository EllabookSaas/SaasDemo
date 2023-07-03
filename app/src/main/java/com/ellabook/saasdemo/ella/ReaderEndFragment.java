package com.ellabook.saasdemo.ella;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.ellabook.saasdemo.R;
import com.ellabook.saassdk.IEllaReaderControl;

/**
 * Created by tiandehua on 2021/01/11
 *
 * @description:
 */
public class ReaderEndFragment extends Fragment {

    private Callback<Integer> callback;
    private IEllaReaderControl control;

    public void setCallback(Callback<Integer> callback) {
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
        View view = inflater.inflate(R.layout.activity_cover, container, false);
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

        view.findViewById(R.id.reader_reread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.callback(0);
                }

                dismiss();
            }
        });
        view.findViewById(R.id.reader_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.callback(1);
                }

//                dismiss();
            }
        });

    }

    public void dismiss() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag("reader_end") == null) {
            fragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, this, "reader_end")
                    .commit();
        }
    }
}
