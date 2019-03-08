package link.zhidou.translator.assist;

/**
 * 系统属性反射类
 * Created by czm on 16-11-21.
 */

import java.lang.reflect.Method;

public class SysProp {
    private static volatile Method set = null;
    private static volatile Method get = null;

    public static void set(String prop, String value) throws Exception {
        if (null == set) {
            synchronized (SysProp.class) {
                if (null == set) {
                    Class<?> cls = Class.forName("android.os.SystemProperties");
                    set = cls.getDeclaredMethod("set", String.class, String.class);
                }
            }
        }
        set.invoke(null, prop, value);
    }


    public static String get(String prop, String defaultvalue) {
        String value = defaultvalue;
        try {
            if (null == get) {
                synchronized (SysProp.class) {
                    if (null == get) {
                        Class<?> cls = Class.forName("android.os.SystemProperties");
                        get = cls.getDeclaredMethod("get", String.class, String.class);
                    }
                }
            }
            value = (String) (get.invoke(null, prop, defaultvalue));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return value;
    }
}