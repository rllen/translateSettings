package link.zhidou.appupdate.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.WindowManager;

import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

import link.zhidou.appupdate.R;
import link.zhidou.appupdate.bean.VersionInfo;
import link.zhidou.appupdate.bean.VersionInfoResponse;
import link.zhidou.appupdate.utils.SysProp;
import link.zhidou.appupdate.utils.app.AppUtils;
import link.zhidou.appupdate.utils.file.FileUtils;
import link.zhidou.appupdate.utils.file.StorageUtils;
import link.zhidou.appupdate.utils.http.DownloadResponse;
import link.zhidou.appupdate.utils.http.IHttpUtils;
import link.zhidou.appupdate.utils.http.OkHttpUtils;
import link.zhidou.appupdate.utils.http.OnRequestListener;
import link.zhidou.appupdate.utils.log.LogUtils;
import link.zhidou.utils.PackageHelper;

/**
 * created by yue.gan 18-7-17
 */
public class AppUpdateService extends Service {
    public static final String ACTION = "link.zhidou.appupdate.AppUpdateService";
    private static final String TAG = "developer";
    private static final String TRANSLATOR_PKG_NAME = "link.zhidou.translator";
    private static final String TRANSLATOR_SPLASH_NAME = "link.zhidou.translator.ui.activity.SplashActivity";
    private static final String META_CHANNEL = "UMENG_CHANNEL";
    private static final String TRF_TRANSLATOR_CHANNEL_PROP = "ro.trf.translator.channel";
    private static final long TASK_INTERVAL = 24 * 60 * 60 * 1000;

    private static final String BASE_URL = "https://translate.zhidou.link/app/";
    private static final String UPDATE_PATH = "version/upgrade";

    private static final class PARAM_KEYS {
        static final String ANDROID_ID = "android_id";
        static final String PKG_NAME = "package_name";
        static final String VERSION_CODE = "version_code";
        static final String CHANNEL = "channel";
        static final String CMD = "cmd";
    }

    public static final class CMD {
        static final int QUERY_NEW_VERSION = 1;
    }

    /**
     * 5.0以后需要设置包名或者ComponentName才能启动服务，否则报错
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, AppUpdateService.class);
    }

    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
        }
    }

    private VersionInfo versionInfo;
    private boolean isDownloading;
    private boolean isInstalling;
    private IHttpUtils iHttpUtils;
    private volatile ServiceHandler mServiceHandler;
    private Handler dialogHandler;
    private boolean cancelDownload = false;
    /* @{Add by lost on 20181019 to resume download after failure */
    private volatile int retryTimes = 20; /**　可以尝试 20 次 */
    /* @} */

    public AppUpdateService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        iHttpUtils = new OkHttpUtils();
        versionInfo = null;
        isDownloading = false;
        isInstalling = false;
        cancelDownload = false;

        HandlerThread thread = new HandlerThread(getClass().getSimpleName());
        thread.start();

        Looper mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        dialogHandler = new Handler();

        //启用日志，在Gradle的BuildType中配置
        LogUtils.enableLog(true);
    }

    private static String getChannel(Context context) {
        String channel = SysProp.get(TRF_TRANSLATOR_CHANNEL_PROP, "");
        if (TextUtils.isEmpty(channel)) {
            channel = AppUtils.getMetaData(context, TRANSLATOR_PKG_NAME, META_CHANNEL, "");
        }
        return channel;
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startAlarmTask();
        if(intent == null) {
            LogUtils.d(TAG, "AppUpdateService onStartCommand intent = null");
            return START_STICKY;
        }

        String channel = getChannel(getContext());
        int versionCode = AppUtils.getVersionCode(getContext(), TRANSLATOR_PKG_NAME);
        String androidId = Build.SERIAL;

        intent.putExtra(PARAM_KEYS.ANDROID_ID, androidId);
        intent.putExtra(PARAM_KEYS.PKG_NAME, TRANSLATOR_PKG_NAME);
        intent.putExtra(PARAM_KEYS.VERSION_CODE, versionCode);
        intent.putExtra(PARAM_KEYS.CHANNEL, channel);
        intent.putExtra(PARAM_KEYS.CMD, CMD.QUERY_NEW_VERSION);

        LogUtils.d(TAG, "AppUpdateService onStartCommand intent = " + intent);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    /**
     * 一定时间后自动执行
     */
    private void startAlarmTask () {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent taskIntent = getIntent(getContext());
            PendingIntent taskPendingIntent = PendingIntent.getService(getContext(), 0, taskIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            if (alarmManager == null) return;
            alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + TASK_INTERVAL, taskPendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getUpdateUrl () {
        return BASE_URL + UPDATE_PATH;
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        int cmd = intent.getIntExtra(PARAM_KEYS.CMD, 0);
        switch (cmd) {
            case CMD.QUERY_NEW_VERSION:
                queryNewVersion(intent);
                break;
        }
    }

    private void queryNewVersion (Intent intent) {
        LogUtils.d(TAG, "AppUpdateService queryNewVersion");
        if (isDownloading || isInstalling) return;
        try {
            String androidId = intent.getStringExtra(PARAM_KEYS.ANDROID_ID);
            String pkgName = intent.getStringExtra(PARAM_KEYS.PKG_NAME);
            String channel = intent.getStringExtra(PARAM_KEYS.CHANNEL);
            int versionCode = intent.getIntExtra(PARAM_KEYS.VERSION_CODE, -1);
            if (TextUtils.isEmpty(androidId) ||
                    TextUtils.isEmpty(pkgName) ||
                    TextUtils.isEmpty(channel) ||
                    versionCode <= 0) return;

            if (versionInfo != null && versionInfo.version_code > versionCode) {
                showTip(versionInfo);
                return;
            }

            HashMap<String, String> params = new HashMap<>(4);
            params.put(PARAM_KEYS.ANDROID_ID, androidId);
            params.put(PARAM_KEYS.PKG_NAME, pkgName);
            params.put(PARAM_KEYS.CHANNEL, channel);
            params.put(PARAM_KEYS.VERSION_CODE, String.valueOf(versionCode));

            String responsStr = iHttpUtils.postStringSync(getUpdateUrl(), params);
            LogUtils.d(TAG, "AppUpdateService queryNewVersion response : " + responsStr);
            if (TextUtils.isEmpty(responsStr)) return;
            VersionInfoResponse versionInfoResponse = (VersionInfoResponse) iHttpUtils
                    .jsonStr2Object(VersionInfoResponse.class, responsStr);
            LogUtils.d(TAG, "AppUpdateService queryNewVersion r : " + versionInfoResponse);
            if (versionInfoResponse.result != 200) return;
            if (versionInfoResponse.data.code != 1) return;
            if (versionInfoResponse.data.data == null) return;
            versionInfo = versionInfoResponse.data.data;
            showTip(versionInfo);
            LogUtils.d(TAG, "AppUpdateService versionInfo : "+versionInfo.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d(TAG, e.toString());
        }
    }

    private Context getContext () {
        return AppUpdateService.this;
    }

    private void showTip (final VersionInfo versionInfo) {
        if (isDownloading || isInstalling) return;
        if (versionInfo.type) {
            downloadAndInstall(versionInfo);
        } else {
            if (tipDialog != null && tipDialog.isShowing()) return;
            tipDialog = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                    .setTitle(R.string.version_found)
                    .setMessage("v" + versionInfo.version + "\n" + versionInfo.descption)
                    .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            downloadAndInstall(versionInfo);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            if (tipDialog.getWindow() != null) {
                tipDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                tipDialog.show();
            }
        }
    }

    private void downloadAndInstall(final VersionInfo versionInfo) {
        LogUtils.d(TAG, "AppUpdateService downloadAndInstall");
        isDownloading = true;
        cancelDownload = false;
        File cache = getStoragePath(versionInfo, 30 * 1024 * 1024);
        if (cache != null) {
            FileUtils.delete(cache, null);
        }
        showDownloadDlg(getString(R.string.ready_to_download));
        /* @{ Give 20 chances */
        retryTimes = 20;
        downloadOneRound(versionInfo);
        /* @} */
    }

    private void downloadOneRound(final VersionInfo versionInfo) {
        retryTimes--;
        File cache = getStoragePath(versionInfo, 30 * 1024 * 1024);
        if (null == cache) {
            return;
        }
        final long start = cache.length(); // 断点续传
        iHttpUtils.postDownloadAsync(versionInfo.url, start, new OnRequestListener() {
            @Override
            public void onResponse(String url, Object responseData) {
                try {
                    DownloadResponse downloadResponse = (DownloadResponse) responseData;
                    long contentLen = downloadResponse.getContentLength();
                    if (contentLen <= 0) contentLen = 30 * 1024 * 1024;
                    File storageFile = getStoragePath(versionInfo, contentLen);
                    final long showTotal = contentLen + start;
                    if (storageFile != null) {
                        File dir = storageFile.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        InputStream in = downloadResponse.getInputStream();
                        RandomAccessFile raf = new RandomAccessFile(storageFile, "rw");
                        raf.seek(start);
                        byte[] buf = new byte[1024];
                        int len;
                        int count = 0;
                        int total = 0;
                        while ((len = in.read(buf)) > 0) {
                            raf.write(buf, 0, len);
                            count ++;
                            total += len;
                            if (cancelDownload) {
                                break;
                            }
                            if (count > 20) {
                                count = 0;
                                long show = total + start;
                                showDownloadDlg(getString(R.string.download_progress, (int)(show * 100f / showTotal)));
                            }
                        }

                        isDownloading = false;
                        closeClosable(in, raf);
                        if (!cancelDownload) {
                            install(storageFile);
                        } else {
                            FileUtils.delete(storageFile, null);
                        }
                    }
                } catch (Exception e) {
                    onError(0, url, e);
                }
            }

            @Override
            public void onRequestError(int errorCode, String url, Exception e) {
                onError(errorCode, url, e);
            }

            @Override
            public void onError(int errorCode, String url, Exception e) {
                e.printStackTrace();
                if (0 == retryTimes) {
                    isDownloading = false;
                    showDownloadDlg(null);
                    showAlertDlg(getString(R.string.download_fail_check_net));
                    LogUtils.d(TAG, "" + e.toString());
                } else {
                    downloadOneRound(versionInfo);
                }
            }
        });
    }

    private void install (final File apkFile) {
        LogUtils.d(TAG, "AppUpdateService install " + apkFile.getPath());
        isInstalling = true;
        showDownloadDlg(null);
        showInstallDlg(getString(R.string.installing));
        try {

            final Uri apkUri = Uri.fromFile(apkFile);
            PackageHelper packageHelper = new PackageHelper(getContext());
            packageHelper.setOnInstalledListener(new PackageHelper.OnInstalledListener() {
                @Override
                public void onInstalled(PackageHelper.ActionResult actionResult) {
                    isInstalling = false;
                    FileUtils.delete(apkFile.getParentFile(), null);
                    if (actionResult.getResult() == InstallResultCode.INSTALL_SUCCEEDED) {
                        AppUtils.startApp(getContext(), TRANSLATOR_PKG_NAME, TRANSLATOR_SPLASH_NAME);
                        showInstallDlg(getString(R.string.install_success));
                    } else if (actionResult.getResult() == InstallResultCode.INSTALL_FAILED_INSUFFICIENT_STORAGE) {
                        showInstallDlg(getString(R.string.install_fail) + " - " + getString(R.string.need_more_space));
                    } else {
                        showInstallDlg(getString(R.string.install_fail) + " - " + actionResult.getResult());
                    }
                    LogUtils.d(TAG, "AppUpdateService install result : " + actionResult.getResult());
                }
            });
            packageHelper.installApp(apkUri);
        } catch (Exception e) {
            isInstalling = false;
            showInstallDlg(getString(R.string.install_fail));
        }
    }

    /**
     * 获取存储位置，空间不足直接弹窗提示
     */
    private File getStoragePath (VersionInfo versionInfo, long spaceNeeded) {
        File targetDir = getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (targetDir == null) {
            showAlertDlg(getString(R.string.sdcard_not_usable));
            return null;
        }
        long spaceFree = StorageUtils.getUsableStorage(targetDir.getPath());
        if (spaceFree < spaceNeeded) {
            showDownloadDlg(null);
            showAlertDlg(getString(R.string.insufficient_space_content,
                    getSizeStr(getContext(), spaceNeeded - spaceFree)));
            return null;
        }
        return new File (targetDir, "Translator_" + versionInfo.version_code + ".apk");
    }

    private void closeClosable(Closeable... closeables) {
        for (Closeable closeable: closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private AlertDialog tipDialog;
    private AlertDialog downlaodDlg;
    private AlertDialog installDlg;
    private void showDownloadDlg (final String msg) {
        dialogHandler.post(new Runnable() {
            @Override
            public void run() {
                if (downlaodDlg == null) {
                    downlaodDlg = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setMessage(msg)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /** @{ Remove all callbacks and messages */
                                    dialogHandler.removeCallbacksAndMessages(null);
                                    /** @} */
                                    cancelDownload = true;
                                    dialog.dismiss();
                                    downlaodDlg = null;
                                }
                            })
                            .create();
                    downlaodDlg.setCanceledOnTouchOutside(false);
                    if (downlaodDlg.getWindow() != null) {
                        downlaodDlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        downlaodDlg.show();
                    }
                } else if (downlaodDlg.isShowing()) {
                    if (TextUtils.isEmpty(msg)) downlaodDlg.dismiss();
                    downlaodDlg.setMessage(msg);
                }
            }
        });
    }

    private void showInstallDlg (final String msg) {
        dialogHandler.post(new Runnable() {
            @Override
            public void run() {
                if (installDlg == null) {
                    installDlg = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                            .setMessage(msg)
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    installDlg = null;
                                }
                            })
                            .create();
                    installDlg.setCanceledOnTouchOutside(false);
                    if (installDlg.getWindow() != null) {
                        installDlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        installDlg.show();
                    }
                } else {
                    if (TextUtils.isEmpty(msg)) installDlg.dismiss();
                    installDlg.setMessage(msg);
                }
            }
        });
    }

    private void showAlertDlg (final String msg) {
        dialogHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dlg = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setMessage(msg)
                        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                dlg.setCanceledOnTouchOutside(false);
                if (dlg.getWindow() != null) {
                    dlg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dlg.show();
                }
            }
        });
    }

    public static String getSizeStr (Context context, long size) {
        if (size > FileUtils.M_SIZE) {
            return context.getString(R.string.size_M, (double)size/FileUtils.M_SIZE);
        } else if (size > FileUtils.K_SIZE) {
            return context.getString(R.string.size_K, (double)size/FileUtils.K_SIZE);
        } else {
            return context.getString(R.string.size_B, (double)size);
        }
    }
}
