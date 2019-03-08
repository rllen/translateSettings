package link.zhidou.translator.model.bean;

import android.text.TextUtils;

import link.zhidou.translator.adapter.SettingShareListAdapter;

/**
 * Creating Time :  2017/9/11.
 * AUTHOR : CaoQi
 * E-MAIL : 18507118810@163.com
 * Description : This is a Class for ----
 */

public class SettingBean {
    private int mImageViewId;
    private String settingName;
    private String userChoice;
    private boolean isShow;
    private boolean mEnable = true;
    private boolean mUserChoiceVisible = false;

    private boolean mShowRedLabel = false;

    public void setShowRedLabel(boolean show) {
        mShowRedLabel = show;
    }

    public boolean showRedLabel() {
        return mShowRedLabel;
    }

    public SettingBean() {

    }

    public SettingBean(int mImageViewId , String settingName, String userChoice, boolean isShow) {
        this.mImageViewId = mImageViewId;
        this.settingName = settingName;
        this.userChoice = userChoice;
        this.isShow = isShow;
    }

    public int getmImageViewId() {
        return mImageViewId;
    }

    public void setmImageViewId(int mImageViewId) {
        this.mImageViewId = mImageViewId;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(String settingName) {
        this.settingName = settingName;
    }

    public String getUserChoice() {
        return userChoice;
    }

    public void setUserChoice(String userChoice) {
        this.userChoice = userChoice;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }


    public void setUserChoiceVisible(boolean userChoiceVisible) {
        mUserChoiceVisible = userChoiceVisible;
    }

    public boolean isUserChoiceVisible() {
        return mUserChoiceVisible;
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }

    public boolean isEnable() {
        return mEnable;
    }


    public int getType() {
        if (TextUtils.isEmpty(settingName)) {
            return SettingShareListAdapter.DIVIDER;
        } else {
            return SettingShareListAdapter.CONTENT;
        }
    }

    private Object extra;

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public Object getExtra() {
        return extra;
    }

    @Override
    public String toString() {
        return "SettingBean{" +
                "settingName='" + settingName + '\'' +
                ", userChoice='" + userChoice + '\'' +
                ", isShow=" + isShow +
                '}';
    }
}
