package com.ellabook.saasdemo.ella;

import static com.ellabook.saasdemo.MainActivity.genSign;
import static com.ellabook.saasdemo.SignBean.ReadTypeMode.FORMAL_READ;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.ellabook.saasdemo.R;
import com.ellabook.saasdemo.ReaderUtils;
import com.ellabook.saassdk.EllaReaderApi;
import com.ellabook.saassdk.IEllaReaderControl;
import com.ellabook.saassdk.IEllaReaderUse;
import com.ellabook.saassdk.data.QuestResult;
import com.ellabook.saassdk.data.Sentence;
import com.ellabook.saassdk.tts.TTSConfig;

import java.util.List;
import java.util.Locale;

/**
 * Created by tiandehua on 2021/1/21
 *
 * @description:
 */
public class ReaderUseImpl implements IEllaReaderUse {
    private static final String TAG = "ReaderUseImpl";

    private static final int SHOW_LOADING = 0;
    private static final int REMOVE_LOADING = 2;
    private static final int LOADING_ERR = 3;

    public static long DELAY_TIME = 30000;

    public View contentView;
    private View upView;
    private View downView;
    private View pauseView;
    private View menuView;
    private View exitView;
    private View playView;
    private TextView pagesView;
    private View pagesViewContent;

    private View bookLoadingView;
    private View loadingView;
    private View loadingErrView;

    private Dialog dialog;

    private IEllaReaderControl mEllaReaderControl;

    private boolean paused = false;

    private boolean NORMAL_READ = false;

    private boolean tryModeToFormalMode = false;

    private String bookCode;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case LOADING_ERR:
                    if (loadingView != null) {
                        loadingErr(msg.arg1);
                    }
                    break;
                case REMOVE_LOADING:
                    break;
            }
        }
    };

    private Context mSource;

    public ReaderUseImpl(Context context, String bookCode, boolean isFormalRead) {
        this.bookCode = bookCode;
        NORMAL_READ = isFormalRead;
        mSource = context;
    }

    /**
     * 控制器view，浮在阅读上
     *
     * @param context activity context
     * @return 显示的view
     */
    @Override
    public View genControlView(Context context) {

        //设置异形屏(Android9.0以上)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Activity readerActivity = (Activity) context;
            if (readerActivity != null) {
                Window window = readerActivity.getWindow();

                WindowManager.LayoutParams lp = window.getAttributes();

                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;

                window.setAttributes(lp);
            }
        }

        contentView = View.inflate(context, R.layout.activity_reader, null);
        menuView = contentView.findViewById(R.id.menu);
        exitView = contentView.findViewById(R.id.exit);
        playView = contentView.findViewById(R.id.play);
        pauseView = contentView.findViewById(R.id.pause);
        upView = contentView.findViewById(R.id.up);
        downView = contentView.findViewById(R.id.down);
        pagesView = contentView.findViewById(R.id.pages);
        pagesViewContent = contentView.findViewById(R.id.rl_pages);

        //试读结束弹窗
        dialog = new AlertDialog.Builder(context).setTitle("试读结束").setMessage("是否继续阅读")
                .setOnCancelListener(dialog -> mEllaReaderControl.resume())
                .setPositiveButton("确定", (dialog, which) -> {
                    mEllaReaderControl.resume();

                    //试读结束,若要继续正式阅读图书,需走一遍租书接口
                    String sign = genSign(bookCode, FORMAL_READ);
                    LogUtils.d("sigh=" + sign);

                    tryModeToFormalMode = true;
                    EllaReaderApi.getInstance().refreshReader(sign);
                    dialog.cancel();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.cancel();
                    mEllaReaderControl.resume();
                })
                .create();

        playView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEllaReaderControl == null) {
                    return;
                }
                if (mEllaReaderControl.resume()) {

                    paused = false;
                    v.setVisibility(View.GONE);
                    pauseView.setVisibility(View.VISIBLE);


                    if (mEllaReaderControl.getCurrentPage() != 1) {
                        upView.setVisibility(View.VISIBLE);
                    }
                    downView.setVisibility(View.VISIBLE);
                }
            }
        });
        pauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEllaReaderControl == null) {
                    return;
                }

                if (mEllaReaderControl.pause()) {
                    paused = true;
                    v.setVisibility(View.GONE);
                    playView.setVisibility(View.VISIBLE);


                    //todo 暂停时不推荐做上下翻页操作,避免出现异常
                    upView.setVisibility(View.GONE);
                    downView.setVisibility(View.GONE);
                }
            }
        });

        exitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEllaReaderControl == null) {
                    return;
                }

                exitTime = System.currentTimeMillis();
                LogUtils.d("退出:时间=" + exitTime);
                mEllaReaderControl.exit();
            }
        });

        initPopWindow(contentView);
        downView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEllaReaderControl == null) {
                    return;
                }
                if (mEllaReaderControl.getCurrentPage() > 1 && mEllaReaderControl.isInLastPage()) {
//                    showReaderEndFragment();
                    showPopWindow(contentView);
                } else {
                    mEllaReaderControl.nextPage();
                }
            }
        });

        upView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEllaReaderControl == null) {
                    return;
                }
                mEllaReaderControl.lastPage();
            }
        });

        menuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayModeDialog();
            }
        });

        showDownViewAnimal();

        return contentView;
    }

    /**
     * 加载view,控制显示的view
     *
     * @param context activity context
     * @return 显示的view
     */
    @Override
    public View genLoadingView(Context context) {
        bookLoadingView = View.inflate(context, R.layout.layout_book_loading, null);
        loadingView = bookLoadingView.findViewById(R.id.loading);
        loadingView.setVisibility(View.VISIBLE);
        loadingErrView = bookLoadingView.findViewById(R.id.loading_err);
        loadingErrView.setVisibility(View.GONE);
        bookLoadingView.findViewById(R.id.close_loading).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookLoadingView.setVisibility(View.GONE);
                contentView.setVisibility(View.VISIBLE);

                handler.removeMessages(LOADING_ERR);
            }
        });
        loadingErrView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingErrView.setVisibility(View.GONE);
                loadingView.setVisibility(View.VISIBLE);

                handler.post(() -> {
                    Message message = Message.obtain();
                    message.arg1 = 11;
                    message.what = LOADING_ERR;
                    handler.sendMessageDelayed(message, DELAY_TIME);
                });
            }
        });
        return bookLoadingView;
    }

    /**
     * 加载时显示
     */
    @Override
    public void loadingBook() {
        contentView.setVisibility(View.GONE);
        bookLoadingView.setVisibility(View.VISIBLE);

        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(loadingView.findViewById(R.id.img_loading));

        Glide.with(loadingView).load(AppCompatResources.getDrawable(loadingView.getContext(), R.drawable.book_loading)).
                into(imageViewTarget);

        handler.post(() -> {
            Message message = Message.obtain();
            message.arg1 = 11;
            message.what = LOADING_ERR;
            handler.sendMessageDelayed(message, DELAY_TIME);
        });
    }

    /**
     * 下一页加载成功
     */
    @Override
    public void loadingSuccess() {
        LogUtils.d("下一页加载成功");
        handler.removeMessages(LOADING_ERR);

        if (bookLoadingView.getVisibility() == View.VISIBLE) {

            contentView.setVisibility(View.VISIBLE);
            bookLoadingView.setVisibility(View.GONE);

            if (mEllaReaderControl != null) {
                mEllaReaderControl.nextPage();
            }
        }

        loadingView.setVisibility(View.VISIBLE);
        loadingErrView.setVisibility(View.GONE);

    }

    /**
     * 加载失败时回调
     */
    @Override
    public void loadingErr(int code) {
        if (code == 11 && bookLoadingView.getVisibility() == View.VISIBLE) {
            loadingView.setVisibility(View.GONE);
            loadingErrView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * logo 的位置控制，在右上角，可控制上边距，右边距
     *
     * @return 右边距，单位dp
     */
    @Override
    public int logoRightMargin() {
        return 50;
    }

    /**
     * logo 的位置控制，在右上角，可控制上边距，右边距
     *
     * @return 上边距，单位dp
     */
    @Override
    public int logoTopMargin() {
        return 14;
    }

    /**
     * 阅读器控制器
     *
     * @param readerControl 用于阅读器控制(上一页,下一页,退出等)
     */
    @Override
    public void onControllerReady(IEllaReaderControl readerControl) {
        mEllaReaderControl = readerControl;
        LogUtils.d("TotalPage=" + readerControl.getTotalPage());
    }

    private long exitTime = 0;

    @Override
    public void onLoadComplete(View rootView) {
        if (rootView == null) {
            return;
        }
        ReaderUtils.LogI(TAG, "onLoadComplete");
        //menuView.setEnabled(false);
    }

    /**
     * 阅读进度改变
     *
     * @param bookCode    图书id
     * @param currentPage 当前页码
     * @param totalPage   总页码
     */
    @Override
    public void onProgressChanged(String bookCode, int currentPage, int totalPage) {
        if (upView == null) {
            return;
        }
        //首页不显示"上一页"按钮
        if (currentPage == 1) {
            upView.setVisibility(View.GONE);
        } else {
            upView.setVisibility(View.VISIBLE);
        }
        //尾页不显示"下一页"按钮
        /*if (currentPage == totalPage) {
            downView.setVisibility(View.INVISIBLE);
        } else {
            downView.setVisibility(View.VISIBLE);
        }*/
        updatePages(currentPage, totalPage);
    }

    /**
     * 阅读模式改变 {@link #ELLA_READ_MODE_MANUAL} {@link #ELLA_READ_MODE_AUTO}
     *
     * @param readMode 新阅读模式
     */
    @Override
    public void onReaderModeChanged(int readMode) {
        pauseView.setVisibility(readMode == IEllaReaderUse.ELLA_READ_MODE_AUTO ? View.VISIBLE : View.GONE);
        paused = false;
        playView.setVisibility(View.GONE);

        switch (readMode) {
            //阅读模式 手动翻页
            case IEllaReaderUse.ELLA_READ_MODE_MANUAL: {
                upView.setVisibility(View.VISIBLE);
                downView.setVisibility(View.VISIBLE);
                // 修改pagesView的布局
                pagesViewContent.setPadding(ReaderUtils.dip2px(pagesView.getContext(), 16), 0, ReaderUtils.dip2px(pagesView.getContext(), 16), 0);
                break;
            }
            //阅读模式 自动翻页
            case IEllaReaderUse.ELLA_READ_MODE_AUTO: {
//                upView.setVisibility(View.GONE);
//                downView.setVisibility(View.GONE);
                // 修改pagesView的布局
//                pagesViewContent.setPadding(0, 0, 0, 0);
                break;
            }
        }
    }

    private void showDownViewAnimal() {
        downView.setTranslationY(-ReaderUtils.dip2px(downView.getContext(), 40));
        downView.animate().translationY(0).setInterpolator(new BounceInterpolator()).setDuration(1200).start();
    }

//    private void showCheckErrorDialog() {
//        ReaderErrorFragment readerErrorFragment = new ReaderErrorFragment();
//        readerErrorFragment.setCallback(new Callback<Integer>() {
//            @Override
//            public void callback(Integer viewId) {
//                //未启动阅读器的场景，调用此接口退出
//                mEllaReaderControl.exit();
//            }
//        });
//        readerErrorFragment.setEllaReaderControl(mEllaReaderControl);
//        try {
//            ReaderUtils.LogE(TAG, "show error dialog");
//            readerErrorFragment.show(mEllaReaderControl.getReaderActivity().getFragmentManager());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    PopupWindow popupWindow;

    private void initPopWindow(View rootView) {
        View content = View.inflate(rootView.getContext(), R.layout.activity_cover, null);
        popupWindow = new PopupWindow(content, -1, -1);
        content.findViewById(R.id.reader_exit).setOnClickListener(v -> {
            if (isViewerReady) {
                //退出
                mEllaReaderControl.exit();
                popupWindow.dismiss();
            }
        });
        content.findViewById(R.id.reader_reread).setOnClickListener(v -> {
            if (isViewerReady) {
                //重读,返回第一页
                mEllaReaderControl.gotoPage(1);
                ReaderUtils.LogI(TAG, "reader end reRead!");
                popupWindow.dismiss();
            }
        });
    }

    //弹出一个popWindow
    private void showPopWindow(View rootView) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAsDropDown(rootView);
        }
    }

    //弹出一个fragment
    private void showReaderEndFragment() {
        ReaderEndFragment readerEndFragment = new ReaderEndFragment();
        readerEndFragment.setCallback(new Callback<Integer>() {
            @Override
            public void callback(Integer type) {
                if (type == 0/*重新阅读,返回第一页*/) {
                    //重读
                    if (isViewerReady) {
                        mEllaReaderControl.gotoPage(1);
                        ReaderUtils.LogI(TAG, "reader end reRead!");
                    }
                } else if (type == 1/*退出*/) {
                    if (isViewerReady) {
                        //退出
                        mEllaReaderControl.exit();
                        readerEndFragment.dismiss();
                    }
                }
            }
        });
        readerEndFragment.setEllaReaderControl(mEllaReaderControl);
        try {
            ReaderUtils.LogI(TAG, "reader end, show cover fragment");
            readerEndFragment.show(mEllaReaderControl.getReaderActivity().getFragmentManager());
        } catch (Exception e) {
            Log.e("err", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

//    private void showReadHistoryDialog() {
//        if (EllaBookViewer.getCurrentPage() != 1 || lastRecord <= 1) {
//            return;
//        }
//        ReaderHistoryFragment historyFragment = new ReaderHistoryFragment();
//        historyFragment.setCallback(lastRecord, new Callback<Integer>() {
//            @Override
//            public void callback(Integer lastIndex) {
//                if (lastIndex > 1) {
//                    ReaderUtils.LogI(TAG, "history gotoPage index="+lastIndex);
//                    EllaBookViewer.gotoPage(lastIndex);
//                }
//            }
//        });
//        try {
//            ReaderUtils.LogI(TAG, "show read history dialog");
//            historyFragment.show(mEllaReaderControl.getReaderActivity().getFragmentManager());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void showPlayModeDialog() {
        ReaderModeChooseFragment fragment = new ReaderModeChooseFragment();

        fragment.setCallback(new Callback<Integer>() {
            @Override
            public void callback(Integer mode) {
                mEllaReaderControl.setReadMode(mode);
            }
        });
        fragment.setEllaReaderControl(mEllaReaderControl);
        fragment.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mEllaReaderControl.getReadMode() == IEllaReaderUse.ELLA_READ_MODE_AUTO) {
                    paused = false;
                    playView.setVisibility(View.GONE);
                    pauseView.setVisibility(View.VISIBLE);
                    mEllaReaderControl.resume();

                    upView.setVisibility(View.VISIBLE);
                    downView.setVisibility(View.VISIBLE);
                }
            }
        });
        try {
            ReaderUtils.LogI(TAG, "----show Reader Mode Choose Dialog");
            fragment.show(mEllaReaderControl.getReaderActivity().getFragmentManager());
        } catch (Exception e) {
            Log.e("err", e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

//    private boolean showHistoryAlready = false;
//    private void showReadHistory() {
//        Activity activity = mEllaReaderControl.getReaderActivity();
//        if (showHistoryAlready || activity == null || activity.isFinishing() || activity.isDestroyed()) {
//            return;
//        }
//        if(TextUtils.isEmpty(mSecondLessonId) && mIsTry) {
//            //试读模式不展示进度
//            return;
//        }
//
//        AppExecutors.diskIO(new Runnable() {
//            @Override
//            public void run() {
//                lastRecord = getLastRecord();
//                handler.sendEmptyMessage(SHOW_POPUP_HISTORY);
//            }
//        });
//        //只用判断一次就好
//        showHistoryAlready = true;
//    }


    private void updatePages(int currentPage, int bookPages) {
        if (currentPage == 0) {
            return;
        }

        ReaderUtils.LogI(TAG, "page change, currentPage=" + currentPage + ", total=" + bookPages);
        pagesView.setText(String.format(Locale.ROOT, "%d/%d", currentPage, bookPages));

    }

//    private int getLastRecord() {
//        return EllaDb.getInstance(mEllaReaderControl.getReaderActivity()).getLastRecord(EllaReaderApi.getInstance().getUid(), mCourseId);
//    }

//    private void saveRecord(final int currentPage, final int totalPage) {
//        AppExecutors.diskIO(new Runnable() {
//            @Override
//            public void run() {
//                ReadRecord record = new ReadRecord();
//                record.uid = EllaReaderApi.getInstance().getUid();
//                record.bookCode = mCourseId;
//                record.currentPage = currentPage;
//                record.totalPage = totalPage;
//                int progress = record.currentPage * 100 / record.totalPage;
//                record.readProgress = progress + "%";
//                EllaDb.getInstance(mActivity).insertReadInfo(record);
//            }
//        });
//    }

    /**
     * 阅读器 activity onPause
     */
    @Override
    public void onPause() {

    }

    /**
     * 阅读器 activity onResume
     */
    @Override
    public void onResume() {

    }

    /**
     * 阅读器activity onStart
     */
    @Override
    public void onStart() {

    }

    /**
     * 阅读器activity onStop
     */
    @Override
    public void onStop() {

    }

    /**
     * 阅读器activity onDestroy
     * 在此释放资源
     */
    @Override
    public void onExit() {
        handler.removeCallbacksAndMessages(null);
        LogUtils.d("退出完成:" + (System.currentTimeMillis() - exitTime) + "毫秒");

        if (dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
//        popupWindow.dismiss();
//        mEllaReaderControl.exit();
    }

    private boolean isBookEnd;
    private boolean isTryEnd;
    private boolean isViewerReady;

    /**
     * 自动播放模式，播放结束
     */
    @Override
    public void onBookEnd() {
//        showReaderEndFragment();

        isBookEnd = true;
//        checkQuit();
    }

    @Override
    public void onTryEnd() {
        mEllaReaderControl.pause();
        handler.post(() -> dialog.show());

        isTryEnd = true;
//        checkQuit();
    }

    @Override
    public void onViewerReady(boolean b) {
        isViewerReady = b;
    }

    @Override
    public void onSubtitleStart() {

    }

    @Override
    public void onSubtitleSentence(List<Sentence> list) {

    }

    @Override
    public TTSConfig getSubtitleDeaconConfig() {
        return null;
    }

    @Override
    public void onMediaPlay(boolean b) {

    }

    @Override
    public void onSubtitleErr(String s) {

    }

    @Override
    public void onClassModeResult(QuestResult[] questResults) {

    }

    @Override
    public void onClassModeEnd() {

    }

    @Override
    public boolean canExitWithKEYBACK() {
        return false;
    }

    private void checkQuit() {
        //试读结束或者书籍自动播放完成,并且,页面已准备状态,进行退出
        if ((isBookEnd) && isViewerReady) {
            mEllaReaderControl.exit();
        }
    }

    @Override
    public void onSuccess() {
        if (tryModeToFormalMode) {
            ToastUtils.showShort("开始正式阅读图书!");
        }
    }

    @Override
    public void onReaderSuccess() {
    }

    /**
     * 阅读器启动出错
     *
     * @param errorCode 错误码
     * @param msg       错误信息
     */
    @Override
    public void onError(int errorCode, String msg) {
        Log.e(TAG, "阅读器启动失败:errorCode=" + errorCode + " msg=" + msg);

        new AlertDialog.Builder(mSource).setTitle("阅读器启动失败").setMessage(msg)
                .setPositiveButton("确定", (dialog, which) -> {

//                    if (NORMAL_READ) {
//                        EllaReaderApi.getInstance().refreshReader(context, bookCode, genSign(bookCode, FORMAL_READ));
//                    } else {
//                        EllaReaderApi.getInstance().refreshReader(context, bookCode, genSign(bookCode, TRIAL_READ));
//                    }

                    //不为空,说明图书已经打开,需要退出阅读器,释放资源
                    if (mEllaReaderControl != null) {
                        mEllaReaderControl.exit();
                    }

                    /*if (mEllaReaderControl != null && mEllaReaderControl.getReaderActivity() != null) {
                        mEllaReaderControl.exit();
                        mEllaReaderControl.getReaderActivity().finish();
                    }*/

                    dialog.cancel();

                }).create().show();

//        ToastUtils.showShort("阅读器启动失败:errorCode=" + errorCode + " msg=" + msg);
    }
}
