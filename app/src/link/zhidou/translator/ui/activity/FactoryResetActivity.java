package link.zhidou.translator.ui.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import link.zhidou.translator.R;
import link.zhidou.translator.assist.MethodUtils;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;

public class FactoryResetActivity extends ActionBarBaseActivity {

    private static final String TAG = FactoryResetActivity.class.getSimpleName();
    private static final String ACTION_MASTER_CLEAR = "android.intent.action.MASTER_CLEAR";
    private static final String EXTRA_REASON = "android.intent.extra.REASON";
    private static final String EXTRA_WIPE_EXTERNAL_STORAGE = "android.intent.extra.WIPE_EXTERNAL_STORAGE";
    public static final String PERSISTENT_DATA_BLOCK_SERVICE = "persistent_data_block";
    private static final String GET_OEM_UNLOCK_ENABLED = "getOemUnlockEnabled";
    private static final String WIPE = "wipe";
    public static final String FORMAT_AND_FACTORY_RESET = "com.android.internal.os.storage.FORMAT_AND_FACTORY_RESET";
    private static final ComponentName COMPONENT_NAME = new ComponentName("android", "com.android.internal.os.storage.ExternalStorageFormatter");

    /**
     * The user has gone through the multiple confirmation, so now we go ahead
     * and invoke the Checkin Service to reset the device to its factory-default
     * state (rebooting in the process).
     */
    private DialogInterface.OnClickListener mFinalDialogClickListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                final Object pdbManager = getSystemService(PERSISTENT_DATA_BLOCK_SERVICE);
                if (pdbManager != null && !(Boolean) MethodUtils.invokeMethod(pdbManager, GET_OEM_UNLOCK_ENABLED)) {
                    int deviceProvisioned;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        deviceProvisioned = Settings.Global.getInt(FactoryResetActivity.this.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
                    } else {
                        deviceProvisioned = Settings.Secure.getInt(FactoryResetActivity.this.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
                    }
                    if (deviceProvisioned != 0) {
                        // if OEM unlock is enabled, this will be wiped during FR process. If disabled, it
                        // will be wiped here, unless the device is still being provisioned, in which case
                        // the persistent data block will be preserved.
                        new AsyncTask<Void, Void, Void>() {
                            int mOldOrientation;
                            ProgressDialog mProgressDialog;

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    MethodUtils.invokeMethod(pdbManager, WIPE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                mProgressDialog.hide();
                                FactoryResetActivity.this.setRequestedOrientation(mOldOrientation);
                                doMasterClear();
                            }

                            @Override
                            protected void onPreExecute() {
                                mProgressDialog = getProgressDialog();
                                mProgressDialog.show();

                                // need to prevent orientation changes as we're about to go into
                                // a long IO request, so we won't be able to access inflate resources on flash
                                mOldOrientation = FactoryResetActivity.this.getRequestedOrientation();
                                FactoryResetActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                            }
                        }.execute();
                    } else {
                        doMasterClear();
                    }
                } else {
                    doMasterClear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ProgressDialog getProgressDialog() {
            final ProgressDialog progressDialog = new ProgressDialog(FactoryResetActivity.this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(getString(R.string.master_clear_progress_title));
            progressDialog.setMessage(getString(R.string.master_clear_progress_text));
            return progressDialog;
        }
    };


    private View.OnClickListener mFinalClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(FactoryResetActivity.this)
                    .setMessage(R.string.master_clear_final_desc)
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFinalDialogClickListener.onClick(dialog, which);
                        }
                    }).show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.factory_reset);
        findViewById(R.id.reset).setOnClickListener(mFinalClickListener);
        initData();
    }

    private void initData() {
        setActionBarTitle(FactoryResetActivity.this);
    }

    private void doMasterClear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(ACTION_MASTER_CLEAR);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            intent.putExtra(EXTRA_REASON, "MasterClearConfirm");
            intent.putExtra(EXTRA_WIPE_EXTERNAL_STORAGE, true);
            sendBroadcast(intent);
            // Intent handling is asynchronous -- assume it will happen soon.
        } else {
            Intent intent = new Intent(FORMAT_AND_FACTORY_RESET);
            intent.setComponent(COMPONENT_NAME);
            startService(intent);
        }
    }
}
