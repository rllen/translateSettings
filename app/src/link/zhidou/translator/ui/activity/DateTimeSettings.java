package link.zhidou.translator.ui.activity;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingShareListAdapter;
import link.zhidou.translator.adapter.TimeZoneListAdapter;
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.ui.view.DatePicker;
import link.zhidou.translator.ui.view.TimePicker;
import link.zhidou.translator.ui.view.dialog.DatePickerDialog;
import link.zhidou.translator.ui.view.dialog.TimePickerDialog;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SPUtil;

/**
 * @author jc
 */
public class DateTimeSettings extends ActionBarBaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private SwitchCompat mDateTimeSwitch;
    private static final String TAG = "DateTimeSettings";
    private static final boolean DEBUG = Log.isLoggable();
    private static final String HOURS_12 = "12";
    private static final String HOURS_24 = "24";
    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ListView mListView;
    private ArrayList<SettingBean> mSettingBeans = new ArrayList<>();
    private SettingShareListAdapter mAdapter;
    /**
     * Time zone
     */
    private SwitchCompat mTimeZoneSwitch;
    private ListView mTimeZoneListView;
    private ArrayList<SettingBean> mTimeZoneSettingBeans = new ArrayList<>();
    private TimeZoneListAdapter mTimeZoneAdapter;
    private TextView mWifiNext;
    private TextView mWifiLast;
    private LinearLayout mTimeAllButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_settings);
        initView();
        initData();
    }

    private boolean getAutoState(String name) {
        try {
            return Settings.Global.getInt(getContentResolver(), name) > 0;
        } catch (Settings.SettingNotFoundException snfe) {
            return false;
        }
    }


    /*  Get & Set values from the system settings  */

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(this);
    }

    private void set24Hour(boolean is24Hour) {
        Settings.System.putString(getContentResolver(),
                Settings.System.TIME_12_24,
                is24Hour ? HOURS_24 : HOURS_12);
        Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
        timeChanged.putExtra("android.intent.extra.TIME_PREF_24_HOUR_FORMAT", is24Hour());
        sendBroadcast(timeChanged);
    }

    /* package */
    static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /* package */
    static void setTime(Context context, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();

        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    private void initView() {
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        if (DEBUG) {
            Log.d(TAG, "autoTimeEnabled: " + autoTimeEnabled);
        }
        mDateTimeSwitch = findViewById(R.id.date_time_auto);
        mDateTimeSwitch.setChecked(autoTimeEnabled ? true : false);
        mDateTimeSwitch.setOnClickListener(this);
        mListView = findViewById(R.id.list_view);
        mAdapter = new SettingShareListAdapter(this, mSettingBeans);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        // Add time zone
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
        mTimeZoneSwitch = findViewById(R.id.time_zone_auto);
        mTimeZoneSwitch.setChecked(autoTimeZoneEnabled ? true : false);
        mTimeZoneSwitch.setOnClickListener(this);
        mTimeZoneListView = findViewById(R.id.time_zone_list_view);
        mTimeZoneAdapter = new TimeZoneListAdapter(this, mTimeZoneSettingBeans);
        mTimeZoneListView.setAdapter(mTimeZoneAdapter);
        mTimeZoneListView.setOnItemClickListener(this);

        mTimeAllButton = (LinearLayout) findViewById(R.id.ll_time_splash);
        Bundle d = this.getIntent().getExtras();
        if (d != null) {
            String ms = d.getString("splash_time");
            if ("first".equals(ms)) {
                mTimeAllButton.setVisibility(View.VISIBLE);
            } else {
                mTimeAllButton.setVisibility(View.GONE);
            }
        }
        mWifiLast = (TextView) findViewById(R.id.tv_time_last);
        mWifiLast.setOnClickListener(this);
        mWifiNext = (TextView) findViewById(R.id.tv_time_next);
        mWifiNext.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register for time ticks and other reasons for time change
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter, null, null);
        updateTimeAndDateDisplay(this);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mIntentReceiver);
        super.onPause();
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTimeAndDateDisplay(DateTimeSettings.this);
        }
    };


    private static String getZoneLongName(Locale locale, TimeZone tz, Date now) {
        boolean daylight = tz.inDaylightTime(now);
        // This returns a name if it can, or will fall back to GMT+0X:00 format.
        return tz.getDisplayName(daylight, TimeZone.LONG, locale);
    }

    private static String getGmtOffsetString(Locale locale, TimeZone tz, Date now) {
        // Use SimpleDateFormat to format the GMT+00:00 string.
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("ZZZZ");
        gmtFormatter.setTimeZone(tz);
        String gmtString = gmtFormatter.format(now);

        // Ensure that the "GMT+" stays with the "00:00" even if the digits are RTL.
        BidiFormatter bidiFormatter = BidiFormatter.getInstance();
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
        gmtString = bidiFormatter.unicodeWrap(gmtString,
                isRtl ? TextDirectionHeuristics.RTL : TextDirectionHeuristics.LTR);
        return gmtString;
    }

    public static String getTimeZoneOffsetAndName(TimeZone tz, Date now) {
        Locale locale = Locale.getDefault();
        String gmtString = getGmtOffsetString(locale, tz, now);
        String zoneNameString = getZoneLongName(locale, tz, now);
        if (zoneNameString == null) {
            return gmtString;
        }

        // We don't use punctuation here to avoid having to worry about localizing that too!
        return gmtString + " " + zoneNameString;
    }


    public void updateTimeAndDateDisplay(Context context) {
        final Calendar now = Calendar.getInstance();
        boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
        boolean autoTimeZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
        SettingBean date = mAdapter.getItem(0);
        date.setUserChoice(sDateFormat.format(now.getTime()));
        date.setEnable(autoTimeEnabled ? false : true);
        SettingBean time = mAdapter.getItem(1);
        time.setEnable(autoTimeEnabled ? false : true);
        time.setUserChoice(DateFormat.getTimeFormat(this).format(now.getTime()));
        mAdapter.notifyDataSetChanged();
        SettingBean timeZone = mTimeZoneAdapter.getItem(0);
        timeZone.setEnable(!autoTimeZoneEnabled);
        timeZone.setUserChoice(getTimeZoneOffsetAndName(now.getTimeZone(), now.getTime()));
        mTimeZoneAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        setActionBarTitle(DateTimeSettings.this);
        mSettingBeans.clear();
        SettingBean bean = new SettingBean();
        bean.setSettingName(getString(R.string.date_time_set_date));
        bean.setUserChoice("2018-05-25");
        bean.setUserChoiceVisible(true);
        bean.setShow(true);
        mSettingBeans.add(bean);

        bean = new SettingBean();
        bean.setSettingName(getString(R.string.date_time_set_time));
        bean.setUserChoice("19:26");
        bean.setUserChoiceVisible(true);
        bean.setShow(true);
        mSettingBeans.add(bean);

        // Time zone
        bean = new SettingBean();
        bean.setSettingName(getString(R.string.date_time_set_timezone));
        final Calendar now = Calendar.getInstance();
        bean.setUserChoice(getTimeZoneOffsetAndName(now.getTimeZone(), now.getTime()));
        bean.setUserChoiceVisible(true);
        bean.setShow(true);
        mTimeZoneSettingBeans.add(bean);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        setDate(this, year, month, day);
        updateTimeAndDateDisplay(this);
    }


    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setTime(this, hourOfDay, minute);
        updateTimeAndDateDisplay(this);
    }

    static void configureDatePicker(DatePicker datePicker) {
        // The system clock can't represent dates outside this range.
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1970, Calendar.JANUARY, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.date_time_auto:
                boolean autoTimeEnabled = getAutoState(Settings.Global.AUTO_TIME);
                if (autoTimeEnabled) {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
                } else {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 1);
                }
                updateTimeAndDateDisplay(this);
                break;
            case R.id.time_zone_auto:
                boolean autoZoneEnabled = getAutoState(Settings.Global.AUTO_TIME_ZONE);
                if (autoZoneEnabled) {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);
                } else {
                    Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 1);
                }
                updateTimeAndDateDisplay(this);
                break;
            case R.id.tv_time_last:
//                backKeycodePress();
                break;
            case R.id.tv_time_next:
                /**
                 * 第一次启动程序，引导页设置点击了跳过按钮，直接跳转到翻译主界面
                 */
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (parent == mListView) {
            final Calendar calendar = Calendar.getInstance();
            if (!mAdapter.getItem(position).isEnable()) {
                return;
            }
            switch (position) {
                case 0:
                    DatePickerDialog d = new DatePickerDialog(
                            this,
                            R.style.LightAlertDialog,
                            this,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    configureDatePicker(d.getDatePicker());
                    d.show();
                    break;
                case 1:
                    TimePickerDialog tpd = new TimePickerDialog(
                            this,
                            R.style.LightAlertDialog,
                            this,
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            DateFormat.is24HourFormat(this));
                    tpd.show();
                default:
                    break;
            }
        } else if (mTimeZoneListView == parent) {
            if (!mTimeZoneAdapter.getItem(position).isEnable()) {
                return;
            }
            switch (position) {
                case 0:
                    startActivity(new Intent(this, TimeZoneSelectActivity.class));
                    break;
                default:
                    break;
            }
        }
    }

}
