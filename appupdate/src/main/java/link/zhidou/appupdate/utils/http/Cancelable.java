package link.zhidou.appupdate.utils.http;

import android.support.annotation.CallSuper;

import java.io.Closeable;
import java.io.IOException;

/**
 * created by yue.gan 18-7-11
 */
public class Cancelable implements Closeable {

    private Cancelable delegate;
    public void setDelegate (Cancelable cancelable) {
        delegate = cancelable;
    }

    @CallSuper
    public void cancel () {
        if (delegate != null) {
            delegate.cancel();
            delegate = null;
        }
    }

    @Override
    public void close() throws IOException {
        cancel();
    }
}
