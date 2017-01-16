package it.ibashkimi.lockscheduler.views;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by indrit on 15/01/17.
 */

public class Day extends FrameLayout implements Checkable {
    public Day(@NonNull Context context) {
        super(context);
    }

    public Day(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Day(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //View.inflate(context, R.layout.da, this);
    }

    public Day(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
