package it.ibashkimi.lockscheduler.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class DayLine extends View {

    private static final String TAG = "DayLine";

    private Paint mActivePaint;
    private Paint mInactivePaint;
    private double[][] mSegments;
    private double[][] mPeriods;
    private float mCenterY;
    private int[] mDaySegment = new int[2];
    private int startX;
    private double dayMillis = 86400000;
    private double widthWithPadding;

    public DayLine(Context context) {
        this(context, null);
    }

    public DayLine(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.AppTheme_DayNight);
    }

    public DayLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DayLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mActivePaint = new Paint();
        mActivePaint.setAntiAlias(true);
        mActivePaint.setColor(ThemeUtils.getColorFromAttribute(context, R.attr.colorAccent));
        mActivePaint.setStrokeWidth(ThemeUtils.dpToPx(context, 2));

        mInactivePaint = new Paint();
        mInactivePaint.setAntiAlias(true);
        //mInactivePaint.setColor(ThemeUtils.getColorFromAttribute(context, R.attr.colorAccent));
        mInactivePaint.setStrokeWidth(ThemeUtils.dpToPx(context, 2));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPeriods = new double[2][2];
        mSegments = new double[2][2];
        /*Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        mPeriods[0][0] = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 3);
        mPeriods[0][1] = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 12);
        mPeriods[1][0] = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 21);
        mPeriods[1][1] = c.getTimeInMillis();*/

        mSegments[0][0] = 10800000;
        mSegments[0][1] = 36000000;
        mSegments[1][0] = 57600000;
        mSegments[1][1] = 72000000;

        mSegments[0][0] = mSegments[0][0] * (widthWithPadding / dayMillis);
        mSegments[0][1] = mSegments[0][1] * (widthWithPadding / dayMillis);

        mSegments[1][0] = mSegments[1][0] * (widthWithPadding / dayMillis);
        mSegments[1][1] = mSegments[1][1] * (widthWithPadding / dayMillis);

        Log.d(TAG, "onDraw: mSegments[0][0]=" + mSegments[0][0] + ", mSegments[0][1]" + mSegments[0][1]);


        canvas.drawLine(mDaySegment[0], mCenterY, mDaySegment[1], mCenterY, mInactivePaint);
        for (double[] mSegment : mSegments) {
            canvas.drawLine((int) mSegment[0], mCenterY, (int) mSegment[1], mCenterY, mActivePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "]");
        float mCenterX = w / 2;
        mCenterY = h / 2;
        widthWithPadding = w - getPaddingLeft() - getPaddingRight();
        int heightWithPadding = h - getPaddingTop() - getPaddingBottom();
        startX = (int) (mCenterX - widthWithPadding / 2);

        mDaySegment[0] = startX;
        mDaySegment[1] = (int) (startX + widthWithPadding);
        Log.d(TAG, "onSizeChanged: mCenterY=" + mCenterY + ", widthWithpadding=" + widthWithPadding + ", startx=" + startX);
    }

}
