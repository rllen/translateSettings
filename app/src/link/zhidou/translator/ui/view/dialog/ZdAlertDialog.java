package link.zhidou.translator.ui.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import link.zhidou.translator.R;

/**
 * Created by keetom on 2018/3/26.
 */

public class ZdAlertDialog extends Dialog{
    private Context mContext;
    private View dialogView;
    private String dialogTitle;
    private String message;
    private String bt_no_text;
    private String bt_yes_text;
    private ZdAlertDialog.OnDialogListener listener;
    private OnClickListener btNoClickListener;
    private OnClickListener btYesClickListener;

    public ZdAlertDialog( Context context) {
        super(context);
    }

    public ZdAlertDialog( Context context, int themeResId,String bt_no_text, String bt_yes_text) {
        super(context, themeResId);
        this.mContext = context;
        this.bt_no_text = bt_no_text;
        this.bt_yes_text = bt_yes_text;
    }

    public ZdAlertDialog( Context context, boolean cancelable,  OnCancelListener cancelListener, String bt_no_text, String bt_yes_text) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.bt_no_text = bt_no_text;
        this.bt_yes_text = bt_yes_text;
    }

    public ZdAlertDialog( Context context, boolean cancelable,  OnCancelListener cancelListener,String dialogTitle, String bt_no_text, String bt_yes_text) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.dialogTitle = dialogTitle;
        this.bt_no_text = bt_no_text;
        this.bt_yes_text = bt_yes_text;
    }

    public ZdAlertDialog( Context context, boolean cancelable,  OnCancelListener cancelListener,String dialogTitle,String message, String bt_no_text, String bt_yes_text) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
        this.dialogTitle = dialogTitle;
        this.message = message;
        this.bt_no_text = bt_no_text;
        this.bt_yes_text = bt_yes_text;
    }

    public ZdAlertDialog setBtNoText(String bt_no_text, OnClickListener listener) {
        this.bt_no_text = bt_no_text;
        this.btNoClickListener = listener;
        return this;
    }

    public ZdAlertDialog setBtYesText(String bt_yes_text, OnClickListener listener) {
        this.bt_yes_text = bt_yes_text;
        this.btYesClickListener = listener;
        return this;
    }

    public ZdAlertDialog setTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
        return this;
    }

    public ZdAlertDialog setMessage(String message){
        this.message = message;
        return this;
    }

    public ZdAlertDialog showDialog() {
        ZdAlertDialog mZdAlertDialog = new ZdAlertDialog(mContext);
        mZdAlertDialog.setTitle(dialogTitle)
                .setMessage(message)
                .setBtNoText(bt_no_text, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                        if (listener != null) {
                            listener.dialogBtNoListener(dialogView, dialogInterface, which);
                        }
                    }
                }).setBtYesText(bt_yes_text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (listener != null) {
                    listener.dialogBtYesListener(dialogView, dialogInterface, which);
                }
            }
        }).create(mContext);
        return mZdAlertDialog;
    }

    //注册监听器方法
    public ZdAlertDialog setOnDiaLogListener(ZdAlertDialog.OnDialogListener listener) {
        this.listener = listener;
        return this;//把当前对象返回,用于链式编程
    }

    //定义一个监听器接口
    public interface OnDialogListener {
        //customView　这个参数需要注意就是如果没有自定义view,那么它则为null
        public void dialogBtNoListener(View customView, DialogInterface dialogInterface, int which);

        public void dialogBtYesListener(View customView, DialogInterface dialogInterface, int which);

    }
    public void create(Context mContext) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ZdAlertDialog dialog = new ZdAlertDialog(mContext);
        View layout = inflater.inflate(R.layout.dialog_yes_or_no, null);
        dialog.addContentView(layout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (dialogTitle != null) {
            ((TextView) layout.findViewById(R.id.title))
                    .setText(dialogTitle);
            layout.findViewById(R.id.title).setVisibility(
                    View.VISIBLE);
        }else {
            layout.findViewById(R.id.title).setVisibility(
                    View.GONE);
        }
        if(message != null){
            ((TextView) layout.findViewById(R.id.tv_message))
                    .setText(message);
            layout.findViewById(R.id.tv_message).setVisibility(
                    View.VISIBLE);
        }else{
            layout.findViewById(R.id.tv_message).setVisibility(
                    View.GONE);
        }
        if (bt_no_text != null) {
            ((TextView) layout.findViewById(R.id.no))
                    .setText(bt_no_text);
            if (btNoClickListener != null) {
                ((TextView) layout.findViewById(R.id.no))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btNoClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        });
            }
        } else {
            layout.findViewById(R.id.no).setVisibility(
                    View.GONE);
        }
        if (bt_yes_text != null) {
            ((TextView) layout.findViewById(R.id.yes))
                    .setText(bt_yes_text);
            if (btYesClickListener != null) {
                ((TextView) layout.findViewById(R.id.yes))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                btYesClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        });
            }
        }else {
            layout.findViewById(R.id.yes).setVisibility(
                    View.GONE);
        }
        dialog.setContentView(layout);
        dialog.show();
    }
}
