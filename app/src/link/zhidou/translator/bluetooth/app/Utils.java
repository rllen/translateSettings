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

package link.zhidou.translator.bluetooth.app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;

import link.zhidou.translator.R;
import link.zhidou.translator.bluetooth.lib.LocalBluetoothManager;
import link.zhidou.translator.bluetooth.lib.Utils.ErrorListener;
import link.zhidou.translator.utils.SingleToast;

/**
 * Utils is a helper class that contains constants for various
 * Android resource IDs, debug logging flags, and static methods
 * for creating dialogs.
 */
public final class Utils {
    private static final String KEY_ERROR = "errorMessage";
    private static final String TAG = "Bluetooth.Utils";

    private Utils() {
    }

    public static int getConnectionStateSummary(int connectionState) {
        switch (connectionState) {
        case BluetoothProfile.STATE_CONNECTED:
            return R.string.bluetooth_connected;
        case BluetoothProfile.STATE_CONNECTING:
            return R.string.bluetooth_connecting;
        case BluetoothProfile.STATE_DISCONNECTED:
            return R.string.bluetooth_disconnected;
        case BluetoothProfile.STATE_DISCONNECTING:
            return R.string.bluetooth_disconnecting;
        default:
            return 0;
        }
    }

    // Create (or recycle existing) and show disconnect dialog.
    static AlertDialog showDisconnectDialog(Context context,
                                            AlertDialog dialog,
                                            DialogInterface.OnClickListener disconnectListener,
                                            CharSequence title, CharSequence message) {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(context)
                    .setPositiveButton(android.R.string.ok, disconnectListener)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            // use disconnectListener for the correct profile(s)
            CharSequence okText = context.getText(android.R.string.ok);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    okText, disconnectListener);
        }
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    static void showError(Context context, String name, int messageResId) {
        String message = context.getString(messageResId, name);
        LocalBluetoothManager manager = getLocalBtManager(context);
        if (manager == null) {
            return;
        }
        SingleToast.show(message);
    }

    public static LocalBluetoothManager getLocalBtManager(Context context) {
        return LocalBluetoothManager.getInstance(context, mOnInitCallback);
    }

    private static final ErrorListener mErrorListener = new ErrorListener() {
        @Override
        public void onShowError(Context context, String name, int messageResId) {
            showError(context, name, messageResId);
        }
    };

    private static final LocalBluetoothManager.BluetoothManagerCallback mOnInitCallback = new LocalBluetoothManager.BluetoothManagerCallback() {
        @Override
        public void onBluetoothManagerInitialized(Context appContext,
                LocalBluetoothManager bluetoothManager) {
//            bluetoothManager.getEventManager().registerCallback(
//                    new DockBluetoothCallback(appContext));
            link.zhidou.translator.bluetooth.lib.Utils.setErrorListener(mErrorListener);
        }
    };
}
