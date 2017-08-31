package com.hl.reddotmove;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.view.menu.MenuAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.hl.reddotmove.utils.CommonUtil;
import com.hl.reddotmove.utils.GeometryUtil;

/**
 * Created by CXH-PC on 2017-08-31.
 */

public class PointMoveView  extends View {

    private Paint mPaint;  //画笔
    private Rect  rect;
    private PointF  mInitCenter;
    private  float  mMoveRadius;  //移动的半径
    private  float  mStaticRadius; //固定的球的半径
    private  float  mMinStaticRadius; //固定球的最小半径
    private Context context;
    private  float  mMaxDistance;  // 最大距离
    private  float  resetDistance;
    private  Paint  mTextPaint;  //小球内部的文字
    private  float  mStatusBarHeight;  //状态栏高度

    private PointF mMoveCenter;
    private PointF mStaticCenter;
    private float  tempRadius;
    private Double  lineK;  //斜率

    private OnDisappearListener  listener;
    private ValueAnimator mAnim;
    String text = "";
    /**
     * 记录是否超出指定范围
     **/
    private boolean isOutOfRange = false;
    /**
     * 记录控件是否消失
     **/
    private boolean isDisappear = false;

    public PointMoveView(Context context) {
        this(context,null);
    }

    public PointMoveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PointMoveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public PointMoveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnDisappearListener(OnDisappearListener mListener) {
        this.listener = mListener;
    }

    /**
     * 初始化
     */
    private void init() {
        mPaint =new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new Rect(0,0,50,50);
        // 初始化拖拽圆和固定圆
        mMoveRadius = CommonUtil.dp2px(10.0f,context);
        mStaticRadius = CommonUtil.dp2px(10.0f,context);
        mMinStaticRadius = CommonUtil.dp2px(3.0f,context);
        mMaxDistance = CommonUtil.dp2px(80.0f,context);
        resetDistance = CommonUtil.dp2px(40.0f,context);

        //初始化画笔
        mPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mMoveRadius*1.2f);
    }


    public void setStatusBarHeight(int statusBarHeight) {
        this.mStatusBarHeight = statusBarHeight;
    }

    /**
     * 设置固定圆的半径
     **/
    public void setDargCircleRadius(float r) {
        mMoveRadius = r;
    }

    /**
     * 设置拖拽圆的半径
     **/
    public void setStickCircleRadius(float r) {
        mStaticRadius = r;
    }

    /**
     * 设置数字
     **/
    public void setNumber(int num) {
        text = String.valueOf(num);
    }

    /**
     * 初始化圆的圆心坐标
     **/
    public void initCenter(float x, float y) {
        mMoveCenter = new PointF(x, y);
        mStaticCenter = new PointF(x, y);
        mInitCenter = new PointF(x, y);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
          canvas.save();
         // 去除状态栏高度偏差
        canvas.translate(0,-mStatusBarHeight);

        if(!isDisappear)
        {
            if(!isOutOfRange)
            {
                Path path =new Path();
                //首先根据两圆心 计算 距离
                float  distance = GeometryUtil.getDistanceBetween2Points(mMoveCenter,mStaticCenter);
                tempRadius  = getCurrentRadius(distance);
                //计算斜率
                float  xDiff = mStaticCenter.x - mMoveCenter.x;
                float  yDiff = mStaticCenter.y - mMoveCenter.y;
                if(xDiff != 0)
                {
                    lineK =  (double)(yDiff / xDiff);
                }
                //计算 两圆的切点  坐标
                PointF[]  mStaticPoint = GeometryUtil.getIntersectionPoints(mMoveCenter,mMoveRadius,lineK);
                PointF[]  mMovePoint  = GeometryUtil.getIntersectionPoints(mStaticCenter,mStaticRadius,lineK);

                PointF  mControlPoint = GeometryUtil.getPointByPercent(mMoveCenter,mStaticCenter,0.618f);
                //绘制两圆
                path.moveTo(mMovePoint[0].x,mMovePoint[0].y);
                path.quadTo(mControlPoint.x,mControlPoint.y,mStaticPoint[0].x,mStaticPoint[0].y);
                path.lineTo(mStaticPoint[1].x,mStaticPoint[1].y);
                path.quadTo(mControlPoint.x,mControlPoint.y,mMovePoint[1].x,mMovePoint[1].y);
                path.close();

                //构建ShapeDrawable  并画在画布上
                ShapeDrawable  shapeDrawable = new ShapeDrawable(new PathShape(path,50f,50f));
                shapeDrawable.getPaint().setColor(Color.RED);
                shapeDrawable.setBounds(rect);
                shapeDrawable.draw(canvas);

                //画定圆
                canvas.drawCircle(mStaticCenter.x,mStaticCenter.y,tempRadius,mPaint);
                //画动圆
                canvas.drawCircle(mMoveCenter.x,mMoveCenter.y,mMoveRadius,mPaint);
                //画数字
                float  textPosition = mMoveCenter.y + mMoveRadius /2f;
                canvas.drawText(text,mMoveCenter.x,textPosition,mPaint);
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);
        switch (actionMasked)
        {
            case  MotionEvent.ACTION_DOWN:
                if(isAnimRunning()){
                    return  false;
                }
                isDisappear = false;
                isOutOfRange = false;
                UpdateMoveCenter(event.getRawX(),event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                //如果两圆距离大于 最大距离  执行 拖拽结束动画
                PointF  p0 = new PointF(mMoveCenter.x,mMoveCenter.y);
                PointF  p1 = new PointF(mStaticCenter.x,mStaticCenter.y);
                if(GeometryUtil.getDistanceBetween2Points(p0,p1) > mMaxDistance)
                {
                    isOutOfRange =true;
                    UpdateMoveCenter(event.getRawX(),event.getRawY());
                    return false;
                }
                UpdateMoveCenter(event.getRawX(),event.getRawY());
                break;

            case MotionEvent.ACTION_UP:
                handleActionUp();
                    break;
            default:
                isOutOfRange = false;
                break;
        }

        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isAnimRunning())
            return false;
        // 如果此时没有执行回弹效果则正常处理TouchEvent
        return super.dispatchTouchEvent(event);
    }

    /**
     * 判断回弹动画是否还在执行
     * @return
     */
    private  boolean  isAnimRunning(){
        if(mAnim !=null && mAnim.isRunning())
        {
            return  true;
        }
        return  false;
    }

    /**
     * 更新托撰圆的位置
     * @param x
     * @param y
     */
    private  void  UpdateMoveCenter(float  x,float  y){
        mMoveCenter.set(x,y);
        invalidate();
    }

    /**
     * 处理结束动画
     */
    private  void  handleActionUp(){
        if(isOutOfRange)
        {
            float  distance = GeometryUtil.getDistanceBetween2Points(mMoveCenter,mInitCenter);
            if(distance < resetDistance)
            {
                //还原
                if(listener != null)
                {
                    listener.onReset(isOutOfRange);
                }
                return;
            }
            //小球爆炸  消失
            disappeared();
        }else{
            //显示回弹动画
            showGoovAnimation();
        }

    }

    /**
     * 让控件消失
     */
    private void disappeared() {
        isDisappear = true;
        invalidate();
        if(listener != null)
        {
            listener.onDisappear(mMoveCenter);
        }
    }

    /**
     * 显示回弹效果
     */
    private  void  showGoovAnimation(){
        mAnim = ValueAnimator.ofFloat(1.0f);
        mAnim.setInterpolator(new OvershootInterpolator(4.0f));
        final PointF startPoint = new PointF(mMoveCenter.x, mMoveCenter.y);
        final PointF endPoint = new PointF(mStaticCenter.x, mStaticCenter.y);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                PointF pointByPercent = GeometryUtil.getPointByPercent(startPoint,
                        endPoint, fraction);
                UpdateMoveCenter((float) pointByPercent.x, (float) pointByPercent.y);
            }
        });
        mAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null)
                    listener.onReset(isOutOfRange);
            }
        });
        if (GeometryUtil.getDistanceBetween2Points(startPoint, endPoint) < 10) {
            mAnim.setDuration(10);
        } else {
            mAnim.setDuration(500);
        }
        mAnim.start();

    }

    //获得定园的半径
    private float getCurrentRadius(float distance) {
        distance = GeometryUtil.getDistanceBetween2Points(mStaticCenter,mMoveCenter);
        float  fraction = 0.2f + 0.8f * distance /mMaxDistance;
        float  value = GeometryUtil.evaluateValue(fraction,mStaticRadius,mMinStaticRadius);
        return  value;
    }

    interface   OnDisappearListener{

        /**
         * 当消失调用
         * @param mMoveCenter
         */
        void  onDisappear(PointF  mMoveCenter);

        /**
         * 当超出范围调用
         * @param isOutOfRange
         */
        void  onReset( boolean  isOutOfRange);
    }
}
