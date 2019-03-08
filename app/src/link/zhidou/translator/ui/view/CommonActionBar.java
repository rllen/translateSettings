package link.zhidou.translator.ui.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import link.zhidou.translator.R;
import link.zhidou.translator.SpeechApp;
import link.zhidou.translator.utils.ViewUtil;

/**
 * Date: 18-1-14
 * Time: 下午10:31
 * Email: lostsearover@gmail.com
 */

public class CommonActionBar extends RelativeLayout implements View.OnClickListener {

    TextView mTitle;
    ImageView mBackButton;
    ImageView mSettingsButton;
    BackPressedListener mBackPressedListener;
    SettingsPressedListener mSettingsPressedListener;
    TitlePressedListener mTitlePressedListener;

    public interface BackPressedListener {
        void onBackPressed(View view);
    }

    public interface SettingsPressedListener {
        void onSettingsPressed(View view);
    }

    public interface TitlePressedListener {
        void onTitlePressed(View view);
    }

    public CommonActionBar(Context context) {
        super(context, null);
    }

    public CommonActionBar(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CommonActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView) findViewById(R.id.title);
        mBackButton = (ImageView) findViewById(R.id.back);
        mBackButton.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        boolean isRtl = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isRtl = ViewUtil.isRtl(SpeechApp.getContext().getResources());
        } else {
            isRtl = ViewUtil.isViewLayoutRtl(this);
        }
        if (isRtl) {
            mBackButton.setImageResource(R.drawable.back_icon_reverse_selector);
        } else {
            mBackButton.setImageResource(R.drawable.back_icon_selector);
        }
        mSettingsButton = (ImageView) findViewById(R.id.settings);
        mSettingsButton.setOnClickListener(this);
    }

    public void updateBackButtonImageSource() {
        boolean isRtl = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            isRtl = ViewUtil.isRtl(SpeechApp.getContext().getResources());
        } else {
            isRtl = ViewUtil.isViewLayoutRtl(this);
        }
        if (isRtl) {
            mBackButton.setImageResource(R.drawable.back_icon_reverse_selector);
        } else {
            mBackButton.setImageResource(R.drawable.back_icon_selector);
        }
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        mTitle.setVisibility(VISIBLE);
        mTitle.setSelected(true);
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
        mTitle.setVisibility(VISIBLE);
        mTitle.setSelected(true);
    }

    public void setTitle(int id) {
        mTitle.setText(id);
        mTitle.setVisibility(VISIBLE);
        mTitle.setSelected(true);
    }

    public void setBackPressedListener(BackPressedListener listener) {
        mBackPressedListener = listener;
    }

    public void setSettingsPressedListener(SettingsPressedListener listener) {
        mSettingsPressedListener = listener;
        mSettingsButton.setVisibility(VISIBLE);
    }

    public void setTitlePressedListener (TitlePressedListener listener) {
        mTitlePressedListener = listener;
    }

    public void setBackButtonVisible(boolean status){
        if (status) {
            mBackButton.setVisibility(View.VISIBLE);
        }else{
            mBackButton.setVisibility(View.INVISIBLE);
        }
    }

    public void setSettingButtonVisible(boolean status){
        if (status) {
            mSettingsButton.setVisibility(View.VISIBLE);
        }else{
            mSettingsButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                if (mBackPressedListener != null) {
                    mBackPressedListener.onBackPressed(v);
                }
                break;
            case R.id.settings:
                if (mSettingsPressedListener != null) {
                    mSettingsPressedListener.onSettingsPressed(v);
                }
                break;
            case R.id.title:
                if (mTitlePressedListener != null) {
                    mTitlePressedListener.onTitlePressed(v);
                }
                break;
            default:
                break;
        }
    }
}
