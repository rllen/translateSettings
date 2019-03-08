/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package link.zhidou.translator.bluetooth.app;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Switch;

import link.zhidou.translator.bluetooth.lib.LocalBluetoothAdapter;
import link.zhidou.translator.bluetooth.lib.LocalBluetoothManager;
import link.zhidou.translator.utils.Log;

/**
 * BluetoothEnabler is a helper to manage the Bluetooth on/off checkbox
 * preference. It turns on/off Bluetooth and ensures the summary of the
 * preference reflects the current state.
 */
public final class BluetoothEnabler implements View.OnClickListener {
    private static final String TAG = "BluetoothEnabler";
    private static final boolean DEBUG = Log.isLoggable();
    private Context mContext;
    private SwitchCompat mSwitch;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;
    ///M: indicate whether need to enable/disable BT or just update the preference
    private boolean mUpdateStatusOnly = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Broadcast receiver is always running on the UI thread here,
            // so we don't need consider thread synchronization.
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            if (DEBUG) {
                Log.d(TAG, "BluetoothAdapter state changed to" + state);
            }
            handleStateChanged(state);
        }
    };

    public BluetoothEnabler(Context context, SwitchCompat swi) {
        mContext = context;
        mSwitch = swi;
        mSwitch.setOnClickListener(this);
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            mLocalAdapter = null;
            mSwitch.setEnabled(false);
        } else {
            mLocalAdapter = manager.getBluetoothAdapter();
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }


    public void resume(Context context) {
        if (mLocalAdapter == null) {
            mSwitch.setEnabled(false);
            return;
        }
        if (mContext != context) {
            mContext = context;
        }
        handleStateChanged(mLocalAdapter.getBluetoothState());
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }
        mContext.unregisterReceiver(mReceiver);
    }

    void handleStateChanged(int state) {
        if (DEBUG) {
            Log.d(TAG, "handleStateChanged, state: " + state);
        }
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                mUpdateStatusOnly = true;
                setChecked(true);
                mSwitch.setEnabled(true);
                mUpdateStatusOnly = false;
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                  mUpdateStatusOnly = true;
                setChecked(false);
                mSwitch.setEnabled(true);
                mUpdateStatusOnly = false;
                break;
            default:
                setChecked(false);
                mSwitch.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (mLocalAdapter != null && !mUpdateStatusOnly) {
            final boolean isEnabled = mLocalAdapter.isEnabled();
            if (DEBUG) {
                Log.d(TAG, "isEnabled: " + isEnabled);
            }
            if (isEnabled) {
                mLocalAdapter.setBluetoothEnabled(false);
            } else {
                mLocalAdapter.setBluetoothEnabled(true);
            }
            mSwitch.setEnabled(false);
        }
    }

    private void setChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            mSwitch.setChecked(isChecked);
        }
    }
}
