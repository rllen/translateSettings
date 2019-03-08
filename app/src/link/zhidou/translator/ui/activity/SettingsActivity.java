package link.zhidou.translator.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.base.BaseActivity;

/**
 * Date: 18-1-10
 * Time: 上午10:07
 * Email: lostsearover@gmail.com
 */

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_host);
    }

}
