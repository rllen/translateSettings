package link.zhidou.translator.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import link.zhidou.translator.R;

/**
 * 文件名：PermissionsActivity
 * 描  述：权限获取页面
 * 作  者：keetom
 * 时  间：2017/9/1
 * 版  权：
 */
public class PermissionsActivity extends Activity {
    public static final int PERMISSIONS_GRANTED = 0; // 权限授权
    public static final int PERMISSIONS_DENIED = 1;  // 权限拒绝
    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String EXTRA_PERMISSIONS = "permission.extra_permission"; // 权限参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    private PermissionsChecker mChecker; // 权限检测器
    private boolean isRequireCheck; // 是否需要系统权限检测

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }
        mChecker = new PermissionsChecker(this);
        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (mChecker.lacksPermissions(permissions)) {
                requestPermissions(permissions); // 请求权限
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }

    /**
     * 方法名：startActivityForResult
     * 功  能：启动当前权限页面的公开接口
     * 参  数：Activity activity
     * 参  数：int requestCode
     * 参  数：String... permissions
     * 返回值：无
     */
    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    /**
     * 方法名：getPermissions
     * 功  能：返回传递的权限参数
     * 参  数：无
     * 返回值：String[] 权限数组
     */
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    /**
     * 方法名：requestPermissions
     * 功  能：请求权限兼容低版本
     * 参  数：String... permissions   权限数组
     * 返回值：无
     */
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    /**
     * 方法名：allPermissionsGranted
     * 功  能：全部权限均已获取
     * 参  数：无
     * 返回值：无
     */
    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
        finish();
    }

    /**
     * 方法名：onRequestPermissionsResult
     * 功  能：用户权限处理 1、如果全部获取, 则直接过。2、如果权限缺失, 则提示Dialog.
     * 参  数：int requestCode         请求码
     * 参  数：String[] permissions    权限数组
     * 参  数：int[] grantResults      结果
     * 返回值：无
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
        }
    }

    /**
     * 方法名：hasAllPermissionsGranted
     * 功  能：含有全部的权限
     * 参  数：int[] grantResults
     * 返回值：如果有权限未获取，则返回false，否则返回true
     */
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 方法名：showMissingPermissionDialog
     * 功  能：显示缺失权限提示框
     * 参  数：无
     * 返回值：无
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_text);
        // 拒绝, 退出应用
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(PERMISSIONS_DENIED);
                finish();
            }
        });
        builder.setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.show();
    }

    /**
     * 方法名：startAppSettings
     * 功  能：启动应用的设置
     * 参  数：无
     * 返回值：无
     */
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }
}