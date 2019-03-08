package link.zhidou.translator.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.whitebyte.hotspotmanager.WifiApManager;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import link.zhidou.translator.BuildConfig;
import link.zhidou.translator.R;
import link.zhidou.translator.adapter.ScanResultListAdapter;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.SPUtil;
import link.zhidou.translator.utils.SingleToast;
import link.zhidou.translator.utils.StringUtils;

import static android.view.KeyEvent.KEYCODE_BACK;
import static link.zhidou.translator.utils.SPKeyContent.FIRST_OPEN;

/**
 * 文件名：WifiListActivity
 * 描  述：Wi-Fi列表类
 * 时  间：2017/11/6
 * 版  权：
 *
 * @author jc
 */
public class WifiListActivity extends ActionBarBaseActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = "WifiListActivity";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final int REQUEST_PASSWORD = 1;
    private static final int REQUEST_SHOW_INFO = 2;
    public static final int REQUEST_DELETE_WIFI = 3;
    public static final int REQUEST_ADD_NETWORK = 4;
    private WifiManager mWifiManager;
    /**
     * wifi_switch
     */
    private SwitchCompat mWifiSwitch;
    private ListView mListView;
    private List<ScanResult> mWifiList = new ArrayList<>();
    public ScanResultListAdapter mAdapter;
    private ScanResult mCurrentScanResult;
    private TextView mWifiNext;
    private LinearLayout mWifiAllButton;
    private TextView mWifiTitle;
    private TextView mWifiLast;
    private WifiApManager mWifiApManager;
    private Disposable mDisposable;
    private View mFooterView = null;
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        initView();
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiApManager = new WifiApManager(this.getApplicationContext());
        registerWifiStatusReceiver();
        initData();
    }

    private void registerWifiStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStatusReceiver, intentFilter);
    }

    private BroadcastReceiver mWifiStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEBUG) {
                Log.d(TAG, "onReceive action = " + action);
            }
            if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (DEBUG) {
                    Log.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION Wifi = " + wifiInfo.getSSID() + ", state = " + state + ", errorCode = " + errorCode);
                }
                if (state == SupplicantState.COMPLETED) {
                    if (DEBUG) {
                        Log.i("WifiReceiver", "(验证成功)");
                    }
                }
                mAdapter.setWifiInfo(wifiInfo, state);
                mAdapter.notifyDataSetChanged();
                if (state == SupplicantState.DISCONNECTED && errorCode == WifiManager.ERROR_AUTHENTICATING) {
                    if (DEBUG) {
                        Log.i("WifiReceiver", "(验证失败)");
                    }
                    SingleToast.show(R.string.error_password);
                    //密码错误，则删除网络
                    deleteNetwork(wifiInfo.getNetworkId());
                    Log.d(TAG, "删除了Wi-Fi：SSID = " + wifiInfo.getSSID() + ", ID = " + wifiInfo.getNetworkId() + ", state = " + state + ", errorCode = " + errorCode);
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                updateWifiList();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (DEBUG) {
                    Log.d(TAG, "Wifi state = " + state);
                }
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    mWifiManager.startScan();
                } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                    mWifiList.clear();
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    private void updateWifiList() {
        if (mWifiApManager == null) {
            return;
        }
        List<ScanResult> results = mWifiManager.getScanResults();
        mWifiList.clear();
        for (ScanResult scanResult : results) {
            if (!TextUtils.isEmpty(scanResult.SSID) && !scanResult.SSID.contains("NVRAM WARNING: Err")) {

                ScanResult tmp = null;
                boolean add = true;
                for (ScanResult sr: mWifiList) {
                    if (scanResult.SSID.equals(sr.SSID) && scanResult.level < sr.level) {
                        add = false;
                        break;
                    } else if (scanResult.SSID.equals(sr.SSID) && scanResult.level > sr.level) {
                        tmp = sr;
                        break;
                    }
                }

                if (add) {
                    if (tmp != null) {
                        mWifiList.remove(tmp);
                    }
                    mWifiList.add(scanResult);
                }
            }
        }

        if (mWifiList.size() > 0) {
            Collections.sort(mWifiList, new Comparator<ScanResult>() {
                @Override
                public int compare(ScanResult o1, ScanResult o2) {
                    int i = o2.level - o1.level;
                    return i;
                }
            });
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        SupplicantState state = SupplicantState.COMPLETED;
        if (wifiInfo != null) {
            state = wifiInfo.getSupplicantState();
        }
        mAdapter.setWifiInfo(wifiInfo, state);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiStatusReceiver);
    }

    private void initView() {
        mWifiSwitch = (SwitchCompat) findViewById(R.id.wifi_switch);
        mWifiSwitch.setOnCheckedChangeListener(this);
        mListView = (ListView) findViewById(R.id.list_view);

        mWifiAllButton = (LinearLayout) findViewById(R.id.ll_wifi_splash);
        Bundle d = this.getIntent().getExtras();
        if (d != null) {
            String ms = d.getString("splash_wifi");
            if ("first".equals(ms)) {
                mWifiAllButton.setVisibility(View.VISIBLE);
            } else {
                mWifiAllButton.setVisibility(View.GONE);
            }
        }
        mWifiLast = (TextView) findViewById(R.id.tv_wifi_last);
        mWifiLast.setOnClickListener(this);
        mWifiNext = (TextView) findViewById(R.id.tv_wifi_next);
        mWifiNext.setOnClickListener(this);

        mAdapter = new ScanResultListAdapter(this, mWifiList);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(1);
        mListView.setItemsCanFocus(true);
        mListView.requestFocus();
        mListView.setOnItemClickListener(this);
        mFooterView = View.inflate(this, R.layout.footer_add_network, null);
        mFooterView.setOnClickListener(this);
    }

    private void showFooterView(boolean show) {
        if (show) {
            mListView.removeFooterView(mFooterView);
            mListView.addFooterView(mFooterView);
        } else {
            mListView.removeFooterView(mFooterView);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mDisposable = Flowable.interval(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        updateWifiList();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (DEBUG) {
                            Log.d(TAG, "error refresh wifi timer");
                            throwable.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
    }

    private void initData() {
        boolean isWifiEnabled = mWifiManager.isWifiEnabled() && !mWifiApManager.isWifiApEnabled();
        mWifiSwitch.setChecked(isWifiEnabled);
        showFooterView(isWifiEnabled);
//        mWifiManager.startScan();
        setActionBarTitle(WifiListActivity.this);
        if (isWifiEnabled) updateWifiList();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            //打开wifi
            if (mWifiApManager.isWifiApEnabled()) {
                //如果wifi热点打开了的话，关闭
                mWifiApManager.setWifiApEnabled(null, false);
            }
        }
        showFooterView(b);
        mWifiManager.setWifiEnabled(b);
//        if (b) {
//            mWifiManager.startScan();
//        }
    }

    private void enterPassword(ScanResult result) {
        Intent intent = new Intent(this, KeyboardActivity.class);
        intent.putExtra(KeyboardActivity.EXTRA_TITLE, result.SSID);
        startActivityForResult(intent, REQUEST_PASSWORD);
    }

    private void showWifiInfo() {
        Intent intent = new Intent(this, WifiInfoActivity.class);
        startActivityForResult(intent, REQUEST_SHOW_INFO);
    }


    private void addWifiNetwork(String ssid, int securityType, String password) {
        WifiConfiguration configuration = createWifiInfo(ssid, password, securityType, true);
        int networkId = mWifiManager.addNetwork(configuration);
        boolean enabled = mWifiManager.enableNetwork(networkId, true);
        boolean saved = mWifiManager.saveConfiguration();
        if (DEBUG) {
            Log.d(TAG, "networkId: " + networkId + ", enabled: " + enabled + ", saved: " + saved);
        }
        updateWifiList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PASSWORD) {
                String password = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                connectNetwork(password);
            } else if (requestCode == REQUEST_SHOW_INFO) {
                if ("del".equals(data.getStringExtra("action"))) {
                    String ssid = data.getStringExtra("ssid");
                    deleteNetwork(ssid);
                }
            } else if (REQUEST_ADD_NETWORK == requestCode) {
                String ssid = data.getStringExtra(AddWifiActivity.EXTRA_SSID);
                int securityType = data.getIntExtra(AddWifiActivity.EXTRA_SECURITY_TYPE, AddWifiActivity.SECURITY_NONE);
                String password = data.getStringExtra(AddWifiActivity.EXTRA_PASSWORD);
                if (DEBUG) {
                    Log.d(TAG, "ssid: " + ssid + ", securityType: " + securityType + ", password: " + password);
                }
                addWifiNetwork(ssid, securityType, password);
            }
        } else if (resultCode == REQUEST_DELETE_WIFI) {
            if ("del".equals(data.getStringExtra("action"))) {
                String ssid = data.getStringExtra("ssid");
                deleteNetwork(ssid);
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void connectNetwork(String password) {
        if (mCurrentScanResult != null) {
            int securityType = getSecurityType(mCurrentScanResult.capabilities);
            WifiConfiguration configuration = createWifiInfo(mCurrentScanResult.SSID, password, securityType, false);
            int networkId = mWifiManager.addNetwork(configuration);
            mWifiManager.saveConfiguration();
            connectNetwork(networkId);
        }
    }

    private int getSecurityType(String capabilities) {
        int securityType = SECURITY_NONE;
        if (capabilities.contains("WPA")) {
            securityType = SECURITY_PSK;
        } else if (capabilities.contains("WEP")) {
            securityType = SECURITY_WEP;
        }
        return securityType;
    }

    protected WifiConfiguration createWifiInfo(String ssid, String password, int securityType, boolean hide) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = convertToQuotedString(ssid);
        WifiConfiguration configured = getConfigured(ssid);
        if (configured != null) {
            deleteNetwork(configured.networkId);
        }
        if (securityType == SECURITY_NONE) { //WIFICIPHER_NOPASS
            config.hiddenSSID = hide;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.clear();
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        } else if (securityType == SECURITY_WEP) { //WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = convertToQuotedString(password);
            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        } else if (securityType == SECURITY_PSK) { //WIFICIPHER_WPA
            config.preSharedKey = convertToQuotedString(password);
            config.hiddenSSID = true;
            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }
        return config;
    }

    private WifiConfiguration getConfigured(String ssid) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals(convertToQuotedString(ssid))) {
                return existingConfig;
            }
        }
        return null;
    }

    public String getSecurityDesc(BitSet allowedKeyManagement) {
        StringBuilder sbuf = new StringBuilder("");
        for (int k = 0; k < allowedKeyManagement.size(); k++) {
            if (allowedKeyManagement.get(k)) {
                sbuf.append(" ");
                if (k < WifiConfiguration.KeyMgmt.strings.length) {
                    sbuf.append(WifiConfiguration.KeyMgmt.strings[k]);
                } else {
                    sbuf.append("??");
                }
            }
        }
        return sbuf.toString();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        ScanResult result = (ScanResult) mAdapter.getItem(position);
        if (result != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null && (result.SSID).equals(StringUtils.trimFirstAndLastChar(wifiInfo.getSSID(), '\"'))) {
                showWifiInfo();
            } else {
                List<WifiConfiguration> configurationList = mWifiManager.getConfiguredNetworks();
                if (configurationList != null) {
                    for (WifiConfiguration configuration : configurationList) {
                        if (result.SSID.equals(StringUtils.trimFirstAndLastChar(configuration.SSID, '\"'))) {
                            if (getSecurityType(getSecurityDesc(configuration.allowedKeyManagement)) == getSecurityType(result.capabilities)) {
                                connectNetwork(configuration.networkId);
                                return;
                            }
                        }
                    }
                }
                mCurrentScanResult = result;
                if (!(result.capabilities.contains("WEP") || result.capabilities.contains("PSK") || result.capabilities.contains("EAP"))) {
                    connectNetwork("");
                } else {
                    enterPassword(result);
                }
            }
        }
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }    

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_wifi_last:
                backKeycodePress();
                break;
            case R.id.tv_wifi_next:
                Intent intent_next = new Intent(getApplicationContext(), DateTimeSettings.class);
                intent_next.putExtra("splash_time", "first");
                startActivity(intent_next);
                finish();
                break;
            case R.id.wifi_add_network:
                Intent intent = new Intent(this, AddWifiActivity.class);
                startActivityForResult(intent, REQUEST_ADD_NETWORK);
                break;
            default:
                break;
        }
    }

    public void backKeycodePress() {
        if (SPUtil.getBoolean(this, FIRST_OPEN, false)) {
            Intent intent_back = new Intent(this, SettingLgActivity.class);
            intent_back.putExtra("splash_lg", "first");
            startActivity(intent_back);
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (!SPUtil.getBoolean(this, FIRST_OPEN, false)) {
            super.onBackPressed();
        }else{
            backKeycodePress();
        }
    }

    private void deleteNetwork(int networkId) {
        mWifiManager.removeNetwork(networkId);
        mWifiManager.saveConfiguration();
        mWifiManager.reconnect();
        updateWifiList();
    }

    private void deleteNetwork(String ssid) {
        List<WifiConfiguration> configurationList = mWifiManager.getConfiguredNetworks();
        if (configurationList != null) {
            for (WifiConfiguration configuration : configurationList) {
                if (ssid.equals(StringUtils.trimFirstAndLastChar(configuration.SSID, '\"'))) {
                    deleteNetwork(configuration.networkId);
                    return;
                }
            }
        }
    }

    private void connectNetwork(int networkId) {
        mWifiManager.disconnect();
        mWifiManager.enableNetwork(networkId, true);
        mWifiManager.reconnect();
        updateWifiList();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) {
            backKeycodePress();
        }
        return super.onKeyDown(keyCode, event);
    }
}