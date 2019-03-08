package link.zhidou.translator.utils;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

public class DimensionUtil {

    private static final String TAG = "DimensionUtil";
    private static final boolean DEBUG = Log.isLoggable();
    public static final double ASPECT_TOLERANCE = DimensionUtil.dpToPx(1);
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static class FontParam {
        public float mTextSize = DimensionUtil.dpToPx(14);
        public float mLetterSpacing = 0.0f;
    }

    public static int getTextWidth(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int width = rect.width();
        return width;
    }

    public static int getTextHeight(String text, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        int height = rect.height();
        return height;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static FontParam getFontParamBaseOnHeight(String content,int height) {
        final FontParam fontParam = new FontParam();
        if (DEBUG) {
            Log.d(TAG, "Content: " + content +  ", height: " + height);
        }
        float defaultFontSize = Math.max(height * 2 / 3.0f, fontParam.mTextSize);
        height = (int)(height * 7 / 10.0f);
        float letterSpacing = fontParam.mLetterSpacing;
        final Paint paint = new Paint();
        paint.setTextSize(defaultFontSize);
        paint.setLetterSpacing(letterSpacing);
        int idealWidth = getTextWidth(content, paint);
        int idealHeight = getTextHeight(content, paint);
        if (DEBUG) {
            Log.d(TAG, "Content: " + content + ", defaultFontSize: " + defaultFontSize + ", letterSpacing: " + letterSpacing + ", idealWidth: " + idealWidth + ", idealHeight: " + idealHeight);
        }
        // 调小，不用调letter spacing
        if (idealHeight > height) {
            do {
                if (defaultFontSize > 0.5f) {
                    defaultFontSize -= 0.5f;
                    paint.setTextSize(defaultFontSize);
                    idealHeight = getTextHeight(content, paint);
                } else {
                    break;
                }
            } while (idealHeight > height );
            fontParam.mTextSize = defaultFontSize;
        } else { // 调大到合适
            while (true) {
                defaultFontSize += 0.5f;
                paint.setTextSize(defaultFontSize);
                idealHeight = getTextHeight(content, paint);
                if (idealHeight > height ) {
                    break;
                } else {
                    fontParam.mTextSize = defaultFontSize;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "Content: " + content + ", fontSize: " + fontParam.mTextSize + ", letterSpacing: " + fontParam.mLetterSpacing);
        }
        return fontParam;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static FontParam getFontParam(String content, int width, int height) {
        final FontParam fontParam = new FontParam();
        if (DEBUG) {
            Log.d(TAG, "Content: " + content + ", region width: " + width + ", height: " + height);
        }
        float defaultFontSize = Math.max(height * 2 / 3.0f, fontParam.mTextSize);
        height = (int)(height * 7 / 10.0f);
        float letterSpacing = fontParam.mLetterSpacing;
        final Paint paint = new Paint();
        paint.setTextSize(defaultFontSize);
        paint.setLetterSpacing(letterSpacing);
        int idealWidth = getTextWidth(content, paint);
        int idealHeight = getTextHeight(content, paint);
        if (DEBUG) {
            Log.d(TAG, "Content: " + content + ", defaultFontSize: " + defaultFontSize + ", letterSpacing: " + letterSpacing + ", idealWidth: " + idealWidth + ", idealHeight: " + idealHeight);
        }
        // 调小，不用调letter spacing
        if (idealWidth > width || idealHeight > height) {
            do {
                if (defaultFontSize > 0.5f) {
                    defaultFontSize -= 0.5f;
                    paint.setTextSize(defaultFontSize);
                    idealWidth = getTextWidth(content, paint);
                    idealHeight = getTextHeight(content, paint);
                } else {
                    break;
                }
            } while (idealHeight > height || idealWidth > width);
            fontParam.mTextSize = defaultFontSize;
            // Tuning letter spacing
            while (true) {
                letterSpacing += 0.01f;
                paint.setLetterSpacing(letterSpacing);
                idealWidth = getTextWidth(content, paint);
                if (idealWidth > width) {
                    break;
                } else {
                    fontParam.mLetterSpacing = letterSpacing;
                }
            }
        } else { // 调大到合适
            while (true) {
                defaultFontSize += 0.5f;
                paint.setTextSize(defaultFontSize);
                idealWidth = getTextWidth(content, paint);
                idealHeight = getTextHeight(content, paint);
                if (idealHeight > height || idealWidth > width) {
                    break;
                } else {
                    fontParam.mTextSize = defaultFontSize;
                }
            }
            while (true) {
                letterSpacing += 0.01f;
                paint.setLetterSpacing(letterSpacing);
                idealWidth = getTextWidth(content, paint);
                if (idealWidth > width) {
                    break;
                } else {
                    fontParam.mLetterSpacing = letterSpacing;
                }
            }
        }
        if (DEBUG) {
            Log.d(TAG, "Content: " + content + ", fontSize: " + fontParam.mTextSize + ", letterSpacing: " + fontParam.mLetterSpacing);
        }
        return fontParam;
    }
}