package link.zhidou.translator.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import link.zhidou.translator.R;
import link.zhidou.translator.ui.activity.KeyboardActivity;
import link.zhidou.translator.ui.view.AutoFitHeightTextView;
import link.zhidou.translator.ui.view.InputMethodRecyclerView;
import link.zhidou.translator.ui.view.RegionView;
import link.zhidou.translator.utils.DimensionUtil;
import link.zhidou.translator.utils.Log;
import link.zhidou.translator.utils.ScreenUtils;

import static link.zhidou.translator.Config.KEYCODE_DOWN;
import static link.zhidou.translator.Config.KEYCODE_ENTER;
import static link.zhidou.translator.Config.KEYCODE_LEFT;
import static link.zhidou.translator.Config.KEYCODE_RIGHT;
import static link.zhidou.translator.Config.KEYCODE_UP;
import static link.zhidou.translator.ui.activity.KeyboardActivity.LOWER_TO_UPPER;


/**
 * Created by keetom on 2018/5/7.
 */

public class InputMethodAdapter extends RecyclerView.Adapter<InputMethodAdapter.InputMethodViewHolder> {
    private static final String TAG = InputMethodAdapter.class.getSimpleName();
    private final Activity mActivity;
    private int mCurPosition = 0;
    private List<InputMethodEntry> mEntries;
    private Context mContext;
    private PopupWindow mPopWindow;
    private InputMethodRecyclerView mRecyclerView;
    private EditText mEditText;
    private final int ONLY_ONE_TEXTVIEW = -99;
    private String mValueEnter;
    private String mValueUp;
    private String mValueLeft;
    private String mValueRight;
    private String mValueDown;
    private static final boolean DEBUG = Log.isLoggable();
    private boolean direct;
    private boolean setFocusFlag;
    int paddingLeft;
    int paddingRight;
    int paddingTop;
    int paddingBottom;
    /**
     * 键盘弹出的popwindow的直径，占整个键盘高度的比例
     */
    private float diameter = 4 / 5f;
    private float popTextSizeProportion = 1.5f;
    private float textSize ;
    private View contentView;
    private LinearLayout selectPopupLv;

    public InputMethodAdapter(List<InputMethodEntry> mEntries, Context mContext, Activity mActivity, InputMethodRecyclerView mRecyclerView, EditText mEditText, boolean direct) {
        this.mActivity = mActivity;
        this.mEntries = mEntries;
        this.mContext = mContext;
        this.mRecyclerView = mRecyclerView;
        this.mEditText = mEditText;
        this.direct = direct;
        //设置键盘是否一直获取上次的焦点
        setFocusAlwaysExists(false);
        aOrAChange();
        final ViewGroup viewGroup = ((KeyboardActivity) mActivity).getContentView();
        paddingLeft = viewGroup.getPaddingLeft();
        paddingRight = viewGroup.getPaddingRight();
        paddingTop = viewGroup.getPaddingTop();
        paddingBottom = viewGroup.getPaddingBottom();

        contentView = LayoutInflater.from(mContext).inflate(R.layout.select_popup, null);
        selectPopupLv = (LinearLayout) contentView.findViewById(R.id.select_popup_lv);
        mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);


    }

    @Override
    public InputMethodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.keybord_table_item, parent, false);
        return new InputMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final InputMethodViewHolder holder, final int position) {
        final int maxHeight = getKeyboardItemHeight();
        holder.mTvUp.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxHeight, mContext.getResources().getDisplayMetrics()));
        holder.mTvUp.setText(mEntries.get(position).upText);
        holder.mTvDown.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxHeight, mContext.getResources().getDisplayMetrics()));
        holder.mTvDown.setText(mEntries.get(position).downText);
        holder.itemView.setTag(position);
        if (TextUtils.isEmpty(mEntries.get(position).downText)) {
            holder.mTvUp.setSingleLineText(true);
            holder.mTvDown.setVisibility(View.GONE);
        } else {
            holder.mTvUp.setSingleLineText(false);
            holder.mTvDown.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurPosition = (int) holder.itemView.getTag();
                showPopupWindow(holder.mTvUp, holder.mTvDown, holder.mTvUp.getTextSize()*popTextSizeProportion);
                if (holder.itemView.getTag() == null) {
                    return;
                }
                //设置RecyclerView获得焦点的位置，首次是默认首个item获得焦点
                mRecyclerView.setDefaultSelect(mCurPosition, setFocusFlag);
            }
        });
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCurPosition = (int) holder.itemView.getTag();
                if (holder.itemView.getTag() == null) {
                    return;
                }
            }
        });
    }

    private int getKeyboardItemWidth() {
        return (ScreenUtils.getScreenWidth(mContext) - paddingLeft - paddingRight)/3;
    }

    private int getKeyboardItemHeight(){
        return (getKeyboardItemWidth()/14)*9;
    }

    @Override
    public int getItemCount() {
        return null == mEntries ? 0 : mEntries.size();
    }

    class InputMethodViewHolder extends RecyclerView.ViewHolder {
        public AutoFitHeightTextView mTvUp;
        public AutoFitHeightTextView mTvDown;
        public LinearLayout mKeybordButton;

        public InputMethodViewHolder(View itemView) {
            super(itemView);
            mTvUp = itemView.findViewById(R.id.tv_up);
            mTvDown = itemView.findViewById(R.id.tv_down);
            mKeybordButton = itemView.findViewById(R.id.ll_keybordbutton);
            if (mKeybordButton != null) {
                ViewGroup.LayoutParams lp;
                lp = mKeybordButton.getLayoutParams();
                lp.height = getKeyboardItemHeight();
                mKeybordButton.setLayoutParams(lp);
            }
        }
    }

    private void showPopupWindow(final AutoFitHeightTextView mTvUp, final AutoFitHeightTextView mTvDown, float mTextSize) {
        if (TextUtils.isEmpty(mTvDown.getText().toString())) {
            onKeyDown(ONLY_ONE_TEXTVIEW, mTvUp, mTvDown);
            return;
        }
        String text = mTvDown.getText() + mTvUp.getText().toString();
        final char[] letter = text.toCharArray();
        selectPopupLv.setFocusable(true);
        selectPopupLv.setFocusableInTouchMode(true);
        selectPopupLv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                onKeyDown(keyCode, mTvUp, mTvDown);
                return false;
            }
        });
        RegionView mRegionView = contentView.findViewById(R.id.regionView);
        ViewGroup.LayoutParams params = mRegionView.getLayoutParams();
        //设置蓝色选择框大小是高度的3/4
        params.height = (int) (mRecyclerView.getHeight() * diameter);
        params.width = (int) (mRecyclerView.getHeight() * diameter);
        mRegionView.setLayoutParams(params);
        mRegionView.setValue(String.valueOf(letter[0]), String.valueOf(letter[1]), String.valueOf(letter[2]), String.valueOf(letter[3]), String.valueOf(letter[4]), mTextSize);
        showUp(mPopWindow, mRecyclerView, diameter);
        mRegionView.setListener(new RegionView.RegionViewClickListener() {

            @Override
            public void clickTop() {
                closePopupWindow();
                updateEdLogin(String.valueOf(letter[1]));
            }

            @Override
            public void clickRight() {
                closePopupWindow();
                updateEdLogin(String.valueOf(letter[2]));
            }

            @Override
            public void clickLeft() {
                closePopupWindow();
                updateEdLogin(String.valueOf(letter[0]));
            }

            @Override
            public void clickCenter() {
                closePopupWindow();
                updateEdLogin(String.valueOf(letter[4]));
            }

            @Override
            public void clickBottom() {
                closePopupWindow();
                updateEdLogin(String.valueOf(letter[3]));
            }
        });
    }

    /**
     * 设置显示在v上方（以v的中心位置为开始位置）
     *
     * @param v
     */
    public void showUp(PopupWindow mPopupWindow, View v, float mDiameter) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(v, Gravity.NO_GRAVITY, paddingLeft + (v.getWidth() / 2) - (int) (v.getHeight() * mDiameter) / 2, location[1] + v.getHeight() / 2 - (int) (v.getHeight() * mDiameter) / 2);
    }

    private void onKeyDown(int keyCode, TextView mTvUp, TextView mTvDown) {
        switch (keyCode) {
            case KEYCODE_ENTER: {
                updateEdLogin(mValueEnter);
                break;
            }
            case KEYCODE_LEFT: {
                updateEdLogin(mValueLeft);
                break;
            }
            case KEYCODE_UP: {
                updateEdLogin(mValueUp);
                break;
            }
            case KEYCODE_RIGHT: {
                updateEdLogin(mValueRight);
                break;
            }
            case KEYCODE_DOWN: {
                updateEdLogin(mValueDown);
                break;
            }
            case ONLY_ONE_TEXTVIEW: {
                String value = mTvUp.getText().toString();
                String left = mContext.getResources().getString(R.string.wifi_login_changeAa);
                String right = mContext.getResources().getString(R.string.wifi_login_del);
                if (right.equals(value)) {
                    String trim = mEditText.getText().toString().trim();
                    if (trim != null && trim.length() > 0) {
                        int selectionStart = mEditText.getSelectionStart();
                        if (selectionStart > 0) {
                            try {
                                String substring1 = trim.substring(0, selectionStart - 1);
                                String substring2 = trim.substring(selectionStart, trim.length());
                                String substring = substring1 + substring2;
                                mEditText.setText(substring);
                                mEditText.setSelection(selectionStart - 1);
                            } catch (Exception e) {
                                if (DEBUG) {
                                    Log.d(TAG, "substring1" + "StringIndexOutOfBoundsException");
                                }
                            }
                        }
                    }
                } else if (left.equals(value)) {
                    aOrAChange();
                    notifyItemRangeChanged(0, 9);
                } else {/*输入键盘文字到密码框*/
                    updateEdLogin(value);
                }
                break;
            }
            default:
                break;
        }
        closePopupWindow();
    }

    private void updateEdLogin(String value) {
        if (value == null || TextUtils.isEmpty(value) || " ".equals(value)) {
            return;
        }
        String text = mEditText.getText().toString();
        int selectionStart = mEditText.getSelectionStart();
        String textEndShuru = text.trim() + value;
        String substring1 = text.substring(0, selectionStart);
        String substring2 = text.substring(selectionStart, text.length());
        textEndShuru = substring1 + value + substring2;
        mEditText.setText(textEndShuru);
        mEditText.requestFocus();
        mEditText.setSelection(selectionStart + 1);
    }

    public void closePopupWindow() {
        if (mPopWindow != null) {
            mPopWindow.dismiss();
        }
        //设置RecyclerView获得焦点的位置，首次是默认首个item获得焦点
        //mRecyclerView.setDefaultSelect(mCurPosition, setFocusFlag);
    }

    public void setFocusAlwaysExists(boolean flag) {
        if (flag) {
            setFocusFlag = true;
        } else {
            setFocusFlag = false;
        }
    }

    public static class InputMethodEntry implements Comparable<InputMethodEntry> {
        public String upText;
        public String downText;

        public InputMethodEntry(String upText, String downText) {
            this.upText = upText;
            this.downText = downText;
        }

        public InputMethodEntry(String upTest) {
            this.upText = upTest;
            downText = "";
        }

        public InputMethodEntry() {

        }

        @Override
        public int compareTo(@NonNull InputMethodEntry entry) {
            return 0;
        }
    }

    public void aOrAChange() {
        mEntries.clear();
        InputMethodEntry entry = new InputMethodEntry();
        if (direct == LOWER_TO_UPPER) {
            changeaToA(entry);
        } else {
            changeAToa(entry);
        }
        entry = new InputMethodEntry("8", "!@#-");
        mEntries.add(entry);
        entry = new InputMethodEntry("9", "$%^_");
        mEntries.add(entry);
        entry = new InputMethodEntry(mContext.getResources().getString(R.string.wifi_login_changeAa));
        mEntries.add(entry);
        entry = new InputMethodEntry("0", "&*.~");
        mEntries.add(entry);
        entry = new InputMethodEntry(mContext.getResources().getString(R.string.wifi_login_del));
        mEntries.add(entry);
        direct = !direct;
    }

    private void changeaToA(InputMethodEntry entry) {
        entry = new InputMethodEntry("1", "abcd");
        mEntries.add(entry);
        entry = new InputMethodEntry("2", "efgh");
        mEntries.add(entry);
        entry = new InputMethodEntry("3", "ijkl");
        mEntries.add(entry);
        entry = new InputMethodEntry("4", "mnop");
        mEntries.add(entry);
        entry = new InputMethodEntry("5", "qrst");
        mEntries.add(entry);
        entry = new InputMethodEntry("6", "uvw ");
        mEntries.add(entry);
        entry = new InputMethodEntry("7", "xyz?");
        mEntries.add(entry);
    }

    private void changeAToa(InputMethodEntry entry) {
        entry = new InputMethodEntry("1", "ABCD");
        mEntries.add(entry);
        entry = new InputMethodEntry("2", "EFGH");
        mEntries.add(entry);
        entry = new InputMethodEntry("3", "IJKL");
        mEntries.add(entry);
        entry = new InputMethodEntry("4", "MNOP");
        mEntries.add(entry);
        entry = new InputMethodEntry("5", "QRST");
        mEntries.add(entry);
        entry = new InputMethodEntry("6", "UVW ");
        mEntries.add(entry);
        entry = new InputMethodEntry("7", "XYZ?");
        mEntries.add(entry);
    }
}