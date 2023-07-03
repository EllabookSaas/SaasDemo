package com.ellabook.saasdemo.ella;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ellabook.saasdemo.R;
import com.ellabook.saassdk.IEllaReaderControl;

/**
 * Created by tiandehua on 2020/11/27
 *
 * @description:
 */
public class ReaderToBuyFragment extends Fragment {

    private Callback<Integer> callback;
    private View viewToBuyExit;
    private boolean isExit;
    private TextView tvToBuyDesc;
    private TextView tvToBuyBt;
    private IEllaReaderControl control;

    public void setCallback(Callback<Integer> callback) {
        this.callback = callback;
    }

    public void setEllaReaderControl(IEllaReaderControl _control) {
        control = _control;
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
        if(!isExit) {
            if(control != null) {
                control.resume();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_read_to_buy, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        viewToBuyExit = view.findViewById(R.id.to_buy_exit);
        tvToBuyDesc = view.findViewById(R.id.tv_to_buy_desc);
        tvToBuyBt = view.findViewById(R.id.bt_to_buy);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //do nothing
                dismiss();
            }
        });

        view.findViewById(R.id.bt_to_buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.callback(0);
                }
                isExit = true;

                dismiss();
            }
        });

        view.findViewById(R.id.to_buy_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null) {
                    callback.callback(1);
                }
                isExit = true;

                dismiss();
            }
        });

        view.findViewById(R.id.ll_to_buy_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });

    }

    private void dismiss() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag("reader_to_buy") == null) {
            fragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, this, "reader_to_buy")
                    .commit();
        }
    }
}
