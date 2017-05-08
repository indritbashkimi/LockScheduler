package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.util.ConditionUtils;
import it.ibashkimi.lockscheduler.util.Utils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@gmail.com)
 */
public class TimeConditionFragment extends Fragment {

    @BindView(R.id.days_summary)
    TextView daysSummary;

    @BindView(R.id.start_time_summary)
    TextView startTimeSummary;

    @BindView(R.id.end_time_summary)
    TextView endTimeSummary;

    private TimeCondition condition;

    private boolean[] days = null;

    private TimeCondition.Time startTime = null;

    private TimeCondition.Time endTime = null;

    public void setData(TimeCondition condition) {
        this.condition = condition;
        this.days = condition.getDaysActive();
        this.startTime = condition.getStartTime();
        this.endTime = condition.getEndTime();
    }

    public TimeCondition assembleCondition() {
        TimeCondition result = new TimeCondition();
        if (days != null)
            result.setDaysActive(days);
        if (startTime != null)
            result.setStartTime(startTime);
        if (endTime != null)
            result.setEndTime(endTime);
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_condition_time, container, false);
        ButterKnife.bind(this, root);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("start_time_hour"))
                startTime = new TimeCondition.Time(savedInstanceState.getInt("start_time_hour"), savedInstanceState.getInt("start_time_minute"));
            if (savedInstanceState.containsKey("end_time_hour"))
                endTime = new TimeCondition.Time(savedInstanceState.getInt("end_time_hour"), savedInstanceState.getInt("end_time_minute"));
            if (savedInstanceState.containsKey("days_0")) {
                days = new boolean[7];
                for (int i = 0; i < 7; i++)
                    days[i] = savedInstanceState.getBoolean("days_" + i);
            }
        }
        if (days != null) {
            TimeCondition timeCondition = new TimeCondition(days);
            daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
        }
        if (startTime != null)
            startTimeSummary.setText(Utils.formatTime(startTime.hour, startTime.minute));
        if (endTime != null)
            endTimeSummary.setText(Utils.formatTime(endTime.hour, endTime.minute));


        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (startTime != null) {
            outState.putInt("start_time_hour", startTime.hour);
            outState.putInt("start_time_minute", startTime.minute);
        }
        if (endTime != null) {
            outState.putInt("end_time_hour", endTime.hour);
            outState.putInt("end_time_minute", endTime.minute);
        }
        if (days != null) {
            for (int i = 0; i < 7; i++)
                outState.putBoolean("days_" + i, days[i]);
        }
    }

    @OnClick(R.id.days)
    public void showWeekDays() {
        if (days == null)
            days = new boolean[]{true, true, true, true, true, true, true};
        int size = 0;
        for (boolean day : days)
            if (day) size++;
        Integer[] selectedIndices = new Integer[size];
        int j = 0;
        for (int i = 0; i < days.length; i++) {
            if (days[i]) {
                selectedIndices[j] = i;
                j++;
            }
        }
        new MaterialDialog.Builder(getContext())
                .title(R.string.time_condition_title)
                .items(R.array.days_of_week)
                .itemsCallbackMultiChoice(selectedIndices, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        days = new boolean[]{false, false, false, false, false, false, false};
                        for (Integer i : which)
                            days[i] = true;
                        TimeCondition timeCondition = new TimeCondition(days);
                        daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
                        return true;
                    }
                })
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .show();
    }

    @OnClick(R.id.start_time)
    public void showStartTimePicker() {
        showTimePicker(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog timePickerDialog, int i, int i1, int i2) {
                TimeCondition.Time time = new TimeCondition.Time(i, i1);
                if (endTime != null && !endTime.isMidnight() && endTime.compareTo(time).isNotHigher()) {
                    showIntervalError();
                    return;
                }
                startTimeSummary.setText(Utils.formatTime(i, i1));
                startTime = new TimeCondition.Time(i, i1);
            }
        });
    }

    private void showIntervalError() {
        Toast.makeText(getContext(), R.string.time_condition_interval_error, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.end_time)
    public void showEndTimePicker() {
        showTimePicker(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePickerDialog timePickerDialog, int i, int i1, int i2) {
                TimeCondition.Time time = new TimeCondition.Time(i, i1);
                if (startTime != null && !startTime.isMidnight() && startTime.compareTo(time).isNotLower()) {
                    showIntervalError();
                    return;
                }
                endTimeSummary.setText(Utils.formatTime(i, i1));
                endTime = time;
            }
        });
    }

    private void showTimePicker(TimePickerDialog.OnTimeSetListener callback) {
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                callback,
                true);
        timePickerDialog.show(getActivity().getFragmentManager(), "time_picker_dialog");
    }

    public TimeCondition getCondition() {
        if (condition == null) {
            condition = new TimeCondition();
        }
        return condition;
    }
}
