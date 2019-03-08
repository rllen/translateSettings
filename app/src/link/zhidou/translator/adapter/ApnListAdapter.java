package link.zhidou.translator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.ApnSettingsActivity.ApnEntity;


/**
 * Wifi扫描列表Adapter
 * Created by czm on 17-10-21.
 */

public class ApnListAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ApnEntity> mEntities;
    private static final String TAG = ApnListAdapter.class.getSimpleName();
    private SelectedListener mListener;
    public ApnListAdapter(Context context, ArrayList<ApnEntity> entities, SelectedListener listener) {
        mContext = context;
        mEntities = entities;
        mListener = listener;
    }

    public static interface SelectedListener {
        void onClick(int position);
        void onSelected(int position);
    }

    @Override
    public int getCount() {
        return mEntities == null ? 0 : mEntities.size();
    }

    @Override
    public ApnEntity getItem(int i) {
        return mEntities == null ? null : mEntities.get(i);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final ApnEntity entity = mEntities.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.apn_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.content = convertView.findViewById(R.id.content);
            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.summary = convertView.findViewById(R.id.summary);
            viewHolder.select = convertView.findViewById(R.id.select);
            viewHolder.tick = convertView.findViewById(R.id.tick);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(position);
                }
            }
        });
        viewHolder.title.setText(entity.title);
        viewHolder.summary.setText(entity.summary);
        viewHolder.tick.setVisibility(entity.selected ? View.VISIBLE : View.INVISIBLE);
        viewHolder.select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onSelected(position);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        View content;
        TextView title;
        TextView summary;
        ImageView tick;
        View select;
    }

}
