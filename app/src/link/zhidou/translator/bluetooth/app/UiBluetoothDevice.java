package link.zhidou.translator.bluetooth.app;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.assist.MethodUtils;
import link.zhidou.translator.bluetooth.lib.CachedBluetoothDevice;
import link.zhidou.translator.bluetooth.lib.LocalBluetoothProfile;
import link.zhidou.translator.ui.view.dialog.ZdDialog;
import link.zhidou.translator.utils.Log;

import static link.zhidou.translator.bluetooth.lib.HiddenActions.PROFILE_A2DP;
import static link.zhidou.translator.bluetooth.lib.HiddenActions.PROFILE_HEADSET;

/**
 * Date: 18-6-26
 * Time: 下午4:02
 * Email: lostsearover@gmail.com
 */
public class UiBluetoothDevice extends LinearLayout implements CachedBluetoothDevice.Callback {
    private static final String TAG = "UiBluetoothDevice";
    private static boolean DEBUG = Log.isLoggable();
    private CachedBluetoothDevice mCachedDevice;
    private TextView mTitle;
    private TextView mStatus;
    private ImageView mIcon;

    public UiBluetoothDevice(Context context) {
        super(context);
    }

    public UiBluetoothDevice(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UiBluetoothDevice(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public UiBluetoothDevice(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }

    public void setCachedDevice(CachedBluetoothDevice cachedDevice) {
        if (mCachedDevice != null) {
            // unregister this.
            mCachedDevice.unregisterCallback(this);
        }
        mCachedDevice = cachedDevice;
        mCachedDevice.registerCallback(this);
        onDeviceAttributesChanged();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle  = findViewById(R.id.title);
        mStatus = findViewById(R.id.status);
        mIcon = findViewById(R.id.icon);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(TAG, "onDetachedFromWindow");
        }
        if (mCachedDevice != null) {
            // unregister this.
            mCachedDevice.unregisterCallback(this);
        }
    }

    public CachedBluetoothDevice getCachedDevice() {
        return mCachedDevice;
    }

    public void onClicked() {
        int bondState = mCachedDevice.getBondState();
        if (mCachedDevice.isConnected()) {
            askDisconnect();
        } else if (bondState == BluetoothDevice.BOND_BONDED) {
            if (DEBUG) {
                Log.d(TAG, mCachedDevice.getName() + " connect");
            }
            mCachedDevice.connect(true);
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            pair();
        }
    }

    private void showPopWindow() {
        new ZdDialog(getContext(), 1,  getContext().getString(R.string.forget), getContext().getString(R.string.cancel))
                .setOnDiaLogListener(new ZdDialog.OnDialogListener() {
                    @Override
                    public void dialogBtOneListener(View customView, DialogInterface dialogInterface, int which) {
                        mCachedDevice.unpair();
                    }

                    @Override
                    public void dialogBtTwoListener(View customView, DialogInterface dialogInterface, int which) {

                    }

                    @Override
                    public void dialogBtThreeListener(View customView, DialogInterface dialogInterface, int which) {

                    }

                    @Override
                    public void dialogBtFourListener(View customView, DialogInterface dialogInterface, int which) {

                    }
                }).showDialog();
    }

    public boolean onLongClicked() {
        if (mCachedDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            showPopWindow();
        }
        return true;
    }

    private void pair() {
        if (!mCachedDevice.startPairing()) {
            Utils.showError(getContext(), mCachedDevice.getName(),
                    R.string.bluetooth_pairing_error_message);
        }
    }


    // Show disconnect confirmation dialog for a device.
    private void askDisconnect() {
        mCachedDevice.disconnect();
    }

    @Override
    public void onDeviceAttributesChanged() {
        mTitle.setText(mCachedDevice.getName());
        final int status = mCachedDevice.getConnectionSummary();
        if (status != 0) {
            mStatus.setVisibility(VISIBLE);
            mStatus.setText(status);
        } else {
            mStatus.setVisibility(GONE);
            mStatus.setText("");
        }
        int iconResId  = getBtClassDrawable();
        if (iconResId != 0) {
            mIcon.setImageResource(iconResId);
        }
        setEnabled(!mCachedDevice.isBusy());
    }

    private int getBtClassDrawable() {
        BluetoothClass btClass = mCachedDevice.getBtClass();
        if (btClass != null) {
            switch (btClass.getMajorDeviceClass()) {
                case BluetoothClass.Device.Major.COMPUTER:
                    return R.drawable.ic_bt_laptop;

                case BluetoothClass.Device.Major.PHONE:
                    return R.drawable.ic_bt_cellphone;

//                case BluetoothClass.Device.Major.PERIPHERAL:
//                    return HidProfile.getHidClassDrawable(btClass);

                case BluetoothClass.Device.Major.IMAGING:
                    return R.drawable.ic_bt_imaging;

                default:
                    // unrecognized device class; continue
                    Log.d(TAG, "unrecognized device class " + btClass);
            }
        } else {
            Log.w(TAG, "mBtClass is null");
        }

        List<LocalBluetoothProfile> profiles = mCachedDevice.getProfiles();
        for (LocalBluetoothProfile profile : profiles) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return resId;
            }
        }
        if (btClass != null) {
            if (doesClassMatch(btClass, PROFILE_A2DP)) {
                return R.drawable.ic_bt_headphones_a2dp;

            }
            if (doesClassMatch(btClass, PROFILE_HEADSET)) {
                return R.drawable.ic_bt_headset_hfp;
            }
        }
        return R.drawable.ic_settings_bluetooth_alpha;
    }

    private static boolean doesClassMatch(BluetoothClass btClass, int profile) {
        try {
            return (boolean) MethodUtils.invokeMethod(btClass, "doesClassMatch", new Object[]{profile});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
