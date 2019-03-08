package link.zhidou.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import link.zhidou.translator.R;
import link.zhidou.translator.model.bean.SettingSexBean;
import link.zhidou.translator.ui.activity.SettingModeActivity;


/**
 * Wifi扫描列表Adapter
 * Created by czm on 17-10-21.
 */

public class SettingModeListAdapter extends BaseAdapter {
    private Context mContext;
    private SettingModeActivity activity;
    private ArrayList<SettingSexBean> mResults;

    public SettingModeListAdapter(Context context, ArrayList<SettingSexBean> results) {
        mContext = context;
        activity = (SettingModeActivity) context;
        mResults = results;
    }

    @Override
    public int getCount() {
        if (mResults != null) {
            return mResults.size();
        }
        return 0;
    }

    @Override
    public SettingSexBean getItem(int i) {
        if (mResults != null && i >= 0 && i < mResults.size()) {
            return mResults.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        SettingSexBean item = mResults.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.lv_setting_sex_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.rl_item_select = convertView.findViewById(R.id.rl_item_select);
            viewHolder.tv_item_setting = convertView.findViewById(R.id.tv_item_setting);
            viewHolder.iv_item_tick = convertView.findViewById(R.id.iv_item_tick);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tv_item_setting.setText(item.getLabel());
        if (item.isShow()) {
            viewHolder.iv_item_tick.setVisibility(View.VISIBLE);
        }else{
            viewHolder.iv_item_tick.setVisibility(View.GONE);
        }
        return convertView;
    }

    static class ViewHolder {
        RelativeLayout rl_item_select;
        TextView tv_item_setting;
        ImageView iv_item_tick;    }

    public ArrayList<SettingSexBean> getResults() {
        return mResults;
    }

    public void setResults(ArrayList<SettingSexBean> results) {
        mResults = results;
        this.notifyDataSetChanged();
    }
}
