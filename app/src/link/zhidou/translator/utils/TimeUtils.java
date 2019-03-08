package link.zhidou.translator.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhidou-kt on 2017/12/28.
 */

public class TimeUtils {
    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getTime() {
        Date date = new Date();// 创建一个时间对象，获取到当前的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置时间显示格式
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static String mi2Str(long orig){
        StringBuilder sb = new StringBuilder();
        int flow = (int) (orig / 1000);
        int hour = flow / 3600;
        int mint = (flow % 3600) / 60;
        int sed = flow % 60;
        if (hour > 0) {
            if (hour > 10) {
                sb.append(hour);
            } else {
                sb.append("0");
                sb.append(hour);
            }
            sb.append(":");

        }
        if (mint < 10) {
            sb.append("0");
            sb.append(mint);
        } else {
            sb.append(mint);
        }
        sb.append(":");
        if (sed < 10) {
            sb.append("0");
            sb.append(sed);
        } else {
            sb.append(sed);
        }
        return sb.toString();
    }
    /**
     * 得到今天的日期
     *
     * @return
     */
    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        return date;
    }

    /**
     * 时间戳转化为时间格式
     *
     * @param timeStamp
     * @return
     */
    public static String timeStampToStr(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(timeStamp * 1000);
        return date;
    }

}
