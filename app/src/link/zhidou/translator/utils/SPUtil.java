package link.zhidou.translator.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.LruCache;

/**
 * 文件名：SplashActivity
 * 描  述：初始化引导界面
 * 作  者：keetom
 * 时  间：2017/8/13
 * 版  权：
 */
public class SPUtil {
    private static final String FILE_NAME = "user_settings_config";
    private static final int LIMIT = 25;
    private static final LruCache<String, Object> sCache = new LruCache<>(LIMIT);
    private static SharedPreferences sp = null;
    private static SharedPreferences.Editor editor = null;

    /**
     * 方法名：put
     * 功  能：SP 存储 数据
     * 参  数：Context context
     * 参  数：String key
     * 参  数：Object object
     * 返回值：无
     */
    public static void put(Context context, String key, Object object) {
        if (null == sp) {
            synchronized (SPUtil.class) {
                if (null == sp) {
                    sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        if (null == editor) {
            synchronized (SPUtil.class) {
                if (null == editor) {
                    editor = sp.edit();
                }
            }
        }
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
        editor.apply();
        editor.commit();
        // Cache it.
        synchronized (sCache) {
            sCache.put(key, object);
        }
    }
    /**
     * 方法名：get
     * 功  能：SP 取出 数据
     * 参  数：Context context
     * 参  数：String key
     * 参  数：Object defaultObject
     * 返回值：Object
     */
    public static Object get(Context context, String key, Object defaultObject) {
        if (null == sp) {
            synchronized (SPUtil.class) {
                if (null == sp) {
                    sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        Object impl = sCache.get(key);
        if (impl != null) {
            return impl;
        }
        if (!sp.contains(key)) {
            return defaultObject;
        } else { // Exists that key preference.
            if (defaultObject instanceof String) {
                impl = sp.getString(key, (String) defaultObject);
                synchronized (sCache) {
                    sCache.put(key, impl);
                }
                return impl;
            } else if (defaultObject instanceof Integer) {
                impl = sp.getInt(key, (Integer) defaultObject);
                synchronized (sCache) {
                    sCache.put(key, impl);
                }
                return impl;
            } else if (defaultObject instanceof Boolean) {
                impl = sp.getBoolean(key, (Boolean) defaultObject);
                synchronized (sCache) {
                    sCache.put(key, impl);
                }
                return impl;
            } else if (defaultObject instanceof Float) {
                impl = sp.getFloat(key, (Float) defaultObject);
                synchronized (sCache) {
                    sCache.put(key, impl);
                }
                return impl;
            } else if (defaultObject instanceof Long) {
                impl = sp.getLong(key, (Long) defaultObject);
                synchronized (sCache) {
                    sCache.put(key, impl);
                }
                return impl;
            }
            throw new IllegalArgumentException("Unsupported object type: " + defaultObject.getClass().getSimpleName());
        }
    }

    public static String getString(Context context, String key, String defaultObject) {
        return (String) get(context, key, defaultObject);
    }

    public static void putString(Context context, String key, String defaultObject) {
        put(context, key, defaultObject);
    }

    public static Integer getInt(Context context, String key, int defaultObject) {
        return (Integer) get(context, key, defaultObject);
    }

    public static void putInt(Context context, String key, int defaultObject) {
        put(context, key, defaultObject);
    }

    public static Long getLong(Context context, String key, long defaultObject) {
        return (Long) get(context, key, defaultObject);
    }

    public static void putLong(Context context, String key, long defaultObject) {
        put(context, key, defaultObject);
    }

    public static Boolean getBoolean(Context context, String key, boolean defaultObject) {
        return (Boolean) get(context, key, defaultObject);
    }

    public static void putBoolean(Context context, String key, boolean defaultObject) {
        put(context, key, defaultObject);
    }

    public static void remove(Context context, String key) {
        if (null == sp) {
            synchronized (SPUtil.class) {
                if (null == sp) {
                    sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        if (null == editor) {
            synchronized (SPUtil.class) {
                if (null == editor) {
                    editor = sp.edit();
                }
            }
        }
        synchronized (sCache) {
            sCache.remove(key);
        }
        editor.remove(key);
        editor.apply();
        editor.commit();
    }

    public static void clear(Context context) {
        if (null == sp) {
            synchronized (SPUtil.class) {
                if (null == sp) {
                    sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        if (null == editor) {
            synchronized (SPUtil.class) {
                if (null == editor) {
                    editor = sp.edit();
                }
            }
        }
        synchronized (sCache) {
            sCache.evictAll();
        }
        editor.clear();
        editor.apply();
        editor.commit();
    }

    public static boolean contains(Context context, String key) {
        if (null == sp) {
            synchronized (SPUtil.class) {
                if (null == sp) {
                    sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        return sp.contains(key);
    }

}