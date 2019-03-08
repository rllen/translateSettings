package link.zhidou.appupdate.utils.http;

/**
 * Created by ganyu on 2016/7/19.
 *
 */
public interface OnRequestListener {
    int ERROR_CODE_UNKENOW = 0;
    int ERROR_CODE_METHOD_ONRESPONSE = 0x0010;
    int ERROR_CODE_METHOD_ONREQUESTERROR = 0x0020;

    /**
     * 收到服务器返回数据
     */
    void onResponse(String url, Object responseData);

    /**
     * 请求失败
     */
    void onRequestError(int errorCode, String url, Exception e);

    /**
     * 自己的处理代码出错，或者自己抛出了异常
     */
    void onError(int errorCode, String url, Exception e);
}
