package link.zhidou.translator;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import link.zhidou.translator.assist.SysProp;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.language.Language;

/**
 * 文件名：Config
 * 描  述：全局配置类
 * 作  者：jc
 * 时  间：2017/11/6
 * 版  权：
 */
public final class Config {
    private static final String TAG = "Config";
    private static final boolean DEBUG = Log.isLoggable();
    /**
     * 系统语言选择返回的结果码
     */
    public static final int CODE_SET_SEX = 11;
    private static final String TRF_TRANSLATOR_CHANNEL_PROP = "ro.trf.translator.channel";

    /**
     * Keydown
     */
    public static final int KEYCODE_A = 19;
    public static final int KEYCODE_B = 20;
    public static final int KEYCODE_MID = 66;
    public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP;
    public static final int KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
    public static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
    public static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
    public static final int KEYCODE_ENTER = KeyEvent.KEYCODE_ENTER;

    private static class InstanceHolder {
        private static final Config sInstance = new Config();
    }

    private Context mContext = null;

    public static Config from(Context context) {
        if (null == InstanceHolder.sInstance.mContext) {
            synchronized (InstanceHolder.sInstance) {
                if (null == InstanceHolder.sInstance.mContext) {
                    InstanceHolder.sInstance.mContext = context.getApplicationContext();
                }
            }
        }
        return InstanceHolder.sInstance;
    }
    private String mSerial = null;
    private String mChannel = null;
    private String mDeviceId = null;

    /**
     * 方法名：getSerial()
     * 功  能：获取设备
     * 参  数：无
     * 返回值：设备序列号
     */
    public String getSerial() {
        if (TextUtils.isEmpty(mSerial)) {
            mSerial = Build.SERIAL;
        }
        if (DEBUG) {
            Log.d(TAG, "mSerial: " + mSerial);
        }
        return mSerial;
    }

    /**
     * 渠道
     * @return
     */
    public String getChannel() {
        if (TextUtils.isEmpty(mChannel)) {
            mChannel = getSystemChannel();
            if (TextUtils.isEmpty(mChannel)) {
                try {
                    ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                    mChannel = appInfo.metaData.getString("UMENG_CHANNEL");
                } catch (Exception e) {
                    mChannel = "";
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "mChannel: " + mChannel);
        }
        return mChannel;
    }


    /* 1.Check system channel
     * 2.Check AndroidManifest.xml UMENG_CHANNEL config.
     */
    private String getSystemChannel() {
        return SysProp.get(TRF_TRANSLATOR_CHANNEL_PROP, "");
    }

    /**
     * 读取设备ID
     * 读取/sys/block/mmcblk0/device/cid
     * @return 设备id
     */
    private String getDevId(){
        try {
            FileReader fileReader = new FileReader("/sys/block/mmcblk0/device/cid");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String id = bufferedReader.readLine();
            return id;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static boolean isSystemApp(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        boolean flag = false;
        if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            // Updated system app
            flag = true;
        } else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            // Non-system app
            flag = true;
        }
        return flag;
    }

    /**
     * 设备ID
     * @return
     */
    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId)) {
            if (isSystemApp(mContext) || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                mDeviceId = getDevId();
            } else {
                mDeviceId = SysProp.get("persist.sys.identify.id", "");
            }
        }
        if (DEBUG) {
            Log.d(TAG, "mDeviceId: " + mDeviceId);
        }
        return mDeviceId;
    }

    public static class Sex {
        public static final String MALE = "Male";
        public static final String FEMALE = "Female";
        public static final String DEFAULT = FEMALE;
    }

    public static class Speed {
        public static final int FAST = 100;
        public static final int COMMON = 75;
        public static final int BIT_SLOW = 50;
        public static final int SLOW = 25;
        public static final int DEFAULT = COMMON;
    }

    public static class Mode {
        public static final String AUTOMATIC = "Automatic";
        public static final String OPERATION = "Operation";
        public static final String DEFAULT = AUTOMATIC;
    }

    public static String labelFromValue(Context context, int labelId, int valueId, String value) {
        String[] labels = context.getResources().getStringArray(labelId);
        String[] values = context.getResources().getStringArray(valueId);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                return labels[i];
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String getDisplayName(
            Locale l, String[] specialLocaleCodes, String[] specialLocaleNames) {
        String code = l.toString();

        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }

        return l.getDisplayLanguage(l);
    }

    public static String getLanguageName(Context context, Language language) {
        final int index = language.ordinal();
        final String[] names = context.getResources().getStringArray(R.array.language_name);
        if (index >= 0 && index < names.length) {
            return names[index];
        } else {
            return null;
        }
    }
}

