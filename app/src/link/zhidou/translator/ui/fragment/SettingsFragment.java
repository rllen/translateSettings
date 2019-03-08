package link.zhidou.translator.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import link.zhidou.translator.Config;
import link.zhidou.translator.R;
import link.zhidou.translator.SpeechApp;
import link.zhidou.translator.adapter.SettingListAdapter;
import link.zhidou.translator.assist.SysProp;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.service.FirmwareNewVersionReceiver;
import link.zhidou.translator.ui.activity.AboutActivity;
import link.zhidou.translator.ui.activity.ApnOverviewActivity;
import link.zhidou.translator.ui.activity.BluetoothSettingsActivity;
import link.zhidou.translator.ui.activity.DisplaySettingsActivity;
import link.zhidou.translator.ui.activity.KeyboardActivity;
import link.zhidou.translator.ui.activity.NetworkOverviewActivity;
import link.zhidou.translator.ui.activity.DateTimeSettings;
import link.zhidou.translator.ui.activity.FactoryResetActivity;
import link.zhidou.translator.ui.activity.SettingLgActivity;
import link.zhidou.translator.ui.activity.SettingModeActivity;
import link.zhidou.translator.ui.activity.SingleOrMultipleChoiceActivity;
import link.zhidou.translator.ui.activity.StorageSettingsActivity;
import link.zhidou.translator.ui.activity.WifiListActivity;
import link.zhidou.translator.ui.activity.WifiShareActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SPKeyContent;
import link.zhidou.translator.utils.SPUtil;
import link.zhidou.translator.utils.SingleToast;
import link.zhidou.translator.utils.Util;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import static link.zhidou.translator.Config.CODE_SET_SEX;
import static link.zhidou.translator.service.FirmwareNewVersionReceiver.FIRMWARE_VERSION;

/**
 * 文件名：SettingsFragment
 * 描  述：设置界面
 * 作  者：keetom
 * 时  间：2017/9/17
 * 版  权：
 */
public class SettingsFragment extends BaseFragment implements CommonActionBar.BackPressedListener, FirmwareNewVersionReceiver.Listener {
    private static final boolean DEBUG = Log.isLoggable();
    private static final String TAG = "Settings";
    private static final int SCREEN_TIMEOUT_REQC = 1001;
    private static int FACTORY_MODE_REQ = 1002;
//    private String mRegion;
    private String mWifi;
    private String mApnSettings;
    private String mNetworkSettings;
    private String mDataRoamingSettings;
    private SettingBean mApnSettingsBean;
    private SettingBean mNetworkSettingsBean;
    private SettingBean mDataRoamingBean;
//    private String mShareName;
    private String mShareName;
    private String mSex;
    private String mSpeed;
    private String mDisplaySettings;
    private String mMode;
    private String mLanguage;
    private String mDateTime;
    private String aboat;
    private String mReset;
    private String mStorage;
    // 固件升级入口
    private String mFirmwareUpgrade;
    private static final String ACTION_MTK_SYSTEM_UPDATE_ENTRY = "com.mediatek.intent.System_Update_Entry";
    private static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 60000;
    private SettingListAdapter mAdapter;
    private SettingBean mSettingBean;
    private String mBluetoothTitle;
    private SettingBean mBluetoothSettingBean;
    private static int FIRMWARE_INDEX = 10;
    private int mTitleHitCountdown = 5;

    public static SettingsFragment newInstance(String s) {
        SettingsFragment myFragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", s);
        myFragment.setArguments(bundle);
        return myFragment;
    }
    private CommonActionBar mActionBar;

    private View getListViewChildAt(int position) {
        int header = mLvSetting.getHeaderViewsCount();
        int first = mLvSetting.getFirstVisiblePosition();
        return mLvSetting.getChildAt(position + header - first);
    }

    private boolean showRedLabel() {
        String curFirmwareVersion =  SysProp.get("ro.xh.display.version","");
        if (TextUtils.isEmpty(curFirmwareVersion)) {
            curFirmwareVersion = SysProp.get("ro.build.display.id", "");
        }
        final String newFirmwareVersion = SysProp.get(FIRMWARE_VERSION, curFirmwareVersion);
        return !(newFirmwareVersion.startsWith(curFirmwareVersion));
    }

    @Override
    public void onNewVersionReceived() {
        final SettingBean bean = mListSetting.get(FIRMWARE_INDEX);
        bean.setShowRedLabel(showRedLabel());
        mAdapter.getView(FIRMWARE_INDEX, getListViewChildAt(FIRMWARE_INDEX), mLvSetting);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.settings, container, false);
        mActionBar = view.findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mActionBar.setTitle(getActivity().getResources().getString(R.string.setting));
        mActionBar.setTitlePressedListener(new CommonActionBar.TitlePressedListener() {
            @Override
            public void onTitlePressed(View view) {
                mTitleHitCountdown--;
                if (mTitleHitCountdown <= 0) {
                    mTitleHitCountdown = 5;
                    enterPassword(FACTORY_MODE_REQ);
                }
            }
        });
        initView2(view);
        FirmwareNewVersionReceiver.add(this);
        return view;
    }

    private void enterPassword (int requestCode) {
        Intent intent = new Intent(getActivity(), KeyboardActivity.class);
        intent.putExtra(KeyboardActivity.EXTRA_TITLE, "PWD");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /// M: @{
        TelephonyManager telephonyManager =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        if (DEBUG) {
            Log.d(TAG, "listening.");
        }
        updateApnEnabled();
        IntentFilter intentFilter = new IntentFilter(ACTION_SUBINFO_RECORD_UPDATED);
        getContext().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroyView() {
        /// M:  @{
        TelephonyManager telephonyManager =
                (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        if (DEBUG) {
            Log.d(TAG, "drop listening.");
        }
        getContext().unregisterReceiver(mReceiver);
        FirmwareNewVersionReceiver.remove(this);
        super.onDestroyView();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == CODE_SET_SEX) {
            String userchoice = data.getStringExtra("userchoice");
            mSettingBean.setUserChoice(userchoice);
            mAdapter.setResults(mListSetting);
        } else if (Activity.RESULT_OK == resultCode) {
            if (SCREEN_TIMEOUT_REQC == requestCode) {
                try {
                    int value = Integer.parseInt(data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF));
                    Settings.System.putInt(getContext().getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist screen timeout setting", e);
                }
            } else if (FACTORY_MODE_REQ == requestCode) {
                String pwd = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                if (TextUtils.isEmpty(pwd) || !pwd.toLowerCase().equals("201808")) {
                    Toast.makeText(getActivity(), "password error", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    ComponentName componentName = new ComponentName("com.teksun.factorytest", "com.teksun.factorytest.MainActivity");
                    Intent intent = new Intent();
                    intent.setComponent(componentName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    try {
                        Intent intent = new Intent();
                        ComponentName componentName = new ComponentName("com.bsm_wqy.validationtools", "com.bsm_wqy.validationtools.VolidationToolsMainActivity");
                        intent.setComponent(componentName);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private ListView mLvSetting;

    private void initView2(View view) {

        initSettingData();

        mLvSetting = (ListView) view.findViewById(R.id.lv_setting);
        mAdapter = new SettingListAdapter(getContext(), mListSetting);
        mLvSetting.setAdapter(mAdapter);
        mLvSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSettingBean = mListSetting.get(i);
                if (mSettingBean.isEnable()) {
                    handClick(mSettingBean);
                } else {
                    // Do nothing
                }
            }
        });
    }

    private RecyclablePhoneStateListener mPhoneStateListener = new RecyclablePhoneStateListener(this);
    private static class RecyclablePhoneStateListener extends PhoneStateListener {
        private WeakReference<SettingsFragment> mWeakRef;
        public RecyclablePhoneStateListener(SettingsFragment settingsFragment) {
            mWeakRef = new WeakReference<>(settingsFragment);
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (DEBUG) {
                Log.d(TAG, "PhoneStateListener, new state = " + state);
            }
            final SettingsFragment settingsFragment = mWeakRef.get();
            if (settingsFragment != null && settingsFragment.getActivity() != null) {
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    settingsFragment.updateApnEnabled();
                }
            }
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SUBINFO_RECORD_UPDATED.equals(action)) {
                if (DEBUG) {
                    Log.d(TAG, "ACTION_SIM_INFO_UPDATE received");
                }
                updateApnEnabled();
            }
        }
    };
    /// @}


    private boolean isRoamingServiceExisted(Context context) {
        Intent service = new Intent("link.zhidou.settings.DATA_ROAMING_SETTINGS");
        service.setPackage("com.android.phone");
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> services =  pm.queryIntentServices(service, 0);
        return services != null && services.size() > 0;
    }

    private void handClick(SettingBean settingBean) {
        if (settingBean == null) {
            return;
        }

//        if (settingBean.getSettingName().equals(mRegion)) {
//            startActivity(new Intent(getContext(), SettingRegionActivity.class));
//        }
        if (settingBean.getSettingName().equals(mBluetoothTitle)) {
            startActivity(new Intent(getContext(), BluetoothSettingsActivity.class));
            return; //TODO: 黄鹏，你们写的为啥没有return
        }

        if (settingBean.getSettingName().equals(mApnSettings)) {
            startActivity(new Intent(getContext(), ApnOverviewActivity.class));
        }

        if (settingBean.getSettingName().equals(mNetworkSettings)) {
            startActivity(new Intent(getContext(), NetworkOverviewActivity.class));
        }

        if (settingBean.getSettingName().equals(mDataRoamingSettings)) {
            try {
                Intent intent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (settingBean.getSettingName().equals(mWifi)) {
            startActivity(new Intent(getContext(), WifiListActivity.class));
        }

        if (settingBean.getSettingName().equals(mShareName)) {
            startActivity(new Intent(getContext(), WifiShareActivity.class));
        }
        if (settingBean.getSettingName().equals(mDisplaySettings)) {
            Intent intent = new Intent(getContext(), DisplaySettingsActivity.class);
            startActivity(intent);
        }

        if (settingBean.getSettingName().equals(mMode)) {
            Intent intentLeft = new Intent(getContext(), SettingModeActivity.class);
            SettingsFragment.this.startActivityForResult(intentLeft, CODE_SET_SEX);
        }

        if (settingBean.getSettingName().equals(mLanguage)) {
            Intent intentLeft = new Intent(getContext(), SettingLgActivity.class);
            startActivity(intentLeft);
        }

        if (settingBean.getSettingName().equals(mDateTime)) {
            Intent intentLeft = new Intent(getContext(), DateTimeSettings.class);
            startActivity(intentLeft);
        }

        if (settingBean.getSettingName().equals(mReset)) {
            Intent intent = new Intent(getContext(), FactoryResetActivity.class);
            startActivity(intent);
        }

        if (settingBean.getSettingName().equals(mFirmwareUpgrade)) {
            try {
                Intent firmwareUpgrade = new Intent(ACTION_MTK_SYSTEM_UPDATE_ENTRY);
                startActivity(firmwareUpgrade);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                SingleToast.show("Firmware upgrade activity not found");
            }
        }

        if (settingBean.getSettingName().equals(mStorage)) {
            Intent intentLeft = new Intent(getContext(), StorageSettingsActivity.class);
            SettingsFragment.this.startActivity(intentLeft);
            return;
        }

        if (settingBean.getSettingName().equals(aboat)) {
            Intent intentLeft = new Intent(getContext(), AboutActivity.class);
            SettingsFragment.this.startActivity(intentLeft);
        }
    }

    private ArrayList<SettingBean> mListSetting;


    void updateApnEnabled() {
        final TelephonyManager telephonyManager = (TelephonyManager) SpeechApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        final int callState = telephonyManager.getCallState();

        int simNum = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            simNum = SubscriptionManager.from(SpeechApp.getContext()).getActiveSubscriptionInfoCount();
        } else {
            // TODO
            simNum = 0;
        }

        if (simNum > 0 && callState == TelephonyManager.CALL_STATE_IDLE) {
            if (mApnSettingsBean != null) {
                mApnSettingsBean.setEnable(true);
                mDataRoamingBean.setEnable(true);
            } else if (mNetworkSettings != null) {
                mNetworkSettingsBean.setEnable(true);
            }
        } else {
            if (mApnSettingsBean != null) {
                mApnSettingsBean.setEnable(false);
                mDataRoamingBean.setEnable(false);
            } else if (mNetworkSettings != null) {
                mNetworkSettingsBean.setEnable(false);
            }
        }
        mAdapter.notifyDataSetChanged();
    }


    public void initSettingData() {
        mListSetting = new ArrayList<>();

        mWifi = getContext().getResources().getString(R.string.wifi);
        SettingBean settingBeanwifi = new SettingBean(R.mipmap.wifi, mWifi, "", true);
        mListSetting.add(settingBeanwifi);

        mShareName = getContext().getResources().getString(R.string.share_name);
        SettingBean settingBeanshare_name = new SettingBean(R.mipmap.hot, mShareName, "", true);
        mListSetting.add(settingBeanshare_name);

//        mSex = getContext().getResources().getString(R.string.sex);
//        SettingBean settingBeansex = new SettingBean(R.mipmap.sex, mSex, "", true);
//        String spSex = SPUtil.getString(getActivity().getBaseContext(), SPKeyContent.SETTING_SEX, Config.Sex.DEFAULT);
//        settingBeansex.setUserChoice(Config.labelFromValue(getContext(), R.array.sex_entries, R.array.sex_values, spSex));
//        mListSetting.add(settingBeansex);
//
//        mSpeed = getContext().getResources().getString(R.string.speed);
//        SettingBean settingBeanspeed = new SettingBean(R.mipmap.speed, mSpeed, "", true);
//        int speed = SPUtil.getInt(getActivity().getBaseContext(), SPKeyContent.SPEECH_CHOICE, Config.Speed.DEFAULT);
//        settingBeanspeed.setUserChoice(Config.labelFromValue(getContext(), R.array.speed_entries, R.array.speed_values, speed));
//        mListSetting.add(settingBeanspeed);

        mMode = getContext().getResources().getString(R.string.mode);
        SettingBean settingBeanmode = new SettingBean(R.mipmap.mode, mMode, "", true);
        String spMode = (String) SPUtil.get(getActivity().getBaseContext(), SPKeyContent.SETTING_MODE, Config.Mode.DEFAULT);
        settingBeanmode.setUserChoice(Config.labelFromValue(getContext(), R.array.mode_entries, R.array.mode_values, spMode));
        mListSetting.add(settingBeanmode);

//        mRegion = getContext().getResources().getString(R.string.region);
//        SettingBean region = new SettingBean(R.mipmap.region, mRegion, "", true);
//        mListSetting.add(region);

        final boolean roamingServiceExisted = isRoamingServiceExisted(SpeechApp.getContext());
        if (!Util.isWifiOnly(SpeechApp.getContext())) {
            if (!roamingServiceExisted) {
                mApnSettings = getContext().getResources().getString(R.string.apn_settings);
                mApnSettingsBean = new SettingBean(R.mipmap.apn, mApnSettings, "", true);
                mListSetting.add(mApnSettingsBean);
                mDataRoamingSettings = getContext().getResources().getString(R.string.roaming);
                mDataRoamingBean = new SettingBean(R.mipmap.data_roaming, mDataRoamingSettings, "", true);
                mListSetting.add(mDataRoamingBean);
                FIRMWARE_INDEX = 10;
            } else {
                mNetworkSettings = getContext().getResources().getString(R.string.network_settings_title);
                mNetworkSettingsBean = new SettingBean(R.mipmap.mobile, mNetworkSettings, "", true);
                mListSetting.add(mNetworkSettingsBean);
                FIRMWARE_INDEX = 9;
            }

        } else {
            FIRMWARE_INDEX = 8;
        }

        mBluetoothTitle = getContext().getResources().getString(R.string.activity_bluetooth_title);
        mBluetoothSettingBean = new SettingBean(R.mipmap.bluetooth, mBluetoothTitle, "", true);
        mListSetting.add(mBluetoothSettingBean);

        mLanguage = getContext().getResources().getString(R.string.activity_lg_title);
        Resources resources = getResources();
        final int resId = roamingServiceExisted ? R.mipmap.language : R.mipmap.language_old;
        SettingBean settingBeanlanguage = new SettingBean(resId, mLanguage,
                Config.toTitleCase(Config.getDisplayName(Locale.getDefault(),
                        resources.getStringArray(R.array.special_locale_codes),
                        resources.getStringArray(R.array.special_locale_names))), true);
        mListSetting.add(settingBeanlanguage);

        mDisplaySettings = getContext().getResources().getString(R.string.display_settings);
        SettingBean displaySettings = new SettingBean(R.mipmap.display, mDisplaySettings, "", true);
        mListSetting.add(displaySettings);

        mDateTime = getContext().getResources().getString(R.string.date_and_time);
        SettingBean settingBeanDateTime = new SettingBean(R.mipmap.time, mDateTime, "", true);
        mListSetting.add(settingBeanDateTime);

        // 加系统重置
        mReset = getContext().getResources().getString(R.string.master_clear_title);
        SettingBean settingBeanReset = new SettingBean(R.mipmap.recovery, mReset, "", true);
        mListSetting.add(settingBeanReset);

        // 加固件升级
        mFirmwareUpgrade = getContext().getResources().getString(R.string.firmware_upgrade);
        SettingBean firmwareVersion = new SettingBean(R.mipmap.update, mFirmwareUpgrade, "", true);
        firmwareVersion.setShowRedLabel(showRedLabel());
        mListSetting.add(firmwareVersion);

        // Storage
        mStorage = getContext().getResources().getString(R.string.storage_settings);
        SettingBean storage = new SettingBean(R.mipmap.storage, mStorage, "", true);
        mListSetting.add(storage);

        aboat = getContext().getResources().getString(R.string.about);
        SettingBean settingBeanaboat = new SettingBean(R.mipmap.about, aboat, "", true);
        mListSetting.add(settingBeanaboat);
    }

    @Override
    public void onBackPressed(View view) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }
}
