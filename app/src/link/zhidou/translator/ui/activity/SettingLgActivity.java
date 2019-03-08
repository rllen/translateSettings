package link.zhidou.translator.ui.activity;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.SPUtil;
import link.zhidou.translator.utils.ViewUtil;

import static link.zhidou.translator.utils.SPKeyContent.FIRST_OPEN;

/**
 * Created by czm on 17-8-9.
 */

public class SettingLgActivity extends ActionBarBaseActivity implements View.OnClickListener {
    private static String TAG = SettingLgActivity.class.getSimpleName();
    private static boolean DEBUG = Log.isLoggable();
    private ListView mLvSettingSex;
    private ArrayAdapter<LocaleInfo> mAdapter;
    private LinearLayout mSplash;
    private TextView mTitle;
    private TextView mLgNextButton;
    private LinearLayout mLGALlButton;
    private TextView mLgSkip;
    private boolean mFinishImmediately = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateFinishImmediately(getIntent());
        setContentView(R.layout.activity_lg);
        initView();
        initData();
    }

    private void initData() {
        setActionBarTitle(SettingLgActivity.this);
    }

    private void updateFinishImmediately(Intent intent) {
        mFinishImmediately = true;
        if (SPUtil.getBoolean(this, FIRST_OPEN, false)) {
            mFinishImmediately = false;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateFinishImmediately(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mFinishImmediately) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ViewCompat.setLayoutDirection(mSplash, newConfig.getLayoutDirection());
                ViewCompat.setLayoutDirection(mLvSettingSex, newConfig.getLayoutDirection());
            }
            mTitle.setText(R.string.activity_lg_title);
            mAdapter.notifyDataSetChanged();
            mLgNextButton.setText(R.string.next);
            mLgSkip.setText(R.string.skip);
            updateBackButton();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    public static class LocaleInfo implements Comparable<LocaleInfo> {
        static final Collator sCollator = Collator.getInstance();

        String label;
        Locale locale;

        public LocaleInfo(String label, Locale locale) {
            this.label = label;
            this.locale = locale;
        }

        public String getLabel() {
            return label;
        }

        public Locale getLocale() {
            return locale;
        }

        @Override
        public String toString() {
            return this.label;
        }

        @Override
        public int compareTo(LocaleInfo another) {
            // Default always first.
            if (another.getLocale().equals(Locale.getDefault())) {
                return 1;
            } else if (locale.equals(Locale.getDefault())) {
                return -1;
            }
            return sCollator.compare(this.label, another.label);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static List<LocaleInfo> getAllAssetLocalesLollipop(Context context) {
        final Resources resources = context.getResources();
        final String[] locales = Resources.getSystem().getAssets().getLocales();
        List<String> localeList = new ArrayList<String>(locales.length);
        Collections.addAll(localeList, locales);
        Collections.sort(localeList);
        final String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        final String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);
        final ArrayList<LocaleInfo> localeInfos = new ArrayList<LocaleInfo>(localeList.size());
        for (String locale : localeList) {
            final Locale l = Locale.forLanguageTag(locale.replace('_', '-'));
            if (l == null || "und".equals(l.getLanguage())
                    || l.getLanguage().isEmpty() || l.getCountry().isEmpty()) {
                continue;
            }
            localeInfos.add(new LocaleInfo(toTitleCase(getDisplayName(l, specialLocaleCodes, specialLocaleNames)), l));
        }
        Collections.sort(localeInfos);
        return localeInfos;
    }


    public static List<LocaleInfo> getAllAssetLocalesLower(Context context) {
        final Resources resources = context.getResources();
        ArrayList<String> localeList = new ArrayList<String>(Arrays.asList(
                Resources.getSystem().getAssets().getLocales()));
        String[] locales = new String[localeList.size()];
        locales = localeList.toArray(locales);

        final String[] specialLocaleCodes = resources.getStringArray(R.array.special_locale_codes);
        final String[] specialLocaleNames = resources.getStringArray(R.array.special_locale_names);
        Arrays.sort(locales);
        final int origSize = locales.length;
        final LocaleInfo[] preprocess = new LocaleInfo[origSize];
        int finalSize = 0;
        for (int i = 0; i < origSize; i++) {
            final String s = locales[i];
            final int len = s.length();
            if (len == 5) {
                String language = s.substring(0, 2);
                String country = s.substring(3, 5);
                final Locale l = new Locale(language, country);

                if (finalSize == 0) {
                    if (DEBUG) {
                        Log.v(TAG, "adding initial " + toTitleCase(l.getDisplayLanguage(l)));
                    }
                    preprocess[finalSize++] =
                            new LocaleInfo(toTitleCase(l.getDisplayLanguage(l)), l);
                } else {
                    // check previous entry:
                    //  same lang and a country -> upgrade to full name and
                    //    insert ours with full name
                    //  diff lang -> insert ours with lang-only name
                    if (preprocess[finalSize - 1].locale.getLanguage().equals(
                            language) &&
                            !preprocess[finalSize - 1].locale.getLanguage().equals("zz")) {
                        if (DEBUG) {
                            Log.v(TAG, "backing up and fixing " +
                                    preprocess[finalSize - 1].label + " to " +
                                    getDisplayName(preprocess[finalSize - 1].locale,
                                            specialLocaleCodes, specialLocaleNames));
                        }
                        preprocess[finalSize - 1].label = toTitleCase(
                                getDisplayName(preprocess[finalSize - 1].locale,
                                        specialLocaleCodes, specialLocaleNames));
                        if (DEBUG) {
                            Log.v(TAG, "  and adding " + toTitleCase(
                                    getDisplayName(l, specialLocaleCodes, specialLocaleNames)));
                        }
                        preprocess[finalSize++] =
                                new LocaleInfo(toTitleCase(
                                        getDisplayName(
                                                l, specialLocaleCodes, specialLocaleNames)), l);
                    } else {
                        String displayName;
                        if (s.equals("zz_ZZ")) {
                            displayName = "[Developer] Accented English";
                        } else if (s.equals("zz_ZY")) {
                            displayName = "[Developer] Fake Bi-Directional";
                        } else {
                            displayName = toTitleCase(l.getDisplayLanguage(l));
                        }
                        if (DEBUG) {
                            Log.v(TAG, "adding " + displayName);
                        }
                        preprocess[finalSize++] = new LocaleInfo(displayName, l);
                    }
                }
            }
        }

        final List<LocaleInfo> localeInfos = new ArrayList<>();
        for (int i = 0; i < finalSize; i++) {
            localeInfos.add(preprocess[i]);
        }
        Collections.sort(localeInfos);
        return localeInfos;
    }

    /**
     * Constructs an Adapter object containing Locale information. Content is sorted by
     * {@link LocaleInfo#label}.
     */
    public static ArrayAdapter<LocaleInfo> constructAdapter(Context context) {
        return constructAdapter(context, R.layout.lv_setting_sex_list_item, R.id.tv_item_setting);
    }

    private static class ViewHolder {
        SettingLgActivity activity;
        TextView text;
        ImageView tick;
        public LocaleInfo info;

        public ViewHolder(SettingLgActivity activity, TextView text, ImageView tick) {
            this.activity = activity;
            this.text = text;
            this.tick = tick;
        }

        public void setLocaleInfo(LocaleInfo info) {
            this.info = info;
        }
    }

    public boolean isFinishImmediately() {
        return mFinishImmediately;
    }

    private static boolean localeEquals(Locale a, Locale b) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            return a.equals(b);
        } else {
            String aL = a.getLanguage();
            String aC = a.getCountry();
//            String aS = a.getScript();
            String bL = b.getLanguage();
            String bC = b.getCountry();
//            String bS = b.getScript();
            return aL.equals(bL) && aC.equals(bC);
        }
    }

    public static ArrayAdapter<LocaleInfo> constructAdapter(final Context context, final int layoutId, final int fieldId) {
        List<LocaleInfo> localeInfos = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            localeInfos = getAllAssetLocalesLollipop(context);
        } else {
            localeInfos = getAllAssetLocalesLower(context);
        }

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ArrayAdapter<LocaleInfo>(context, layoutId, fieldId, localeInfos) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                TextView text;
                ImageView tick = null;
                ViewHolder holder = null;
                if (convertView == null) {
                    view = inflater.inflate(layoutId, parent, false);
                    text = (TextView) view.findViewById(fieldId);
                    tick = (ImageView) view.findViewById(R.id.iv_item_tick);
                    holder = new ViewHolder((SettingLgActivity) context, text, tick);
                    view.setTag(holder);
                } else {
                    final boolean parentIsRtl = ViewUtil.isViewLayoutRtl(parent);
                    final boolean childIsRtl = ViewUtil.isViewLayoutRtl(convertView);
                    if (childIsRtl != parentIsRtl) {
                        convertView.setLayoutDirection(parent.getLayoutDirection());
                    }
                    view = convertView;
                    holder = ((ViewHolder) view.getTag());
                }
                LocaleInfo item = getItem(position);
                holder.setLocaleInfo(item);
                if (localeEquals(item.getLocale(), Locale.getDefault())) {
                    holder.tick.setVisibility(View.VISIBLE);
                } else {
                    holder.tick.setVisibility(View.GONE);
                }
                holder.text.setText(item.toString());
                holder.text.setTextLocale(item.getLocale());
                return view;
            }
        };
    }

    private static String toTitleCase(String s) {
        if (s.length() == 0) {
            return s;
        }

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String getDisplayName(
            Locale l, String[] specialLocaleCodes, String[] specialLocaleNames) {
        String code = l.toString();

        for (int i = 0; i < specialLocaleCodes.length; i++) {
            if (specialLocaleCodes[i].equals(code)) {
                return specialLocaleNames[i];
            }
        }

        return l.getDisplayLanguage(l);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = constructAdapter(this);
        mLvSettingSex.setAdapter(mAdapter);
    }

    private void initView() {
        mSplash = findViewById(R.id.ll_lg_splash);
        mTitle = (TextView) findViewById(R.id.title);
        mLvSettingSex = (ListView) findViewById(R.id.lv_setting_sex);
        mLvSettingSex.setItemsCanFocus(true);
        mLvSettingSex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ViewHolder tag = (ViewHolder) view.getTag();
                SettingLgActivity settingLgActivity = tag.activity;
                updateLocale(tag.info.locale);
                if (settingLgActivity.isFinishImmediately()) {
                    settingLgActivity.finish();
                }
            }
        });
        mLGALlButton = (LinearLayout) findViewById(R.id.ll_lg_splash);
//        Bundle d = this.getIntent().getExtras();
//        if (d != null) {
//            String ms = d.getString("splash_lg");
//            if ("first".equals(ms)) {
//                mLGALlButton.setVisibility(View.VISIBLE);
//            } else {
//                mLGALlButton.setVisibility(View.GONE);
//            }
//        }
        mLgSkip = (TextView) findViewById(R.id.tv_lg_skip);
        mLgSkip.setOnClickListener(this);
        mLgNextButton = (TextView) findViewById(R.id.tv_lg_next);
        mLgNextButton.setOnClickListener(this);
        if (mFinishImmediately) {
            mLGALlButton.setVisibility(View.GONE);
        } else {
            mLGALlButton.setVisibility(View.VISIBLE);
        }
    }


    public static void updateLocale(Locale locale) {
        try {
            Class cls = Class.forName("android.app.IActivityManager");
            Object instance = Class.forName("android.app.ActivityManagerNative");
            instance = ((Class) instance).getDeclaredMethod("getDefault", new Class[0]).invoke(instance, new Object[0]);
            Configuration localConfiguration = (Configuration) cls.getDeclaredMethod("getConfiguration", new Class[0]).invoke(instance, new Object[0]);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                localConfiguration.setLocale(locale);
            } else {
                localConfiguration.locale = locale;
            }
            Class.forName("android.content.res.Configuration").getField("userSetLocale").set(localConfiguration, Boolean.valueOf(true));
            cls.getDeclaredMethod("updateConfiguration", new Class[]{Configuration.class}).invoke(instance, new Object[]{localConfiguration});
            BackupManager.dataChanged("com.android.providers.settings");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_lg_skip:
                /**
                 * 第一次启动程序，引导页设置语音界面点击了跳过按钮，直接跳转到翻译主界面
                 */
                SPUtil.putBoolean(getApplicationContext(), FIRST_OPEN, false);
                Intent intent_skip = new Intent();
                intent_skip.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent_skip);
                finish();
                break;
            case R.id.tv_lg_next:
                Intent intent_next = new Intent();
                intent_next.setClass(SettingLgActivity.this, WifiListActivity.class);
                intent_next.putExtra("splash_wifi", "first");
                startActivity(intent_next);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!SPUtil.getBoolean(this, FIRST_OPEN, false)) {
            super.onBackPressed();
        }
    }
}