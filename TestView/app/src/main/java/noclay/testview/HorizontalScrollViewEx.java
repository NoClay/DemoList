package noclay.testview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import static android.R.attr.breadCrumbShortTitle;
import static android.R.attr.x;

/**
 * Created by 82661 on 2016/10/17.
 */

public class HorizontalScrollViewEx extends ViewGroup {
    private static final String TAG = "HorizontalScrollViewEx";
    private int mChildrenSize;
    private int mChildWidth;
    private int mChildIndex;
    //记录上次滑动的坐标
    private int mLastX = 0;
    private int mLastY = 0;
    //记录上次滑动的坐标(onInterceptTouchEvent)
    private int mLastXIntercept = 0;
    private int mLastYIntercept = 0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;


    public HorizontalScrollViewEx(Context context) {
        super(context);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalScrollViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mScroller == null) {
            mScroller = new Scroller(getContext());
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    //动画滚动到最终位置结束动画，这里代表本身HorizontalScrollViewEx处理触摸事件
                    intercepted = true;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int deltaX = x - mLastXIntercept;
                int deltaY = y - mLastYIntercept;
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    //水平滑动则处理此事件
                    intercepted = true;
                } else {
                    intercepted = false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                intercepted = false;
                break;
            }
            default:
                break;
        }
        Log.d(TAG, "onInterceptTouchEvent: intercepted = " + intercepted);
        mLastX = x;
        mLastY = y;
        mLastXIntercept = x;
        mLastYIntercept = y;
        return intercepted;
    }


    /**
     * 本身在处理触摸事件
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:{
                int deltaX = x - mLastX;
                int deltaY = y - mLastY;
                scrollBy(-deltaX, 0);
                break;
            }
            case MotionEvent.ACTION_UP:{
                int scrollX = getScrollX();
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
                if(Math.abs(xVelocity) >= 50){
                    mChildIndex = xVelocity > 0 ? mChildIndex - 1 : mChildIndex + 1;
                }else{
                    mChildIndex = (scrollX + mChildWidth / 2) / mChildWidth;
                }
                mChildIndex = Math.max(0, Math.min(mChildIndex, mChildrenSize - 1));
                int dx = mChildIndex * mChildWidth - scrollX;
                smoothScrollBy(dx, 0);
                mVelocityTracker.clear();
                break;
            }
            default:break;
        }
        mLastX = x;
        mLastY = y;
        return true;
    }


    /**
     * 重写onMeasure方法，对容器内的测量进行修正
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        /**
         * super.onMeasure方法内部调用了setMeasuredDimension(getDefaultSize(
         * getSuggestedMinimumWidth(), widthMeasureSpec),
         * getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
         */
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpaceMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpaceMode = MeasureSpec.getMode(heightMeasureSpec);
        if(childCount == 0){
            //no child -- no width, no height
            setMeasuredDimension(0, 0);
        }else if(widthSpaceMode == MeasureSpec.AT_MOST &&
                heightSpaceMode == MeasureSpec.AT_MOST){
            //width is wrap and height is wrap
            final View childView = getChildAt(0);
            measuredWidth = childView.getMeasuredWidth() * childCount;
            measuredHeight = childView.getMeasuredHeight();
            setMeasuredDimension(measuredWidth, measuredHeight);
        }else if(heightSpaceMode == MeasureSpec.AT_MOST){
            //height is wrap but width not
            final View childView = getChildAt(0);
            measuredHeight = childView.getMeasuredHeight();
            setMeasuredDimension(widthSpaceSize, measuredHeight);
        }else if(widthMeasureSpec == MeasureSpec.AT_MOST){
            //width is wrap but height not
            final View childView = getChildAt(0);
            measuredWidth = childView.getMeasuredWidth();
            setMeasuredDimension(measuredWidth, heightSpaceSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        mChildrenSize = childCount;
        for(int i = 0; i < childCount; i ++){
            final View childView = getChildAt(i);
            if(childView.getVisibility() != View.GONE){
                final int childWidth = childView.getMeasuredWidth();
                mChildWidth = childWidth;
                childView.layout(childLeft, 0, childLeft + childWidth,
                        childView.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    /**
     * 两个startScrollBy方法，一个加上了时间间隔，一个并没有
     * 之前整错了，用错了方法
     * @param dx
     * @param dy
     */
    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(getScrollX(), 0, dx, 0, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mVelocityTracker.recycle();
        super.onDetachedFromWindow();
    }


}
