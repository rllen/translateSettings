package link.zhidou.translator.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.ArrayList;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingShareListAdapter;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.Log;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

/**
 * Created by czm on 17-8-9.
 */

public class DisplaySettingsActivity extends ActionBarBaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = DisplaySettingsActivity.class.getSimpleName();
    private static final int SCREEN_TIMEOUT_REQC = 1001;
    private ListView mListView;
    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 60000;
    private ArrayList<SettingBean> mSettingsBeans = new ArrayList<>();
    private SettingShareListAdapter mAdapter = null;
    private SeekBar mSeekBar = null;
    private Handler mUiHandler = null;

    /**
     * Gets brightness level.
     *
     * @param context
     * @return brightness level between 0 and 255.
     */
    private static int getBrightness(Context context) {
        try {
            int brightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            return brightness;
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 设置当前屏幕亮度值  0--255
     */
    private void setBrightness(int paramInt){
        try{
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private ContentObserver mBrightnessObserver = null ;

    /**
     * 保存当前的屏幕亮度值，并使之生效
     */
    private void setScreenBrightness(int paramInt){
        Window localWindow = getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        float f = paramInt / 255.0F;
        localLayoutParams.screenBrightness = f;
        localWindow.setAttributes(localLayoutParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_settings);
        setActionBarTitle(DisplaySettingsActivity.this);
        mUiHandler = new Handler(getMainLooper());
        mBrightnessObserver = new ContentObserver(mUiHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                mSettingsBeans.get(0).setUserChoice(getDefaultTimeOutDesc());
                mAdapter.notifyDataSetChanged();
            }
        };
        initData();
        initView();
        getContentResolver().registerContentObserver(Settings.System.getUriFor(SCREEN_OFF_TIMEOUT), false, mBrightnessObserver);
    }

    private void initData() {
        mSettingsBeans.clear();
        SettingBean bean = new SettingBean();
        bean.setSettingName(getString(R.string.screen_timeout));
        bean.setUserChoice(getDefaultTimeOutDesc());
        bean.setUserChoiceVisible(true);
        bean.setShow(true);
        mSettingsBeans.add(bean);
        mAdapter = new SettingShareListAdapter(this, mSettingsBeans);
    }

    private String getDefaultTimeOutDesc() {
        final int currentTimeout = Settings.System.getInt(getContentResolver(), SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        String[] entries = getResources().getStringArray(R.array.screen_timeout_entries);
        String[] values = getResources().getStringArray(R.array.screen_timeout_values);
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(String.valueOf(currentTimeout))) {
                return entries[i];
            }
        }
        return entries[1];
    }

    private void startScreenTimeOutSettings() {
        Intent intent = new Intent(this, SingleOrMultipleChoiceActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_TITLE, getString(R.string.screen_timeout));
        bundle.putInt(SingleOrMultipleChoiceActivity.EXTRA_MODE, SingleOrMultipleChoiceActivity.SINGLE);
        bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_ENTRIES, getResources().getStringArray(R.array.screen_timeout_entries));
        bundle.putStringArray(SingleOrMultipleChoiceActivity.EXTRA_VALUES, getResources().getStringArray(R.array.screen_timeout_values));
        bundle.putBoolean(SingleOrMultipleChoiceActivity.EXTRA_FINISH_NOW, true);
        final int currentTimeout = Settings.System.getInt(getContentResolver(), SCREEN_OFF_TIMEOUT, FALLBACK_SCREEN_TIMEOUT_VALUE);
        bundle.putString(SingleOrMultipleChoiceActivity.EXTRA_PREF, String.valueOf(currentTimeout));
        intent.putExtras(bundle);
        startActivityForResult(intent, SCREEN_TIMEOUT_REQC);
    }

    public void setSeekBarColor(SeekBar seekBar, int color){
        LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
        Drawable dra=layerDrawable.getDrawable(2);
        dra.setColorFilter(color, PorterDuff.Mode.SRC);
        Drawable dra2=seekBar.getThumb();
        seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        seekBar.invalidate();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mSeekBar = findViewById(R.id.progress);
        setSeekBarColor(mSeekBar, Color.parseColor("#1885f2"));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int progress = seekBar.getProgress();
                int brightness = (int) ((progress / 100f) * 255f);
                setScreenBrightness(brightness);
                setBrightness(brightness);
            }
        });
        int progress = (int) ((getBrightness(this) / 255f) * 100);
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        startScreenTimeOutSettings();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (SCREEN_TIMEOUT_REQC == requestCode) {
                try {
                    int value = Integer.parseInt(data.getStringExtra(SingleOrMultipleChoiceActivity.EXTRA_NEW_PREF));
                    Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist screen timeout setting", e);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        getContentResolver().unregisterContentObserver(mBrightnessObserver);
        super.onDestroy();
    }
}
