package link.zhidou.translator.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.Scroller;

/**
 * Created by keetom on 2017/9/21.
 */
public abstract class UI {

    protected Context context;
    //当前UI界面的布局文件
    protected View contentView;
    //当前UI界面在父控件的起点X坐标
    protected int startX;
    //当前UI界面在父控件的终点X坐标
    protected int stopX;
    //当前UI界面的宽度
    protected int width;

    protected UI(Context context, View contentView){
        this.context = context;
        this.contentView = contentView;
    }

    protected abstract void calculate(float leftScale, float rightScale);

    public void show(Scroller mScroller){
        if(mScroller != null){
            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), startX - mScroller.getFinalX(), 0);
        }
    }
}
