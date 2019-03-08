package link.zhidou.translator.adapter;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import link.zhidou.translator.R;
import link.zhidou.translator.SpeechApp;
import link.zhidou.translator.model.bean.PreferenceBean;
import link.zhidou.translator.utils.ViewUtil;


public class ApnEditorListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<PreferenceBean> mBeans;
    private static final String TAG = ApnEditorListAdapter.class.getSimpleName();
    private float mDisabledAlpha;
    public static final int CONTENT = 0;
    public static final int DIVIDER = 1;
    public ApnEditorListAdapter(Context context, ArrayList<PreferenceBean> beans) {
        mContext = context;
        mBeans = beans;
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
        mDisabledAlpha = typedValue.getFloat();
    }

    @Override
    public int getCount() {
        if (mBeans != null) {
            return mBeans.size();
        }
        return 0;
    }

    @Override
    public PreferenceBean getItem(int i) {
        if (mBeans != null && i >= 0 && i < mBeans.size()) {
            return mBeans.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return mBeans.get(position).getType();
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return mBeans.get(position).isEnable();
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        PreferenceBean bean = mBeans.get(position);
        if (CONTENT == getItemViewType(position)) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.settings_apn_editor_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.title = convertView.findViewById(R.id.title);
                viewHolder.summary = convertView.findViewById(R.id.summary);
                viewHolder.iv_arrow = convertView.findViewById(R.id.iv_arrow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.title.setText(bean.getTitle());
            viewHolder.summary.setText(bean.getSummary());
            viewHolder.summary.setVisibility(View.VISIBLE);
            boolean isRtl = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isRtl = ViewUtil.isRtl(SpeechApp.getContext().getResources());
            } else {
                isRtl = ViewUtil.isViewLayoutRtl(convertView);
            }
            if (isRtl) {
                viewHolder.iv_arrow.setImageResource(R.drawable.arrow_left);
                viewHolder.iv_arrow.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_arrow.setImageResource(R.drawable.arrow);
                viewHolder.iv_arrow.setVisibility(View.VISIBLE);
            }

            if (bean.isEnable()) {
                convertView.setAlpha(1.0f);
            } else {
                convertView.setAlpha(mDisabledAlpha);
            }
            return convertView;
        } else {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.lv_setting_list_item_divider, null);
            }
            return convertView;
        }
    }

    static class ViewHolder {
        TextView title;
        TextView summary;
        ImageView iv_arrow;

    }

    public void setPreferenceBeans(ArrayList<PreferenceBean> prefs) {
        mBeans = prefs;
        this.notifyDataSetChanged();
    }


}
