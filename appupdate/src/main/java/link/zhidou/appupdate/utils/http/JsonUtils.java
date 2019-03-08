package link.zhidou.appupdate.utils.http;

import com.google.gson.Gson;

/**
 * created by yue.gan 18-7-13
 */
public class JsonUtils {
    private static Gson gson;
    private static Gson getGson () {
        if(gson == null) gson = new Gson();
        return gson;
    }

    public static String Object2JsonString (Object object) {
        try {
            return getGson().toJson(object);
        } catch (Exception e) {
            return object.toString();
        }
    }
}
