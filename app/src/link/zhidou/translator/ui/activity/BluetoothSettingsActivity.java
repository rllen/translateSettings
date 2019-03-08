package link.zhidou.translator.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.BluetoothDeviceAdapter;
import link.zhidou.translator.bluetooth.app.BluetoothEnabler;
import link.zhidou.translator.bluetooth.app.UiBluetoothDevice;
import link.zhidou.translator.bluetooth.app.Utils;
import link.zhidou.translator.bluetooth.lib.BluetoothCallback;
import link.zhidou.translator.bluetooth.lib.BluetoothDeviceFilter;
import link.zhidou.translator.bluetooth.lib.CachedBluetoothDevice;
import link.zhidou.translator.bluetooth.lib.LocalBluetoothAdapter;
import link.zhidou.translator.bluetooth.lib.LocalBluetoothManager;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;

import static link.zhidou.translator.bluetooth.lib.BluetoothDeviceFilter.FILTER_TYPE_AUDIO;

public class BluetoothSettingsActivity extends ActionBarBaseActivity implements
        AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener,
        CommonActionBar.SettingsPressedListener,
        BluetoothCallback,
        PopupMenu.OnMenuItemClickListener {
    private static final String TAG = "BluetoothSettings";
    private static final boolean DEBUG = Log.isLoggable();
    private static final int ALL_TYPE = 0;
    private static final int AVAILABLE_TYPE = 1;
    private boolean mInitialScanStarted;
    private boolean mInitiateDiscoverable;
    private ListView mListView;
    private BluetoothDeviceAdapter mAdapter;
    private LocalBluetoothAdapter mLocalAdapter;
    private LocalBluetoothManager mLocalManager;
    private BluetoothEnabler mBluetoothEnabler;
    private BluetoothDeviceFilter.Filter mFilter = new ComboFilter(BluetoothDeviceFilter.getFilter(FILTER_TYPE_AUDIO), null);
    private class ComboFilter implements BluetoothDeviceFilter.Filter {
        private BluetoothDeviceFilter.Filter mMajor;
        private BluetoothDeviceFilter.Filter mMinor;
        public ComboFilter(BluetoothDeviceFilter.Filter major, BluetoothDeviceFilter.Filter minor) {
            mMajor = major;
            mMinor = minor;
        }

        public void setMinor(BluetoothDeviceFilter.Filter minor) {
            mMinor = minor;
        }

        @Override
        public boolean matches(BluetoothDevice device) {
            boolean majorMatch =  mMajor.matches(device);
            if (majorMatch) {
                if (mMinor != null) {
                    return mMinor.matches(device);
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }
    private List<CachedBluetoothDevice> mCachedDevices = new ArrayList<>();
    private int mCleanType = ALL_TYPE;
    private PopupMenu mPopMenu = null;
    private static final int MENU_ID_SCAN = Menu.FIRST;
    final void setFilter(BluetoothDeviceFilter.Filter filter) {
        mFilter = filter;
    }
    private void setCleanType(int type) {
        mCleanType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initView();
        mInitialScanStarted = false;
        mInitiateDiscoverable = true;
        mBluetoothEnabler = new BluetoothEnabler(this, (SwitchCompat) findViewById(R.id.bluetooth_switch));
        mLocalManager = Utils.getLocalBtManager(this);
        mLocalAdapter = mLocalManager.getBluetoothAdapter();
    }

    @Override
    public void onResume()  {
        super.onResume();
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume(this);
        }
        mLocalManager.setForegroundActivity(this);
        mLocalManager.getEventManager().registerCallback(this);
        if (mLocalAdapter != null) {
            updateContent(mLocalAdapter.getBluetoothState());
        }
    }

    private void addDeviceCategory(int cleanType, BluetoothDeviceFilter.Filter filter, boolean addCachedDevices) {
        mCleanType = cleanType;
        ((ComboFilter)mFilter).setMinor(filter);
        if (addCachedDevices) {
            addCachedDevices();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mLocalManager.setForegroundActivity(null);
        if (mLocalManager == null) {
            return;
        }
        removeAllDevices();
        mLocalManager.setForegroundActivity(null);
        mLocalManager.getEventManager().unregisterCallback(this);
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.pause();
        }
        // Make the device only visible to connected devices.
        mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
    }

    private void removeAllDevices() {
        if (DEBUG) {
            Log.d(TAG, "removeAllDevices, type: " + mCleanType);
        }
        mLocalAdapter.stopScanning();
        if (mCleanType == ALL_TYPE) {
            mCachedDevices.clear();
        } else {
            Collection<CachedBluetoothDevice> cleanableDevices = new ArrayList<>();
            Iterator<CachedBluetoothDevice> iterator = mCachedDevices.iterator();
            while (iterator.hasNext()) {
                CachedBluetoothDevice device = iterator.next();
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    cleanableDevices.add(device);
                }
            }
            mCachedDevices.removeAll(cleanableDevices);
        }
        mAdapter.notifyDataSetChanged();
    }

    void addCachedDevices() {
        if (DEBUG) {
            Log.d(TAG, "addCachedDevices");
        }
        Collection<CachedBluetoothDevice> cachedDevices = mLocalManager.getCachedDeviceManager().getCachedDevicesCopy();
        for (CachedBluetoothDevice cachedDevice : cachedDevices) {
            onDeviceAdded(cachedDevice);
        }
    }

    @Override
    public void onSettingsPressed(View view) {
        if (null == mPopMenu) {
            mPopMenu = new PopupMenu(this, view, Gravity.CENTER);
            mPopMenu.setOnMenuItemClickListener(this);
        }
        refreshMenu();
        mPopMenu.show();
    }

    private void refreshMenu() {
        if (mPopMenu != null) {
            mPopMenu.getMenu().clear();
            boolean bluetoothIsEnabled = mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON;
            boolean isDiscovering = mLocalAdapter.isDiscovering();
            if (DEBUG) {
                Log.d(TAG, "refreshMenu, isDiscovering " + isDiscovering);
            }
            final int textId = isDiscovering ? R.string.bluetooth_searching_for_devices : R.string.bluetooth_search_for_devices;
            mPopMenu.getMenu().add(Menu.NONE, MENU_ID_SCAN, 0, textId)
                    .setEnabled(bluetoothIsEnabled && !isDiscovering)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ID_SCAN:
                if (mLocalAdapter.getBluetoothState() == BluetoothAdapter.STATE_ON) {
                    startScanning();
                }
                return true;
            default:
                break;
        }
        return false;
    }

    private void initView() {
        setActionBarTitle(this);
        mActionBar.setSettingsPressedListener(this);
        mListView = findViewById(R.id.list_view);
        mAdapter = new BluetoothDeviceAdapter(mCachedDevices);
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(1);
        mListView.setItemsCanFocus(true);
        mListView.requestFocus();
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mLocalAdapter.stopScanning();
        UiBluetoothDevice device = (UiBluetoothDevice) view;
        device.onClicked();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        UiBluetoothDevice device = (UiBluetoothDevice) view;
        return device.onLongClicked();
    }

    /**
     * Bluetooth callback
     * @param bluetoothState
     */
    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        if (DEBUG) {
            Log.d(TAG, "onBluetoothStateChanged, bluetoothState: " + bluetoothState);
        }
        if (BluetoothAdapter.STATE_ON == bluetoothState) {
            mInitiateDiscoverable = true;
        }
        updateContent(bluetoothState);
    }

    private void updateContent(int bluetoothState) {
        switch (bluetoothState) {
            case BluetoothAdapter.STATE_ON:
                // Clear first
                setCleanType(ALL_TYPE);
                removeAllDevices();
                addDeviceCategory(ALL_TYPE, BluetoothDeviceFilter.BONDED_DEVICE_FILTER, true);
                addDeviceCategory(AVAILABLE_TYPE, BluetoothDeviceFilter.UNBONDED_DEVICE_FILTER, mInitialScanStarted);
                addCachedDevices();
                if (!mInitialScanStarted) {
                    startScanning();
                }
                refreshMenu();
                // mLocalAdapter.setScanMode is internally synchronized so it is okay for multiple
                // threads to execute.
                if (mInitiateDiscoverable) {
                    // Make the device visible to other devices.
                    mLocalAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
                    mInitiateDiscoverable = false;
                }
                return; // not break

            case BluetoothAdapter.STATE_TURNING_OFF:
                break;
            case BluetoothAdapter.STATE_OFF:
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                mInitialScanStarted = false;
                break;
        }
        setCleanType(ALL_TYPE);
        removeAllDevices();
        refreshMenu();
    }

    private void startScanning() {
        if (DEBUG) {
            Log.d(TAG, "startScanning");
        }
        if (AVAILABLE_TYPE == mCleanType) {
            removeAllDevices();
        }
        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }

    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        if (DEBUG) {
            Log.d(TAG, "onDeviceBondStateChanged, cachedDevice: " + cachedDevice.getName() + ", bondState: " + bondState);
        }
        setCleanType(ALL_TYPE);
        removeAllDevices();
        updateContent(mLocalAdapter.getBluetoothState());
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        if (DEBUG) {
            Log.d(TAG, "onScanningStateChanged, started: " + started);
        }
        refreshMenu();
    }

    @Override
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        if (DEBUG) {
            Log.d(TAG, "onDeviceDeleted, cachedDevice: " + cachedDevice.getName());
        }
        mCachedDevices.remove(cachedDevice);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        if (DEBUG) {
            Log.d(TAG, "cachedDevice: " + cachedDevice + ", state: " + state);
        }
    }

    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if (DEBUG) {
            Log.d(TAG, "onDeviceAdded, Device name is " + cachedDevice.getName());
        }
        if (mCachedDevices.contains(cachedDevice)) {
            if (DEBUG) {
                Log.d(TAG, "Device name " + cachedDevice.getName() + " is already in list");
            }
            return;
        }
        // Prevent updates while the list shows one of the state messages
        if (mLocalAdapter.getBluetoothState() != BluetoothAdapter.STATE_ON) return;
        if (mFilter.matches(cachedDevice.getDevice())) {
            if (DEBUG) {
                Log.d(TAG, "Device name " + cachedDevice.getName() + " added");
            }
            mCachedDevices.add(cachedDevice);
            mAdapter.notifyDataSetChanged();
        }
    }
}
