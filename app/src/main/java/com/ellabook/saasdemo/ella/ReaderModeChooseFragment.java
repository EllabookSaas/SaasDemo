package com.ellabook.saasdemo.ella;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ellabook.saasdemo.R;
import com.ellabook.saassdk.IEllaReaderControl;
import com.ellabook.saassdk.IEllaReaderUse;

/**
 * Created by tiandehua on 2020/8/17
 *
 * @description:
 */
public class ReaderModeChooseFragment extends Fragment {

    private Callback<Integer> callback;
    private IEllaReaderControl control;
    private OnDismissListener dismissListener;

    public void setCallback(Callback<Integer> callback) {
        this.callback = callback;
    }

    public void setEllaReaderControl(IEllaReaderControl _control) {
        control = _control;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        dismissListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //暂停阅读器
        if (control != null) {
            control.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (control != null) {
            control.resume();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_read_mode, container, false);
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

        RadioGroup rg = view.findViewById(R.id.rg_play_mode);

        if (control != null) {
            if (control.getReadMode() == IEllaReaderUse.ELLA_READ_MODE_MANUAL) {
                view.<RadioButton>findViewById(R.id.rb_play_mode_hand).setChecked(true);
//                rg.check(R.id.rb_play_mode_hand);
            } else {
                view.<RadioButton>findViewById(R.id.rb_play_mode_auto).setChecked(true);
//                rg.check(R.id.rb_play_mode_auto);
            }
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_play_mode_hand) {
                    chooseMode(group.getContext(), IEllaReaderUse.ELLA_READ_MODE_MANUAL);
                } else if (checkedId == R.id.rb_play_mode_auto) {
                    chooseMode(group.getContext(), IEllaReaderUse.ELLA_READ_MODE_AUTO);
                }
            }
        });

        view.findViewById(R.id.tv_play_mode_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void chooseMode(Context context, int playModeRead) {
        if (callback != null) {
            if (!checkIsSupport(playModeRead)) {
                Toast.makeText(context, "暂不支持此模式", Toast.LENGTH_SHORT).show();
                return;
            }
            callback.callback(playModeRead);
        }

        dismiss();
    }

    private boolean checkIsSupport(int playModeRead) {
        if (control == null) {
            return true;
        }
        int[] supportReadMode = control.getSupportedReadMode();
        for (int value : supportReadMode) {
            if (value == playModeRead) {
                return true;
            }
        }
        return false;
    }

    private void dismiss() {
        getFragmentManager().beginTransaction().remove(this).commit();
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag("reader_mode") == null) {
            fragmentManager
                    .beginTransaction()
                    .add(android.R.id.content, this, "reader_mode")
                    .commit();
        }
    }
}
