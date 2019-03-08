package link.zhidou.translator.ui.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.ApnListAdapter;
import link.zhidou.translator.assist.MethodUtils;
import link.zhidou.translator.assist.SimHotSwapHandler;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.DataState;
import link.zhidou.translator.utils.Log;

@TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
public class ApnSettingsActivity extends BaseActivity implements
        View.OnClickListener,
        ApnListAdapter.SelectedListener,
        PopupMenu.OnMenuItemClickListener,
        CommonActionBar.BackPressedListener,
        CommonActionBar.SettingsPressedListener {
    private static final String TAG = "ApnSettings";
    private static final boolean DEBUG = Log.isLoggable();

    private ArrayList<ApnEntity> mApnEntities = new ArrayList<>();
    public static final String EXTRA_POSITION = "position";
    public static final String RESTORE_CARRIERS_URI =
            "content://telephony/carriers/restore";
    public static final String PREFERRED_APN_URI =
            "content://telephony/carriers/preferapn";

    public static final String APN_ID = "apn_id";
    public static final String SUB_ID = "sub_id";
    public static final String MVNO_TYPE = "mvno_type";
    public static final String MVNO_MATCH_DATA = "mvno_match_data";

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;
    private static final int MVNO_TYPE_INDEX = 4;
    private static final int MVNO_MATCH_DATA_INDEX = 5;
    /// M: check source type, some types are not editable
    private static final int SOURCE_TYPE_INDEX = 6;

    private static final int MENU_NEW = Menu.FIRST;
    private static final int MENU_RESTORE = Menu.FIRST + 1;

    private static final int EVENT_RESTORE_DEFAULTAPN_START = 1;
    private static final int EVENT_RESTORE_DEFAULTAPN_COMPLETE = 2;

    private static final int DIALOG_RESTORE_DEFAULTAPN = 1001;

    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);

    private UserManager mUm;
    private SimHotSwapHandler mSimHotSwapHandler;
    private RestoreApnUiHandler mRestoreApnUiHandler;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private HandlerThread mRestoreDefaultApnThread;
    public static final String ACTION_ANY_DATA_CONNECTION_STATE_CHANGED = "android.intent.action.ANY_DATA_STATE";
    private SubscriptionInfo mSubscriptionInfo;
    private String mSelectedKey;
    private String mMvnoType;
    private String mMvnoMatchData;
    public static final String STATE_KEY = "state";
    private static boolean mRestoreDefaultApnMode;
    // main board
    private CommonActionBar mActionBar;
    private ListView mListView;
    private ApnListAdapter mAdapter;
    private IntentFilter mMobileStateFilter;

    private boolean mUnavailable;

//    final static String PHONE_FACTORY = "com.android.internal.telephony.PhoneFactory";
//    final static String PHONE = "com.android.internal.telephony.Phone";
//    private static final String getDefaultPhone = "getDefaultPhone";
//    private Object mPhone;
//
//    public static Object getPhone() {
//        try {
//            return MethodUtils.invokeStaticMethod(Class.forName(PHONE_FACTORY), getDefaultPhone);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private void updatePhone(int subId) {
//        try {
//            int phoneId = (int)MethodUtils.invokeStaticMethod(SubscriptionManager.class, "getPhoneId", new Object[]{subId});
//            mPhone = MethodUtils.invokeStaticMethod(Class.forName(PHONE_FACTORY), "getPhone", new Object[]{phoneId});
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (mPhone == null) {
//            // Do the best we can
//            getPhone();
//        }
//        if (mPhone == null) {
//            Log.e(TAG, "Failed to get phone");
//        }
//    }

    private Object mUiccController = null;
    @Override
    public void onClick(int position) {
        if (DEBUG) {
            Log.d(TAG, "onClick, position: " + position);
        }
        ApnEntity entity = mApnEntities.get(position);
        int pos = Integer.parseInt(entity.key);
        Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
        Intent intent = new Intent(ApnEditorActivity.ACTION_EDIT, url);
        int subId = mSubscriptionInfo != null ? mSubscriptionInfo.getSubscriptionId()
                : SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        intent.putExtra(SUB_ID, subId);
        startActivity(intent);
    }

    @Override
    public void onSelected(int position) {

        ApnEntity entity = mApnEntities.get(position);
        if (DEBUG) {
            Log.d(TAG, "onSelected, position: " + position + ", mSelectedKey: " + mSelectedKey + ", target key: " + entity.key);
        }
        if (!mSelectedKey.equals(entity.key)) {
            setSelectedApnKey(entity.key);
        }
        updateSelected();
        mAdapter.notifyDataSetChanged();
    }

    private static DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(STATE_KEY);
        if (str != null) {
            return Enum.valueOf(DataState.class, str);
        } else {
            return DataState.DISCONNECTED;
        }
    }

    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                DataState state = getMobileDataState(intent);
                if (DEBUG) {
                    Log.d(TAG, "onReceive ACTION_ANY_DATA_CONNECTION_STATE_CHANGED,state = " + state);
                }
                if (DataState.CONNECTED == state) {
                    if (!mRestoreDefaultApnMode) {
                        fillList();
                    }
                }
            }
        }
    };

    /**
     * Return whether the project is Gemini or not.
     * @return If Gemini, return true, else return false
     */
    public static boolean isHotSwapHanppened(List<SubscriptionInfo> originaList,
                                             List<SubscriptionInfo> currentList) {
        boolean result = originaList.size() != currentList.size();
        if (DEBUG) {
            Log.d(TAG, "isHotSwapHanppened : " + result);
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apn_settings);
        // Booting board
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
//        mActionBar.setSettingsPressedListener(this);
        mActionBar.setTitle(R.string.apn_settings);

        final int subId = getIntent().getIntExtra(SUB_ID, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        mUm = (UserManager) getSystemService(Context.USER_SERVICE);

        mMobileStateFilter = new IntentFilter(ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        /// M: for Airplane mode check
        mMobileStateFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        if (!mUm.hasUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)) {
            mActionBar.setSettingsPressedListener(this);
        }
        mSubscriptionInfo = SubscriptionManager.from(this).getActiveSubscriptionInfo(subId);
//        updatePhone(mSubscriptionInfo.getSubscriptionId());
        try {
            Class cls = Class.forName("com.android.internal.telephony.uicc.UiccController");
            if (cls != null) {
                mUiccController = MethodUtils.invokeStaticMethod(cls, "getInstance");
            } else {
                Log.e(TAG, "Got UiccController class fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (DEBUG) {
                 Log.d(TAG, "onSimHotSwap, finish activity");
                }
                ApnSettingsActivity.this.finish();
            }
        });
        /// @}
        /// M: @{
        if (mSubscriptionInfo == null) {
            if (DEBUG) {
                Log.d(TAG, "onCreate()... Invalid subId: " + subId);
            }
            finish();
        }
        initView();

    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mMobileStateReceiver, mMobileStateFilter);

        if (!mRestoreDefaultApnMode) {
            fillList();
            /// M: In case dialog not dismiss as activity is in background, so when resume back,
            // need to remove the dialog @{
            removeDialog(DIALOG_RESTORE_DEFAULTAPN);
            /// @}
        }
    }

    @Override
    public void onPause() {
        unregisterReceiver(mMobileStateReceiver);
        super.onPause();
    }

    private void initView() {
        mListView = findViewById(R.id.entities);
        mAdapter = new ApnListAdapter(this, mApnEntities, this);
        mListView.setAdapter(mAdapter);
    }

    private Uri getPreferApnUri(int subId) {
        Uri preferredUri = Uri.withAppendedPath(Uri.parse(PREFERRED_APN_URI), "/subId/" + subId);
        Log.d(TAG, "getPreferredApnUri: " + preferredUri);
        return preferredUri;
    }

    private String getSelectedApnKey() {
        String key = null;

        /// M: add sub id for prefer APN @{
        /*
        Cursor cursor = getContentResolver().query(PREFERAPN_URI, new String[] {"_id"},
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
                */
        int subId = mSubscriptionInfo.getSubscriptionId();
        Cursor cursor = getContentResolver().query(getPreferApnUri(subId), new String[] { "_id" }, null, null,
                Telephony.Carriers.DEFAULT_SORT_ORDER);
        /// @}
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(ID_INDEX);
        }
        cursor.close();
        if (DEBUG) {
            Log.d(TAG, "getSelectedApnKey(), key = " + key);
        }
        return key;
    }

    public static class ApnEntity implements Comparable<ApnEntity> {
        public String key;
        public String title;
        public String summary;
        public boolean persistent;
        public boolean selectable;
        public int subId;
        public boolean selected = false;

        @Override
        public int compareTo(@NonNull ApnEntity o) {
            if (selectable) {
                if (!o.selectable) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                if (o.selectable) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }

        @Override
        public String toString() {
            return "ApnEntity{" +
                    "key='" + key + '\'' +
                    ", title='" + title + '\'' +
                    ", summary='" + summary + '\'' +
                    ", persistent=" + persistent +
                    ", selectable=" + selectable +
                    ", subId=" + subId +
                    ", selected=" + selected +
                    '}';
        }
    }

    private class RestoreApnUiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_COMPLETE:
                    if (DEBUG) {
                        Log.d(TAG, "restore APN complete~~");
                    }
                    fillList();
                    mRestoreDefaultApnMode = false;
                    removeDialog(DIALOG_RESTORE_DEFAULTAPN);
                    Toast.makeText(
                            ApnSettingsActivity.this,
                            getResources().getString(
                                    R.string.restore_default_apn_completed),
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private Uri getDefaultApnUri(int subId) {
        return Uri.withAppendedPath(DEFAULTAPN_URI, "/subId/" + subId);
    }

    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_START:
                    if (DEBUG) {
                        Log.d(TAG, "restore APN start~~");
                    }
                    ContentResolver resolver = getContentResolver();
                    /// M: add sub id for APN
                    // resolver.delete(DEFAULTAPN_URI, null, null);
                    resolver.delete(getDefaultApnUri(mSubscriptionInfo.getSubscriptionId()), null, null);
                    mRestoreApnUiHandler.sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_COMPLETE);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mRestoreDefaultApnThread != null) {
            mRestoreDefaultApnThread.quit();
        }

        /// M: for [SIM Hot Swap]
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        super.onDestroy();
    }

    /**
     * MNO: Mobile Network Operator
     * MVNO: Mobile Virtual Network Operator
     */
    private void fillList() {
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String mccmnc = "";
        try {
            if (mSubscriptionInfo != null) {
                mccmnc = (String) MethodUtils.invokeMethod(tm, "getSimOperator", new Object[]{mSubscriptionInfo.getSubscriptionId()});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.d(TAG, "mccmnc = " + mccmnc);
        }
        final String where = "numeric=\""
                + mccmnc
                + "\" AND NOT (type='ia' AND (apn=\"\" OR apn IS NULL))";

        if (DEBUG) {
            Log.d(TAG, "fillList where: " + where);
        }

        final Cursor cursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI,
                new String[] { "_id", "name", "apn", "type", "mvno_type", "mvno_match_data",
                        "sourcetype" }, where, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
        /// @}

        if (cursor != null) {
            if (DEBUG) {
                Log.d(TAG, "fillList, cursor count: " + cursor.getCount());
            }
//            IccRecords r = null;
//            if (mUiccController != null && mSubscriptionInfo != null) {
//                r = mUiccController.getIccRecords(SubscriptionManager.getPhoneId(
//                        mSubscriptionInfo.getSubscriptionId()), UiccController.APP_FAM_3GPP);
//            }

            Object r = null;
            if (mUiccController != null && mSubscriptionInfo != null) {
                try {
                    int family = (int) MethodUtils.invokeStaticMethod(SubscriptionManager.class, "getPhoneId", mSubscriptionInfo.getSubscriptionId());
                    r = MethodUtils.invokeMethod(mUiccController, "getIccRecords", family, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            // Clear first.
            mApnEntities.clear();
            mSelectedKey = getSelectedApnKey();
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String name = cursor.getString(NAME_INDEX);
                String apn = cursor.getString(APN_INDEX);
                String key = cursor.getString(ID_INDEX);
                String type = cursor.getString(TYPES_INDEX);
                String mvnoType = cursor.getString(MVNO_TYPE_INDEX);
                String mvnoMatchData = cursor.getString(MVNO_MATCH_DATA_INDEX);
                final boolean selectable = ((type == null) || (!type.equals("mms")
                        && !type.equals("ia") && !type.equals("ims")));
                if (selectable) {
                    ApnEntity entity = new ApnEntity();
                    entity.key = key;
                    entity.title = name;
                    entity.summary = apn;
                    entity.persistent = false;
                    entity.selectable = selectable;
                    entity.subId = mSubscriptionInfo == null ? null : mSubscriptionInfo.getSubscriptionId();
                    mApnEntities.add(entity);
                    setMvnoTypeAndData(r, mvnoType, mvnoMatchData);
                }
                if (DEBUG) {
                    Log.d(TAG, "mSelectedKey = " + mSelectedKey + " key = " + key + " name = " + name);
                }
                cursor.moveToNext();
            }
            if (DEBUG) {
                Log.d(TAG, "entities: " + mApnEntities);
            }
            cursor.close();
            Collections.sort(mApnEntities);
            setPreferApnChecked(mApnEntities);
            updateSelected();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void setMvnoTypeAndData(Object r, String mvnoType,  String mvnoMatchData) {
        if (r != null && !TextUtils.isEmpty(mvnoType) && !TextUtils.isEmpty(mvnoMatchData)) {
            boolean ret = false;
            try {
                Class cls = Class.forName("com.android.internal.telephony.dataconnection.ApnSetting");
                if (cls != null) {
                    ret = (boolean) MethodUtils.invokeStaticMethod(cls, "mvnoMatches", r, mvnoType, mvnoMatchData);
                } else {
                    Log.e(TAG, "Got ApnSetting class fail");
                }
                if (ret) {
                    // Since adding to mvno list, save mvno info
                    mMvnoType = mvnoType;
                    mMvnoMatchData = mvnoMatchData;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addNewApn() {
        Intent intent = new Intent(ApnEditorActivity.ACTION_INSERT, Telephony.Carriers.CONTENT_URI);
        int subId = mSubscriptionInfo != null ? mSubscriptionInfo.getSubscriptionId()
                : SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        intent.putExtra(SUB_ID, subId);
        if (!TextUtils.isEmpty(mMvnoType) && !TextUtils.isEmpty(mMvnoMatchData)) {
            intent.putExtra(MVNO_TYPE, mMvnoType);
            intent.putExtra(MVNO_MATCH_DATA, mMvnoMatchData);
        }
        startActivity(intent);
    }

    public void updateSelected() {
        if (DEBUG) {
            Log.d(TAG, "updateSelected");
        }
        for (int i = 0; i < mApnEntities.size(); i++) {
            final ApnEntity entity = mApnEntities.get(i);
            if (mSelectedKey.equals(entity.key)) {
                entity.selected = true;
                if (DEBUG) {
                    Log.d(TAG, "Set " + entity.key + " selected");
                }
            } else {
                entity.selected = false;
            }
        }
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.restore_default_apn));
            dialog.setCancelable(false);
            return dialog;
        }
        return null;
    }

    private boolean restoreDefaultApn() {
        showDialog(DIALOG_RESTORE_DEFAULTAPN);
        mRestoreDefaultApnMode = true;

        if (mRestoreApnUiHandler == null) {
            mRestoreApnUiHandler = new RestoreApnUiHandler();
        }

        if (mRestoreApnProcessHandler == null ||
                mRestoreDefaultApnThread == null) {
            mRestoreDefaultApnThread = new HandlerThread(
                    "Restore default APN Handler: Process Thread");
            mRestoreDefaultApnThread.start();
            mRestoreApnProcessHandler = new RestoreApnProcessHandler(
                    mRestoreDefaultApnThread.getLooper(), mRestoreApnUiHandler);
        }

        mRestoreApnProcessHandler
                .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_START);
        return true;
    }

    private void setSelectedApnKey(String key) {
        if (DEBUG) {
            Log.d(TAG, "setSelectedApnKey: " + key);
        }
        mSelectedKey = key;
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(APN_ID, mSelectedKey);
        resolver.update(getPreferApnUri(mSubscriptionInfo.getSubscriptionId()), values, null, null);
    }

    // compare prefer apn and set preference checked state
    private void setPreferApnChecked(ArrayList<ApnEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        String selectedKey = null;
        if (mSelectedKey != null) {
            for (ApnEntity apnEntity : entities) {
                if (mSelectedKey.equals(apnEntity.key)) {
                    selectedKey = mSelectedKey;
                }
            }
        }

        // can't find prefer APN in the list, reset to the first one
        if (selectedKey == null && entities.get(0) != null) {
            selectedKey = entities.get(0).key;
        }

        // save the new APN
        if (selectedKey != null && !selectedKey.equals(mSelectedKey)) {
            setSelectedApnKey(selectedKey);
            mSelectedKey = selectedKey;
        }
        if (DEBUG) {
            Log.d(TAG, "setPreferApnChecked, APN = " + mSelectedKey);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onSettingsPressed(View view) {
        PopupMenu popup = new PopupMenu(this, view, Gravity.CENTER);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_apn_settings, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public void onClick(View v) {
        // TODO
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new:
                addNewApn();
                break;
            case R.id.menu_restore:
                restoreDefaultApn();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
