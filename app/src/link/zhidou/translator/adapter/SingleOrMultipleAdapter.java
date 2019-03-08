package link.zhidou.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.SingleOrMultipleChoiceActivity.ChoiceItem;

/**
 * Date: 18-3-19
 * Time: 下午8:01
 * Email: lostsearover@gmail.com
 */

public class SingleOrMultipleAdapter extends BaseAdapter {

    List<ChoiceItem> mChoices;
    private Context mContext;
    public  SingleOrMultipleAdapter(Context context, List<ChoiceItem> list) {
        mContext = context;
        mChoices = list;
    }

    @Override
    public int getCount() {
        return null == mChoices ? 0 : mChoices.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        ChoiceItem item = mChoices.get(position);
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.lv_setting_sex_list_item,null);
            viewHolder = new ViewHolder();
            viewHolder.title = convertView.findViewById(R.id.tv_item_setting);
            viewHolder.tick = convertView.findViewById(R.id.iv_item_tick);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.title.setText(item.entry);
        viewHolder.tick.setVisibility(item.checked ? View.VISIBLE : View.GONE);
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public ChoiceItem getItem(int position) {
        return mChoices.get(position);
    }

    static class ViewHolder{
        TextView title;
        ImageView tick;
    }
}
