package link.zhidou.translator.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import link.zhidou.translator.Config;
import link.zhidou.translator.R;
import link.zhidou.translator.adapter.SettingModeListAdapter;
import link.zhidou.translator.model.bean.SettingSexBean;
import link.zhidou.translator.ui.activity.base.ActionBarBaseActivity;
import link.zhidou.translator.utils.SPKeyContent;
import link.zhidou.translator.utils.SPUtil;

/**
 * Created by czm on 17-8-9.
 */

public class SettingModeActivity extends ActionBarBaseActivity {

    private ListView mLvSettingSex;
    private ArrayList<SettingSexBean> mBeanArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        initView();
        initData();
    }

    private void initData() {
        setActionBarTitle(SettingModeActivity.this);
    }

    private void initView() {

        mLvSettingSex = (ListView) findViewById(R.id.lv_setting_mode);

        initListData();

        final SettingModeListAdapter settingSexListAdapter = new SettingModeListAdapter(this, mBeanArrayList);
        mLvSettingSex.setAdapter(settingSexListAdapter);
        mLvSettingSex.setItemsCanFocus(true);
        mLvSettingSex.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                for (int j = 0; j < mBeanArrayList.size(); j++) {
                    SettingSexBean settingSexBean = mBeanArrayList.get(j);
                    if (j == position) {
                        settingSexBean.setShow(true);
                        SPUtil.putString(SettingModeActivity.this, SPKeyContent.SETTING_MODE, mBeanArrayList.get(position).getValue());
                    } else {
                        settingSexBean.setShow(false);
                    }
                }
//                setResults(mResults);
                setResultCQ(mBeanArrayList.get(position).getLabel());
            }
        });
    }

    private void initListData() {
        String spMode = (String) SPUtil.get(this, SPKeyContent.SETTING_MODE, Config.Mode.DEFAULT);

        SettingSexBean settingSexBean1 = new SettingSexBean();
        settingSexBean1.setLabel(Config.labelFromValue(this, R.array.mode_entries, R.array.mode_values, Config.Mode.AUTOMATIC));
        settingSexBean1.setValue(Config.Mode.AUTOMATIC);

        SettingSexBean settingSexBean2 = new SettingSexBean();
        settingSexBean2.setLabel(Config.labelFromValue(this, R.array.mode_entries, R.array.mode_values, Config.Mode.OPERATION));
        settingSexBean2.setValue(Config.Mode.OPERATION);

        if (Config.Mode.AUTOMATIC.equals(spMode)) {
            settingSexBean1.setShow(true);
        } else if (Config.Mode.OPERATION.equals(spMode)) {
            settingSexBean2.setShow(true);
        }

        mBeanArrayList = new ArrayList<>();
        mBeanArrayList.add(settingSexBean1);
        mBeanArrayList.add(settingSexBean2);


    }
    final static int CODE_SET_SEX = 11;
    public void setResultCQ(String sex) {
        Intent data = getIntent();
        data.putExtra("userchoice",sex);
        setResult(CODE_SET_SEX, data);
        finish();
    }

}
