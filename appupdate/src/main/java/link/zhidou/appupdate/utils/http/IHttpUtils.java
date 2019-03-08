package link.zhidou.appupdate.utils.http;

import java.util.Map;

/**
 * Created by ganyu on 2016/7/19.
 *
 */
public interface IHttpUtils {
    Cancelable postDownloadAsync(String url, long startSize, OnRequestListener listener);
    String postStringSync(String url, Map<String, String> formParams);
    Object jsonStr2Object(Class clazz, String jsonStr);
    String object2JsonStr(Object object);

}
