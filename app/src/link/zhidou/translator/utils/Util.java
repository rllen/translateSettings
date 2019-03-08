package link.zhidou.translator.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import link.zhidou.translator.assist.MethodUtils;

/**
 * Created by jc on 18-6-2.
 */

public class Util {

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            boolean supported = (boolean) MethodUtils.invokeMethod(cm, "isNetworkSupported", ConnectivityManager.TYPE_MOBILE);
            return !supported;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Throwable getRootCause(Throwable t) {
        Throwable root = t.getCause();
        if (null == root) {
            return t;
        }
        return getRootCause(root);
    }
}
