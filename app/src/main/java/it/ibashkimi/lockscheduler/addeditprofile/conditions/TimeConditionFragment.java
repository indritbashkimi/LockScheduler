package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        } else {
            if (days == null)
                days = new boolean[]{true, true, true, true, true, true, true};
            if (startTime == null) {
                startTime = new TimeCondition.Time(0, 0);
            }
            if (endTime == null) {
                endTime = new TimeCondition.Time(0, 0);
            }

        }
        TimeCondition timeCondition = new TimeCondition(days);
        daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
        startTimeSummary.setText(Utils.formatTime(startTime.hour, startTime.minute));
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
        DaysPickerDialogFragment dialogFragment = new DaysPickerDialogFragment();
        dialogFragment.setDays(days);
        dialogFragment.show(getChildFragmentManager(), "days_picker");
    }

    public void onDaysSelected(boolean[] days) {
        this.days = days.clone();
        TimeCondition timeCondition = new TimeCondition(days);
        daysSummary.setText(ConditionUtils.daysToString(getContext(), timeCondition));
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.time_condition_title);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onDaysSelected(days);
                }
            });
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
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            for (int i = 0; i < 7; i++)
                outState.putBoolean("day_" + i, days[i]);
        }
    }
}
