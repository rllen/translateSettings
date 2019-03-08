package link.zhidou.translator.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.zhidou.roaming.IRoamingSettings;

import java.util.concurrent.CountDownLatch;

/**
 * Date: 18-4-10
 * Time: 下午6:13
 * Email: lostsearover@gmail.com
 */

public class RoamingSettings {

    private static final String TAG = "RoamingSettings";
    private static volatile RoamingSettings sInstance;
    private Context mContext;
    private IRoamingSettings mRoamingSettings;
    private CountDownLatch mCountDownLatch;

    private RoamingSettings(Context context) {
        this.mContext = context.getApplicationContext();
        connectBinderPoolService();
    }

    public static RoamingSettings from(Context context) {
        if (null == sInstance) {
            synchronized (RoamingSettings.class) {
                if (null == sInstance) {
                    sInstance = new RoamingSettings(context);
                }
            }
        }
        return sInstance;
    }


    private synchronized void connectBinderPoolService() {
        mCountDownLatch = new CountDownLatch(1);
        Intent service = new Intent("link.zhidou.settings.DATA_ROAMING_SETTINGS");
        service.setPackage("com.android.phone");
        mContext.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            mRoamingSettings.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRoamingSettings = null;
            connectBinderPoolService();
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRoamingSettings = IRoamingSettings.Stub.asInterface(service);
            try {
                mRoamingSettings.asBinder().linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRoamingSettings = null;
            connectBinderPoolService();
        }
    };


    public boolean isDataRoamingEnabled(int subId) {
        try {
            return mRoamingSettings.isDataRoamingEnabled(subId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setDataRoamingEnabled(int subId, boolean enabled) {
        try {
            return mRoamingSettings.setDataRoamingEnabled(subId, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
