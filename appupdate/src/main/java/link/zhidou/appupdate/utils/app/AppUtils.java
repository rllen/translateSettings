package link.zhidou.appupdate.utils.app;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by ganyu on 2016/10/10.
 *
 * <p>1、获取版本号、版本名、包名、meta data</p>
 * <p>2、检查指定应用是否存在，启动其他应用</p>
 * <p></p>
 */
public class AppUtils {


    public static String getProcessName (Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int pid = Process.myPid();
        List<ActivityManager.RunningAppProcessInfo> infos = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info: infos) {
            if (pid == info.pid) {
                return info.processName;
            }
        }
        return "";
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);
        if (!taskInfos.isEmpty()) {
            ComponentName topActivity = taskInfos.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static int getVersionCode(Context context, String pkgName) {
        int versionCode = 0;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(pkgName, 0);
            versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getPackageName(Context context) {
        try {
            return context.getPackageName();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMetaData (Context context, String key, String defaultVal) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().
                    getApplicationInfo(getPackageName(context), PackageManager.GET_META_DATA);
            String data = applicationInfo.metaData.getString(key);
            if (TextUtils.isEmpty(data)) return defaultVal;
            return data;
        } catch (Exception e) {
        }
        return defaultVal;
    }

    public static String getMetaData (Context context, String pkgName, String key, String defaultVal) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().
                    getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
            String data = applicationInfo.metaData.getString(key);
            if (TextUtils.isEmpty(data)) return defaultVal;
            return data;
        } catch (Exception e) {
        }
        return defaultVal;
    }

    public static boolean isApkExist (Context context, String packageName){
        if (TextUtils.isEmpty(packageName)) return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isApkExist (Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, 0);
        return list.size() > 0;
    }

    public static String getTopActivity(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
        ComponentName componentInfo = taskInfo.get(0).topActivity;

        if (componentInfo != null) {

            return componentInfo.getClassName();
        } else {
            return null;
        }

    }

    public static void startApp (Context context, String pkgName, String clsName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        ComponentName componentName = new ComponentName(pkgName, clsName);
        intent.setComponent(componentName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
