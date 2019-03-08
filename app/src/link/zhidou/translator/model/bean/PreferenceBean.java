package link.zhidou.translator.model.bean;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import link.zhidou.translator.adapter.ApnEditorListAdapter;
import link.zhidou.translator.ui.activity.KeyboardActivity;
import link.zhidou.translator.ui.activity.SingleOrMultipleChoiceActivity;

public class PreferenceBean {

    private int mRequestCode;
    private int mIcon;
    private String mTitle;
    private String mSummary;
    private String mPref;
    private boolean mEnable = true;
    private boolean mMultiple = false;
    private String[] mEntries;
    private String[] mValues;
    private int mPrefIndex;
    private boolean[] mCheckState;
    private boolean[] mUiCheckState;
    Intent mIntent;

    public PreferenceBean() {

    }
    public PreferenceBean(int requestCode, String title) {
        mRequestCode = requestCode;
        mTitle = title;
    }

    public PreferenceBean(int requestCode, String title, String[] entries, String[] values) {
        mRequestCode = requestCode;
        mTitle = title;
        mEntries = entries;
        mValues = values;
    }

    public PreferenceBean(int requestCode, String title, String[] entries, String[] values, boolean multiple) {
        mRequestCode = requestCode;
        mTitle = title;
        mEntries = entries;
        mValues = values;
        mMultiple = multiple;
    }

    public void setPref(String pref) {
        mPref = pref;
    }

    public void setPrefIndex(int index) {
        mPrefIndex = index;
        mPref = String.valueOf(mValues[index]);
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getPref() {
        return mPref;
    }

    public int getPrefIndex() {
        return mPrefIndex;
    }

    public String getSummary() {
        return mSummary;
    }

    public int findIndexOfValue(String value) {
        for (int i = 0; i < mValues.length; i++) {
            if (value.equals(mValues[i])) {
                return i;
            }
        }
        return -1;
    }

    public void initCheckedState(String pref) {
        if (pref == null) {
            return;
        }

        mPref = pref;

        for (int i = 0; i < mValues.length; i++) {
            mCheckState[i] = mPref.contains(mValues[i]);
        }
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    public boolean isEnable() {
        return mEnable;
    }


    public int getType() {
        if (TextUtils.isEmpty(mTitle)) {
            return ApnEditorListAdapter.DIVIDER;
        }
        return ApnEditorListAdapter.CONTENT;
    }

    public String getTitle() {
        return mTitle;
    }


    public void handleClick(Activity activity) {
        if (mEntries == null) {
            Intent intent = new Intent(activity, KeyboardActivity.class);
            intent.putExtra(KeyboardActivity.EXTRA_TITLE, mTitle);
            intent.putExtra(KeyboardActivity.EXTRA_IN_VALUE, TextUtils.isEmpty(mPref) ? "" : mPref);
            activity.startActivityForResult(intent, mRequestCode);
        } else if (mMultiple) {
            Intent intent = new Intent(activity, SingleOrMultipleChoiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_TITLE, mTitle);
            bundle.putInt(SingleOrMultipleChoiceActivity.EXTRA_MODE, SingleOrMultipleChoiceActivity.MULTIPLE);
            bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_ENTRIES, mEntries);
            bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_VALUES, mValues);
            bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_PREF, mPref);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, mRequestCode);
        } else {
            Intent intent = new Intent(activity, SingleOrMultipleChoiceActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_TITLE, mTitle);
            bundle.putInt(SingleOrMultipleChoiceActivity.EXTRA_MODE, SingleOrMultipleChoiceActivity.SINGLE);
            bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_ENTRIES, mEntries);
            bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_VALUES, mValues);
            bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_PREF, mPref);
            intent.putExtras(bundle);
            activity.startActivityForResult(intent, mRequestCode);
        }
    }

}
