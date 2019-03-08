package link.zhidou.translator.storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.text.TextUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import link.zhidou.translator.R;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SPKeyContent;
import link.zhidou.translator.utils.SPUtil;
import link.zhidou.translator.utils.SingleToast;

public class PathProvider extends BroadcastReceiver {
    private static final String TAG = "PathProvider";
    private static final boolean DEBUG = Log.isLoggable();
    public static final SimpleDateFormat sDetailedFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyyMMdd");
    public static final String DEFAULT_TRANSLATION_DIR = Environment.getExternalStorageDirectory() + "/Translation/";
    public static final String DEFAULT_RECORDING_DIR = Environment.getExternalStorageDirectory() + "/Recording/";
    private String mTranslationBaseDir = DEFAULT_TRANSLATION_DIR;
    private String mRecordingBaseDir = DEFAULT_RECORDING_DIR;
    private StorageManagerProxy mProxy = null;
    private Context mContext;
    private String mVolumeId = null;
    private String mVolumePath = null;
    private VolumeInfo mLastVolume = null;
    private VolumeChangedListener mListener;
    public interface VolumeChangedListener {
        void onChanged();
    }

    private void showDefaultVolumeChanged(VolumeInfo volume) {
        if (null == mLastVolume) {
            mLastVolume = volume;
            return;
        }
        if (mLastVolume != null && !volume.getId().equals(mLastVolume.getId()) && !volume.getAbsolutePath().equals(mLastVolume.getAbsolutePath())) {
            SingleToast.show(mContext.getString(R.string.select_memory, mProxy.getBestVolumeDescription(volume)));
            mLastVolume = volume;
        }
    }

    public void register(VolumeChangedListener listener) {
        mListener = listener;
    }

    public void unregister() {
        mListener = null;
    }

    private static class InstanceHolder {
        static PathProvider sInstance = new PathProvider();
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mProxy = new StorageManagerProxy(mContext);
        updateBaseDir();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为 USB大容量存储被共享，挂载被解除
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
        intentFilter.addDataScheme("file");
        mContext.registerReceiver(this, intentFilter);
    }

    private synchronized void updateBaseDir() {
        List<VolumeInfo> volumes = mProxy.getVolumes();
        if (DEBUG) {
            for (VolumeInfo volumeInfo : volumes) {
                Log.d(TAG, volumeInfo.getEntity().toString());
            }
        }
        String volumeId = SPUtil.getString(mContext, SPKeyContent.DEFAULT_VOLUME_ID, "");
        String volumePath = SPUtil.getString(mContext, SPKeyContent.DEFAULT_VOLUME_PATH, "");
        Log.i(TAG, "Preferred volumeId: " + volumeId + ", preferred volumePath: " + volumePath);
        if (!TextUtils.isEmpty(volumeId) && !TextUtils.isEmpty(volumePath)) {
            for (VolumeInfo volumeInfo : volumes) {
                if (volumeId.equals(volumeInfo.getId()) && volumeInfo.isMountedReadable()) {
                    if (volumeInfo.isPrivate()) {
                        mVolumeId = volumeId;
                        mVolumePath = volumePath;
                        mTranslationBaseDir = DEFAULT_TRANSLATION_DIR;
                        mRecordingBaseDir = DEFAULT_RECORDING_DIR;
                    } else if (volumePath.equals(volumeInfo.getAbsolutePath())){
                        mTranslationBaseDir = volumePath + "/Translation/";
                        mRecordingBaseDir = volumePath + "/Recording/";
                        mVolumeId = volumeId;
                        mVolumePath = volumePath;
                    }
                    showDefaultVolumeChanged(volumeInfo);
                    Log.i(TAG, "mVolumeId: " + mVolumeId + ", mVolumePath: " + mVolumePath + ", mTranslationBaseDir: " + mTranslationBaseDir + ", mRecordingBaseDir: " + mRecordingBaseDir);
                    return;
                }
            }
        }
        for (VolumeInfo volumeInfo : volumes) {
            if (volumeInfo.isPrivate()) {
                mVolumeId = volumeInfo.getId();
                mVolumePath = Environment.getExternalStorageDirectory().getPath();
                mTranslationBaseDir = DEFAULT_TRANSLATION_DIR;
                mRecordingBaseDir = DEFAULT_RECORDING_DIR;
                showDefaultVolumeChanged(volumeInfo);
                break;
            }
        }
        Log.i(TAG, "mVolumeId: " + mVolumeId + ", mVolumePath: " + mVolumePath + ", mTranslationBaseDir: " + mTranslationBaseDir + ", mRecordingBaseDir: " + mRecordingBaseDir);
    }

    public void setDefaultVolume(String volumeId, String volumePath) {
        SPUtil.putString(mContext, SPKeyContent.DEFAULT_VOLUME_ID, volumeId);
        SPUtil.putString(mContext, SPKeyContent.DEFAULT_VOLUME_PATH, volumePath);
        updateBaseDir();
    }

    public String getPreVolumeId() {
        return SPUtil.getString(mContext, SPKeyContent.DEFAULT_VOLUME_ID, "");
    }

    public String getPreVolumePath() {
        return SPUtil.getString(mContext, SPKeyContent.DEFAULT_VOLUME_PATH, "");
    }

    public String getDefaultVolumeId() {
        return mVolumeId;
    }

    public String getDefaultVolumePath() {
        return mVolumePath;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Log.d(TAG, "Action: " + intent.getAction() + ", extra: " + intent.getScheme());
        }
        updateBaseDir();
        if (mListener != null) {
            mListener.onChanged();
        }
    }

    public static PathProvider get() {
        return InstanceHolder.sInstance;
    }

    public String getTranslationDir() {
        return mTranslationBaseDir;
    }

    public String getRecordingDir() {
        return mRecordingBaseDir;
    }

    public static String generateFileName(boolean simple, String middle, String suffix) {
        StringBuilder sb = new StringBuilder();
        sb.append(simple ? sFormat.format(new Date()) : sDetailedFormat.format(new Date()));
        sb.append("_");
        sb.append(middle);
        sb.append(suffix);
        return sb.toString();
    }
}
