package link.zhidou.translator.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import java.util.ArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingShareListAdapter;
import link.zhidou.translator.assist.SimHotSwapHandler;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.RoamingSettings;

/**
 * Date: 18-4-12
 * Time: 下午5:00
 * Email: lostsearover@gmail.com
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class NetworkSettingsActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemClickListener,
        CommonActionBar.BackPressedListener {

    private static final String TAG = "NetworkSettings";
    private static final boolean DEBUG = Log.isLoggable();
    public static final String SUB_ID = "sub_id";
    private CommonActionBar mActionBar;
    private ListView mListView;
    private ArrayList<SettingBean> mSettingBeans = new ArrayList<>();
    private SettingShareListAdapter mAdapter;
    private Switch mRoamingSwitch = null;
    private boolean mRoamingEnabled = false;
    private SubscriptionInfo mSubscriptionInfo;
    private SimHotSwapHandler mSimHotSwapHandler;
    private int mSubId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_settings);
        mRoamingSwitch = findViewById(R.id.roaming_switch);
        mRoamingSwitch.setOnCheckedChangeListener(this);
        // Booting board
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mActionBar.setTitle(R.string.network_settings_title);
        mSubId = getIntent().getIntExtra(SUB_ID, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        mSubscriptionInfo = SubscriptionManager.from(this).getActiveSubscriptionInfo(mSubId);
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (DEBUG) {
                    Log.d(TAG, "onSimHotSwap, finish activity");
                }
                NetworkSettingsActivity.this.finish();
            }
        });
        /// @}
        /// M: @{
        if (mSubscriptionInfo == null) {
            if (DEBUG) {
                Log.d(TAG, "onCreate()... Invalid subId: " + mSubId);
            }
            finish();
        } else {
            updateRoamingViewStatus(mSubscriptionInfo.getSubscriptionId());
        }

        initViews();
        initData();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = new Intent(this, ApnSettingsActivity.class);
        intent.putExtra(ApnSettingsActivity.SUB_ID, mSubId);
        startActivity(intent);
    }

    private void initViews() {
        mListView = findViewById(R.id.list_view);
        mAdapter = new SettingShareListAdapter(this, mSettingBeans);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        mSettingBeans.clear();
        SettingBean bean = new SettingBean();
        bean.setSettingName(getString(R.string.apn_settings));
        bean.setUserChoice("");
        bean.setShow(true);
        mSettingBeans.add(bean);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    private void updateRoamingViewStatus(final int subId) {
        Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> e) throws Exception {
                e.onNext(RoamingSettings.from(NetworkSettingsActivity.this).isDataRoamingEnabled(subId));
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean enabled) throws Exception {
                mRoamingEnabled = enabled;
                mRoamingSwitch.setChecked(enabled);
            }
        });
    }

    private void setRoamingStatus(final int subId, final boolean enabled) {
        Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(FlowableEmitter<Boolean> e) throws Exception {
                e.onNext(RoamingSettings.from(NetworkSettingsActivity.this).setDataRoamingEnabled(subId, enabled));
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            mRoamingEnabled = enabled;
                        }
                        if (DEBUG) {
                            Log.e(TAG, "setDataRoamingEnabled to " + enabled + ", result: " + aBoolean);
                        }
                    }
                });
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mRoamingEnabled != isChecked) {
            setRoamingStatus(mSubscriptionInfo.getSubscriptionId(), isChecked);
        }
    }

    @Override
    protected void onDestroy() {
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        super.onDestroy();
    }
}
