package link.zhidou.translator.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SingleOrMultipleAdapter;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.utils.Log;

public class SingleOrMultipleChoiceActivity extends BaseActivity implements AdapterView.OnItemClickListener, CommonActionBar.BackPressedListener {

    private static final String TAG = "SingleOrMultiple";
    private static final boolean DEBUG = Log.isLoggable();
    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_ENTRIES = "extra_entries";
    public static final String EXTRA_VALUES = "extra_values";
    public static final String EXTRA_PREF = "extra_pref";
    public static final String EXTRA_NEW_PREF = "extra_new_pref";
    public static final String EXTRA_FINISH_NOW = "extra_finish_now";
    public boolean mFinishNow = false;
    public static final int SINGLE = 0;
    public static final int MULTIPLE = 1;
    private String[] mEntries;
    private String[] mValues;
    private boolean[] mChecked;
    private String mTitle;
    private int mMode;
    private String mPref;

    private ListView mListView;
    private CommonActionBar mActionBar;
    private SingleOrMultipleAdapter mAdapter;
    private List<ChoiceItem> mChoices = new ArrayList<>();
    private int mPrefIndex = -1;

    public static class ChoiceItem {
        public String entry;
        public String value;
        public boolean checked;

        public ChoiceItem(String entry, String value, boolean checked) {
            this.entry = entry;
            this.value = value;
            this.checked = checked;
        }

        @Override
        public String toString() {
            return "ChoiceItem{" +
                    "entry='" + entry + '\'' +
                    ", value='" + value + '\'' +
                    ", checked=" + checked +
                    '}';
        }
    }

    private void initCheckedStatus() {
        mChecked = new boolean[mEntries.length];
        if (mPref != null) {
            final String[] prefs = mPref.split(",");
            final HashSet<String> prefSet = new HashSet<>();
            for (final String pref : prefs) {
                prefSet.add(pref);
            }
            for (int i = 0; i < mValues.length; i++) {
                if (prefSet.contains(mValues[i])) {
                    mChecked[i] = true;
                    if (SINGLE == mMode) {
                        mPrefIndex = i;
                    }
                } else {
                    mChecked[i] = false;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sex);
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setBackPressedListener(this);
        mListView = findViewById(R.id.lv_setting_sex);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mMode = bundle.getInt(EXTRA_MODE);
            mTitle = bundle.getString(EXTRA_TITLE);
            mEntries = bundle.getStringArray(EXTRA_ENTRIES);
            mValues = bundle.getStringArray(EXTRA_VALUES);
            mPref = bundle.getString(EXTRA_PREF);
            mFinishNow = bundle.getBoolean(EXTRA_FINISH_NOW);
            initCheckedStatus();
        }
        for (int i = 0; i < mEntries.length; i++) {
            mChoices.add(new ChoiceItem(mEntries[i], mValues[i], mChecked[i]));
        }
        if (DEBUG) {
            Log.d(TAG, "Choices: " + mChoices);
        }
        mAdapter = new SingleOrMultipleAdapter(this, mChoices);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mActionBar.setTitle(mTitle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (SINGLE == mMode) {
            final ChoiceItem item = mAdapter.getItem(position);
            if (position != mPrefIndex) {
                if (mPrefIndex != -1) {
                    mAdapter.getItem(mPrefIndex).checked = false;
                }
                item.checked = true;
                mPrefIndex = position;

            }
            mAdapter.notifyDataSetChanged();
            if (mFinishNow) {
                setResult();
                finish();
            }
        } else {
            final ChoiceItem item = mAdapter.getItem(position);
            item.checked = !item.checked;
            mAdapter.notifyDataSetChanged();
        }
    }


    public void setResult() {
        Intent intent = new Intent();
        if (SINGLE == mMode) {
            if (mPrefIndex != -1) {
                intent.putExtra(EXTRA_NEW_PREF, mChoices.get(mPrefIndex).value);
            }
            setResult(RESULT_OK, intent);
        } else {
            final StringBuilder sb = new StringBuilder();
            int size = mChoices.size();
            int pre = size;
            for (int i = 0; i < size; i++) {
                ChoiceItem item = mChoices.get(i);
                if (item.checked) {
                    if (i > pre) {
                        sb.append(",");
                    }
                    pre = i;
                    sb.append(item.value);
                }
            }
            intent.putExtra(EXTRA_NEW_PREF, sb.toString());
            setResult(RESULT_OK, intent);
        }
    }

    @Override
    public void onBackPressed(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        setResult();
        super.onBackPressed();
    }
}
