package noclay.testview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.util.Measure;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by 82661 on 2016/10/17.
 */

public class CircleView extends View {
    private static final String TAG = "CircleView";
    private int mColor = Color.RED;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //Paint.ANTI_ALIAS_FLAG 是抗锯齿的意思

    public CircleView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mPaint.setColor(mColor);
    }

    /**
     * 自定义属性的时候用这个方法
     * @param context
     * @param attrs
     */
    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = a.getColor(R.styleable.CircleView_circle_color, Color.RED);
        a.recycle();
        init();
    }

    /**
     * 自定义了style的时候调用该函数
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleView);
        mColor = a.getColor(R.styleable.CircleView_circle_color, Color.RED);
        a.recycle();
        init();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingBottom() - getPaddingTop();
        int radius = Math.min(width, height) / 2;
        canvas.drawCircle(getPaddingLeft() + width / 2,
                getPaddingTop() + height / 2, radius, mPaint);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: width" + getWidth());
        Log.d(TAG, "onMeasure: height" + getHeight());
        Log.d(TAG, "onMeasure: widthMode" + MeasureSpec.getMode(widthMeasureSpec));
        Log.d(TAG, "onMeasure: heightMode" + MeasureSpec.getMode(heightMeasureSpec));
        boolean widthIsWrap = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST;
        boolean heightIsWrap = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST;
        Log.d(TAG, "onMeasure: widthModeIsWrap    " + widthIsWrap);
        Log.d(TAG, "onMeasure: heightModeIsWrap    " + heightIsWrap);
        if(widthIsWrap && !heightIsWrap){
            // width is wrap, and height is not
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.AT_MOST
            );
        }else if(heightIsWrap && !widthIsWrap){
            //height is wrap, and width is not
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.AT_MOST
            );
        }else if(heightIsWrap && widthIsWrap){
            //height and width are both wrap
            heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    200,
                    MeasureSpec.AT_MOST
            );
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
