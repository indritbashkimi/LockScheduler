package it.ibashkimi.lockscheduler.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.utils.ThemeUtils;


public class DayLine extends View {

    private static final String TAG = "DayLine";

    private Paint mActivePaint;
    private Paint mInactivePaint;
    private Paint mTextPaint;
    private double[][] mSegments;
    private double[][] mPeriods;
    private float mCenterX;
    private float mCenterY;
    private int[] mDaySegment = new int[2];
    private int startX;
    private double dayMillis = 86400000;
    private double widthWithPadding;
    private float textWidth;

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

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                12, getResources().getDisplayMetrics()));
        mTextPaint.setColor(ThemeUtils.getColorFromAttribute(context, android.R.attr.textColorSecondary));
        textWidth = mTextPaint.measureText("00", 0, 2);
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
        double width = widthWithPadding - textWidth;
        mSegments[0][0] = mSegments[0][0] * (width / dayMillis);
        mSegments[0][1] = mSegments[0][1] * (width / dayMillis);

        mSegments[1][0] = mSegments[1][0] * (width / dayMillis);
        mSegments[1][1] = mSegments[1][1] * (width / dayMillis);

        Log.d(TAG, "onDraw: mSegments[0][0]=" + mSegments[0][0] + ", mSegments[0][1]" + mSegments[0][1]);


        canvas.drawLine(mDaySegment[0] + textWidth / 2, mCenterY, mDaySegment[1] - textWidth / 2, mCenterY, mInactivePaint);
        for (double[] mSegment : mSegments) {
            canvas.drawLine((int) mSegment[0] + textWidth / 2, mCenterY, (int) mSegment[1], mCenterY, mActivePaint);
        }

        canvas.drawText("00", 0 + textWidth / 2, mCenterY - 20, mTextPaint);
        canvas.drawText("06", mCenterX / 2 - textWidth / 2, mCenterY - 20, mTextPaint);
        canvas.drawText("12", mCenterX - textWidth / 2, mCenterY - 20, mTextPaint);
        canvas.drawText("18", 3 * mCenterX / 2 - textWidth / 2, mCenterY - 20, mTextPaint);
        canvas.drawText("24", (float) widthWithPadding - textWidth / 2, mCenterY - 20, mTextPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged() called with: w = [" + w + "], h = [" + h + "]");
        mCenterX = w / 2;
        mCenterY = h / 2;
        widthWithPadding = w - getPaddingLeft() - getPaddingRight();
        int heightWithPadding = h - getPaddingTop() - getPaddingBottom();
        startX = (int) (mCenterX - widthWithPadding / 2);

        mDaySegment[0] = startX;
        mDaySegment[1] = (int) (startX + widthWithPadding);
        Log.d(TAG, "onSizeChanged: mCenterY=" + mCenterY + ", widthWithpadding=" + widthWithPadding + ", startx=" + startX);
    }

}
