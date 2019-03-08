package link.zhidou.translator.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import link.zhidou.appupdate.service.AppUpdateService;
import link.zhidou.translator.BuildConfig;
import link.zhidou.translator.Config;
import link.zhidou.translator.R;
import link.zhidou.translator.SpeechApp;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SingleToast;
import link.zhidou.translator.utils.Util;

/**
 * Created by keetom on 2017/10/26.
 */

public class AboutActivity extends ActionBarBaseActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();
    private static boolean DEBUG = Log.isLoggable();
    private int mAdbHitCountdown = 12;
    private int mLogoHitCountdown = 12;
    private int mTitleHitCountdown = 12;
    private AdbObserver mAdbObserver = null;
    private int REQUEST_ADB = 0xf0;
    private int REQUEST_HOME = 0xf1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        initData();
        TextView versionText = (TextView) findViewById(R.id.version);
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//            String formatVersion = String.format(getString(R.string.version_text), version);
            String formatVersion = version;
            if (BuildConfig.YOUDAO_TRANS) {
                formatVersion += "_youdao";
            } else if (BuildConfig.BAIDU_TRANS){
                formatVersion += "_baidu";
            } else if (BuildConfig.BING_TRANS) {
                formatVersion += "_bing";
            }
            versionText.setText(formatVersion);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TextView sn = (TextView) findViewById(R.id.sn);
        sn.setText(Config.from(this).getSerial());

        TextView model = (TextView) findViewById(R.id.model);
        model.setText(Build.MODEL);
        
        if (!Util.isWifiOnly(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                int phoneCount = tm.getPhoneCount();
                if (phoneCount > 1) {
                    phoneCount = 1;
                }
                View first = findViewById(R.id.first);
                TextView firstTitle = findViewById(R.id.first_title);
                TextView firstContent = findViewById(R.id.first_content);
                View second = findViewById(R.id.second);
                TextView secondTitle = findViewById(R.id.second_title);
                TextView secondContent = findViewById(R.id.second_content);
                if (phoneCount == 0) {
                    first.setVisibility(View.GONE);
                    second.setVisibility(View.GONE);
                } else if (1 == phoneCount) {
                    second.setVisibility(View.GONE);
                    String imei = tm.getImei(0);
                    firstTitle.setText(R.string.imei);
                    firstContent.setText(imei);
                    first.setVisibility(View.VISIBLE);
                } else if (2 == phoneCount) {
                    String imei = tm.getImei(0);
                    firstTitle.setText(R.string.imei_1);
                    firstContent.setText(imei);
                    first.setVisibility(View.VISIBLE);
                    String imei2 = tm.getImei(1);
                    secondTitle.setText(R.string.imei_2);
                    secondContent.setText(imei2);
                    second.setVisibility(View.VISIBLE);
                }

            } else {
                // TODO
            }
        } else {
            View first = findViewById(R.id.first);
            View second = findViewById(R.id.second);
            first.setVisibility(View.GONE);
            second.setVisibility(View.GONE);
        }

        findViewById(R.id.desc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdbHitCountdown--;
                if (mAdbHitCountdown <= 0) {
                    mAdbHitCountdown = 12;
                    enterPassword(REQUEST_ADB);
                }
            }
        });

        findViewById(R.id.iv_aboat_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLogoHitCountdown--;
                if (mLogoHitCountdown <= 0) {
                    mLogoHitCountdown = 12;
                    enterPassword(REQUEST_HOME);
                }
            }
        });

        mAdbObserver = new AdbObserver(this, new Handler(Looper.getMainLooper()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), true, mAdbObserver);
        } else {
            getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Global.ADB_ENABLED), true, mAdbObserver);
        }

        //应用更新提醒
        startService(AppUpdateService.getIntent(this));
    }

    private void initData() {
        setActionBarTitle(AboutActivity.this);

        mActionBar.setTitlePressedListener(new CommonActionBar.TitlePressedListener() {
            @Override
            public void onTitlePressed(View view) {
                mTitleHitCountdown --;
                if (mTitleHitCountdown <= 0) {
                    mTitleHitCountdown = 12;
                    new AlertDialog.Builder(view.getContext())
                            .setMessage(Config.from(SpeechApp.getContext()).getChannel())
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setCancelable(false).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getContentResolver().unregisterContentObserver(mAdbObserver);
        } else {
            getContentResolver().unregisterContentObserver(mAdbObserver);
        }
        super.onDestroy();
    }

    private static class AdbObserver extends ContentObserver {

        Activity activity = null;

        public AdbObserver(Activity activity, Handler handler) {
            super(handler);
            this.activity = activity;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            int enabled;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                enabled = Settings.Global.getInt(activity.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            } else {
                enabled = Settings.Secure.getInt(activity.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
            }
            if (DEBUG) {
                Log.d(TAG, "enabled: " + enabled);
            }
            if (1 == enabled) {
                SingleToast.show(R.string.usb_debug_enabled);
            } else {
                SingleToast.show(R.string.usb_debug_disabled);
            }
        }
    }

    private void enterPassword (int requestCode) {
        Intent intent = new Intent(this, KeyboardActivity.class);
        intent.putExtra(KeyboardActivity.EXTRA_TITLE, "PWD");
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == KeyboardActivity.RESULT_OK) {
            String pwd = data.getStringExtra(KeyboardActivity.EXTRA_OUT_VALUE);
            if (TextUtils.isEmpty(pwd) || !pwd.toLowerCase().equals("201807")) {
                Toast.makeText(AboutActivity.this, "password error", Toast.LENGTH_SHORT).show();
                return;
            }

            if (requestCode == REQUEST_ADB) {
                int enabled;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    enabled = Settings.Global.getInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                } else {
                    enabled = Settings.Secure.getInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                }
                if (1 == enabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        Settings.Global.putInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                    } else {
                        Settings.Secure.putInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        Settings.Global.putInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    } else {
                        Settings.Secure.putInt(AboutActivity.this.getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                    }
                }
            } else if (requestCode == REQUEST_HOME) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName("com.android.launcher3", "com.android.launcher3.Launcher");
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
