package link.zhidou.translator.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AlertDialog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.ApnEditorListAdapter;
import link.zhidou.translator.assist.MethodUtils;
import link.zhidou.translator.assist.SimHotSwapHandler;
import link.zhidou.translator.model.bean.PreferenceBean;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;

public class ApnEditorActivity extends BaseActivity implements
        PopupMenu.OnMenuItemClickListener,
        CommonActionBar.BackPressedListener,
        CommonActionBar.SettingsPressedListener,
        AdapterView.OnItemClickListener {

    private static final String TAG = "ApnEditor";
    private static final boolean DEBUG = Log.isLoggable();
    public static final String ACTION_EDIT = "link.zhidou.intent.action.EDIT";
    public static final String ACTION_INSERT = "link.zhidou.intent.action.INSERT";
    // main board
    private CommonActionBar mActionBar;

    // status
    public static final String SUB_ID = "sub_id";

    public static final String MVNO_TYPE = "mvno_type";
    public static final String MVNO_MATCH_DATA = "mvno_match_data";
    private final static String SAVED_POS = "pos";
    private final static String KEY_AUTH_TYPE = "auth_type";
    private final static String KEY_PROTOCOL = "apn_protocol";
    private final static String KEY_ROAMING_PROTOCOL = "apn_roaming_protocol";
    private final static String KEY_CARRIER_ENABLED = "carrier_enabled";
    private final static String KEY_BEARER_MULTI = "bearer_multi";
    private final static String KEY_MVNO_TYPE = "mvno_type";

    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_CANCEL = Menu.FIRST + 2;
    private static final int ERROR_DIALOG_ID = 0;
    /// M: show dialog when change default APN
    private static final int CONFIRM_CHANGE_DIALOG_ID = 1;

    private static final int NAME_REQC = 1;
    private static final int APN_REQC = NAME_REQC + 2;
    private static final int PROXY_REQC = NAME_REQC + 3;
    private static final int PORT_REQC = NAME_REQC + 4;
    private static final int USER_REQC = NAME_REQC + 5;
    private static final int SERVER_REQC = NAME_REQC + 6;
    private static final int PASSWORD_REQC = NAME_REQC + 7;
    private static final int MMSC_REQC = NAME_REQC + 8;
    private static final int MCC_REQC = NAME_REQC + 9;
    private static final int MNC_REQC = NAME_REQC + 10;
    private static final int MMS_PROXY_REQC = NAME_REQC + 11;
    private static final int MMS_PORT_REQC = NAME_REQC + 12;
    private static final int AUTH_TYPE_REQC = NAME_REQC + 13;
    private static final int APN_TYPE_REQC = NAME_REQC + 14;
    private static final int PROTOCOL_REQC = NAME_REQC + 15;
    private static final int ROAMING_PROTOCOL_REQC = NAME_REQC + 16;
    private static final int BEARER_REQC = NAME_REQC + 17;
    private static final int MVNO_TYPE_REQC = NAME_REQC + 18;
    private static final int MVNO_MATCH_DATA_REQC = NAME_REQC + 19;

    private static String sNotSet;
    private PreferenceBean mName;
    private PreferenceBean mApn;
    private PreferenceBean mProxy;
    private PreferenceBean mPort;
    private PreferenceBean mUser;
    private PreferenceBean mServer;
    private PreferenceBean mPassword;
    private PreferenceBean mMmsc;
    private PreferenceBean mMcc;
    private PreferenceBean mMnc;
    private PreferenceBean mMmsProxy;
    private PreferenceBean mMmsPort;

    private PreferenceBean mAuthType; //　单选

    private PreferenceBean mApnType;
    private PreferenceBean mProtocol; //　单选
    private PreferenceBean mRoamingProtocol; //　单选
    private PreferenceBean mBearerMulti; //　多选
    private PreferenceBean mMvnoType; //　单选
    private PreferenceBean mMvnoMatchData;


    private String mCurMnc;
    private String mCurMcc;

    private Uri mUri;
    private Cursor mCursor;
    private boolean mNewApn;
    private boolean mFirstTime;
    private int mSubId;
    private Resources mRes;
    private TelephonyManager mTelephonyManager;
    private int mBearerInitialVal = 0;
    private String mMvnoTypeStr;
    private String mMvnoMatchDataStr;

    public static final String BEARER_BITMASK = "bearer_bitmask";
//    public static final String SOURCE_TYPE = "source_type";


    // the source type for apn
    // by apns-config.xml
    public static final int SOURCE_TYPE_USER_EDIT = 1; // from user editing
    public static final int SOURCE_TYPE_OMACP = 2; // from OMACP
    public static final String TETHER_TYPE = "tethering";
    public static final String APN_TYPE = "apn_type";


    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static String[] sProjection = new String[] {
            Telephony.Carriers._ID,     // 0
            Telephony.Carriers.NAME,    // 1
            Telephony.Carriers.APN,     // 2
            Telephony.Carriers.PROXY,   // 3
            Telephony.Carriers.PORT,    // 4
            Telephony.Carriers.USER,    // 5
            Telephony.Carriers.SERVER,  // 6
            Telephony.Carriers.PASSWORD, // 7
            Telephony.Carriers.MMSC, // 8
            Telephony.Carriers.MCC, // 9
            Telephony.Carriers.MNC, // 10
            Telephony.Carriers.NUMERIC, // 11
            Telephony.Carriers.MMSPROXY,// 12
            Telephony.Carriers.MMSPORT, // 13
            Telephony.Carriers.AUTH_TYPE, // 14
            Telephony.Carriers.TYPE, // 15
            Telephony.Carriers.PROTOCOL, // 16
            Telephony.Carriers.CARRIER_ENABLED, // 17
            Telephony.Carriers.BEARER, // 18
            BEARER_BITMASK, // 19
            Telephony.Carriers.ROAMING_PROTOCOL, // 20
            Telephony.Carriers.MVNO_TYPE,   // 21
            Telephony.Carriers.MVNO_MATCH_DATA,  // 22
    };

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int PROXY_INDEX = 3;
    private static final int PORT_INDEX = 4;
    private static final int USER_INDEX = 5;
    private static final int SERVER_INDEX = 6;
    private static final int PASSWORD_INDEX = 7;
    private static final int MMSC_INDEX = 8;
    private static final int MCC_INDEX = 9;
    private static final int MNC_INDEX = 10;
    private static final int MMSPROXY_INDEX = 12;
    private static final int MMSPORT_INDEX = 13;
    private static final int AUTH_TYPE_INDEX = 14;
    private static final int TYPE_INDEX = 15;
    private static final int PROTOCOL_INDEX = 16;
    private static final int CARRIER_ENABLED_INDEX = 17;
    private static final int BEARER_INDEX = 18;
    private static final int BEARER_BITMASK_INDEX = 19;
    private static final int ROAMING_PROTOCOL_INDEX = 20;
    private static final int MVNO_TYPE_INDEX = 21;
    private static final int MVNO_MATCH_DATA_INDEX = 22;
    /// M: [APN Source Type]
//    private static final int SOURCE_TYPE_INDEX = 23;



    ///----------------------------------------MTK------------------------------------------------
    /// M: for [SIM Hot Swap]
    private SimHotSwapHandler mSimHotSwapHandler;
    private boolean mReadOnlyMode = false;

    /**Views */
    ApnEditorListAdapter mAdapter;
    ListView mListView;
    ArrayList<PreferenceBean> mPrefBeans = new ArrayList<>();
    /** Views */

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (DEBUG) {
                Log.d(TAG, "background changed apn ");
            }
            mFirstTime = true;
            try {
                stopManagingCursor(mCursor);
            } finally {
                if (mCursor != null) {
                    mCursor.close();
                }
            }
            mCursor = managedQuery(mUri, sProjection, null, null, null);
            mCursor.moveToFirst();
            fillUi();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PreferenceBean bean = mAdapter.getItem(position);
        if (bean.isEnable()) {
            bean.handleClick(this);
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_apn_editor);
        // Booting board
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mActionBar.setSettingsPressedListener(this);
        mActionBar.setTitle(R.string.apn_edit);

        mListView = findViewById(R.id.lv);
        mRes = getResources();
        sNotSet = getResources().getString(R.string.apn_not_set);
        mName = new PreferenceBean(NAME_REQC, getString(R.string.apn_name));
        mPrefBeans.add(mName);
        // @{ apn type
        mApnType = new PreferenceBean(APN_TYPE_REQC, getString(R.string.apn_type), mRes.getStringArray(R.array.apn_type_generic), mRes.getStringArray(R.array.apn_type_generic), true);
        mPrefBeans.add(mApnType);
        // @}
        mApn = new PreferenceBean(APN_REQC, getString(R.string.apn_apn));
        mPrefBeans.add(mApn);
        mProxy = new PreferenceBean(PROXY_REQC, getString(R.string.apn_http_proxy));
        mPrefBeans.add(mProxy);
        mPort = new PreferenceBean(PORT_REQC, getString(R.string.apn_http_port));
        mPrefBeans.add(mPort);
        mUser = new PreferenceBean(USER_REQC, getString(R.string.apn_user));
        mPrefBeans.add(mUser);
        mServer = new PreferenceBean(SERVER_REQC, getString(R.string.apn_server));
        mPrefBeans.add(mServer);
        mPassword = new PreferenceBean(PASSWORD_REQC, getString(R.string.apn_password));
        mPrefBeans.add(mPassword);
        mMmsProxy = new PreferenceBean(MMS_PROXY_REQC, getString(R.string.apn_mms_proxy));
        mPrefBeans.add(mMmsProxy);
        mMmsPort = new PreferenceBean(MMS_PORT_REQC, getString(R.string.apn_mms_port));
        mPrefBeans.add(mMmsPort);
        mMmsc = new PreferenceBean(MMSC_REQC, getString(R.string.apn_mmsc));
        mPrefBeans.add(mMmsc);
        mMcc = new PreferenceBean(MCC_REQC, getString(R.string.apn_mcc));
        mPrefBeans.add(mMcc);
        mMnc = new PreferenceBean(MNC_REQC, getString(R.string.apn_mnc));
        mPrefBeans.add(mMnc);
        mAuthType = new PreferenceBean(AUTH_TYPE_REQC, getString(R.string.apn_auth_type), mRes.getStringArray(R.array.apn_auth_entries), mRes.getStringArray(R.array.apn_auth_values));
        mPrefBeans.add(mAuthType);
        mProtocol = new PreferenceBean(PROTOCOL_REQC, getString(R.string.apn_protocol), mRes.getStringArray(R.array.apn_protocol_entries), mRes.getStringArray(R.array.apn_protocol_values));
        mPrefBeans.add(mProtocol);
        mRoamingProtocol = new PreferenceBean(ROAMING_PROTOCOL_REQC, getString(R.string.apn_roaming_protocol), mRes.getStringArray(R.array.apn_protocol_entries), mRes.getStringArray(R.array.apn_protocol_values));
        mPrefBeans.add(mRoamingProtocol);
        mBearerMulti = new PreferenceBean(BEARER_REQC, getString(R.string.bearer), mRes.getStringArray(R.array.bearer_entries), mRes.getStringArray(R.array.bearer_values), true);
        mPrefBeans.add(mBearerMulti);
        mMvnoType = new PreferenceBean(MVNO_TYPE_REQC, getString(R.string.mvno_type), mRes.getStringArray(R.array.ext_mvno_type_entries), mRes.getStringArray(R.array.ext_mvno_type_values));
        mPrefBeans.add(mMvnoType);
        mMvnoMatchData = new PreferenceBean(MVNO_MATCH_DATA_REQC, getString(R.string.mvno_match_data));
        mPrefBeans.add(mMvnoMatchData);

        //
        mAdapter = new ApnEditorListAdapter(this, mPrefBeans);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        mSubId = intent.getIntExtra(SUB_ID, SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        if (DEBUG) {
            Log.d(TAG, "subId: " + mSubId);
        }
        mFirstTime = icicle == null;

        if (action.equals(ACTION_EDIT)) {
            mUri = intent.getData();
            /// M: for [Read Only APN], some APN can not be edited
            mReadOnlyMode = intent.getBooleanExtra("readOnly", false);
            Log.d(TAG, "Read only mode : " + mReadOnlyMode);
        } else if (action.equals(ACTION_INSERT)) {
            if (mFirstTime || icicle.getInt(SAVED_POS) == 0) {
                mUri = getContentResolver().insert(intent.getData(), new ContentValues());
            } else {
                mUri = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI,
                        icicle.getInt(SAVED_POS));
            }
            mNewApn = true;
            mMvnoTypeStr = intent.getStringExtra(MVNO_TYPE);
            mMvnoMatchDataStr = intent.getStringExtra(MVNO_MATCH_DATA);
            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.w(TAG, "Failed to insert new telephony provider into "
                        + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            finish();
            return;
        }

        if (DEBUG) {
            Log.d(TAG, "uri: " + mUri);
        }
        mCursor = managedQuery(mUri, sProjection, null, null, null);
        mCursor.moveToFirst();

        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        fillUi();

        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new SimHotSwapHandler.OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (DEBUG) {
                    Log.d(TAG, "onSimHotSwap, finish Activity~~");
                }
                finish();
            }
        });
        /// @}

        /// M: @{
        if (!mNewApn) {
            getContentResolver().registerContentObserver(mUri, true, mContentObserver);
        }
        /// @}
    }

    @Override
    protected void onDestroy() {
        if (!mNewApn) {
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        super.onDestroy();
    }

    private void fillUi() {
        /// M: @{
        if (mCursor.getCount() == 0) {
            Log.w(TAG, "fillUi(), cursor count is 0, finish~~");
            finish();
            return;
        }
        /// @}

        if (mFirstTime) {
            mFirstTime = false;
            // Fill in all the values from the db in both text editor and summary
            mName.setPref(mCursor.getString(NAME_INDEX));
            mApn.setPref(mCursor.getString(APN_INDEX));
            mProxy.setPref(mCursor.getString(PROXY_INDEX));
            mPort.setPref(mCursor.getString(PORT_INDEX));
            mUser.setPref(mCursor.getString(USER_INDEX));
            mServer.setPref(mCursor.getString(SERVER_INDEX));
            mPassword.setPref(mCursor.getString(PASSWORD_INDEX));
            mMmsProxy.setPref(mCursor.getString(MMSPROXY_INDEX));
            mMmsPort.setPref(mCursor.getString(MMSPORT_INDEX));
            mMmsc.setPref(mCursor.getString(MMSC_INDEX));
            mMcc.setPref(mCursor.getString(MCC_INDEX));
            mMnc.setPref(mCursor.getString(MNC_INDEX));
            String strType = mCursor.getString(TYPE_INDEX);
            if (DEBUG) {
                Log.d(TAG, "Apn type from cursor: " + strType);
            }
            mApnType.setPref(checkNull(strType));
            if (mNewApn) {
                String numeric = null;
                try {
                    numeric = (String) MethodUtils.invokeMethod(mTelephonyManager, "getSimOperator", new Object[]{mSubId});
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // MCC is first 3 chars and then in 2 - 3 chars of MNC
                if (numeric != null && numeric.length() > 4) {
                    // Country code
                    String mcc = numeric.substring(0, 3);
                    // Network code
                    String mnc = numeric.substring(3);
                    // Auto populate MNC and MCC for new entries, based on what SIM reports
                    mMcc.setPref(mcc);
                    mMnc.setPref(mnc);
                    mCurMnc = mnc;
                    mCurMcc = mcc;
                }
                String apnType = getIntent().getStringExtra(APN_TYPE);
                if (TETHER_TYPE.equals(apnType)) {
                    mApnType.setPref("tethering");
                } else {
                    mApnType.setPref("default");
                }
            }
            int authVal = mCursor.getInt(AUTH_TYPE_INDEX);
            if (DEBUG) {
                Log.d(TAG, "authVal: " + authVal);
            }
            if (authVal != -1) {
                mAuthType.setPrefIndex(authVal);
            } else {
                mAuthType.setPref(null);
            }

            mProtocol.setPref(mCursor.getString(PROTOCOL_INDEX));
            mRoamingProtocol.setPref(mCursor.getString(ROAMING_PROTOCOL_INDEX));
            mBearerInitialVal = mCursor.getInt(BEARER_INDEX);
            HashSet<String> bearers = new HashSet<String>();
            int bearerBitmask = mCursor.getInt(BEARER_BITMASK_INDEX);
            if (DEBUG) {
                Log.d(TAG, "mBearerInitialVal: " + mBearerInitialVal + ", bearerBitmask: " + bearerBitmask);
            }
            if (bearerBitmask == 0) {
                if (mBearerInitialVal == 0) {
                    bearers.add("" + 0);
                }
            } else {
                int i = 1;
                while (bearerBitmask != 0) {
                    if ((bearerBitmask & 1) == 1) {
                        bearers.add("" + i);
                    }
                    bearerBitmask >>= 1;
                    i++;
                }
            }

            if (mBearerInitialVal != 0 && bearers.contains("" + mBearerInitialVal) == false) {
                // add mBearerInitialVal to bearers
                bearers.add("" + mBearerInitialVal);
            }
            Iterator<String> iterator = bearers.iterator();
            final StringBuilder sb = new StringBuilder();
            while (iterator.hasNext()) {
                sb.append(iterator.next());
                if (iterator.hasNext()) {
                    sb.append(",");
                }
            }
            if (DEBUG) {
                Log.d(TAG, "bearers: " + sb);
            }
            mBearerMulti.setPref(sb.toString()); // Check more
            mMvnoType.setPref(mCursor.getString(MVNO_TYPE_INDEX));
            mMvnoMatchData.setEnable(false);
            mMvnoMatchData.setPref(mCursor.getString(MVNO_MATCH_DATA_INDEX));
            if (mNewApn && mMvnoTypeStr != null && mMvnoMatchDataStr != null) {
                mMvnoType.setPref(mMvnoTypeStr);
                mMvnoMatchData.setPref(mMvnoMatchDataStr);
            }
        }
        mName.setSummary(checkNull(mName.getPref()));
        mApn.setSummary(checkNull(mApn.getPref()));
        mProxy.setSummary(checkNull(mProxy.getPref()));
        mPort.setSummary(checkNull(mPort.getPref()));
        mUser.setSummary(checkNull(mUser.getPref()));
        mServer.setSummary(checkNull(mServer.getPref()));
        mPassword.setSummary(starify(mPassword.getPref()));
        mMmsProxy.setSummary(checkNull(mMmsProxy.getPref()));
        mMmsPort.setSummary(checkNull(mMmsPort.getPref()));
        mMmsc.setSummary(checkNull(mMmsc.getPref()));
        mMcc.setSummary(checkNull(mMcc.getPref()));
        mMnc.setSummary(checkNull(mMnc.getPref()));
        mApnType.setSummary(checkNull(mApnType.getPref()));

        String authVal = mAuthType.getPref();
        if (authVal != null) {
            int authValIndex = Integer.parseInt(authVal);
            mAuthType.setPrefIndex(authValIndex);

            String []values = mRes.getStringArray(R.array.apn_auth_entries);
            mAuthType.setSummary(values[authValIndex]);
        } else {
            mAuthType.setSummary(sNotSet);
        }

        mProtocol.setSummary(
                checkNull(protocolDescription(mProtocol.getPref(), mProtocol)));
        mRoamingProtocol.setSummary(
                checkNull(protocolDescription(mRoamingProtocol.getPref(), mRoamingProtocol)));
        mBearerMulti.setSummary(
                checkNull(bearerMultiDescription(mBearerMulti.getPref().split(","))));
        mMvnoType.setSummary(
                checkNull(mvnoDescription(mMvnoType.getPref())));
        mMvnoMatchData.setSummary(checkNull(mMvnoMatchData.getPref()));
        mAdapter.notifyDataSetChanged();
    }


    private String mvnoDescription(String newValue) {
        int mvnoIndex = mMvnoType.findIndexOfValue(newValue);
        if (DEBUG) {
            Log.d(TAG, "newValue: " + newValue + ", mvnoIndex: " + mvnoIndex);
        }
        if (mvnoIndex == -1) {
            return null;
        } else {
            final String[] entries = mRes.getStringArray(R.array.ext_mvno_type_entries);
            final String[] values = mRes.getStringArray(R.array.ext_mvno_type_values);
            if (TextUtils.isEmpty(values[mvnoIndex])) {
                mMvnoMatchData.setEnable(false);
            } else {
                mMvnoMatchData.setEnable(true);
            }
            if (newValue != null) {
                if (values[mvnoIndex].equals("spn")) {
                    String data = mTelephonyManager.getSimOperatorName();
                    mMvnoMatchData.setPref(data);
                    mMvnoMatchData.setSummary(data);
                } else if (values[mvnoIndex].equals("imsi")) {
                    try {
                        String numeric = (String) MethodUtils.invokeMethod(mTelephonyManager, "getSimOperator", new Object[] {mSubId});
                        mMvnoMatchData.setPref(numeric + "x");
                        mMvnoMatchData.setSummary(numeric + "x");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (values[mvnoIndex].equals("gid")) {
                    try {
                        final String data = (String) MethodUtils.invokeMethod(mTelephonyManager, "getGroupIdLevel1");
                        mMvnoMatchData.setPref(data);
                        mMvnoMatchData.setSummary(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return entries[mvnoIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private String bearerMultiDescription(String[] raw) {
        String[] entries = mRes.getStringArray(R.array.bearer_entries);
        StringBuilder retVal = new StringBuilder();
        boolean first = true;
        for (String bearer : raw) {
            int bearerIndex = mBearerMulti.findIndexOfValue(bearer);
            try {
                if (first) {
                    retVal.append(entries[bearerIndex]);
                    first = false;
                } else {
                    retVal.append(", " + entries[bearerIndex]);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // ignore
            }
        }
        String val = retVal.toString();
        if (!TextUtils.isEmpty(val)) {
            return val;
        }
        return null;
    }

    /**
     * Returns the UI choice (e.g., "IPv4/IPv6") corresponding to the given
     * raw value of the protocol preference (e.g., "IPV4V6"). If unknown,
     * return null.
     */
    private String protocolDescription(String raw, PreferenceBean protocol) {
        int protocolIndex = protocol.findIndexOfValue(raw);
        if (protocolIndex == -1) {
            return null;
        } else {
            String[] values = mRes.getStringArray(R.array.apn_protocol_entries);
            try {
                return values[protocolIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    /**
     * Check the key fields' validity and save if valid.
     * @param force save even if the fields are not valid, if the app is
     *        being suspended
     * @return true if the data was saved
     */
    private boolean validateAndSave(boolean force) {
        String name = checkNotSet(mName.getPref());
        String apn = checkNotSet(mApn.getPref());
        String mcc = checkNotSet(mMcc.getPref());
        String mnc = checkNotSet(mMnc.getPref());

        if (getErrorMsg() != null && !force) {
            showDialog(ERROR_DIALOG_ID);
            return false;
        }

        if (!mCursor.moveToFirst() && !mNewApn) {
            Log.w(TAG,
                    "Could not go to the first row in the Cursor when saving data.");
            return false;
        }

        // If it's a new APN and a name or apn haven't been entered, then erase the entry
        if (force && mNewApn && name.length() < 1 && apn.length() < 1 && mUri != null) {
            getContentResolver().delete(mUri, null, null);
            /// M:
            mUri = null;
            return false;
        }

        ContentValues values = new ContentValues();

        // Add a dummy name "Untitled", if the user exits the screen without adding a name but
        // entered other information worth keeping.
        values.put(Telephony.Carriers.NAME, name.length() < 1 ? getResources().getString(R.string.untitled_apn) : name);
        values.put(Telephony.Carriers.APN, apn);
        values.put(Telephony.Carriers.PROXY, checkNotSet(mProxy.getPref()));
        values.put(Telephony.Carriers.PORT, checkNotSet(mPort.getPref()));
        values.put(Telephony.Carriers.MMSPROXY, checkNotSet(mMmsProxy.getPref()));
        values.put(Telephony.Carriers.MMSPORT, checkNotSet(mMmsPort.getPref()));
        values.put(Telephony.Carriers.USER, checkNotSet(mUser.getPref()));
        values.put(Telephony.Carriers.SERVER, checkNotSet(mServer.getPref()));
        values.put(Telephony.Carriers.PASSWORD, checkNotSet(mPassword.getPref()));
        values.put(Telephony.Carriers.MMSC, checkNotSet(mMmsc.getPref()));

        String authVal = mAuthType.getPref();
        if (authVal != null) {
            values.put(Telephony.Carriers.AUTH_TYPE, Integer.parseInt(authVal));
        }
        values.put(Telephony.Carriers.PROTOCOL, checkNotSet(mProtocol.getPref()));
        values.put(Telephony.Carriers.ROAMING_PROTOCOL, checkNotSet(mRoamingProtocol.getPref()));
        values.put(Telephony.Carriers.TYPE, checkNotSet(mApnType.getPref()));
        values.put(Telephony.Carriers.MCC, mcc);
        values.put(Telephony.Carriers.MNC, mnc);
        values.put(Telephony.Carriers.NUMERIC, mcc + mnc);
        if (mCurMnc != null && mCurMcc != null) {
            if (mCurMnc.equals(mnc) && mCurMcc.equals(mcc)) {
                values.put(Telephony.Carriers.CURRENT, 1);
            }
        }

        /** Need check more */
        String[] bearerSet = mBearerMulti.getPref().split(",");
        int bearerBitmask = 0;
        for (String bearer : bearerSet) {
            if (Integer.parseInt(bearer) == 0) {
                bearerBitmask = 0;
                break;
            } else {
                try {
                    bearerBitmask |= (int) MethodUtils.invokeStaticMethod(ServiceState.class, "getBitmaskForTech", Integer.parseInt(bearer));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        values.put(BEARER_BITMASK, bearerBitmask);

        int bearerVal;
        try {
            if (bearerBitmask == 0 || mBearerInitialVal == 0) {
                bearerVal = 0;
            } else if ((boolean) MethodUtils.invokeStaticMethod(ServiceState.class, "getBitmaskForTech", bearerBitmask, mBearerInitialVal)) {
                bearerVal = mBearerInitialVal;
            } else {
                // bearer field was being used but bitmask has changed now and does not include the
                // initial bearer value -- setting bearer to 0 but maybe better behavior is to choose a
                // random tech from the new bitmask??
                bearerVal = 0;
            }
            values.put(Telephony.Carriers.BEARER, bearerVal);
        } catch (Exception e) {
            e.printStackTrace();
        }

        values.put(Telephony.Carriers.MVNO_TYPE, checkNotSet(mMvnoType.getPref()));
        values.put(Telephony.Carriers.MVNO_MATCH_DATA, checkNotSet(mMvnoMatchData.getPref()));
        /// M: firstly insert ,then update if need {@
        if (mUri == null) {
            Log.i(TAG, "former inserted URI was already deleted, insert a new one");
            mUri = getContentResolver().insert(getIntent().getData(), new ContentValues());
        } else {
            /// @}
            getContentResolver().update(mUri, values, null, null);
        }

        return true;
    }

    private String getErrorMsg() {
        String errorMsg = null;

        String name = checkNotSet(mName.getPref());
        String apn = checkNotSet(mApn.getPref());
        String mcc = checkNotSet(mMcc.getPref());
        String mnc = checkNotSet(mMnc.getPref());
        /// M: for [APN Type List]
        String apnType = mApnType.getPref();

        if (name.length() < 1) {
            errorMsg = mRes.getString(R.string.error_name_empty);
            /// M: not check apn length if type is "ia",
            /// as default ia type, the apn item maybe null
        } else if ((apnType == null || !apnType.contains("ia")) && apn.length() < 1) {
            errorMsg = mRes.getString(R.string.error_apn_empty);
        } else if (mcc.length() != 3) {
            errorMsg = mRes.getString(R.string.error_mcc_not3);
        } else if ((mnc.length() & 0xFFFE) != 2) {
            errorMsg = mRes.getString(R.string.error_mnc_not23);
        }

        return errorMsg;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();

            return new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setPositiveButton(android.R.string.ok, null)
                    .setMessage(msg)
                    .create();
            /// M: add confirm dialog
        } else if (id == CONFIRM_CHANGE_DIALOG_ID) {
            return  new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.error_title)
                    .setMessage(getString(R.string.apn_predefine_change_dialog_notice))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (validateAndSave(false)) {
                                finish();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();

            if (msg != null) {
                ((AlertDialog)dialog).setMessage(msg);
            }
        }
    }

    private void deleteApn() {
        if (mUri != null) {
            getContentResolver().delete(mUri, null, null);
        }
        finish();
    }

    private String starify(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            char[] password = new char[value.length()];
            for (int i = 0; i < password.length; i++) {
                password[i] = '*';
            }
            return new String(password);
        }
    }

    private String checkNull(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            return value;
        }
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals(sNotSet)) {
            return "";
        } else {
            return value;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (validateAndSave(true) && mCursor != null && mCursor.getCount() != 0) {
            icicle.putInt(SAVED_POS, mCursor.getInt(ID_INDEX));
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(TAG, "requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
        if (RESULT_OK == resultCode) {
            String value;
            switch (requestCode) {
                case NAME_REQC:
                   value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mName.setPref(value);
                    mName.setSummary(checkNull(value));
                    mAdapter.notifyDataSetChanged();
                    break;
                case APN_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mApn.setPref(value);
                    mApn.setSummary(checkNull(mApn.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case PROXY_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mProxy.setPref(value);
                    mProxy.setSummary(checkNull(mProxy.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case PORT_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mPort.setPref(value);
                    mPort.setSummary(checkNull(mPort.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case USER_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mUser.setPref(value);
                    mUser.setSummary(checkNull(mUser.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case SERVER_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mServer.setPref(value);
                    mServer.setSummary(checkNull(mServer.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case PASSWORD_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mPassword.setPref(value);
                    mPassword.setSummary(starify(mPassword.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case MMS_PROXY_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMmsProxy.setPref(value);
                    mMmsProxy.setSummary(checkNull(mMmsProxy.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case MMS_PORT_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMmsPort.setPref(value);
                    mMmsPort.setSummary(checkNull(mMmsPort.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case MMSC_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMmsc.setPref(value);
                    mMmsc.setSummary(checkNull(mMmsc.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case MCC_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMcc.setPref(value);
                    mMcc.setSummary(checkNull(mMcc.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case MNC_REQC:
                    value =  data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMnc.setPref(value);
                    mMnc.setSummary(checkNull(mMnc.getPref()));
                    mAdapter.notifyDataSetChanged();
                    break;
                case APN_TYPE_REQC:
                     value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                     if (value != null) {
                         mApnType.setPref(value);
                         mApnType.setSummary(checkNull(mApnType.getPref()));
                         mAdapter.notifyDataSetChanged();
                     }
                     break;
                case AUTH_TYPE_REQC:
                    value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    if (value != null) {
                        mAuthType.setPref(value);
                        String authVal = mAuthType.getPref();
                        if (authVal != null) {
                            int authValIndex = Integer.parseInt(authVal);
                            mAuthType.setPrefIndex(authValIndex);
                            String[] values = mRes.getStringArray(R.array.apn_auth_entries);
                            mAuthType.setSummary(values[authValIndex]);
                        } else {
                            mAuthType.setSummary(sNotSet);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case PROTOCOL_REQC:
                    value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    if (value != null) {
                        mProtocol.setPref(value);
                        mProtocol.setSummary(
                                checkNull(protocolDescription(mProtocol.getPref(), mProtocol)));
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case ROAMING_PROTOCOL_REQC:
                    value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    if (value != null) {
                        mRoamingProtocol.setPref(value);
                        mRoamingProtocol.setSummary(checkNull(protocolDescription(mRoamingProtocol.getPref(), mRoamingProtocol)));
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case BEARER_REQC:
                    value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    if (value != null) {
                        mBearerMulti.setPref(value);
                        mBearerMulti.setSummary(checkNull(bearerMultiDescription(mBearerMulti.getPref().split(","))));
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case MVNO_TYPE_REQC:
                    value = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    if (value != null) {
                        mMvnoType.setPref(value);
                        mMvnoType.setSummary(checkNull(mvnoDescription(mMvnoType.getPref())));
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                case MVNO_MATCH_DATA_REQC:
                    value = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mMvnoMatchData.setPref(value);
                    mMvnoMatchData.setSummary(checkNull(mMvnoMatchData.getPref()));
                    break;
                default:
                    // Do nothing.

            }
        }
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
        inflater.inflate(R.menu.menu_apn_editor, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                deleteApn();
                return true;
            case R.id.menu_save:
                if (validateAndSave(false)) {
                    finish();
                }
                return true;
            case R.id.menu_cancel:
                if (mNewApn && mUri != null) {
                    getContentResolver().delete(mUri, null, null);
                }
                finish();
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (validateAndSave(false)) {
            super.onBackPressed();
        }
    }

}
