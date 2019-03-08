package link.zhidou.translator.ui.fragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
/**
 * 文件名：BaseFragment
 * 描  述：Fragment基类
 * 作  者：keetom
 * 时  间：2017/9/12
 * 版  权：
 */
public class BaseFragment extends Fragment {
    /**
     * 方法名：onKeyLongPress
     * 功  能：禁止物理按钮长按事件
     * 参  数：int keyCode
     * 参  数：KeyEvent event
     * 返回值：false
     */
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }
    /**
     * 方法名：onKeyUp
     * 功  能：禁止物理按钮抬起事件
     * 参  数：int keyCode
     * 参  数：KeyEvent event
     * 返回值：false
     */
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }
    /**
     * 方法名：onKeyDown
     * 功  能：禁止物理按钮按下事件
     * 参  数：int keyCode
     * 参  数：KeyEvent event
     * 返回值：false
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
}
