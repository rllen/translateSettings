package link.zhidou.translator.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import info.whitebyte.hotspotmanager.WifiApManager;
import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingShareListAdapter;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SingleToast;

/**
 * 热点设置
 * Created by keetom on 2017/10/20.
 */

public class WifiShareActivity extends ActionBarBaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener {
    private static final int REQUEST_AP_NAME = 1;
    private static final int REQUEST_AP_PASSWORD = 2;
    private SwitchCompat wifi_share_switch;
    private static final String TAG = "Hotspot";
    private static final boolean DEBUG = Log.isLoggable();
    private WifiApManager mWifiApManager;
    private ListView mListView;
    private ArrayList<SettingBean> mSettingBeans = new ArrayList<>();
    private SettingShareListAdapter mAdapter;
    private static final String ACTION_WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static final String EXTRA_WIFI_STATE = "wifi_state";
    private WifiConfiguration mWifiConfiguration = null;
    private BroadcastReceiver mHotspotMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ACTION_WIFI_AP_STATE_CHANGED.equals(action)) {
                //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                final int state = intent.getIntExtra(EXTRA_WIFI_STATE,  0);
                if (DEBUG) {
                    Log.d(TAG, "state: " + state);
                }
                wifi_share_switch.setChecked(isHotspotEnabled(state));

                if (mWifiConfiguration != null && state == 11) {
                    mWifiApManager.setWifiApEnabled(mWifiConfiguration, true);
                    mWifiConfiguration = null;
                }
            }
        }
    };

    public static boolean isHotspotEnabled(int state) {
        if (11 == state || 10 == state) {
            return false;
        } else if (13 == state || 12 == state) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_share);
        mWifiApManager = new WifiApManager(getApplicationContext());
        initView();
        initData();
        boolean enabled = mWifiApManager.isWifiApEnabled();
        wifi_share_switch.setChecked(enabled);
        wifi_share_switch.setOnClickListener(this);
        IntentFilter intentFilter = new IntentFilter(ACTION_WIFI_AP_STATE_CHANGED);
        registerReceiver(mHotspotMonitor, intentFilter);
    }

    private void initView() {
        wifi_share_switch = findViewById(R.id.wifi_share_switch);
        mListView = findViewById(R.id.list_view);
        mAdapter = new SettingShareListAdapter(this, mSettingBeans);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        setActionBarTitle(WifiShareActivity.this);
        mSettingBeans.clear();
        WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();

        String[] safeStrings = getResources().getStringArray(R.array.ap_safe_type);
        String safeType = safeStrings[0];
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            safeType = safeStrings[1];
        }
        SettingBean bean = new SettingBean();
        bean.setSettingName(getString(R.string.wifi_share_name));
        bean.setUserChoice(wifiConfiguration.SSID);
        bean.setShow(true);
        mSettingBeans.add(bean);

        String pass = wifiConfiguration.preSharedKey;
        if (TextUtils.isEmpty(pass)) {
            pass = getString(R.string.empty);
        }
        bean = new SettingBean();
        bean.setSettingName(getString(R.string.tv_set_password));
        bean.setUserChoice(pass);
        bean.setShow(true);
        mSettingBeans.add(bean);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v == wifi_share_switch) {
            final boolean isEnabled = mWifiApManager.isWifiApEnabled();
            if (isEnabled) {
                WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
                mWifiApManager.setWifiApEnabled(wifiConfiguration, false);
            } else {
                if (hasSimCard()) {
                    WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
                    mWifiApManager.setWifiApEnabled(wifiConfiguration, true);
                } else {
                    wifi_share_switch.setChecked(false);
                    SingleToast.show(R.string.error_no_sim);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                enterEditApName();
                break;
            case 1:
            case 2:
                enterEditPassword();
                break;
        }
    }

    private void enterEditApName() {
        WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
        Intent intent = new Intent(this, KeyboardActivity.class);
        intent.putExtra(KeyboardActivity.EXTRA_TITLE, getString(R.string.wifi_share_name));
        intent.putExtra(KeyboardActivity.EXTRA_IN_VALUE, wifiConfiguration.SSID);
        startActivityForResult(intent, REQUEST_AP_NAME);
    }

    private void enterEditPassword() {
        WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
        Intent intent = new Intent(this, KeyboardActivity.class);
        intent.putExtra(KeyboardActivity.EXTRA_TITLE, getString(R.string.tv_set_password));
        intent.putExtra(KeyboardActivity.EXTRA_IN_VALUE, wifiConfiguration.preSharedKey);
        intent.putExtra(KeyboardActivity.EXTRA_MIN_LENGTH, 8);
        startActivityForResult(intent, REQUEST_AP_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String value = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
            if (requestCode == REQUEST_AP_NAME) {
                if (!TextUtils.isEmpty(value)) {
                    WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
                    wifiConfiguration.SSID = value;
                    if (mWifiApManager.isWifiApEnabled()) {
                        mWifiApManager.setWifiApEnabled(null, false);
                        mWifiConfiguration = wifiConfiguration;
                    } else {
                        mWifiApManager.setWifiApConfiguration(wifiConfiguration);
                    }
                    initData();
                }
            } else if (requestCode == REQUEST_AP_PASSWORD) {
                WifiConfiguration wifiConfiguration = mWifiApManager.getWifiApConfiguration();
                wifiConfiguration.allowedKeyManagement.clear();
                if (TextUtils.isEmpty(value)) {
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                } else {
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                }
                wifiConfiguration.preSharedKey = value;
                if (mWifiApManager.isWifiApEnabled()) {
                    mWifiApManager.setWifiApEnabled(null, false);
                    mWifiConfiguration = wifiConfiguration;
                } else {
                    mWifiApManager.setWifiApConfiguration(wifiConfiguration);
                }
                initData();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public boolean hasSimCard() {
        Context context = WifiShareActivity.this.getBaseContext();
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mHotspotMonitor);
    }

}
