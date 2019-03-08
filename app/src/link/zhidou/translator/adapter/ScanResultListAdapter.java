package link.zhidou.translator.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import link.zhidou.translator.R;


/**
 * Wifi扫描列表Adapter
 * Created by czm on 17-10-21.
 */

public class ScanResultListAdapter extends BaseAdapter {
    private Context mContext;
    private List<ScanResult> mResults;
    private WifiInfo mWifiInfo;
    private SupplicantState mSupplicantState;

    public ScanResultListAdapter(Context context, List<ScanResult> results) {
        mContext = context;
        mResults = results;
    }

    @Override
    public int getCount() {
        if (mResults != null) {
            return mResults.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (mResults != null && i >= 0 && i < mResults.size()) {
            return mResults.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void setWifiInfo(WifiInfo wifiInfo, SupplicantState supplicantState) {
        int position = 0;
        mWifiInfo = wifiInfo;
        mSupplicantState = supplicantState;
        if (wifiInfo == null) return;
        for (ScanResult scanResult : mResults) {
            if ((scanResult.SSID.equals(mWifiInfo.getSSID()) || ("\"" + scanResult.SSID + "\"").equals(mWifiInfo.getSSID())) && !TextUtils.isEmpty(scanResult.SSID)) {
                mResults.add(0, mResults.remove(position));
                break;
            }
            position++;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_wifi, null);
            viewHolder = new ViewHolder();
            viewHolder.ivSignal = (ImageView) convertView.findViewById(R.id.iv_signal);
            viewHolder.ivLock = (ImageView) convertView.findViewById(R.id.iv_lock);
            viewHolder.tvSSID = (TextView) convertView.findViewById(R.id.tv_wifi_ssid);
            viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.tv_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ScanResult item = mResults.get(position);
        viewHolder.tvSSID.setText(item.SSID);
        if (item.capabilities.contains("WEP") || item.capabilities.contains("PSK") || item.capabilities.contains("EAP")) {
            viewHolder.ivSignal.setImageResource(R.drawable.wifi_signal_lock_dark);
            viewHolder.ivLock.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ivSignal.setImageResource(R.drawable.wifi_signal_open_dark);
            viewHolder.ivLock.setVisibility(View.INVISIBLE);
        }
        int level = WifiManager.calculateSignalLevel(item.level, 5);
        viewHolder.ivSignal.setImageLevel(level);
        if (mWifiInfo != null) {
            if (item.SSID.equals(mWifiInfo.getSSID()) || ("\"" + item.SSID + "\"").equals(mWifiInfo.getSSID())) {
//            if(item.BSSID.equals(mWifiInfo.getBSSID())){
                if (mSupplicantState == SupplicantState.COMPLETED) {
                    viewHolder.tvStatus.setText(R.string.wifi_connected);
                } else {
                    viewHolder.tvStatus.setText(R.string.wifi_connecting);
                }
                viewHolder.tvStatus.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvStatus.setText("");
                viewHolder.tvStatus.setVisibility(View.GONE);
            }
        } else {
            viewHolder.tvStatus.setText("");
            viewHolder.tvStatus.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView ivSignal;
        ImageView ivLock;
        TextView tvSSID;
        TextView tvStatus;
    }
}
