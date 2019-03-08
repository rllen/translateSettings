package link.zhidou.translator.ui.activity;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.ViewUtil;
import link.zhidou.translator.utils.ZoneGetter;

/**
 * @author lost
 */
public class TimeZoneSelectActivity extends ActionBarBaseActivity {
    private static String TAG = TimeZoneSelectActivity.class.getSimpleName();
    private static boolean DEBUG = Log.isLoggable();
    private ListView mLvTimeZone;
    private ArrayAdapter<Map<String, Object>> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_zone_select);
        initView();
        initData();
    }

    private void initData() {
        setActionBarTitle(TimeZoneSelectActivity.this);
    }

    /**
     *
     * @param context
     * @return
     */
    public static ArrayAdapter<Map<String, Object>> constructAdapter(Context context) {
        return constructAdapter(context, false, R.layout.time_zone_list_item, R.id.title, R.id.summary);
    }

    private static class ViewHolder {
        TimeZoneSelectActivity activity;
        TextView title;
        TextView summary;
        ImageView tick;
        public Map<String, Object> info;

        public ViewHolder(TimeZoneSelectActivity activity, TextView title, TextView summary, ImageView tick) {
            this.activity = activity;
            this.title = title;
            this.summary = summary;
            this.tick = tick;
        }

        public void setZoneInfo(Map<String, Object> info) {
            this.info = info;
        }
    }


    private static class MyComparator implements Comparator<Map<?, ?>> {
        private String mSortingKey;

        public MyComparator(String sortingKey) {
            mSortingKey = sortingKey;
        }

        public void setSortingKey(String sortingKey) {
            mSortingKey = sortingKey;
        }

        @Override
        public int compare(Map<?, ?> map1, Map<?, ?> map2) {
            Object value1 = map1.get(mSortingKey);
            Object value2 = map2.get(mSortingKey);

            /*
             * This should never happen, but just in-case, put non-comparable
             * items at the end.
             */
            if (!isComparable(value1)) {
                return isComparable(value2) ? 1 : 0;
            } else if (!isComparable(value2)) {
                return -1;
            }

            return ((Comparable) value1).compareTo(value2);
        }

        private boolean isComparable(Object value) {
            return (value != null) && (value instanceof Comparable);
        }
    }

    public static ArrayAdapter<Map<String, Object>> constructAdapter(final Context context, boolean sortedByName, final int layoutId, final int titleId, int summaryId) {
        final String sortKey = (sortedByName ? ZoneGetter.KEY_DISPLAYNAME : ZoneGetter.KEY_OFFSET);
        final MyComparator comparator = new MyComparator(sortKey);
        final List<Map<String, Object>> sortedList = ZoneGetter.getZonesList(context);
        Collections.sort(sortedList, comparator);
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return new ArrayAdapter<Map<String, Object>>(context, layoutId, titleId, sortedList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                TextView title;
                TextView summary;
                ImageView tick = null;
                ViewHolder holder = null;
                if (convertView == null) {
                    view = inflater.inflate(layoutId, parent, false);
                    title = (TextView) view.findViewById(titleId);
                    summary = (TextView) view.findViewById(R.id.summary);
                    tick = (ImageView) view.findViewById(R.id.iv_item_tick);
                    holder = new ViewHolder((TimeZoneSelectActivity) context, title, summary, tick);
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
                Map<String, Object> item = getItem(position);
                holder.setZoneInfo(item);

                if (timeZoneEquals((String) item.get(ZoneGetter.KEY_ID),TimeZone.getDefault())) {
                    holder.tick.setVisibility(View.VISIBLE);
                } else {
                    holder.tick.setVisibility(View.GONE);
                }
                holder.title.setText((String)item.get(ZoneGetter.KEY_DISPLAYNAME));
                holder.summary.setText((String)item.get(ZoneGetter.KEY_GMT));
                return view;
            }
        };
    }

    private static boolean timeZoneEquals(String id, TimeZone timeZone) {
        return timeZone.getID().equals(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = constructAdapter(this);
        mLvTimeZone.setAdapter(mAdapter);
    }

    private void initView() {
        mLvTimeZone = (ListView) findViewById(R.id.lv_time_zone);
        mLvTimeZone.setItemsCanFocus(true);
        mLvTimeZone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long l) {
                final Map<?, ?> map = (Map<?, ?>)listView.getItemAtPosition(position);
                final String tzId = (String) map.get(ZoneGetter.KEY_ID);
                // Update the system timezone value
                final AlarmManager alarm = (AlarmManager) TimeZoneSelectActivity.this.getSystemService(Context.ALARM_SERVICE);
                alarm.setTimeZone(tzId);
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}