package sickworm.com.misportsconnectview;

import android.content.Context;

/**
 * dp，sp，px 转换类
 *
 * Created by sickworm on 2017/10/16.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class DensityUtils {

    private DensityUtils() {

    }

    /**
     * dp转换成px
     */
    public static float dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return dpValue * scale;
    }

    /**
     * px转换成dp
     */
    public static float px2dp(Context context,float pxValue){
        float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale;
    }

    /**
     * sp转换成px
     */
    public static float sp2px(Context context,float spValue){
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    /**
     * px转换成sp
     */
    public static float px2sp(Context context,float pxValue){
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale;
    }
}
