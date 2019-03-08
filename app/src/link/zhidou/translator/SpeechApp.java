package link.zhidou.translator;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.multidex.MultiDex;

import com.squareup.leakcanary.LeakCanary;

import java.lang.reflect.Field;
import java.util.Collection;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import link.zhidou.translator.service.LowStorageReceiver;
import link.zhidou.translator.storage.PathProvider;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.NetworkState;
import link.zhidou.translator.utils.ProcessUtil;
import link.zhidou.translator.utils.SingleToast;

public class SpeechApp extends Application implements NetworkState.NetworkStateListener {
    private static final String TAG = "SpeechApp";
    private static final boolean DEBUG = Log.isLoggable();
    private static Application sApplication = null;
    private boolean mIsWifi = false;
    private LowStorageReceiver mLowStorageReceiver;

    public static SpeechApp getApp() {
        return (SpeechApp) sApplication;
    }

    public static Context getContext() {
        return sApplication.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        if (ProcessUtil.isHostProcess(this)) {
            PathProvider.get().init(this);
            init();
            NetworkState.from(this).addListener(this);
            initService();
            mLowStorageReceiver = new LowStorageReceiver();
            mLowStorageReceiver.registSelf(sApplication);
        }
        if (!BuildConfig.SMALL) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(this);
            // Normal app init code...
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void init() {
        /**
         * 默认错误处理
         */
        if (RxJavaPlugins.getErrorHandler() != null || RxJavaPlugins.isLockdown()) {
            return;
        }
        RxJavaPlugins.setErrorHandler(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (DEBUG) {
                    throwable.printStackTrace();
                    Flowable.create(new FlowableOnSubscribe<Object>() {
                        @Override
                        public void subscribe(FlowableEmitter<Object> e) {
                            SingleToast.show("I am lost");
                            e.onComplete();
                        }
                    }, BackpressureStrategy.BUFFER).subscribeOn(AndroidSchedulers.mainThread()).subscribe();
                }
            }
        });
    }

    @Override
    public void onTerminate() {
        if (ProcessUtil.isHostProcess(this)) {
            NetworkState.from(this).removeListener(this);
            if (mLowStorageReceiver != null) {
                mLowStorageReceiver.unregistSelf(sApplication);
            }
        }
        super.onTerminate();
    }

    @Override
    public void onNetworkStateChanged(boolean isConnected, boolean isWifi) {
        if (DEBUG) {
            Log.d(TAG, "isConnected: " + isConnected + ", isWifi: " + isWifi);
        }
        mIsWifi = isWifi;
    }

    private void initService () {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");//给个错误语言让其绑定服务后返回错误不启动
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        speechRecognizer.startListening(recognizerIntent);

        try {
            Field field = SpeechRecognizer.class.getDeclaredField("mPendingTasks");
            field.setAccessible(true);
            Collection collection = (Collection) field.get(speechRecognizer);
            collection.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}