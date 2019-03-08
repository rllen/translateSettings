package link.zhidou.translator.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;

/**
 * Date: 18-1-10
 * Time: 上午10:07
 * Email: lostsearover@gmail.com
 */

public class AddWifiActivity extends ActionBarBaseActivity implements View.OnClickListener  {

    public static final String EXTRA_SSID = "extra-ssid";
    public static final String EXTRA_SECURITY_TYPE = "extra-security-type";
    public static final String EXTRA_PASSWORD = "extra-password";
    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    public static final int SECURITY_NONE = 0;
    public static final int SECURITY_WEP = 1;
    public static final int SECURITY_PSK = 2;
    private static final int SSID_REQC = 999;
    private static final int SECURITY_TYPE_REQC = 1000;
    private static final int PASSWORD_REQC = 1001;
    private TextView mSsidSummaryView;
    private TextView mPasswordView;
    private TextView mSecurityTypeSummary;
    private View mSecurityFields;
    private String mSsid = "";
    private String mPassword = "";
    private int mAccessPointSecurity = SECURITY_NONE;
    private String mSecurityType;
    private String[] mSecurityOptions = null;
    private View mSubmit = null;
    private float mDisabledAlpha;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wifi);
        setActionBarTitle(this);
        mSecurityType = getString(R.string.wifi_security_none);
        mSecurityOptions = getResources().getStringArray(R.array.wifi_security);
        mSsidSummaryView = findViewById(R.id.ssid_summary);
        mSsidSummaryView.setText(mSsid);
        mSecurityTypeSummary = findViewById(R.id.security_type_summary);
        mSecurityTypeSummary.setText(mSecurityOptions[mAccessPointSecurity]);
        mSubmit = findViewById(R.id.submit);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
        mDisabledAlpha = typedValue.getFloat();
        findViewById(R.id.ssid_field).setOnClickListener(this);
        findViewById(R.id.security_type_fields).setOnClickListener(this);
        findViewById(R.id.security_fields).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.submit).setEnabled(false);
        findViewById(R.id.submit).setAlpha(mDisabledAlpha);
    }

    /* show submit button if password, ip and proxy settings are valid */
    void enableSubmitIfAppropriate() {
        boolean enabled = isSubmittable();
        mSubmit.setEnabled(enabled);
        mSubmit.setAlpha(enabled ? 1.0f : mDisabledAlpha);
    }

    private void showSecurityFields() {
        if(SECURITY_NONE == mAccessPointSecurity) {
            findViewById(R.id.security_fields).setVisibility(View.GONE);
            findViewById(R.id.security_fields_divider).setVisibility(View.GONE);
            mPassword = "";
            mPasswordView = null;
            return;
        }
        findViewById(R.id.security_fields).setOnClickListener(this);
        findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
        findViewById(R.id.security_fields_divider).setVisibility(View.VISIBLE);
        if (null == mPasswordView) {
            mPasswordView = findViewById(R.id.password);
        }
    }

    boolean isValidPsk(String password) {
        if (password.length() == 64 && password.matches("[0-9A-Fa-f]{64}")) {
            return true;
        } else if (password.length() >= 8 && password.length() <= 63) {
            return true;
        }
        return false;
    }


    boolean isSubmittable() {
        boolean enabled = false;
        boolean passwordInvalid = false;
        if (mPasswordView != null
                && (((mAccessPointSecurity == SECURITY_WEP && mPasswordView.length() == 0))
                || (mAccessPointSecurity == SECURITY_PSK && !isValidPsk(mPassword)))) {
            passwordInvalid = true;
        }
        if ((TextUtils.isEmpty(mSsid)) || passwordInvalid) {
            enabled = false;
        } else {
            enabled = true;
        }

        return enabled;
    }


    public String getPassword() {
        switch (mAccessPointSecurity) {
            case SECURITY_NONE:
                return "";
            case SECURITY_WEP:
                if (!TextUtils.isEmpty(mPassword)) {
                    String password = mPassword;
                    int length = mPassword.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && password.matches("[0-9A-Fa-f]*")) {
                        return password;
                    } else {
                        return  '"' + password + '"';
                    }
                }
            case SECURITY_PSK:
                if (!TextUtils.isEmpty(mPassword)) {
                    String password = mPassword;
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        return password;
                    } else {
                        return  '"' + password + '"';
                    }
                }
            default:
                return "";
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.security_type_fields:
                Intent intent = new Intent(this, SingleOrMultipleChoiceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_TITLE, getString(R.string.wifi_security));
                bundle.putInt(SingleOrMultipleChoiceActivity.EXTRA_MODE, SingleOrMultipleChoiceActivity.SINGLE);
                bundle.putBoolean(SingleOrMultipleChoiceActivity.EXTRA_FINISH_NOW, true);
                bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_ENTRIES, mSecurityOptions);
                bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_VALUES, mSecurityOptions);
                bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_PREF, mSecurityType);
                intent.putExtras(bundle);
                startActivityForResult(intent, SECURITY_TYPE_REQC);
                break;
            case R.id.ssid_field:
                intent = new Intent(this, KeyboardActivity.class);
                intent.putExtra(KeyboardActivity.EXTRA_TITLE, getString(R.string.wifi_ssid));
                intent.putExtra(KeyboardActivity.EXTRA_IN_VALUE, TextUtils.isEmpty(mSsid) ? "" : mSsid);
                startActivityForResult(intent, SSID_REQC);
                break;
            case R.id.security_fields:
                intent = new Intent(this, KeyboardActivity.class);
                intent.putExtra(KeyboardActivity.EXTRA_TITLE, getString(R.string.wifi_password));
                intent.putExtra(KeyboardActivity.EXTRA_IN_VALUE, TextUtils.isEmpty(mPassword) ? "" : mPassword);
                startActivityForResult(intent, PASSWORD_REQC);
                break;
            case R.id.submit:
                intent = new Intent();
                intent.putExtra(EXTRA_SSID, mSsid);
                intent.putExtra(EXTRA_SECURITY_TYPE, mAccessPointSecurity);
                intent.putExtra(EXTRA_PASSWORD, mPassword);
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }


    private int getAccessPointSecurity(String securityType) {
        for (int i = 0; i < mSecurityOptions.length; i++) {
            if (securityType.equals(mSecurityOptions[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK == resultCode) {
            switch (requestCode) {
                case SECURITY_TYPE_REQC:
                    mSecurityType = data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF);
                    mAccessPointSecurity = getAccessPointSecurity(mSecurityType);
                    mSecurityTypeSummary.setText(mSecurityOptions[mAccessPointSecurity]);
                    showSecurityFields();
                    enableSubmitIfAppropriate();
                    break;
                case SSID_REQC:
                    mSsid = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mSsidSummaryView.setText(mSsid);
                    enableSubmitIfAppropriate();
                    break;
                case PASSWORD_REQC:
                    mPassword = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
                    mPasswordView.setText(mPassword);
                    enableSubmitIfAppropriate();
                    break;
                default:
                    break;
            }
        }
    }


}
