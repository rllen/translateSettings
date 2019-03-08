package link.zhidou.translator.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import link.zhidou.translator.R;

/**
 * created by yue.gan 18-8-22
 */
public class LowStorageReceiver extends BroadcastReceiver {

    public void registSelf (Context context) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
        context.registerReceiver(this, intentFilter);
    }

    public void unregistSelf (Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
            AlertDialog dialog = new AlertDialog.Builder(context, R.style.LightAlertDialog)
                    .setCancelable(false)
                    .setMessage(R.string.low_memory_warning)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            dialog.show();
        }
    }
}
