package link.zhidou.translator.utils;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Date: 17-11-24
 * Time: 上午11:13
 * Email: lostsearover@gmail.com
 */

public class ViewUtil {

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    public static boolean isViewLayoutRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == View.LAYOUT_DIRECTION_RTL;
    }
}
