package link.zhidou.translator.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.StringUtils;

import static link.zhidou.translator.ui.activity.WifiListActivity.REQUEST_DELETE_WIFI;

public class WifiInfoActivity extends BaseActivity implements View.OnClickListener, CommonActionBar.BackPressedListener {
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    CommonActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_info);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        mWifiInfo = mWifiManager.getConnectionInfo();
//        TextView ip = findViewById(R.id.tv_ip);
//        ip.setText(intToIP(mWifiInfo.getIpAddress()));
//        TextView wifi_name = findViewById(R.id.wifi_name);
//        wifi_name.setSelected(true);
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mActionBar.setTitle(StringUtils.trimFirstAndLastChar(mWifiInfo.getSSID(), '\"'));
        findViewById(R.id.btn_del_network).setOnClickListener(this);
    }

    @Override
    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    protected String intToIP(int longIp) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((longIp & 0x000000FF)));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((longIp >>> 24)));
        return sb.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_del_network:
                delNetwork();
                break;
        }
    }

    private void delNetwork() {
        int checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_MULTICAST_STATE);
        if (checkSelfPermission == PackageManager.PERMISSION_GRANTED) {
            /*有权限*/
            //todo 删除Wi-Fi之后弹Toast提示
            Intent intent = new Intent();
            intent.putExtra("ssid", StringUtils.trimFirstAndLastChar(mWifiInfo.getSSID(), '\"'));
            intent.putExtra("action", "del");
            setResult(REQUEST_DELETE_WIFI, intent);
            //            CustomToast.INSTANCE.showToast(this, "有权限"+checkSelfPermission+"Manifest.permission.CHANGE_WIFI_MULTICAST_STATE");
        } else if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
//            CustomToast.INSTANCE.showToast(this, "没有权限");
        }
        finish();
    }
}
