package link.zhidou.translator.ui.activity.base;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.AboutActivity;
import link.zhidou.translator.ui.activity.AddWifiActivity;
import link.zhidou.translator.ui.activity.BluetoothSettingsActivity;
import link.zhidou.translator.ui.activity.DateTimeSettings;
import link.zhidou.translator.ui.activity.DisplaySettingsActivity;
import link.zhidou.translator.ui.activity.FactoryResetActivity;
import link.zhidou.translator.ui.activity.SettingLgActivity;
import link.zhidou.translator.ui.activity.SettingModeActivity;
import link.zhidou.translator.ui.activity.StorageSettingsActivity;
import link.zhidou.translator.ui.activity.TimeZoneSelectActivity;
import link.zhidou.translator.ui.activity.WifiListActivity;
import link.zhidou.translator.ui.activity.WifiShareActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.SPUtil;

import static link.zhidou.translator.utils.SPKeyContent.FIRST_OPEN;

/**
 * @author keetom
 * @date 2018/1/15
 */

public class ActionBarBaseActivity extends BaseActivity implements CommonActionBar.BackPressedListener {
    public CommonActionBar mActionBar;
    protected void setActionBarTitle(Context context) {
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener((CommonActionBar.BackPressedListener) context);
        Activity activity = (Activity) context;
        if (activity instanceof WifiListActivity) {
            //wifi
            mActionBar.setTitle(context.getResources().getString(R.string.wifi));
        } else if (activity instanceof SettingModeActivity) {
            //模式
            mActionBar.setTitle(context.getResources().getString(R.string.mode));
        } else if (activity instanceof SettingLgActivity) {
            //系统语言
            mActionBar.setTitle(context.getResources().getString(R.string.activity_lg_title));
            if (SPUtil.getBoolean(this, FIRST_OPEN, false)) {
                mActionBar.setBackButtonVisible(false);
            }
        } else if (activity instanceof FactoryResetActivity) {
            //恢复出场设置
            mActionBar.setTitle(context.getResources().getString(R.string.master_clear_title));
        } else if (activity instanceof AboutActivity) {
            //关于
            mActionBar.setTitle(context.getResources().getString(R.string.about));
        } else if (activity instanceof WifiShareActivity) {
            //移动热点
            mActionBar.setTitle(context.getResources().getString(R.string.wifi_share));
        } else if (activity instanceof DateTimeSettings) {
            mActionBar.setTitle(context.getResources().getString(R.string.date_and_time));
        } else if (activity instanceof TimeZoneSelectActivity) {
            mActionBar.setTitle(R.string.date_time_set_timezone);
        } else if (activity instanceof BluetoothSettingsActivity){
            //蓝牙
            mActionBar.setTitle(R.string.activity_bluetooth_title);
        } else if (activity instanceof DisplaySettingsActivity) {
            mActionBar.setTitle(R.string.display_settings);
        } else if (activity instanceof StorageSettingsActivity) {
            mActionBar.setTitle(R.string.storage_settings);
        } else if (activity instanceof AddWifiActivity) {
            mActionBar.setTitle(R.string.wifi_add_network);
        }
    }

    protected void updateBackButton() {
        mActionBar.updateBackButtonImageSource();
    }

    @Override
    public void onBackPressed(View view) {
        onBackPressed();
    }
}