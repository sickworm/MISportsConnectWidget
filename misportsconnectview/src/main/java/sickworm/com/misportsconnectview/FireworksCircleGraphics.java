package sickworm.com.misportsconnectview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.v4.content.ContextCompat;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 烟花圆环控制类
 *
 * 速度相关参数对应时间单位为每帧（1/60秒）
 *
 * 日志耗性能所以就直接注释了
 *
 * Created by chenhao on 2017/10/15.
 */
class FireworksCircleGraphics {
    /** 圆环旋转速度 degree **/
    private static final int ROTATE_RATE = 3;
    /** 圆环半径与 Canvas 宽度之比 **/
    private static final float RADIUS_SCALE = 0.32f;

    /** 线条数目 **/
    private static final int LINE_AMOUNT = 15;
    /** 线条弧长 0-360 degree **/
    private static final int LINE_DEGREE = 345;
    /** 线条大小 dp **/
    private static final float LINE_SIZE = 0.5f;
    /** 线条半径偏离范围最大值 dp（越大显得越宽） **/
    private static final float LINE_MAX_DR = 4f;
    /** 线条圆心偏离范围最大值 dp （越大显得越宽）**/
    private static final float LINE_MAX_DC = 4f;
    /** 线条弧长变化速率范围（绝对值）dp **/
    private static final float LINE_MAX_CHANGE_RATE = 0.015f;
    /** 线条弧长变化速率衰减速率 dp **/
    private static final float LINE_DECAY_RATE = LINE_MAX_CHANGE_RATE / 180;
    /** 线条边界反向力比率 **/
    private static final float LINE_SIDE_RATIO = LINE_DECAY_RATE * 10;
    /** 线条随机摆动触发间隔 frame **/
    private static final int LINE_RANDOM_AFTER_FRAMES = 60;

    /** 星星数目 **/
    private static final int STAR_AMOUNT = 30;
    /** 星星大小 dp **/
    private static final float STAR_SIZE = 8f;
    /** 星星逃离 X 轴最大速度 dp **/
    private static final float STAR_MAX_VX = 2.5f;
    /** 星星逃离 Y 轴最大速度 dp **/
    private static final float STAR_MAX_VY = 2.5f;
    /** 星星速度衰减速率 dp **/
    private static final float STAR_DECAY_RATE = 0.003f;
    /** 星星速度衰减常量 dp **/
    private static final float STAR_DECAY_RATE_CONST = 0.001f;
    /** 星星消失临界距离 dp **/
    private static final float STAR_DISAPPEAR_DISTANCE = 60f;
    /** 星星消失临界亮度 dp **/
    private static final float STAR_DISAPPEAR_ALPHA = 0.05f;



    /** 线条半径偏离范围最大值 px（约为圆环宽度 / 2） **/
    private float lineMaxDxy;
    /** 线条圆心偏离范围最大值 px **/
    private float lineMaxDr;
    /** 线条弧长变化速率范围（绝对值）px **/
    private float lineMaxChangeRate;
    /** 线条弧长变化速率衰减速率 px **/
    private float lineDecayRate;

    /** 星星逃离 X 轴最大速度 px **/
    private float starMaxVx;
    /** 星星逃离 Y 轴最大速度 px **/
    private float starMaxVy;
    /** 星星速度衰减速率 px **/
    private float starDecayRate;
    /** 星星速度衰减常量 px **/
    private float starDecayRateConst;
    /** 星星消失临界距离 px **/
    private float starDisappearDistance;

    private int endColor;
    private int startColor;

    private Paint linePaint;
    private Paint starPaint;
    private Random random;
    private int rotateDegree = -90;

    /** 对象复用，减少 GC **/
    private int width = 0;
    private int height = 0;
    private int circleX = 0;
    private int circleY = 0;
    private RectF lineRectF = new RectF(0, 0, 0, 0);
    private boolean needRefresh = false;

//    /** 调试用，用于追踪某个点 **/
//    private static int traceCount = 0;

    /** 线条参数列表 **/
    private LineArgument[] lineArgumentList;
    /** 星星参数列表 **/
    private StarArgument[] starArgumentList;

    FireworksCircleGraphics(Context context) {

        lineMaxDxy = DensityUtils.dp2px(context, LINE_MAX_DR);
        lineMaxDr = DensityUtils.dp2px(context, LINE_MAX_DC);
        lineMaxChangeRate = DensityUtils.dp2px(context, LINE_MAX_CHANGE_RATE);
        lineDecayRate = DensityUtils.dp2px(context, LINE_DECAY_RATE);
        lineRectF = new RectF(0, 0, 0, 0);

        /* 星星大小 px */
        float starSize = DensityUtils.dp2px(context, STAR_SIZE);
        starMaxVx = DensityUtils.dp2px(context, STAR_MAX_VX);
        starMaxVy = DensityUtils.dp2px(context, STAR_MAX_VY);
        starDecayRate = DensityUtils.dp2px(context, STAR_DECAY_RATE);
        starDecayRateConst = DensityUtils.dp2px(context, STAR_DECAY_RATE_CONST);
        starDisappearDistance = DensityUtils.dp2px(context, STAR_DISAPPEAR_DISTANCE);

        endColor = ContextCompat.getColor(context, R.color.transparent);
        startColor = ContextCompat.getColor(context, R.color.white);

        random = new Random(new SecureRandom().nextInt());
        lineArgumentList = new LineArgument[LINE_AMOUNT];
        for (int i = 0; i < lineArgumentList.length; i++) {
            lineArgumentList[i] = new LineArgument();
        }
        starArgumentList = new StarArgument[STAR_AMOUNT];
        for (int i = 0; i < starArgumentList.length; i++) {
            starArgumentList[i] = new StarArgument();
        }

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeWidth(DensityUtils.dp2px(context, LINE_SIZE));
        linePaint.setAntiAlias(true);

        starPaint = new Paint();
        starPaint.setStrokeCap(Paint.Cap.ROUND);
        starPaint.setStrokeWidth(starSize);
//        starPaint.setAntiAlias(true);
    }

    void draw(Canvas canvas) {
        canvas.save();

        if (canvas.getHeight() != height || canvas.getWidth() != width) {
            needRefresh = true;
            height = canvas.getHeight();
            width = canvas.getWidth();
        }

        if (needRefresh) {
            circleX = (int) (width * MISportsConnectView.START_CIRCLE_X_SCALE);
            circleY = (int) (height * MISportsConnectView.START_CIRCLE_Y_SCALE);
            SweepGradient lineSweepGradient = new SweepGradient(circleX, circleY, endColor, startColor);
            linePaint.setShader(lineSweepGradient);
            needRefresh = false;

            // 星星需要宽度才能启动
            for(StarArgument argument : starArgumentList) {
                argument.reset();
            }
        }

        int radius = (int) (canvas.getWidth() * RADIUS_SCALE);
        canvas.rotate(rotateDegree, circleX, circleY);
        for (LineArgument argument : lineArgumentList) {

            float dx = argument.dx;
            float dy = argument.dy;
            float dr = argument.dr;
            lineRectF.set(
                    circleX - radius - dr - dx,
                    circleY - radius - dr - dy,
                    circleX + radius + dr + dx,
                    circleY + radius + dr + dy);
            // 模拟倾斜效果
            float dAngle = (-dx) < -(360 - LINE_DEGREE)? (360 - LINE_DEGREE) : (-dx);
            canvas.drawArc(
                    lineRectF,
                    360 - LINE_DEGREE + dAngle,
                    LINE_DEGREE,
                    false, linePaint);
        }
        for (StarArgument argument : starArgumentList) {
            float dx = argument.dx;
            float dy = argument.dy;
            int alphaMask = ((int) (argument.alpha * 0xff)) << 24 ;
            int transparentColor = (startColor & 0x00ffffff) + alphaMask;
//            LogUtils.d(String.format(Locale.CHINA, "dx=%2.2f\tdy=%2.2f\talpha=0x%08x\tcolor=0x%08x",
//                    dx, dy, alphaMask, transparentColor));
            starPaint.setColor(transparentColor);
            canvas.drawPoint(circleX + radius + dx, circleY + dy, starPaint);
        }
        starPaint.setColor(startColor);
        canvas.drawPoint(circleX + radius, circleY, starPaint);

        canvas.restore();

    }

    /**
     * 计算下一帧参数
     */
    void next() {
        rotateDegree = (rotateDegree + ROTATE_RATE) % 360;
        for (LineArgument argument : lineArgumentList) {
            argument.next();
        }
        for (StarArgument argument : starArgumentList) {
            argument.next();
        }
    }

    /**
     * 线条位置数据，有惯性和随机属性。半径随机效果一般，最后没用到
     * 加速度由 衰减值，随机值，边界反向力三个力合成，并影响偏移值
     */
    private class LineArgument {
        /** 圆心 X 轴偏移值 **/
        float dx;
        /** 圆心 Y 轴偏移值 **/
        float dy;
        /** 圆半径 r 偏移值 **/
        float dr;
        /** 圆心 X 轴偏移速度 **/
        float vx;
        /** 圆心 Y 轴偏移速度 **/
        float vy;
        /** 圆半径 r 轴偏移速度 **/
        float vr;
        /** 圆心 X 轴偏移加速度 **/
        float ax;
        /** 圆心 Y 轴偏移加速度 **/
        float ay;
        /** 圆半径 r 轴偏移加速度 **/
        float ar;

        /** 帧数计算，用于施加随机力 **/
        int frameCount = 0;

        LineArgument() {
            dx = nextSignedFloat() * lineMaxDr;
            dy = nextSignedFloat() * lineMaxDr;
            dr = nextSignedFloat() * lineMaxDxy;

            vx = 0;
            vy = 0;
            vr = 0;

            ax = 0;
            ay = 0;
            ar = 0;

            frameCount = random.nextInt() % LINE_RANDOM_AFTER_FRAMES;
        }

        /**
         * 计算下一帧（每秒60帧）的各项参数值
         */
        void next() {
            // a = 衰减值 + 随机值 + 边界反向力
            float newAx =
                    (vx > 0? -1 : 1) * lineDecayRate  +
                    (frameCount == 0? (nextSignedFloat() * lineMaxChangeRate) : 0) +
                    (Math.abs(dx) > lineMaxDxy ? (-LINE_SIDE_RATIO * dx / lineMaxDxy) : 0);
            float newAy =
                    (vy > 0? -1 : 1) * lineDecayRate  +
                    (frameCount == 0? (nextSignedFloat() * lineMaxChangeRate) : 0) +
                    (Math.abs(dy) > lineMaxDxy ? (-LINE_SIDE_RATIO * dy / lineMaxDxy) : 0);
            float newAr =
                    (vr > 0? -1 : 1) * lineDecayRate  +
                    (frameCount == 0? (nextSignedFloat() * lineMaxChangeRate) : 0) +
                    (Math.abs(dr) > lineMaxDr ? (-LINE_SIDE_RATIO * dr / lineMaxDr) : 0);

            float newVx = vx + (ax + newAx) / 2;
            float newVy = vy + (ax + newAy) / 2;
            float newVr = vr + (ax + newAr) / 2;

            dx += (vx + newVx) / 2;
            dy += (vy + newVy) / 2;
            dr += (vr + newVr) / 2;

//            if (traceCount++ % LINE_AMOUNT == 0) {
//                LogUtils.d(String.format(java.util.Locale.CHINA, "ax=% 2.2f\tvx=% 2.2f\tdx=% 2.2f\tdr=% 2.2f\tfr=% 5d\tsr=% 2.2f\t",
//                        ax, vx, dx,
//                        -LINE_DECAY_RATE * vx * Math.abs(vx),
//                        frameCount,
//                        -LINE_SIDE_RATIO * dx * Math.abs(dx)));
//            }

            ax = newAx;
            ay = newAy;
            ar = newAr;
            vx = newVx;
            vy = newVy;
//            vr = newVr;
            frameCount = ++frameCount % LINE_RANDOM_AFTER_FRAMES;
        }
    }


    /**
     * 星星位置数据，有惯性和随机属性
     * 具有初始速度
     * 加速度由 衰减值（模拟空气阻力）组成
     * 星星透明度与距离相关
     * 速度过低或距离过远则消失并重新出发
     */
    private class StarArgument {
        /** 距离源点 X 轴偏移 **/
        float dx;
        /** 距离源点 Y 轴偏移 **/
        float dy;
        /** 逃离源点 X 轴速度 **/
        float vx;
        /** 逃离源点 Y 轴速度 **/
        float vy;
        /** 逃离源点 X 轴加速度 **/
        double ax;
        /** 逃离源点 Y 轴加速度 **/
        double ay;
        /** 星星透明度 **/
        float alpha;

        StarArgument() {
            reset();
        }

        private void reset() {
            dx = 0;
            dy = 0;

            vx = nextSignedFloat() * starMaxVx;
            // Y 轴因旋转存在初始速度
            vy = random.nextFloat() * starMaxVy +
                -2 * (float)Math.PI * width * RADIUS_SCALE * ((float)ROTATE_RATE / 360);

            ax = 0;
            ay = 0;

            alpha = 1;
        }

        void next() {
            ax = -(vx * Math.abs(vx) * starDecayRate - starDecayRateConst);
            ay = -(vy * Math.abs(vy) * starDecayRate - starDecayRateConst);
            if (ax < 0) {
                ax = 0;
            }
            if (ay < 0) {
                ay = 0;
            }

            dx += vx / 2;
            vx += ax;
            dx += vx / 2;

            dy += vy / 2;
            vy += ay;
            dy += vy / 2;

            alpha = 1 - (float) Math.sqrt(dx * dx + dy * dy) / starDisappearDistance;

//            if (traceCount++ % STAR_AMOUNT == 0) {
//                LogUtils.i(String.format(Locale.CHINA, "ax=% 2.2f\tvx=% 2.2f\tdx=% 2.2f\tay=% 2.2f\tvy=% 2.2f\tdy=% 2.2f\talpha=%2.2f",
//                        ax, vx, dx, ay, vy, dy, alpha));
//                if (alpha < STAR_DISAPPEAR_ALPHA) {
//                    LogUtils.i("reset");
//                }
//            }

            if (alpha < STAR_DISAPPEAR_ALPHA) {
                reset();
            }
        }
    }

    private float nextSignedFloat() {
        return (random.nextBoolean()? 1 : -1) * random.nextFloat();
    }
}
