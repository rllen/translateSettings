package link.zhidou.translator.assist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SubscriptionManager;


import java.util.Arrays;

import link.zhidou.translator.utils.Log;

public class SimHotSwapHandler {

    private static final String TAG = "SimHotSwapHandler";
    private static final boolean DEBUG = Log.isLoggable();
    private SubscriptionManager mSubscriptionManager;
    private Context mContext;
    private int[] mSubscriptionIdListCache;
    private OnSimHotSwapListener mListener;
    public static final String ACTION_SUBINFO_RECORD_UPDATED = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
    private BroadcastReceiver mSubReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleHotSwap();
        }
    };

    public SimHotSwapHandler(Context context) {
        mContext = context;
        mSubscriptionManager = SubscriptionManager.from(context);
        try {
            mSubscriptionIdListCache = (int[]) MethodUtils.invokeMethod(mSubscriptionManager, "getActiveSubscriptionIdList");
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("Cache list: ", mSubscriptionIdListCache);
    }

    public void registerOnSimHotSwap(OnSimHotSwapListener listener) {
        if (mContext != null) {
            mContext.registerReceiver(mSubReceiver, new IntentFilter(ACTION_SUBINFO_RECORD_UPDATED));
            mListener = listener;
        }
    }

    public void unregisterOnSimHotSwap() {
        if (mContext != null) {
            mContext.unregisterReceiver(mSubReceiver);
        }
        mListener = null;
    }

    private void handleHotSwap() {
        int[] subscriptionIdListCurrent = null;
        try {
            subscriptionIdListCurrent = (int[]) MethodUtils.invokeMethod(mSubscriptionManager, "getActiveSubscriptionIdList");
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("handleHotSwap, current subId list: ", subscriptionIdListCurrent);
        boolean isEqual = Arrays.equals(mSubscriptionIdListCache, subscriptionIdListCurrent);
        if (DEBUG) {
            Log.d(TAG, "isEqual: " + isEqual);
        }
        if (!isEqual && mListener != null) {
            mListener.onSimHotSwap();
        }
    }

    public interface OnSimHotSwapListener {
        void onSimHotSwap();
    }

    private void print(String msg, int[] lists) {
        if (lists != null) {
            for (int i : lists) {
                if (DEBUG) {
                    Log.d(TAG, msg + i);
                }
            }
        } else {
            if (DEBUG) {
                Log.d(TAG, msg + "is null");
            }
        }
    }
}
