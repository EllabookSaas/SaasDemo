package com.ellabook.saasdemo;

import static com.ellabook.saasdemo.SignBean.ReadTypeMode.FORMAL_READ;
import static com.ellabook.saasdemo.SignBean.ReadTypeMode.TRIAL_READ;
import static com.ellabook.saassdk.IDecompressionListener.ERROR_NOT_EXIST;
import static com.ellabook.saassdk.IEllaReaderUse.ELLA_READ_MODE_MANUAL;
import static com.ellabook.saassdk.annotation.DownloadZipModeConstants.ALL_ZIP;
import static com.ellabook.saassdk.annotation.DownloadZipModeConstants.SUB_ZIP;
import static com.ellabook.saassdk.annotation.LinkModeConstants.LinkMode_Api;
import static com.ellabook.saassdk.annotation.LinkModeConstants.LinkMode_Out;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.ellabook.saasdemo.ella.ReaderUseImpl;
import com.ellabook.saassdk.EllaReaderApi;
import com.ellabook.saassdk.IDecompressionListener;
import com.ellabook.saassdk.IDownloadListener;
import com.ellabook.saassdk.IRequestListener;
import com.ellabook.saassdk.data.BookInfo;
import com.google.gson.Gson;

import java.io.File;
import java.util.UUID;


public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "Main";

    private String chooseBook;
    private Handler mHandler = new Handler();

    //todo APP_KEY需要到智云平台申请
    private final static String APP_KEY = "kx0r5h5ekdnwxcei";

    //todo APP_SECRET需要到智云平台申请
    private final static String APP_SECRET = "788f00d9bb961ef6fe82cceda1c6f135";

    private byte downloadMode = ALL_ZIP;

    private String readMode = FORMAL_READ;

    ProgressDialog requestDialog;
    ProgressDialog downloadDialog;
    ProgressDialog readDialog;
    ProgressDialog decompressDialog;

    private TextView tvProgress;
    private EditText startPage;
    private Button delete;

    public ProgressDialog getDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        //屏蔽后续操作,只有网络请求成功或失败后才可进行下一步操作
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    IDownloadListener downloadListener = new IDownloadListener() {
        @Override
        public void onStart(String bookCode) {
            tvProgress.setText("图书 " + bookCode + " 开始下载");
        }

        @Override
        public void onFinish(String bookCode) {
            tvProgress.setText("图书 " + bookCode + " 下载完成");
            downloadDialog.dismiss();
            findViewById(R.id.btDownload).setEnabled(true);
            delete.setEnabled(true);

            LogUtils.d("自动打开图书,bookCode=" + bookCode);
            openBook(bookCode);
        }

        @Override
        public void onProgress(String bookCode, float progress) {
            downloadDialog.dismiss();
            tvProgress.setText("图书 " + bookCode + " 下载" + progress);
        }

        @Override
        public void onError(String bookCode, int errorCode, String msg) {
            downloadDialog.dismiss();
            findViewById(R.id.btDownload).setEnabled(true);
            tvProgress.setText("图书 " + bookCode + " 下载失败， errorCode=" + errorCode + ", msg=" + msg);
            delete.setEnabled(true);
        }
    };

//    private EditText customBookCode;
//    private RadioGroup chooseBookGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkNoActivity();

        checkBackgroundLimitType();

        requestDialog = getDialog();
        downloadDialog = getDialog();
        readDialog = getDialog();
        decompressDialog = getDialog();

        setContentView(R.layout.activity_main2);

        initData();
        initListener();

//        chooseBookGroup = findViewById(R.id.rgBook);
//        customBookCode = findViewById(R.id.customBookCode);

        TextView tvDeviceId = findViewById(R.id.tvDeviceId);
        tvDeviceId.setText("设备id:" + EllaReaderApi.getInstance().getDeviceId(this));

        startPage = findViewById(R.id.startPage);
        tvProgress = findViewById(R.id.tvProgress);
        tvProgress.setText(EllaReaderApi.getInstance().checkIsDownloaded(chooseBook(), downloadMode) ? "已下载" : "未下载");
    }

    private void initData() {
        EllaReaderApi.getInstance().setDownloadZipMode(ALL_ZIP);

        Intent intent = getIntent();

        chooseBook = intent.getStringExtra("bookCode");

        this.<TextView>findViewById(R.id.book_name).setText(intent.getStringExtra("bookName"));
        this.<TextView>findViewById(R.id.book_code).setText(chooseBook);

//        bookMap.put(R.id.rbBook1, "B201801190346");
//        bookMap.put(R.id.rbBook2, "B201912300722");
//        bookMap.put(R.id.rbBook3, "B201801190025");
//
//        this.<RadioButton>findViewById(R.id.rbBook1).setText("B201801190346");
//        this.<RadioButton>findViewById(R.id.rbBook2).setText("B201912300722");
//        this.<RadioButton>findViewById(R.id.rbBook3).setText("B201801190025");
    }

    private void initListener() {
        findViewById(R.id.btDownload).setOnClickListener(this);
        findViewById(R.id.btPauseDownload).setOnClickListener(this);
        findViewById(R.id.btOpenBook).setOnClickListener(this);
        delete = findViewById(R.id.btDelete);
        delete.setOnClickListener(this);

        findViewById(R.id.btDecompress).setOnClickListener(this);

        this.<RadioButton>findViewById(R.id.zipModeAll).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                downloadMode = ALL_ZIP;
                EllaReaderApi.getInstance().setDownloadZipMode(downloadMode);

                tvProgress.setText(EllaReaderApi.getInstance().checkIsDownloaded(chooseBook(), downloadMode) ? "已下载" : "未下载");
            }
        });
        this.<RadioButton>findViewById(R.id.zipModeSub).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                downloadMode = SUB_ZIP;
                EllaReaderApi.getInstance().setDownloadZipMode(downloadMode);

                tvProgress.setText(EllaReaderApi.getInstance().checkIsDownloaded(chooseBook(), downloadMode) ? "已下载" : "未下载");
            }
        });

        this.<RadioButton>findViewById(R.id.readModeFormol).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                readMode = FORMAL_READ;
            }
        });
        this.<RadioButton>findViewById(R.id.readModeTry).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                readMode = TRIAL_READ;
            }
        });
    }

    public void checkNoActivity() {
        int alwaysFinish = Settings.Global.getInt(getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
        if (alwaysFinish == 1) {
            Dialog dialog;
            dialog = new AlertDialog.Builder(this)
                    .setMessage(
                            "由于您已开启'不保留活动',可能会导致图书无法正常阅读.我们建议您点击左下方'设置'按钮,在'开发者选项'中关闭'不保留活动'功能.")
                    .setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss()).setPositiveButton("设置", (dialog12, which) -> {
                        startSettingActivity();
                    }).create();
            dialog.show();
        }
    }

    @SuppressLint("PrivateApi")
    public void checkBackgroundLimitType() {
        try {
            Class<?> cls = Class.forName("android.app.ActivityManagerNative");
            Object invoke = cls.getMethod("getDefault", new Class[0]).invoke(cls, new Object[0]);
            if (((Integer) invoke.getClass().getMethod("getProcessLimit", new Class[0]).invoke(invoke, new Object[0])).intValue() == 0) {
                Dialog dialog;
                dialog = new AlertDialog.Builder(this)
                        .setMessage(
                                "后台运行受限,可能会导致图书无法正常阅读.我们建议您点击左下方'设置'按钮,在'开发者选项'中更改后台运行限制")
                        .setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss()).setPositiveButton("设置", (dialog12, which) -> {
                            startSettingActivity();
                        }).create();
                dialog.show();
            }
        } catch (Exception e2) {
        }
    }

    public void startSettingActivity() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        startActivity(intent);
    }

    //获取签名方法
    public static String genSign(String bookCode, @SignBean.ReadTypeMode.ReadType String readType) {
        String appKey = APP_KEY;
        String appSecret = APP_SECRET;

        SignBean signBean = new SignBean(bookCode, appKey, UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis()), readType);
        signBean.genSign(appSecret);
        //        String requestStr = GsonUtils.toJson(signBean);//序列化时,null值字段加入
        String requestStr = new Gson().toJson(signBean);//序列化时,null值字段不加入
        return requestStr;

//        String uuid = UUID.randomUUID().toString();
//        long timeStamp = System.currentTimeMillis();
//        @SuppressLint("DefaultLocale") String requestStr = String.format("appKey=%s&bookCode=%s&noncestr=%s&readType=%s&timestamp=%d&key=%s", appKey, bookCode, uuid, readType, timeStamp, appSecret);
//        Log.i("tag", "requestStr=" + requestStr);
//        String sign = MD5Utils.getStrMD5(requestStr);
//        Log.i("tag", "sign=" + sign);
//        String sign2 = String.format("{'bookCode':'%s','sign':'%s','readType':'%s','appKey':'%s','noncestr':'%s','timestamp':'%d'}", bookCode, sign, readType, appKey, uuid, timeStamp);
//        Log.i("tag", "sign2=" + sign2);
//        return sign2;
    }

    void downloadBook(String bookCode) {
        findViewById(R.id.btDownload).setEnabled(false);

        if (EllaReaderApi.getInstance().getLinkMode() == LinkMode_Out) {
            // 海外版
            EllaReaderApi.getInstance().requestOrder(this, bookCode, genSign(bookCode, readMode), new IRequestListener() {
                @Override
                public void onSuccess(String s) {
                    EllaReaderApi.getInstance().startDownload(MainActivity.this, bookCode, genSign(bookCode, readMode), downloadListener);
                }

                @Override
                public void onError(String bookCode, String errorCode, String msg) {
                    ToastUtils.showShort(msg);
                    downloadDialog.dismiss();
                }
            });
        } else {
            EllaReaderApi.getInstance().startDownload(this, bookCode, genSign(bookCode, readMode), downloadListener);
        }
    }

    private void pauseDownload() {
        EllaReaderApi.getInstance().stopDownload();
    }

    void openBook(String bookCode) {
        int page;
        try {
            page = TextUtils.isEmpty(startPage.getText().toString()) ? 1 : Integer.parseInt(startPage.getText().toString());
        } catch (Exception e) {
            page = 1;
            ToastUtils.showShort(e + "\n" + "启动第1页");
        }
        ToastUtils.showShort("启动第" + page + "页");
        BookInfo bookInfo = new BookInfo(bookCode, genSign(bookCode, readMode), new ReaderUseImpl(this, bookCode, FORMAL_READ.equals(readMode)) {

            @Override
            public void onSuccess() {
                super.onSuccess();
                readDialog.dismiss();
            }

            @Override
            public void onError(int errorCode, String msg) {
                super.onError(errorCode, msg);
                readDialog.dismiss();
                delete.setEnabled(true);
            }

            @Override
            public void onExit() {
                super.onExit();
                delete.setEnabled(true);
            }
        });
        EllaReaderApi.getInstance().startReader(this, bookInfo);
    }

    //删除文件夹和文件夹里面的文件
    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return false;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDir(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
        return true;
    }

    void deleteBook(String bookCode) {
        LogUtils.d("删除图书,bookCode=" + bookCode);
        File rootFile = new File(getExternalFilesDir(null), "ellabook");
        File bookZip = new File(rootFile, bookCode + ".zip");
        if (bookZip.exists()) {
            boolean deleteZip = bookZip.delete();
            if (deleteZip) {
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        File bookFile = new File(rootFile, bookCode);
        if (bookFile.exists() && bookFile.isDirectory()) {
            boolean isSuccess = deleteDir(bookFile);
            if (isSuccess) {
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "图书资源不存在", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btDownload:
                downloadDialog.show();
                downloadBook(chooseBook());
                delete.setEnabled(false);
                break;
            case R.id.btOpenBook:
                readDialog.show();
                LogUtils.d("手动打开图书,bookCode=" + chooseBook());
                openBook(chooseBook());
                delete.setEnabled(false);
//                overridePendingTransition(0,0);
                break;
            case R.id.btDelete:
//                finish();
                deleteBook(chooseBook());
                break;
            case R.id.btPauseDownload:
                pauseDownload();
                break;
            case R.id.btDecompress:

                Log.d("TAG", "开始解压");
                decompressDialog.show();

                decompress(chooseBook(), new IDecompressionListener() {
                    @Override
                    public void onSuccess(String code, String s, File file) {
                        tvProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress.setText("图书 " + code + "解压缩成功");
                            }
                        });
                        Log.d("TAG", "解压成功");
                        decompressDialog.dismiss();
                    }

                    @Override
                    public void onFail(String code, String s, int i) {
                        tvProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                tvProgress.setText("图书 " + code + "解压失败:" + s);
                            }
                        });
                        Log.e("TAG", "解压失败:" + s);
                        decompressDialog.dismiss();
                    }
                });
                break;
            default:
                break;
        }
    }

    /**
     * 测试解压缩
     */
    private void decompress() {
        File rootPath = new File(getExternalFilesDir(null), "ellabook");
        File src = new File(rootPath, chooseBook() + ".zip");
        Log.i(TAG, "compress src filePath=" + src.getName());
        EllaReaderApi.getInstance().decompressionEllaBook(MainActivity.this, src.getPath(), chooseBook(),
                new IDecompressionListener() {
                    @Override
                    public void onSuccess(String code, String srcFile, File compressDir) {
                        Log.i(TAG, "compress success, file=" + compressDir.getPath());
                        showToast("解压缩成功，路径：" + compressDir.getPath());
                    }

                    @Override
                    public void onFail(String code, String srcFile, int errorCode) {
                        Log.i(TAG, "compress fail, errorCode=" + errorCode);
                        showToast("解压缩失败 errorCode=" + errorCode);
                    }
                });
    }

    private void decompress(String bookName, IDecompressionListener iDecompressionListener) {
        File rootPath = new File(getExternalFilesDir(null), "ellabook");
        File src = new File(rootPath, bookName + ".zip");
        if (src.exists()) {
            Log.i(TAG, "compress src filePath=" + src.getName());
            EllaReaderApi.getInstance().decompressionEllaBook(MainActivity.this, src.getPath(), bookName, iDecompressionListener);
        } else {
            if (EllaReaderApi.getInstance().checkIsDownloaded(bookName, ALL_ZIP)) {
                iDecompressionListener.onSuccess(bookName, src.getPath(), src);
            } else {
                iDecompressionListener.onFail(bookName, src.getPath() + "不存在", ERROR_NOT_EXIST);
            }
        }
    }

    private void showToast(String toastStr) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toastStr, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String chooseBook() {
//        if (chooseBookGroup.getCheckedRadioButtonId() == R.id.rbBook4) {
//            return customBookCode.getText() != null ?
//                    customBookCode.getText().toString() != null ?
//                            customBookCode.getText().toString() : "" : "";
//        }
//        return bookMap.get(chooseBookGroup.getCheckedRadioButtonId());
        return chooseBook;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EllaReaderApi.getInstance().stopDownload();
    }
}
