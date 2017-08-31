package com.xiaokele.huaweiview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by QiyiLive on 2017/8/30.
 */

public class HuaWeiView extends View {

    //画圆弧的画笔
    private final Paint paint;
    //外围正方形的宽
    private int len;
    //要画的圆弧的外切矩形
    private RectF oval;
    //初始化起始角度和经过的角度
    private float startAngle=120;
    private float sweepAngle=300;
    private boolean useCenter=false;
    //画刻度的画笔
    private final Paint linePaint;
    //圆的半径
    private int mRadius;
    //内部圆的半径
    private int smallCircleRadius;
    //大字size
    private int bigTextSize;
    //小字size
    private int smallTextSize;
    //有色目标刻度画笔
    private final Paint targetLinePaint;
    //有色部分的角度
    private float targetAngle;
    //是否在运行
    private boolean isRunning;
    //运行状态：1、前进；2、后退
    private int state=2;
    private int score;
    private final Paint smallCirclePaint;
    private int red;
    private int green;
    private final Paint textPaint;

    public HuaWeiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //初始化画圆弧的画笔
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        //让画的圆是空心的（不填充）
        paint.setStyle(Paint.Style.STROKE);
        //初始化画刻度的画笔
        linePaint = new Paint();
        linePaint.setColor(Color.WHITE);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(2);
        //带颜色部分的画笔
        targetLinePaint = new Paint();
        targetLinePaint.setColor(Color.GREEN);
        targetLinePaint.setAntiAlias(true);
        targetLinePaint.setStrokeWidth(2);
        //内部圆的画笔
        smallCirclePaint = new Paint();
        //画字的画笔
        textPaint = new Paint();
        //字体居中显示
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        len = Math.min(width, height);
        mRadius =len/2;
        smallCircleRadius=mRadius-60;
        bigTextSize=smallCircleRadius/2;
        smallTextSize=smallCircleRadius/6;
        oval = new RectF(0, 0, len, len);
        setMeasuredDimension(len,len);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画圆弧的方法
        canvas.drawArc(oval,startAngle,sweepAngle,useCenter, paint);
        //画刻度线的方法
        drawViewLine(canvas);
        //画内部的圆和文字
        drawScoreText(canvas);
    }

    /**
     * 画刻度线的方法
     * */
    private void drawViewLine(Canvas canvas) {
        Log.e("onDraw","drawViewLine");
        canvas.save();
        canvas.translate(mRadius,mRadius);
        canvas.rotate(30);
        //确定该每次旋转到的角度
        float rotateAngle=sweepAngle/100;
        //记录已经画过的有色部分
        float hasDraw=0;
        for (int i=0;i<=100;i++){
            if (targetAngle != 0 && hasDraw <= targetAngle) {
                //计算颜色比例
                float percent=hasDraw/sweepAngle;
                red = 255 - (int) (255 * percent);
                green = (int)(255*percent);
                targetLinePaint.setARGB(255, red, green,0);
                if (null != onAngleColorListener) {
                    onAngleColorListener.colorListener(red, green);
                }
                canvas.drawLine(0,mRadius,0,mRadius-40,targetLinePaint);
            }else{
                canvas.drawLine(0,mRadius,0,mRadius-40,linePaint);
            }
            hasDraw+=rotateAngle;
            canvas.rotate(rotateAngle);
        }
        canvas.restore();
    }

    /**
     * 画文字
     * */
    public void drawScoreText(Canvas canvas) {
        //画内部圆
        smallCirclePaint.setColor(Color.argb(100, red, green, 0));
        canvas.drawCircle(mRadius, mRadius, smallCircleRadius, smallCirclePaint);
        //画文字
        textPaint.setTextSize(bigTextSize);
        canvas.drawText("" + score, mRadius, mRadius, textPaint);
        textPaint.setTextSize(smallTextSize);
        canvas.drawText("分", mRadius + smallCircleRadius / 2, mRadius - smallCircleRadius / 4, textPaint);
        canvas.drawText("点击优化",mRadius,mRadius+smallCircleRadius/2,textPaint);
    }

    /**
     * 设置改变的角度
     * */
    public void changeAngle(final float trueAngle){
        if (isRunning) {
            return;
        }
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                switch (state) {
                    //前进
                    case 1:
                        targetAngle+=3;
                        Log.e("前进", "targetAngle=" + targetAngle);
                        //目标角度到要的角度
                        if (targetAngle >= trueAngle) {
                            targetAngle=trueAngle;
                            //停止运行
                            isRunning=false;
                            //状态改为后退
                            state=2;
                            //结束本次运动
                            timer.cancel();
                        }
                        break;
                    //后退
                    case 2:
                        isRunning=true;
                        targetAngle-=3;
                        Log.e("后退", "targetAngle=" + targetAngle);
                        if (targetAngle <= 0) {
                            targetAngle=0;
                            state=1;
                        }
                        break;
                    default:
                        break;
                }
                //计算分数
                score = (int)(targetAngle/sweepAngle*100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        postInvalidate();
                    }
                }).start();
            }
        },100,10);
    }

    //对外提供的接口
    private OnAngleColorListener onAngleColorListener;

    public void setOnAngleColorListener(OnAngleColorListener onAngleColorListener) {
        this.onAngleColorListener=onAngleColorListener;
    }

    public interface OnAngleColorListener{
        void colorListener(int red,int green);
    }
}
