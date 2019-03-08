package link.zhidou.translator.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import link.zhidou.translator.Config;
import link.zhidou.translator.R;
import link.zhidou.translator.adapter.InputMethodAdapter;
import link.zhidou.translator.ui.activity.base.BaseActivity;
import link.zhidou.translator.ui.view.CommonActionBar;
import link.zhidou.translator.ui.view.InputMethodRecyclerView;

public class KeyboardActivity extends BaseActivity implements CommonActionBar.BackPressedListener {
    private static final String TAG = KeyboardActivity.class.getSimpleName();
    private CommonActionBar mActionBar;
    private InputMethodRecyclerView mRecyclerView;
    private EditText mEditText;
    private TextView mConfirm;
    private InputMethodAdapter mInputMethodAdapter;
    private List<InputMethodAdapter.InputMethodEntry> mEntries = new ArrayList<>();
    public static final boolean LOWER_TO_UPPER = true;
    public static final boolean UPPER_TO_LOWER = false;
    /**
     * 大小写切换
     */
    private boolean direct = LOWER_TO_UPPER;
    private String mTitle;
    private String mInValue;
    private int mMinLength;
    private ArrayList<Object> mOptions;
    Context mContext;

    public static final String EXTRA_TITLE = "extra-title";
    public static final String EXTRA_IN_VALUE = "extra-in-value";
    public static final String EXTRA_OUT_VALUE = "extra-out-value";
    public static final String EXTRA_MIN_LENGTH = "extra-min-length";
    public static final String EXTRA_OPTIONS = "extra-options";

    public static Intent getIntent(Context context, String title, int minLength, List<String> options) {
        Intent intent = new Intent(context, KeyboardActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TITLE, title);
        bundle.putInt(EXTRA_MIN_LENGTH, minLength);
        bundle.putSerializable(EXTRA_OPTIONS, (Serializable) options);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_view);
        initDate();
        initView();
        initListener();
    }

    public final ViewGroup getContentView(){
        return this.findViewById(android.R.id.content);
    }

    private void initDate() {
        Intent intent = getIntent();
        mTitle = intent.getStringExtra(EXTRA_TITLE);
        mInValue = intent.getStringExtra(EXTRA_IN_VALUE);
        mMinLength = intent.getIntExtra(EXTRA_MIN_LENGTH, -1);
        mOptions = (ArrayList<Object>) intent.getSerializableExtra(EXTRA_OPTIONS);
        mContext = getBaseContext();
    }

    private void initView() {
        mActionBar = findViewById(R.id.common_action_bar);
        mActionBar.setTitle(mTitle);
        mActionBar.setBackPressedListener(this);
        mRecyclerView = findViewById(R.id.rv_keyboard_button_container);
        mEditText = findViewById(R.id.et_input);
        mConfirm = findViewById(R.id.tv_confirm);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mInputMethodAdapter = new InputMethodAdapter(mEntries, this,this, mRecyclerView, mEditText, direct);
        mRecyclerView.setAdapter(mInputMethodAdapter);
        mRecyclerView.setScrollEnabled(false);
        mRecyclerView.setVerticalScrollBarEnabled(false);
        mEditText.setText(mInValue);
    }

    private boolean isInUserSet(String value) {
        if (mOptions == null || mOptions.size() == 0) {
            return false;
        }
        for (int i = 0; i < mOptions.size(); i++) {
            final String nickName = (String) mOptions.get(i);
            if (value.equals(nickName)) {
                return true;
            }
        }
        return false;
    }

    private void initListener() {
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String outValue = mEditText.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_OUT_VALUE, mEditText.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hintSystemInput(mEditText);
            }
        });

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                hintSystemInput(mEditText);
            }
        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    @Override
    public void onBackPressed(View view) {
        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == Config.KEYCODE_A || event.getKeyCode() == Config.KEYCODE_B || event.getKeyCode() == Config.KEYCODE_MID) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void hintSystemInput(EditText mEditText) {
        //4.0以下 danielinbiti
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            mEditText.setInputType(InputType.TYPE_NULL);
        } else {
            this.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(mEditText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}