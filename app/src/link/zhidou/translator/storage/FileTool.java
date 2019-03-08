package link.zhidou.translator.storage;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import link.zhidou.translator.SpeechApp;
import link.zhidou.translator.utils.Log;

/**
 * Date: 18-8-28
 * Time: 下午4:23
 * Email: lostsearover@gmail.com
 */
public class FileTool {
    private static final String TAG = "FileTool";
    private static final boolean DEBUG = Log.isLoggable();
    /** Only apply for system apps */
    public static final void notify(final String filePath) {
        SpeechApp.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
    }

    public static final void delete(String path) {
        if (DEBUG) {
            Log.i(TAG, "deleting " + path);
        }
        final File file = TextUtils.isEmpty(path) ? null : new File(path);
        if (file != null && file.isFile()) {
            file.delete();
            notify(path);
        }
    }

    public static final File createNewFile(String path) {
        final File file = new File(path);
        try {
            if (file.exists()) {
                file.delete();
            }
            if (file.createNewFile()) {
                notify(path);
                return file;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final File create(String path) {
        final File file = new File(path);
        try {
            if (file.exists()) {
                return file;
            }
            if (file.createNewFile()) {
                notify(path);
                return file;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
