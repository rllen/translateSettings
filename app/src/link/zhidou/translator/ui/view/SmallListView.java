package link.zhidou.translator.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Date: 18-6-19
 * Time: 下午7:52
 * Email: lostsearover@gmail.com
 */
public class SmallListView extends ListView {

    public SmallListView(Context context) {
        super(context);
    }

    public SmallListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmallListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
