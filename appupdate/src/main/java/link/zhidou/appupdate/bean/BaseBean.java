package link.zhidou.appupdate.bean;


import link.zhidou.appupdate.utils.http.JsonUtils;

/**
 * created by yue.gan 18-7-12
 */
public class BaseBean {

    @Override
    public String toString() {
        return JsonUtils.Object2JsonString(this);
    }
}
