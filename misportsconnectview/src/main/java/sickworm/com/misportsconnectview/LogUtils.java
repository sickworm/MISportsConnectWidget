package sickworm.com.misportsconnectview;

import android.util.Log;

/**
 * 日志控制类
 *
 * Created by sickworm on 2017/10/13.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class LogUtils {
    public static int LOG_LEVEL_DEBUG = 1;
    public static int LOG_LEVEL_INFO = 2;
    public static int LOG_LEVEL_WARN = 3;
    public static int LOG_LEVEL_ERROR = 4;
    private static String TAG = "LogUtils";
    private static int logLevel = LOG_LEVEL_DEBUG;

    public static void setLogLevel(int level) {
        logLevel = level;
    }

    public static void d(Object object) {
        if (logLevel <= LOG_LEVEL_DEBUG) {
            Log.d(TAG, toString(object));
        }
    }

    public static void i(Object object) {
        if (logLevel <= LOG_LEVEL_INFO) {
            Log.i(TAG, toString(object));
        }
    }

    public static void w(Object object) {
        if (logLevel <= LOG_LEVEL_WARN) {
            Log.w(TAG, toString(object));
        }
    }

    public static void e(Object object) {
        if (logLevel <= LOG_LEVEL_ERROR) {
            Log.e(TAG, toString(object));
        }
    }

    private static String toString(Object object) {
        if (object == null) {
            return "null";
        }

        return object.toString();
    }
}
