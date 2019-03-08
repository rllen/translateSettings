package link.zhidou.translator.storage;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.assist.MethodUtils;

/**
 * Date: 18-8-10
 * Time: 下午5:36
 * Email: lostsearover@gmail.com
 */
public class StorageManagerProxy {

    private static final String GET_PRIMARY_STORAGE_SIZE = "getPrimaryStorageSize";
    private static final String GET_VOLUMES = "getVolumes";
    private static final String GET_VOLUME_LIST  = "getVolumeList";
    private static final String GET_DESCRIPTION_COMPARATOR = "getDescriptionComparator";
    private static final String GET_BEST_VOLUME_DESCRIPTION = "getBestVolumeDescription";
    private static final String GET_DESCRIPTION = "getDescription";
    private static final String VOLUME_INFO_CLASS = "android.os.storage.VolumeInfo";
    private static final String KK_VOLUME_INFO_CLASS = "android.os.storage.StorageVolume"; // Kitkat
    private StorageManager mStorageManager;
    private Context mContext;

    public StorageManagerProxy(Context context) {
        mContext = context.getApplicationContext();
        mStorageManager = (StorageManager) context.getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
    }

    public long getPrimaryStorageSize () {
        try {
            return (long) MethodUtils.invokeMethod(mStorageManager, GET_PRIMARY_STORAGE_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<VolumeInfo> getVolumes() {
        try {
            List<Object> entities = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             entities =  (List) MethodUtils.invokeMethod(mStorageManager, GET_VOLUMES);
            } else {
                Object[] temps = (Object[]) MethodUtils.invokeMethod(mStorageManager, GET_VOLUME_LIST);
                for (Object obj : temps) {
                    entities.add(obj);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Collections.sort(entities, (Comparator) MethodUtils.invokeStaticMethod(Class.forName(VOLUME_INFO_CLASS), GET_DESCRIPTION_COMPARATOR));
            }
            List<VolumeInfo> volumes = new ArrayList<>();
            for (Object object : entities) {
                VolumeInfo volumeInfo = new VolumeInfo(object);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (volumeInfo.getType() == VolumeInfo.TYPE_PRIVATE || volumeInfo.getType() == VolumeInfo.TYPE_PUBLIC) {
                        if (volumeInfo.isMountedReadable()) {
                            volumes.add(volumeInfo);
                        }
                    }
                } else if (volumeInfo.isMountedReadable()){
                    volumes.add(volumeInfo);
                }
            }
            return volumes;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public String getBestVolumeDescription(VolumeInfo volumeInfo) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (String) MethodUtils.invokeMethod(mStorageManager, GET_BEST_VOLUME_DESCRIPTION, new Object[]{volumeInfo.getEntity()});
            } else {
                if (volumeInfo.isPrivate()) {
                    return mContext.getString(R.string.root_internal_storage);
                } else {
                    return (String) MethodUtils.invokeMethod(volumeInfo.getEntity(), GET_DESCRIPTION, new Object[]{mContext});
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }
}
