package link.zhidou.appupdate.utils.file;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.File;

/**
 * created by yue.gan 18-7-11
 */
public class StorageUtils {

    private static boolean isExternalStorageUsable () {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            //可读可写
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //只读
            return false;
        }
        return false;
    }

    public static long getUsableStorage (String path) {
        try {
            if (TextUtils.isEmpty(path)) return -1;
            File file = new File(path);
            if (!file.exists()) {
                file = file.getParentFile();
                file.mkdirs();
            }
            StatFs statFs = new StatFs(file.getAbsolutePath());
            long bolckSize = statFs.getBlockSizeLong();
            long avaiBlocks = statFs.getAvailableBlocksLong();
            return avaiBlocks * bolckSize;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static long getTotalStorage (String path) {
        try {
            if (TextUtils.isEmpty(path)) return -1;
            File file = new File(path);
            if (!file.exists()) {
                file = file.getParentFile();
                file.mkdirs();
            }
            StatFs statFs = new StatFs(file.getAbsolutePath());
            long bolckSize = statFs.getBlockSizeLong();
            long avaiBlocks = statFs.getBlockCountLong();
            return avaiBlocks * bolckSize;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取外部存储可用空间大小
     */
    public static long getExternalUsableStorage () {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs statFs = new StatFs(sdcardDir.getPath());
            long bolckSize = statFs.getBlockSizeLong();
            long avaiBlocks = statFs.getAvailableBlocksLong();
            return avaiBlocks * bolckSize;
        }
        return -1;
    }

    /**
     * 获取/data下可用空间大小
     */
    public static long getSystemUsableStorage () {
        File root = Environment.getDataDirectory();
        StatFs statFs = new StatFs(root.getPath());
        long bolckSize = statFs.getBlockSizeLong();
        long avaiBlocks = statFs.getAvailableBlocksLong();
        return avaiBlocks * bolckSize;
    }

    /**
     * app在sdcard上文件存储目录, type为null则返回sdcard上android/data/xxx/file的目录
     */
    public static File getExternalFileDir (Context context, String type) {
        if (!isExternalStorageUsable()) {
            return null;
        }
        return Environment.getExternalStorageDirectory();//context.getExternalFilesDir(type);
    }

    /**
     * app的文件存储internal目录
     */
    public static File getInternalFileDir(Context context) {
        return context.getFilesDir();
    }
}
