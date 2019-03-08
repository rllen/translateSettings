package link.zhidou.translator.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * 文件名：PermissionsChecker
 * 描  述：检查权限的工具类
 * 作  者：keetom
 * 时  间：2017/9/1
 * 版  权：
 */
public class PermissionsChecker {
    private final Context mContext;

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 方法名：lacksPermissions(String... permissions)
     * 功  能：判断权限集合
     * 参  数：String... permissions
     * 返回值：缺少权限返回true 否则返回false
     */
    public boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 方法名：lacksPermission(String permission)
     * 功  能：判断是否缺少权限
     * 参  数：String permission
     * 返回值：缺少权限返回true 否则返回false
     */
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}