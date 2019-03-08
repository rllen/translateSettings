package link.zhidou.translator.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.storage.PathProvider;
import link.zhidou.translator.storage.StorageManagerProxy;
import link.zhidou.translator.storage.VolumeInfo;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;

public class StorageSettingsActivity extends ActionBarBaseActivity implements AdapterView.OnItemClickListener,PathProvider.VolumeChangedListener {

    private ListView mListView;
    private VolumeAdapter mAdapter;
    static class ViewHolder {
        TextView mTitle;
        TextView mDesc;
        View mTick;
    }

    private static class VolumeAdapter extends BaseAdapter {

        long mPrimaryStorageSize;
        List<VolumeInfo> mVolumes;
        StorageManagerProxy mProxy;
        Context mContext;

        public VolumeAdapter(Context context) {
            mContext = context;
            mProxy = new StorageManagerProxy(context);
            mVolumes = mProxy.getVolumes();
            mPrimaryStorageSize = mProxy.getPrimaryStorageSize();
        }

        public void setVolumes(List<VolumeInfo> volumes) {
            mVolumes = volumes;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mVolumes == null ? 0 : mVolumes.size();
        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (null == convertView) {
                LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_storage, null);
                holder = new ViewHolder();
                holder.mTitle = convertView.findViewById(R.id.title);
                holder.mDesc = convertView.findViewById(R.id.desc);
                holder.mTick = convertView.findViewById(R.id.iv_item_tick);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            VolumeInfo info = getItem(position);
            long totalSpace = info.isPrivate() && mPrimaryStorageSize > 0 ? mPrimaryStorageSize: info.getTotalSpace();
            final String used = Formatter.formatFileSize(mContext, totalSpace - info.getFreeSpace());
            final String total = Formatter.formatFileSize(mContext, totalSpace);
            holder.mTitle.setText(mProxy.getBestVolumeDescription(info));
            holder.mDesc.setText(mContext.getString(R.string.storage_volume_summary, used, total));
            String defaultVolumeId = PathProvider.get().getDefaultVolumeId();
            String defaultVolumePath = PathProvider.get().getDefaultVolumePath();

            if (TextUtils.isEmpty(defaultVolumeId)) {
                if (info.isPrivate()) {
                    holder.mTick.setVisibility(View.VISIBLE);
                } else {
                    holder.mTick.setVisibility(View.GONE);
                }
            } else if (defaultVolumeId.equals(info.getId())) {
               if (info.isPrivate()) {
                    holder.mTick.setVisibility(View.VISIBLE);
               } else if (defaultVolumePath.equals(info.getAbsolutePath())) {
                    holder.mTick.setVisibility(View.VISIBLE);
               } else {
                    holder.mTick.setVisibility(View.GONE);
               }
            } else {
                holder.mTick.setVisibility(View.GONE);
            }
            return convertView;
        }

        @Override
        public VolumeInfo getItem(int position) {
            return mVolumes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

    }

    @Override
    public void onChanged() {
        mAdapter.setVolumes(new StorageManagerProxy(this).getVolumes());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_settings);
        initView();
        initData();
        setActionBarTitle(this);
        PathProvider.get().register(this);
    }

    @Override
    protected void onDestroy() {
        PathProvider.get().unregister();
        super.onDestroy();
    }

    private void initView() {
        mListView = findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        mAdapter = new VolumeAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VolumeInfo info = mAdapter.getItem(position);
        PathProvider.get().setDefaultVolume(info.getId(), info.getAbsolutePath());
        finish();
    }
}
