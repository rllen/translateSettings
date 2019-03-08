package link.zhidou.translator.ui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import link.zhidou.translator.R;

/**
 * Created by keetom on 2018/3/26.
 */

public class ZdDialog extends Dialog {

    private Context mContext;
    private String bt_one_text;
    private String bt_two_text;
    private String bt_three_text;
    private String bt_four_text;
    private View dialogView;
    private String dialogMessage;
    private OnDialogListener listener;
    private OnClickListener btOneClickListener;
    private OnClickListener btTwoClickListener;
    private OnClickListener btThreeClickListener;
    private OnClickListener btFourClickListener;

    public ZdDialog(@NonNull Context context) {
        super(context);
    }

    public ZdDialog(@NonNull Context context, int themeResId,String bt_one_text, String bt_three_text) {
        super(context, themeResId);
        this.mContext = context;
        this.bt_one_text = bt_one_text;
        this.bt_three_text = bt_three_text;
    }

    public ZdDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener, String bt_one_text, String bt_two_text, String bt_three_text) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.bt_one_text = bt_one_text;
        this.bt_two_text = bt_two_text;
        this.bt_three_text = bt_three_text;
    }

    public ZdDialog(@NonNull Context context,
                    boolean cancelable,
                    @Nullable OnCancelListener cancelListener,
                    String bt_one_text,
                    String bt_two_text,
                    String bt_three_text,
                    String bt_four_text) {
        this(context, cancelable, cancelListener, bt_one_text, bt_two_text, bt_three_text);
        this.bt_four_text = bt_four_text;
    }

    public ZdDialog setBtOneText(String bt_one_text, OnClickListener listener) {
        this.bt_one_text = bt_one_text;
        this.btOneClickListener = listener;
        return this;
    }

    public ZdDialog setBtTwoText(String bt_two_text, OnClickListener listener) {
        this.bt_two_text = bt_two_text;
        this.btTwoClickListener = listener;
        return this;
    }


    public ZdDialog setBtThreeText(String bt_three_text, OnClickListener listener) {
        this.bt_three_text = bt_three_text;
        this.btThreeClickListener = listener;
        return this;
    }


    public ZdDialog setBtFourText(String bt_four_text, OnClickListener listener) {
        this.bt_four_text = bt_four_text;
        this.btFourClickListener = listener;
        return this;
    }

    public void showDialog() {
        ZdDialog zdDialog = new ZdDialog(mContext);
        zdDialog.setBtOneText(bt_one_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (listener != null) {
                    listener.dialogBtOneListener(dialogView, dialogInterface, which);
                }
            }
        }).setBtTwoText(bt_two_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (listener != null) {
                    listener.dialogBtTwoListener(dialogView, dialogInterface, which);
                }
            }
        }).setBtThreeText(bt_three_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (listener != null) {
                    listener.dialogBtThreeListener(dialogView, dialogInterface, which);
                }
            }
        }).setBtFourText(bt_four_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (listener != null) {
                    listener.dialogBtFourListener(dialogView, dialogInterface, which);
                }
            }
        }).create(mContext);
    }

    //注册监听器方法
    public ZdDialog setOnDiaLogListener(OnDialogListener listener) {
        this.listener = listener;
        return this;//把当前对象返回,用于链式编程
    }

    //定义一个监听器接口
    public interface OnDialogListener {
        //customView　这个参数需要注意就是如果没有自定义view,那么它则为null
        public void dialogBtOneListener(View customView, DialogInterface dialogInterface, int which);

        public void dialogBtTwoListener(View customView, DialogInterface dialogInterface, int which);

        public void dialogBtThreeListener(View customView, DialogInterface dialogInterface, int which);

        void dialogBtFourListener (View customView, DialogInterface dialogInterface, int which);
    }

    public void create(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ZdDialog dialog = new ZdDialog(mContext);
        View layout = inflater.inflate(R.layout.dialog_three_select, null);
        dialog.addContentView(layout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (bt_one_text != null) {
            ((TextView) layout.findViewById(R.id.tv_one))
                    .setText(bt_one_text);
            if (btOneClickListener != null) {
                ((TextView) layout.findViewById(R.id.tv_one))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btOneClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        });
            }
        } else {
            layout.findViewById(R.id.tv_one).setVisibility(
                    View.GONE);
        }
        if (bt_two_text != null) {
            ((TextView) layout.findViewById(R.id.tv_two))
                    .setText(bt_two_text);
            if (btTwoClickListener != null) {
                ((TextView) layout.findViewById(R.id.tv_two))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btTwoClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        });
            }
        }else {
            layout.findViewById(R.id.tv_two).setVisibility(
                    View.GONE);
            layout.findViewById(R.id.tv_line_two).setVisibility(
                    View.GONE);
        }
        // set the cancel button
        if (bt_three_text != null) {
            ((TextView) layout.findViewById(R.id.tv_three))
                    .setText(bt_three_text);
            if (btThreeClickListener != null) {
                ((TextView) layout.findViewById(R.id.tv_three))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btThreeClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                            }
                        });
            }
        } else {
            // if no confirm button just set the visibility to GONE
            layout.findViewById(R.id.tv_three).setVisibility(
                    View.GONE);
        }
        if (!TextUtils.isEmpty(bt_four_text)) {
            ((TextView) layout.findViewById(R.id.tv_four))
                    .setText(bt_four_text);
            if (btFourClickListener != null) {
                layout.findViewById(R.id.tv_four)
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btFourClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                            }
                        });
            }
        } else {
            // if no confirm button just set the visibility to GONE
            layout.findViewById(R.id.tv_four).setVisibility(
                    View.GONE);
            layout.findViewById(R.id.tv_line_three).setVisibility(
                    View.GONE);
        }
        dialog.setContentView(layout);
        dialog.show();
    }
}
