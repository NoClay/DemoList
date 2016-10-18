package noclay.testview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by 82661 on 2016/10/18.
 */

public class MyImageView extends LinearLayout {
    private static final String TAG = "MyImageView";
    private TextView textView;
    private ImageView imageView;
    private LinearLayout linearLayout;
    private static Context context;

    public MyImageView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyImageView);
        textView.setText(array.getText(R.styleable.MyImageView_title));
        imageView.setImageDrawable(getResources().
                getDrawable(array.getResourceId(R.styleable.MyImageView_image, R.drawable.image)));
        array.recycle();
    }

    public MyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyImageView);
        textView.setText(array.getText(R.styleable.MyImageView_title));
        imageView.setImageDrawable(getResources().
                getDrawable(array.getResourceId(R.styleable.MyImageView_image, R.drawable.image)));
        array.recycle();
    }

    public void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.my_iamge_view, this, true);
        textView = (TextView) findViewById(R.id.image_view_title);
        imageView = (ImageView) findViewById(R.id.image_view_image);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = 0;
        int measuredHeight = 0;
        final int childCount = getChildCount();
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthSpaceSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpaceMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpaceSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpaceMode = MeasureSpec.getMode(heightMeasureSpec);
        Log.d(TAG, "onMeasure: width = " + widthSpaceSize);
        Log.d(TAG, "onMeasure: height = " + heightSpaceSize);
        if(widthSpaceMode == MeasureSpec.EXACTLY && heightSpaceMode == MeasureSpec.EXACTLY){
            //在有具体数值的时候调整子项的大小
            if(7 * widthSpaceSize <= 5 * heightSpaceSize){//规定长宽比例为5:7
                setMeasure(widthSpaceSize);
            }else{
                setMeasure(heightSpaceSize);
            }
        }
    }

    private void setMeasure(int value){
        Log.d(TAG, "onMeasure: true");
        LayoutParams lp = (LayoutParams) imageView.getLayoutParams();
        lp.width = value * 4 / 5 ;
        lp.height = value * 4 / 5;
        lp.bottomMargin = lp.topMargin = value * 1 / 10;
        imageView.setLayoutParams(lp);

        lp = (LayoutParams) textView.getLayoutParams();
        lp.width = value;
        lp.height = value * 2 / 5;
        textView.setLayoutParams(lp);
        textView.setTextSize(px2sp(lp.height / 2));
    }

    public static int px2sp(float value) {
        final float scale =context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (value / scale + 0.5f);
    }
}
