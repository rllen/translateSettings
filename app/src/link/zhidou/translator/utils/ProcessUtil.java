package link.zhidou.translator.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Date: 18-5-2
 * Time: 下午6:55
 * Email: lostsearover@gmail.com
 */
public class ProcessUtil {

    private static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    public static boolean isHostProcess(Context context) {
        return context.getPackageName().equals(getCurProcessName(context));
    }
}
