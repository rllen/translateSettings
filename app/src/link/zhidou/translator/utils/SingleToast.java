package link.zhidou.translator.utils;

import android.content.res.Resources;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import link.zhidou.translator.SpeechApp;

/**
 * Date: 17-11-17
 * Time: 下午8:06
 * Email: lostsearover@gmail.com
 */

public class SingleToast {

    private static Toast singleToast = null;

    public static void show(String content) {
        int textview_id = Resources.getSystem().getIdentifier("message", "id", "android");
        if (null == singleToast) {
            singleToast = Toast.makeText(SpeechApp.getContext(), content, Toast.LENGTH_SHORT);
        } else {
            singleToast.setText(content);
        }
        ((TextView) singleToast.getView().findViewById(textview_id)).setGravity(Gravity.CENTER);
        singleToast.show();
    }

    public static void show(int resId) {
        int textview_id = Resources.getSystem().getIdentifier("message", "id", "android");
        if (null == singleToast) {
            singleToast = Toast.makeText(SpeechApp.getContext(), SpeechApp.getContext().getString(resId), Toast.LENGTH_SHORT);
        } else {
            singleToast.setText(SpeechApp.getContext().getString(resId));
        }
        ((TextView) singleToast.getView().findViewById(textview_id)).setGravity(Gravity.CENTER);
        singleToast.show();
    }
}
