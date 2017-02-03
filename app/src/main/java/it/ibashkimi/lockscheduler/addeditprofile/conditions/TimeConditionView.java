package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.Context;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.TimeCondition;


public class TimeConditionView implements CompoundButton.OnCheckedChangeListener {
    private Context context;
    private TimeCondition condition;
    private ViewGroup root;
    private View body;
    private TextView title;
    private ImageView delete;
    private CheckBox[] days = new CheckBox[7];
    private boolean shown;
    private ConditionsFragment parent;
    private TimeCondition timeCondition;

    public TimeConditionView(ConditionsFragment parent, TimeCondition condition, ViewGroup timeLayout, Bundle savedInstanceState) {
        this.context = parent.getContext();
        this.parent = parent;
        this.condition = condition;
        this.root = timeLayout;
        this.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });
        this.title = (TextView) root.findViewById(R.id.time_title);
        this.delete = (ImageView) root.findViewById(R.id.time_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeCondition = null;
                showTimeEmpty();
            }
        });
        this.body = root.findViewById(R.id.time_body);
        days = new CheckBox[7];
        days[0] = (CheckBox) timeLayout.findViewById(R.id.day_0);
        days[1] = (CheckBox) timeLayout.findViewById(R.id.day_1);
        days[2] = (CheckBox) timeLayout.findViewById(R.id.day_2);
        days[3] = (CheckBox) timeLayout.findViewById(R.id.day_3);
        days[4] = (CheckBox) timeLayout.findViewById(R.id.day_4);
        days[5] = (CheckBox) timeLayout.findViewById(R.id.day_5);
        days[6] = (CheckBox) timeLayout.findViewById(R.id.day_6);
        for (CheckBox day : days)
            day.setOnCheckedChangeListener(this);
        if (savedInstanceState != null) {
            boolean visible = savedInstanceState.getBoolean("timeBodyVisible", false);
            body.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("timeBodyVisible", body.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int index = -1;
        switch (buttonView.getId()) {
            case R.id.day_0:
                index = 0;
                break;
            case R.id.day_1:
                index = 1;
                break;
            case R.id.day_2:
                index = 2;
                break;
            case R.id.day_3:
                index = 3;
                break;
            case R.id.day_4:
                index = 4;
                break;
            case R.id.day_5:
                index = 5;
                break;
            case R.id.day_6:
                index = 6;
                break;
        }
        timeCondition.getDaysActive()[index] = isChecked;
        showTime(timeCondition.getDaysActive());
    }

    public void showTimeEmpty() {
        TransitionManager.beginDelayedTransition(root);
        this.body.setVisibility(View.GONE);
        this.delete.setVisibility(View.GONE);
        this.shown = false;
    }

    public void showTime(boolean[] daysArray) {
        TransitionManager.beginDelayedTransition(root);
        this.body.setVisibility(View.VISIBLE);
        this.delete.setVisibility(View.VISIBLE);
        //this.title
        for (int i = 0; i < 7; i++) {
            days[i].setChecked(daysArray[i]);
        }
        this.shown = true;
    }

    public void showTimePicker() {
        timeCondition = new TimeCondition("Time", new boolean[7]);
        showTime(timeCondition.getDaysActive());
        //parent.showTimePicker();
    }

    public void setTimeCondition(TimeCondition timeCondition) {
        this.timeCondition = timeCondition;
        showTime(timeCondition.getDaysActive());
    }

    public TimeCondition getTimeCondition() {
        return timeCondition;
    }
}
