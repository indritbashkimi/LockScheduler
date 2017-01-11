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

import java.util.Calendar;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class DayLine extends View {

    private static final String TAG = "DayLine";

    private Paint mActivePaint;
    private Paint mInactivePaint;
    private int[][] mSegments;
    private long[][] mPeriods;
    private float mCenterY;
    private int[] mDaySegment = new int[2];
    private int startX;
    private int dayMillis = 86400000;
    private int widthWithPadding;

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
        mPeriods = new long[2][2];
        mSegments = new int[2][2];
        Calendar c = Calendar.getInstance();
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
        mPeriods[1][1] = c.getTimeInMillis();

        for (int i = 0; i < mPeriods.length; i++) {
            long[] period = mPeriods[i];
            Log.d(TAG, "onDraw: " + period[0] + " : " + period[1]);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(period[0]);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            int second = cal.get(Calendar.SECOND);
            mSegments[i][0] = (int) ((hour * 3600000 + minute * 60000 + second * 1000) * (startX + widthWithPadding) / dayMillis);
            mSegments[i][1] = (int) ((mSegments[i][0] + (period[1] - period[0])) * (startX + widthWithPadding) / dayMillis);

            //mSegments[i][0] = startX + mSegments[i][0];
            //mSegments[i][1] = startX + mSegments[i][1];
        }


        canvas.drawLine(mDaySegment[0], mCenterY, mDaySegment[1], mCenterY, mInactivePaint);
        for (int[] mSegment : mSegments) {
            canvas.drawLine(mSegment[0], mCenterY, mSegment[1], mCenterY, mActivePaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float mCenterX = w / 2;
        mCenterY = h / 2;
        widthWithPadding = w - getPaddingLeft() - getPaddingRight();
        int heightWithPadding = h - getPaddingTop() - getPaddingBottom();
        startX = (int) (mCenterX - widthWithPadding / 2);

        mDaySegment[0] = startX;
        mDaySegment[1] = startX + widthWithPadding;
    }

}
