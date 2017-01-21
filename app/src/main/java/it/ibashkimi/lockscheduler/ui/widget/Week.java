package it.ibashkimi.lockscheduler.ui.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import it.ibashkimi.lockscheduler.R;


public class Week extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    private final boolean[] week = new boolean[7];
    private CheckBox[] weekView = new CheckBox[7];

    public Week(@NonNull Context context) {
        this(context, null);
    }

    public Week(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Week(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Week(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        setOrientation(HORIZONTAL);

        /*int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
        if (firstDayOfWeek == Calendar.SUNDAY) {

        }*/
        for (int i = 0; i < 7; i++) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            CheckBox view = (CheckBox) inflater.inflate(R.layout.day_button, this, true);
            //addView(weekView[i]);
            //weekView[i].setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}
