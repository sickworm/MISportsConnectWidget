package sickworm.com.misportsconnectview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * 小米手环连接界面控件
 *
 * 速度相关参数对应时间单位为每帧（1/60秒）
 *
 * Created by sickworm on 2017/10/15.
 */
public class MISportsConnectView extends View {
    /** 控件默认宽度 dp **/
    static final int DEFAULT_WIDTH = 400;
    /** 控件默认高度 dp **/
    static final int DEFAULT_HEIGHT = 300;

    /** 初始圆心位置 X 与 Canvas 宽度之比 **/
    static final float START_CIRCLE_X_SCALE = 0.5f;
    /** 初始圆心位置 Y 与 Canvas 宽度之比 **/
    static final float START_CIRCLE_Y_SCALE = 0.5f;

    /** 圆环半径大小 dp （画笔大小）**/
    private static final int BIG_CIRCLE_SIZE = 16;
    /** 圆环半径与 Canvas 宽度之比 **/
    private static final float BIG_CIRCLE_RADIUS_SCALE = 0.38f;
    /** 圆环抖动效果半径 dp **/
    private static final float BIG_CIRCLE_SHAKE_RADIUS = 20;
    /** 圆环抖动效果偏移 dp **/
    private static final float BIG_CIRCLE_SHAKE_OFFSET = 0.4f;
    /** 圆环旋转速度 degree **/
    static final float BIG_CIRCLE_ROTATE_SPEED = 0.5f;
    /** 圆环光晕效果层数 **/
    private static final int CIRCLE_BLUR_LAYER_AMOUNT = 4;
    /** 圆环光晕效果大小 dp **/
    private static final float CIRCLE_BLUR_SIZE = 16;


    /** 虚线/实线 半径与 Canvas 宽度之比**/
    private static final float DOTTED_SOLID_CIRCLE_SIZE = 0.32f;
    /** 虚线画笔大小 dp **/
    private static final float DOTTED_CIRCLE_WIDTH = 2f;
    /** 虚线间隔大小 dp **/
    private static final float DOTTED_CIRCLE_GAG = 1f;
    /** 实线画笔大小 dp **/
    private static final float SOLID_CIRCLE_WIDTH = 2f;
    /** 实线头的圆点大小 dp **/
    private static final float DOT_SIZE = 8f;

    /** 主栏字体大小 sp **/
    private static final int MAIN_TITLE_FONT_SIZE_SP = 64;
    /** 副栏字体大小 sp **/
    private static final int SUB_TITLE_FONT_SIZE_SP = 14;
    /** 副栏字体偏移 dp **/
    private static final int SUB_TITLE_FONT_OFFSET_DP = 50;
    /** 手表图标偏移 dp **/
    private static final int WATCH_OFFSET_DP = 84;
    /** 手表图标大小 dp **/
    private static final int WATCH_SIZE = 24;

    /** 圆心变化控制变量 scale **/
    private float circleOffsetY = 0;
    /** 圆半径控制变量 scale **/
    private float circleRadiusIncrement = 0;
    /** 圆环从透明到实体的显示进度 % **/
    private float circleAlphaProgress = 0;
    /** 步数离圆心的 Y 轴偏移 px **/
    private float mainTitleOffsetY;
    /** 副标题离圆心的 Y 轴偏移 px **/
    private float subTitleOffsetY;
    /** 手表 icon 离圆心的 Y 轴偏移 px **/
    private float watchOffset;

    /** 步数画笔 **/
    private Paint mainTitlePaint;
    /** 副标题画笔 **/
    private Paint subTitlePaint;
    /** 圆环画笔 **/
    private Paint bigCirclePaint;
    /** 光晕画笔 **/
    private Paint blurPaint;
    /** 虚线画笔 **/
    private Paint dottedCirclePaint;
    /** 实线画笔 **/
    private Paint solidCirclePaint;
    /** 点画笔 **/
    private Paint dotPaint;

    private boolean needRefreshText = true;
    private FireworksCircleGraphics fireworksCircle;
    private AnimationThread animationThread;
    private AnimationState animationState = AnimationState.LOADING;
    /** 旋转度数 **/
    private float degree = 0;

    /** 复用对象，减少 GC **/
    private String mainTitleString = "";
    private String subTitleString = "";
    private String subTitleSeparator = "";
    private int circleColor = 0;
    private float blurSize = 0;
    private RectF solidCircleRectF = new RectF();
    private RectF blurOvalRectF = new RectF();

    /** 副标题文字居中偏移量 **/
    private float subTitleOffsetX = 0;
    private Bitmap backgroundBitmap;
    private Bitmap watchBitmap;


    /** 外部接口相关 **/
    private SportsData sportsData = new SportsData();
    private boolean isConnected = false;
    private int width;
    private int height;
    private float circleX;
    private float circleY;

    public MISportsConnectView(Context context) {
        super(context);
        init(context);
    }

    public MISportsConnectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MISportsConnectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mainTitlePaint = new Paint();
        mainTitlePaint.setColor(ContextCompat.getColor(context, R.color.white));
        mainTitlePaint.setTextAlign(Paint.Align.CENTER);
        mainTitlePaint.setTextSize(DensityUtils.sp2px(context, MAIN_TITLE_FONT_SIZE_SP));
        mainTitleOffsetY = -(mainTitlePaint.getFontMetrics().ascent +
                mainTitlePaint.getFontMetrics().descent) / 2;
        mainTitlePaint.setAntiAlias(true);

        circleColor = ContextCompat.getColor(context, R.color.whiteTransparent);
        subTitlePaint = new Paint();
        subTitlePaint.setColor(circleColor);
        subTitlePaint.setTextSize(DensityUtils.sp2px(context, SUB_TITLE_FONT_SIZE_SP));
        subTitleOffsetY = DensityUtils.sp2px(context, SUB_TITLE_FONT_OFFSET_DP);
        subTitleSeparator = getResources().getString(R.string.sub_title_separator);
        subTitlePaint.setAntiAlias(true);

        bigCirclePaint = new Paint();
        bigCirclePaint.setStrokeWidth(DensityUtils.dp2px(context, BIG_CIRCLE_SIZE));
        bigCirclePaint.setStyle(Paint.Style.STROKE);
        bigCirclePaint.setAntiAlias(true);

        blurPaint = new Paint(bigCirclePaint);
        blurSize = DensityUtils.dp2px(context, CIRCLE_BLUR_SIZE);

        PathEffect pathEffect1 = new CornerPathEffect(DensityUtils.dp2px(getContext(), BIG_CIRCLE_SHAKE_RADIUS));
        PathEffect pathEffect2 = new DiscretePathEffect(DensityUtils.dp2px(getContext(), BIG_CIRCLE_SHAKE_RADIUS),
                DensityUtils.dp2px(getContext(), BIG_CIRCLE_SHAKE_OFFSET));
        PathEffect pathEffect = new ComposePathEffect(pathEffect1, pathEffect2);
        bigCirclePaint.setPathEffect(pathEffect);

        dottedCirclePaint = new Paint();
        dottedCirclePaint.setStrokeWidth(DensityUtils.dp2px(context, DOTTED_CIRCLE_WIDTH));
        dottedCirclePaint.setColor(ContextCompat.getColor(context, R.color.whiteTransparent));
        dottedCirclePaint.setStyle(Paint.Style.STROKE);
        float gagPx = DensityUtils.dp2px(context, DOTTED_CIRCLE_GAG);
        dottedCirclePaint.setPathEffect(new DashPathEffect(new float[]{gagPx, gagPx}, 0));
        dottedCirclePaint.setAntiAlias(true);

        solidCirclePaint = new Paint();
        solidCirclePaint.setStrokeWidth(DensityUtils.dp2px(context, SOLID_CIRCLE_WIDTH));
        solidCirclePaint.setColor(ContextCompat.getColor(context, R.color.white));
        solidCirclePaint.setStyle(Paint.Style.STROKE);
        solidCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        solidCirclePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setStrokeWidth(DensityUtils.dp2px(context, DOT_SIZE));
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setColor(ContextCompat.getColor(context, R.color.white));
        dotPaint.setAntiAlias(true);

        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg_step_law);

        // 设置手表 icon 的大小
        watchBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_headview_watch);
        float scale = DensityUtils.dp2px(context, WATCH_SIZE) / watchBitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        watchBitmap = Bitmap.createBitmap(watchBitmap,
                0, 0, watchBitmap.getWidth(), watchBitmap.getHeight(),
                matrix, true);
        watchOffset = DensityUtils.sp2px(context, WATCH_OFFSET_DP);

        fireworksCircle = new FireworksCircleGraphics(context);

        animationThread = new AnimationThread();
        animationThread.start();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE && animationThread.isInterrupted()) {
            animationThread.start();
        } else {
            animationThread.interrupt();
        }
    }

    /**
     * 屏幕发生变化时更新动画参数
     */
    private void resetDataIfNeeded(Canvas canvas) {
        if (needRefreshText) {
            refreshText();
            needRefreshText = false;
        }

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (width == this.width && height == this.height) {
            return;
        }

        this.width = width;
        this.height = height;
        circleX = width * START_CIRCLE_X_SCALE;
        circleY = height * START_CIRCLE_Y_SCALE;

        // 背景：设置背景大小，使其可覆盖整个 View
        float scaleX = (float)width / backgroundBitmap.getWidth();
        float scaleY = (float)height / backgroundBitmap.getHeight() ;
        float scale = Math.max(scaleX, scaleY);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        backgroundBitmap = Bitmap.createBitmap(backgroundBitmap,
                0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight(),
                matrix, true);


        float bigCircleRadius = width * BIG_CIRCLE_RADIUS_SCALE;
        Shader bigCircleLinearGradient = new LinearGradient(
                circleX - bigCircleRadius, circleY,
                circleX + bigCircleRadius, circleY,
                ContextCompat.getColor(getContext(), R.color.whiteTransparent),
                ContextCompat.getColor(getContext(), R.color.white),
                Shader.TileMode.CLAMP);
        bigCirclePaint.setShader(bigCircleLinearGradient);
        Shader blurLinearGradient = new LinearGradient(
                circleX, circleY,
                circleX + bigCircleRadius, circleY,
                ContextCompat.getColor(getContext(), R.color.transparent),
                ContextCompat.getColor(getContext(), R.color.white),
                Shader.TileMode.CLAMP);
        blurPaint.setShader(blurLinearGradient);
    }

    private void refreshText() {
        // 字块
        mainTitleString = Integer.toString(sportsData.step);
        String format = getResources().getString(R.string.sub_title_format);
        subTitleString = String.format(format, sportsData.distance / 1000, sportsData.calories);
        // 副标题文字居中
        float indexBefore = subTitlePaint.measureText(subTitleString, 0, subTitleString.indexOf(subTitleSeparator));
        float indexAfter = subTitlePaint.measureText(subTitleString, 0, subTitleString.indexOf(subTitleSeparator) + 1);
        subTitleOffsetX = -(indexBefore + indexAfter) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        resetDataIfNeeded(canvas);

        // 背景，左下角对齐
        canvas.drawBitmap(backgroundBitmap, 0, height - backgroundBitmap.getHeight(), null);

        // 平移动画效果
        canvas.save();
        canvas.translate(0, width * circleOffsetY);
        switch (animationState) {
            case LOADING:
                drawFireworks(canvas);
                drawText(canvas);
                break;
            case UP1:
                drawText(canvas);
                drawBigCircle(canvas);
                break;
            case DOWN1:
                drawText(canvas);
                drawBigCircle(canvas);
                break;
            case STOP:
            case UP2:
                drawText(canvas);
                drawBigCircle(canvas);
                drawProgressCircle(canvas);
                break;
            case FINISH:
                drawText(canvas);
                drawBigCircle(canvas);
                drawProgressCircle(canvas);
                break;
            case DISCONNECT:
                drawText(canvas);
                drawBigCircle(canvas);
                drawProgressCircle(canvas);
                break;
        }
        canvas.restore();

        invalidate();
    }

    // 烟花圆环
    private void drawFireworks(Canvas canvas) {
        fireworksCircle.draw(canvas);
    }

    // 字块和设备图标
    private void drawText(Canvas canvas) {
        canvas.drawText(mainTitleString, circleX, circleY + mainTitleOffsetY, mainTitlePaint);
        canvas.drawText(subTitleString, circleX + subTitleOffsetX, circleY + subTitleOffsetY, subTitlePaint);

        canvas.drawBitmap(watchBitmap, circleX - watchBitmap.getWidth() / 2,
                circleY - watchBitmap.getHeight() / 2 + watchOffset, null);
    }

    // 大圆环
    private void drawBigCircle(Canvas canvas) {
        float bigCircleRadius = width * BIG_CIRCLE_RADIUS_SCALE;

        // 扩大和旋转动画效果
            canvas.save();
            canvas.scale(
                    1 + circleRadiusIncrement / BIG_CIRCLE_RADIUS_SCALE,
                    1 + circleRadiusIncrement / BIG_CIRCLE_RADIUS_SCALE,
                    circleX, circleY);
            canvas.rotate(degree, circleX, circleY);

        // 光晕
        blurPaint.setAlpha((int) (Color.alpha(circleColor) * circleAlphaProgress));
        for (int i = 0; i < CIRCLE_BLUR_LAYER_AMOUNT; i++) {
            blurPaint.setAlpha(0xff * (CIRCLE_BLUR_LAYER_AMOUNT - i) / (CIRCLE_BLUR_LAYER_AMOUNT * 3));
            blurOvalRectF.set(circleX - bigCircleRadius, circleY - bigCircleRadius,
                    circleX + bigCircleRadius + i * blurSize / CIRCLE_BLUR_LAYER_AMOUNT, circleY + bigCircleRadius);
            canvas.drawOval(blurOvalRectF, blurPaint);
        }

        bigCirclePaint.setAlpha((int) (0xff * circleAlphaProgress));
        canvas.drawCircle(circleX, circleY, bigCircleRadius, bigCirclePaint);

        canvas.restore();
    }

    // 进度圆环
    private void drawProgressCircle(Canvas canvas) {
        float dottedCircleRadius = width * DOTTED_SOLID_CIRCLE_SIZE;

        solidCircleRectF.set(circleX - dottedCircleRadius, circleY - dottedCircleRadius, circleX + dottedCircleRadius, circleY + dottedCircleRadius);
        canvas.drawCircle(circleX, circleY, dottedCircleRadius, dottedCirclePaint);
        canvas.drawArc(solidCircleRectF, -90, 3.6f * sportsData.progress, false, solidCirclePaint);
        // 计算进度点位置
        canvas.drawPoint(circleX + dottedCircleRadius * (float)Math.cos((3.6f * sportsData.progress - 90)* Math.PI / 180),
                circleY + dottedCircleRadius * (float)Math.sin((3.6f * sportsData.progress - 90) * Math.PI / 180),
                dotPaint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = this.getMeasuredSize(widthMeasureSpec, true);
        int height = this.getMeasuredSize(heightMeasureSpec, false);
        setMeasuredDimension(width, height);
    }

    private int getMeasuredSize(int length, boolean isWidth) {
        int specMode = MeasureSpec.getMode(length);
        int specSize = MeasureSpec.getSize(length);
        int retSize;
        int padding = (isWidth? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom());

        // MATCH_PARENT or xx dp
        if (specMode == MeasureSpec.EXACTLY) {
            retSize = specSize;
        } else {
            // WRAP_CONTENT or unset
            retSize = isWidth? DEFAULT_WIDTH + padding : DEFAULT_HEIGHT + padding;
            if (specMode == MeasureSpec.UNSPECIFIED) {
                retSize = Math.min(retSize, specSize);
            }
        }

        return retSize;
    }

    /**
     * 设置运动数据
     *
     * 速度相关参数对应时间单位为每帧（1/60秒）
     */
    public void setSportsData(SportsData sportsData) {
         // 防 null 处理
        this.sportsData = new SportsData(sportsData);
        refreshText();
    }

    /**
     * 设置连接状态
     */
    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    /**
     * 动画状态机
     */
    private enum AnimationState {
        LOADING,
        UP1,
        DOWN1,
        STOP,
        UP2,
        FINISH,
        DISCONNECT;

        AnimationState nextState() {
            switch(this) {
                case LOADING:       return UP1;
                case UP1:           return DOWN1;
                case DOWN1:         return STOP;
                case STOP:          return UP2;
                case UP2:           return FINISH;
                case FINISH:        return DISCONNECT;
                case DISCONNECT:    return LOADING;
                default:            return LOADING;
            }
        }
    }

    /**
     * 动画控制线程，包括动画状态机跳转和动画参数
     */
    private class AnimationThread extends Thread {
        /**
         * 动画刷新间隔，每秒 60 帧
         */
        private static final int INTERVAL_MILL = 17;
        private static final int LOADING_TIME_MILL = -INTERVAL_MILL;
        private static final int UP1_TIME_MILL = 250;
        private static final int DOWN1_TIME_MILL = UP1_TIME_MILL;
        private static final int STOP_TIME_MILL = 120;
        private static final int UP2_TIME_MILL = 300;
        private static final int FINISH_TIME_MILL = -INTERVAL_MILL;
        private static final int DISCONNECT_TIME_MILL = 200;

        /** 圆环淡入淡出时间 **/
        private static final float APPEAR_MILLS = 10 * INTERVAL_MILL;

        /** UP1 阶段高度变化百分比 **/
        private static final float UP1_SCALE = -0.05f;

        /** DOWN1 阶段高度变化百分比 **/
        private static final float DOWN1_SCALE = -UP1_SCALE;

        /** UP2 阶段高度变化百分比 **/
        private static final float UP2_SCALE = UP1_SCALE;

        /** DISCONNECT 阶段高度变化百分比 **/
        private static final float DISCONNECT_DOWN2_SCALE = -UP2_SCALE;

        private final int[] ANIMATION_TIME_LIST = {
                LOADING_TIME_MILL, UP1_TIME_MILL, DOWN1_TIME_MILL, STOP_TIME_MILL, UP2_TIME_MILL, FINISH_TIME_MILL, DISCONNECT_TIME_MILL};

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            int index = 0;
            while(true) {
                int durationTime = ANIMATION_TIME_LIST[index];
                index = (index + 1) % ANIMATION_TIME_LIST.length;

                // 每个阶段的动画帧数
                int times = durationTime / INTERVAL_MILL;
                int count = 0;

                nextAnimation:
                while (times < 0 || count++ < times) {
                    long startTime = System.currentTimeMillis();
                    switch(animationState) {
                        case LOADING:
                            fireworksCircle.next();
                            if (isConnected) {
                                break nextAnimation;
                            }
                            break;
                        case UP1:
                            if (count <= APPEAR_MILLS / INTERVAL_MILL) {
                                circleAlphaProgress = count / (APPEAR_MILLS / INTERVAL_MILL);
                            }
                            circleOffsetY = (float) (UP1_SCALE * Math.sin((float)count / times * Math.PI / 2));
                            circleRadiusIncrement = -circleOffsetY;
                            break;
                        case DOWN1:
                            circleOffsetY = (float) (DOWN1_SCALE * -Math.sin(Math.PI / 2 + (float)count / times * Math.PI / 2));
                            circleRadiusIncrement = -circleOffsetY;
                            break;
                        case STOP:
                            break;
                        case UP2:
                            circleOffsetY = (float) (UP2_SCALE * Math.sin((float)count / times * Math.PI / 2));
                            break;
                        case FINISH:
                            if (!isConnected) {
                                break nextAnimation;
                            }
                            break;
                        case DISCONNECT:
                            if (times - count <= APPEAR_MILLS / INTERVAL_MILL) {
                                circleAlphaProgress = (times - count) / (APPEAR_MILLS / INTERVAL_MILL);
                            }
                            circleOffsetY = (float) (DISCONNECT_DOWN2_SCALE * -Math.sin(Math.PI / 2 + (float)count / times * Math.PI / 2));
                            break;
                        default:
                            break;
                    }

                    degree = (degree + BIG_CIRCLE_ROTATE_SPEED) % 360;
                    long usedTime = System.currentTimeMillis() - startTime;
                    LogUtils.d("calculate used time mill " + usedTime);
                    try {
                        if (usedTime > INTERVAL_MILL) {
                            continue;
                        }
                        // 尽可能 17ms 更新一次
                        sleep(INTERVAL_MILL - usedTime);
//                        sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
                animationState = animationState.nextState();
            }
        }
    }
}
