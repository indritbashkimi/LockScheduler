package com.ibashkimi.lockscheduler.addeditprofile.conditions.time;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.model.condition.Time;
import com.ibashkimi.lockscheduler.model.condition.TimeCondition;
import com.ibashkimi.lockscheduler.util.ConditionUtils;
import com.ibashkimi.lockscheduler.util.Utils;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;


public class TimeConditionFragment extends Fragment {

    TextView daysSummary;

    TextView startTimeSummary;

    TextView endTimeSummary;

    private TimeCondition condition;

    private boolean[] days = null;

    private Time startTime = null;

    private Time endTime = null;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_condition_time, container, false);
        daysSummary = root.findViewById(R.id.days_summary);
        startTimeSummary = root.findViewById(R.id.start_time_summary);
        endTimeSummary = root.findViewById(R.id.end_time_summary);
        root.findViewById(R.id.days).setOnClickListener(view -> showWeekDays());
        root.findViewById(R.id.start_time).setOnClickListener(view -> showStartTimePicker());
        root.findViewById(R.id.end_time).setOnClickListener(view -> showEndTimePicker());

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("start_time_hour"))
                startTime = new Time(savedInstanceState.getInt("start_time_hour"), savedInstanceState.getInt("start_time_minute"));
            if (savedInstanceState.containsKey("end_time_hour"))
                endTime = new Time(savedInstanceState.getInt("end_time_hour"), savedInstanceState.getInt("end_time_minute"));
            if (savedInstanceState.containsKey("days_0")) {
                days = new boolean[7];
                for (int i = 0; i < 7; i++)
                    days[i] = savedInstanceState.getBoolean("days_" + i);
            }
        } else {
            if (days == null)
                days = new boolean[]{true, true, true, true, true, true, true};
            if (startTime == null) {
                startTime = new Time(0, 0);
            }
            if (endTime == null) {
                endTime = new Time(0, 0);
            }

        }
        TimeCondition timeCondition = new TimeCondition(days);
        daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
        startTimeSummary.setText(Utils.formatTime(startTime.hour, startTime.minute));
        endTimeSummary.setText(Utils.formatTime(endTime.hour, endTime.minute));


        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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

    public void showWeekDays() {
        DaysPickerDialogFragment dialogFragment = new DaysPickerDialogFragment();
        dialogFragment.setDays(days);
        dialogFragment.show(getChildFragmentManager(), "days_picker");
    }

    public void onDaysSelected(boolean[] days) {
        this.days = days.clone();
        TimeCondition timeCondition = new TimeCondition(days);
        daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
    }

    public void showStartTimePicker() {
        showTimePicker((timePickerDialog, i, i1, i2) -> {
            Time time = new Time(i, i1);
            if (endTime != null && !endTime.isMidnight() && endTime.compareTo(time).isNotHigher()) {
                showIntervalError();
                return;
            }
            startTimeSummary.setText(Utils.formatTime(i, i1));
            startTime = new Time(i, i1);
        });
    }

    private void showIntervalError() {
        Toast.makeText(getContext(), R.string.time_condition_interval_error, Toast.LENGTH_SHORT).show();
    }

    public void showEndTimePicker() {
        showTimePicker((timePickerDialog, i, i1, i2) -> {
            Time time = new Time(i, i1);
            if (startTime != null && !startTime.isMidnight() && startTime.compareTo(time).isNotLower()) {
                showIntervalError();
                return;
            }
            endTimeSummary.setText(Utils.formatTime(i, i1));
            endTime = time;
        });
    }

    private void showTimePicker(TimePickerDialog.OnTimeSetListener callback) {
        TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                callback,
                true);
        timePickerDialog.setThemeDark(isNight());
        timePickerDialog.show(getActivity().getFragmentManager(), "time_picker_dialog");
    }

    public TimeCondition getCondition() {
        if (condition == null) {
            condition = new TimeCondition();
        }
        return condition;
    }

    public boolean isNight() {
        return getResources().getBoolean(R.bool.night_mode);
    }


    public static class DaysPickerDialogFragment extends DialogFragment {

        private TimeConditionFragment listener;
        private boolean[] days;

        public void setDays(boolean[] days) {
            this.days = days;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            Fragment parent = getParentFragment();
            if (parent == null || !(parent instanceof TimeConditionFragment)) {
                throw new ClassCastException("Parent fragment must be TimeConditionFragment.");
            }
            listener = (TimeConditionFragment) parent;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (savedInstanceState != null) {
                days = new boolean[7];
                for (int i = 0; i < 7; i++)
                    days[i] = savedInstanceState.getBoolean("day_" + i);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(R.string.time_condition_days);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> listener.onDaysSelected(days));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setMultiChoiceItems(R.array.days_of_week, days, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    days[which] = isChecked;
                }
            });
            return builder.create();
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            for (int i = 0; i < 7; i++)
                outState.putBoolean("day_" + i, days[i]);
        }
    }
}