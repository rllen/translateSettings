package link.zhidou.translator.assist;

/**
 * Date: 17-11-13
 * Time: 上午11:51
 * Email: lostsearover@gmail.com
 */

public class CrashResp {
    public String api_name;
    public String message;
    public String referer;
    public boolean refresh;
    public int result;
    public String state;
    Data data;

    public static class Data {
        public int code;
    }
}
