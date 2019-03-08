package link.zhidou.translator.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jc on 17-4-24.
 */

public class NetworkState {
    private Context mContext;
    private boolean mIsConnected = false;
    private boolean mIsWifi = false;
    private Set<NetworkStateListener> listeners;
    private Set<WeakReference<NetworkStateListener>> mWeakListeners;

    private synchronized void clearInvalidListener() {
        if (mWeakListeners == null) {
            return;
        }
        Iterator<WeakReference<NetworkStateListener>> iterator = mWeakListeners.iterator();
        List<WeakReference<NetworkStateListener>> list = new ArrayList<>();
        while (iterator.hasNext()) {
            WeakReference<NetworkStateListener> reference = iterator.next();
            if (reference.get() == null) {
                list.add(reference);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            mWeakListeners.remove(list.get(i));
        }
    }

    public void addListener(NetworkStateListener listener) {
        if (listeners == null) {
            listeners = new HashSet<>();
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        listener.onNetworkStateChanged(mIsConnected, mIsWifi);
    }

    public void register(NetworkStateListener listener) {
        if (mWeakListeners == null) {
            mWeakListeners = new HashSet<>();
        }
        clearInvalidListener();
        mWeakListeners.add(new WeakReference<>(listener));
        listener.onNetworkStateChanged(mIsConnected, mIsWifi);
    }

    private void notifyWeakListener() {
        if (mWeakListeners == null) {
            return;
        }
        clearInvalidListener();
        Iterator<WeakReference<NetworkStateListener>> iterator = mWeakListeners.iterator();
        while (iterator.hasNext()) {
            iterator.next().get().onNetworkStateChanged(mIsConnected, mIsWifi);
        }
    }

    public void removeListener(NetworkStateListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public interface NetworkStateListener {
        void onNetworkStateChanged(boolean isConnected, boolean isWifi);
    }

    NetworkChangedBroadCastReceiver broadCastReceiver = new NetworkChangedBroadCastReceiver();

    static class InstanceHolder {
        static final NetworkState sInstance = new NetworkState();
    }

    public static NetworkState from(Context context) {
        if (null == InstanceHolder.sInstance.mContext) {
            synchronized (InstanceHolder.sInstance) {
                if (null == InstanceHolder.sInstance.mContext) {
                    InstanceHolder.sInstance.init(context.getApplicationContext());
                }
            }
        }
        return InstanceHolder.sInstance;
    }

    private NetworkState() {

    }

    class NetworkChangedBroadCastReceiver extends BroadcastReceiver {

        public void registerReceiver(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(this, filter);
        }

        public void unregisterReceiver(Context context) {
            context.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                mIsWifi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
                if (listeners != null && !listeners.isEmpty()) {
                    Iterator<NetworkStateListener> iterator = listeners.iterator();
                    while (iterator.hasNext()) {
                        iterator.next().onNetworkStateChanged(mIsConnected, mIsWifi);
                    }
                }
                notifyWeakListener();
            }
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public boolean isWifi() {
        return mIsWifi;
    }

    public boolean hasWifi() {
        return mIsConnected && mIsWifi;
    }

    public boolean hasNetwork() {
        return mIsConnected;
    }

    private void init(Context context) {
        mContext = context;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        mIsWifi = activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        broadCastReceiver.registerReceiver(context);
    }
}

