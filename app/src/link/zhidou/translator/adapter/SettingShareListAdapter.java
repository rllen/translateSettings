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
import link.zhidou.translator.model.bean.SettingBean;
import link.zhidou.translator.utils.ViewUtil;


/**
 * Wifi扫描列表Adapter
 * Created by czm on 17-10-21.
 */

public class SettingShareListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<SettingBean> mResults;
    private static final String TAG = SettingShareListAdapter.class.getSimpleName();
    private float mDisabledAlpha;
    public static final int CONTENT = 0;
    public static final int DIVIDER = 1;
    public SettingShareListAdapter(Context context, ArrayList<SettingBean> results) {
        mContext = context;
        mResults = results;
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(android.R.attr.disabledAlpha, typedValue, true);
        mDisabledAlpha = typedValue.getFloat();
    }

    @Override
    public int getCount() {
        if (mResults != null) {
            return mResults.size();
        }
        return 0;
    }

    @Override
    public SettingBean getItem(int i) {
        if (mResults != null && i >= 0 && i < mResults.size()) {
            return mResults.get(i);
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
        return mResults.get(position).getType();
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return mResults.get(position).isEnable();
//    }


//    @Override
//    public boolean areAllItemsEnabled() {
//        return super.areAllItemsEnabled();
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        SettingBean settingBean = mResults.get(position);
        if (CONTENT == getItemViewType(position)) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.lv_setting_share_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.tv_setting_function = convertView.findViewById(R.id.tv_setting_function);
                viewHolder.tv_user_select = convertView.findViewById(R.id.tv_user_select);
                viewHolder.iv_arrow = convertView.findViewById(R.id.iv_arrow);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.tv_setting_function.setText(settingBean.getSettingName());
            if (settingBean.isUserChoiceVisible()) {
                viewHolder.tv_user_select.setText(settingBean.getUserChoice());
                viewHolder.tv_user_select.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tv_user_select.setText(settingBean.getUserChoice());
                viewHolder.tv_user_select.setVisibility(View.GONE);
            }
            boolean isRtl = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isRtl = ViewUtil.isRtl(SpeechApp.getContext().getResources());
            } else {
                isRtl = ViewUtil.isViewLayoutRtl(convertView);
            }
            if (isRtl) {
                viewHolder.iv_arrow.setImageResource(R.drawable.arrow_left);
                if (settingBean.isShow()) {
                    viewHolder.iv_arrow.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.iv_arrow.setVisibility(View.GONE);
                }
            } else {
                viewHolder.iv_arrow.setImageResource(R.drawable.arrow);
                if (settingBean.isShow()) {
                    viewHolder.iv_arrow.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.iv_arrow.setVisibility(View.GONE);
                }
            }

            if (settingBean.isEnable()) {
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
        TextView tv_setting_function;
        TextView tv_user_select;
        ImageView iv_arrow;

    }

    public ArrayList<SettingBean> getResults() {
        return mResults;
    }

    public void setResults(ArrayList<SettingBean> results) {
        mResults = results;
        this.notifyDataSetChanged();
    }


}
