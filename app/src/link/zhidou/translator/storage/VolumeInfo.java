package link.zhidou.translator.storage;

import android.os.Build;

import java.io.File;

import link.zhidou.translator.assist.MethodUtils;

/**
 * Date: 18-8-10
 * Time: 下午7:18
 * Email: lostsearover@gmail.com
 */
public class VolumeInfo {

    private static final String GET_TYPE = "getType";
    private static final String GET_PATH = "getPath";
    private static final String LOW_GET_PATH = "getPathFile";
    private static final String GET_ID = "getId";
    private static final String LOW_GET_ID = "getStorageId";
    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_EMULATED = 2;
    public static final int TYPE_ASEC = 3;
    public static final int TYPE_OBB = 4;
    private static final String VOLUME_INFO_CLASS = "android.os.storage.VolumeInfo";
    private static final String IS_MOUNTED_READABLE = "isMountedReadable";
    private static final String IS_REMOVABLE = "isRemovable";
    private static final String GET_STATE = "getState";
    private Object mEntity;

    public VolumeInfo(Object entity) {
        mEntity = entity;
    }

    public Object getEntity() {
        return mEntity;
    }

    public int getType() {
        try {
            return (int) MethodUtils.invokeMethod(mEntity, GET_TYPE);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return -1;
    }

    public boolean isMountedReadable() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (boolean) MethodUtils.invokeMethod(mEntity, IS_MOUNTED_READABLE);
            } else {
                String state = (String) MethodUtils.invokeMethod(mEntity, GET_STATE);
                return "mounted".equals(state);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    public boolean isRemovable() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return TYPE_PUBLIC == getType();
            } else {
                return (boolean) MethodUtils.invokeMethod(mEntity, IS_REMOVABLE);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    private File getPath() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (File) MethodUtils.invokeMethod(mEntity, GET_PATH);
            } else {
                return (File) MethodUtils.invokeMethod(mEntity, LOW_GET_PATH);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public String getAbsolutePath() {
        File file = getPath();
        return null == file ? "" : file.getAbsolutePath();
    }

    public long getTotalSpace() {
        File file = getPath();
        return null == file ? -1 : file.getTotalSpace();
    }

    public long getFreeSpace() {
        File file = getPath();
        return null == file ? -1 : file.getFreeSpace();
    }

    public long getUsedSpace() {
        return getTotalSpace() - getFreeSpace();
    }

    public String getId() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (String) MethodUtils.invokeMethod(mEntity, GET_ID);
            } else {
                return String.valueOf(MethodUtils.invokeMethod(mEntity, LOW_GET_ID));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public boolean isPrivate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return TYPE_PRIVATE == getType();
        } else {
            return !isRemovable();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Volume: ");
        sb.append(getId()).append(",");
        sb.append(getPath()).append(",");
        sb.append(getTotalSpace()).append(",");
        sb.append(getFreeSpace()).append(",");
        sb.append(getUsedSpace()).append(",");
        sb.append(isMountedReadable());
        return sb.toString();
    }
}

