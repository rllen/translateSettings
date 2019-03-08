package link.zhidou.translator.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import link.zhidou.translator.assist.SysProp;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SPKeyContent;
import link.zhidou.translator.utils.SPUtil;

/**
 * Date: 18-4-10
 * Time: 下午8:51
 * Email: lostsearover@gmail.com
 */

public class FirmwareNewVersionReceiver extends BroadcastReceiver {

    private static final String TAG = "FirmwareUpdate";
    private static final boolean DEBUG = Log.isLoggable();
    private static final String VERSION_NAME = "version_name";
    private static final String VERSION_DESC = "version_desc";
    public static final String FIRMWARE_VERSION = "persist.sys.zd.fmv";
    public interface Listener  {
        void onNewVersionReceived();
    }

    private static Set<Listener> sListeners = new HashSet<>();

    public static void add(Listener listener) {
        sListeners.add(listener);
    }

    public static void remove(Listener listener) {
        sListeners.remove(listener);
    }

    private static void notifyListeners() {
        final Iterator<Listener> iterator = sListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().onNewVersionReceived();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "New fm version: " + intent.getStringExtra(VERSION_NAME));
        }
        try {
            SysProp.set(FIRMWARE_VERSION, intent.getStringExtra(VERSION_NAME));
            SPUtil.putString(context, SPKeyContent.FIRMWARE_UPDATE_DESC, intent.getStringExtra(VERSION_DESC));
            notifyListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
