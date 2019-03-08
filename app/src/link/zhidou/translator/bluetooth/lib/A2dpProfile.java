/*
 * Copyright (C) 2011 The Android Open Source Project
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

package link.zhidou.translator.bluetooth.lib;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.assist.MethodUtils;
import link.zhidou.translator.utils.Log;

public final class A2dpProfile implements LocalBluetoothProfile, HiddenActions{
    private static final String TAG = "A2dpProfile";
    private static final boolean DEBUG = Log.isLoggable();

    private BluetoothA2dp mService;
    private boolean mIsProfileReady;

    private final LocalBluetoothAdapter mLocalAdapter;
    private final CachedBluetoothDeviceManager mDeviceManager;
    /**
     * Reflect
     */
    static final ParcelUuid[] SINK_UUIDS = {
        BluetoothUuid.AudioSink,
        BluetoothUuid.AdvAudioDist,
    };

    static final String NAME = "A2DP";
    private final LocalBluetoothProfileManager mProfileManager;

    // Order of this profile in device profiles list
    private static final int ORDINAL = 1;

    // These callbacks run on the main thread.
    private final class A2dpServiceListener
            implements BluetoothProfile.ServiceListener {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (DEBUG) {
                Log.d(TAG,"Bluetooth service connected");
            }
            mService = (BluetoothA2dp) proxy;
            // We just bound to the service, so refresh the UI for any connected A2DP devices.
            List<BluetoothDevice> deviceList = mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = mDeviceManager.findDevice(nextDevice);
                // we may add a new device here, but generally this should not happen
                if (device == null) {
                    Log.w(TAG, "A2dpProfile found new device: " + nextDevice);
                    device = mDeviceManager.addDevice(mLocalAdapter, mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(A2dpProfile.this, BluetoothProfile.STATE_CONNECTED);
                device.refresh();
            }
            mIsProfileReady=true;
        }

        public void onServiceDisconnected(int profile) {
            if (DEBUG) {
                Log.d(TAG,"Bluetooth service disconnected");
            }
            mIsProfileReady=false;
        }
    }

    public boolean isProfileReady() {
        return mIsProfileReady;
    }

    A2dpProfile(Context context, LocalBluetoothAdapter adapter,
                CachedBluetoothDeviceManager deviceManager,
                LocalBluetoothProfileManager profileManager) {
        mLocalAdapter = adapter;
        mDeviceManager = deviceManager;
        mProfileManager = profileManager;
        mLocalAdapter.getProfileProxy(context, new A2dpServiceListener(),
                BluetoothProfile.A2DP);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (mService == null) return new ArrayList<BluetoothDevice>(0);
        return mService.getDevicesMatchingConnectionStates(
              new int[] {BluetoothProfile.STATE_CONNECTED,
                         BluetoothProfile.STATE_CONNECTING,
                         BluetoothProfile.STATE_DISCONNECTING});
    }

    public boolean connect(BluetoothDevice device) {
        if (mService == null) return false;
        List<BluetoothDevice> sinks = getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
                /// M: avoid apps disconnecting A2DP that is connecting
                if(sink != null && device!= null
                    && getConnectionStatus(sink) == BluetoothProfile.STATE_CONNECTING
                    && sink.getAddress().equals(device.getAddress())) {
                    if (DEBUG) {
                        Log.d(TAG, "The target device is connecting, don't disconnect");
                    }
                    continue;
                }
                try {
                    MethodUtils.invokeMethod(mService, "disconnect", new Object[]{sink});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            return (boolean) MethodUtils.invokeMethod(mService, "connect", new Object[]{device});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean disconnect(BluetoothDevice device) {
        if (mService == null) return false;
        try {
            int priority = (int) MethodUtils.invokeMethod(mService, "getPriority", new Object[]{device});
            // Downgrade priority as user is disconnecting the headset.
            if (priority > PRIORITY_ON){
                MethodUtils.invokeMethod(mService, "setPriority", new Object[] {device, PRIORITY_ON});
            }
            return (boolean) MethodUtils.invokeMethod(mService, "disconnect", new Object[] {device});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    private int getPriority(BluetoothDevice device) {
        try {
            int priority = (int) MethodUtils.invokeMethod(mService, "getPriority", new Object[]{device});
            return priority;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void setPriority(BluetoothDevice device, int priority) {
        try {
            MethodUtils.invokeMethod(mService, "setPriority", new Object[]{device, priority});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (mService == null) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
        return mService.getConnectionState(device);
    }

    public boolean isPreferred(BluetoothDevice device) {
        if (mService == null) return false;
        return getPriority(device) > PRIORITY_OFF;
    }

    public int getPreferred(BluetoothDevice device) {
        if (mService == null) return PRIORITY_OFF;
        return getPriority(device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (mService == null) return;
        if (preferred) {
            if (getPriority(device) < PRIORITY_ON) {
                setPriority(device, PRIORITY_ON);
            }
        } else {
            setPriority(device, PRIORITY_OFF);
        }
    }
    boolean isA2dpPlaying() {
        if (mService == null) return false;
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (!sinks.isEmpty()) {
            if (mService.isA2dpPlaying(sinks.get(0))) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return ORDINAL;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_a2dp;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                return R.string.bluetooth_a2dp_profile_summary_use_for;

            case BluetoothProfile.STATE_CONNECTED:
                return R.string.bluetooth_a2dp_profile_summary_connected;

            default:
                return Utils.getConnectionStateSummary(state);
        }
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_bt_headphones_a2dp;
    }

    protected void finalize() {
        if (DEBUG) {
            Log.d(TAG, "finalize()");
        }
        if (mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.A2DP,
                                                                       mService);
                mService = null;
            }catch (Throwable t) {
                Log.w(TAG, "Error cleaning up A2DP proxy", t);
            }
        }
    }
}
