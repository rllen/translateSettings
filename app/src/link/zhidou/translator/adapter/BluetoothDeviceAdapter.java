package link.zhidou.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.bluetooth.app.UiBluetoothDevice;
import link.zhidou.translator.bluetooth.lib.CachedBluetoothDevice;

public class BluetoothDeviceAdapter extends BaseAdapter {

    private List<CachedBluetoothDevice> mCachedDevices = null;
    public BluetoothDeviceAdapter(List<CachedBluetoothDevice> list) {
        mCachedDevices = list;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return null == mCachedDevices ? 0 : mCachedDevices.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_bluetooth_device, parent,false);
        }
        UiBluetoothDevice device = (UiBluetoothDevice) convertView;
        device.setCachedDevice(mCachedDevices.get(position));
        return device;
    }

    @Override
    public Object getItem(int position) {
        return mCachedDevices.get(position);
    }


}
