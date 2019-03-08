package link.zhidou.translator.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingShareListAdapter;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class NetworkOverviewActivity extends BaseActivity implements
        CommonActionBar.BackPressedListener,
        AdapterView.OnItemClickListener {
    private static final String TAG = "NetworkSettings";
    private static final boolean DEBUG = Log.isLoggable();

    // main board
    private CommonActionBar mActionBar;
    private SubscriptionManager mSubscriptionManager = null;
    private List<SubscriptionInfo> mActiveSubInfos = null;
    private SettingShareListAdapter mAdapter;
    private ArrayList<SettingBean> mSettingBeans = new ArrayList<>();
    private ListView mListView;

    private boolean initializeSubscriptions() {
        List<SubscriptionInfo> sil = mSubscriptionManager.getActiveSubscriptionInfoList();
        mActiveSubInfos.clear();
        if (sil != null) {
            mActiveSubInfos.addAll(sil);
        }
        if (DEBUG) {
            Log.d(TAG, "ActiveSubInfo size: " + mActiveSubInfos.size());
        }
        mSettingBeans.clear();
        if (mActiveSubInfos.size() == 1) {
            goToNetworkSettingsDirectly(mActiveSubInfos.get(0).getSubscriptionId());
            finish();
            return true;
        }
        for (int i = 0; i < mActiveSubInfos.size(); i++) {
            SubscriptionInfo info = mActiveSubInfos.get(i);
            SettingBean bean = new SettingBean();
            bean.setSettingName(String.valueOf(info.getDisplayName()));
            bean.setShow(true);
            try {
                bean.setExtra(info.getSubscriptionId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSettingBeans.add(bean);
            if (DEBUG) {
                Log.d(TAG, "Card: " + i + ", subId: " + info.getSubscriptionId() + ", display name: " + String.valueOf(mActiveSubInfos.get(i).getDisplayName()));
            }
        }
        mAdapter.notifyDataSetChanged();
        return false;
    }

    private void goToNetworkSettingsDirectly(int subId) {
        Intent intent = new Intent(this, NetworkSettingsActivity.class);
        intent.putExtra(NetworkSettingsActivity.SUB_ID, subId);
        startActivity(intent);
    }

    // Fix memory leak issue, only hold weak reference @{
    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new WeakOnSubscriptionsChangeListener(this);
    private static class WeakOnSubscriptionsChangeListener extends SubscriptionManager.OnSubscriptionsChangedListener {

        WeakReference<NetworkOverviewActivity> mWeakRef = null;

        public WeakOnSubscriptionsChangeListener(NetworkOverviewActivity activity) {
            mWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void onSubscriptionsChanged() {
            NetworkOverviewActivity activity = mWeakRef.get();
            if (activity != null) {
                if (DEBUG) {
                    Log.d(TAG, "onSubscriptionsChanged: start");
                }
                // 热插拔，直接finish
                if (isHotSwapHanppened(activity.mActiveSubInfos, activity.mSubscriptionManager.getActiveSubscriptionInfoList())) {
                    activity.finish();
                    return;
                }
                activity.initializeSubscriptions();
            }
        }
    }
    // @}


    private void initView() {
        mListView = findViewById(R.id.cards);
        mAdapter = new SettingShareListAdapter(this, mSettingBeans);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SettingBean bean = mAdapter.getItem(position);
        Intent intent = new Intent(this, NetworkSettingsActivity.class);
        intent.putExtra(NetworkSettingsActivity.SUB_ID, (int) bean.getExtra());
        startActivity(intent);
    }

    /**
     * Return whether the project is Gemini or not.
     * @return If Gemini, return true, else return false
     */
    public static boolean isHotSwapHanppened(List<SubscriptionInfo> originaList,
                                             List<SubscriptionInfo> currentList) {
        boolean result = currentList == null || originaList.size() != currentList.size();
        if (DEBUG) {
            Log.d(TAG, "isHotSwapHanppened : " + result);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apn_overview);
        // Booting board
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mActionBar.setTitle(R.string.network_settings_title);
        initView();
        mSubscriptionManager = SubscriptionManager.from(this);
        // Initialize mActiveSubInfo
        final int max = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
        mActiveSubInfos = new ArrayList<>(max);
        if (!initializeSubscriptions()) {
            mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        }
    }

    @Override
    protected void onDestroy() {
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        super.onDestroy();
    }

    @Override
    public void onBackPressed(View view) {
        onBackPressed();
    }

}
