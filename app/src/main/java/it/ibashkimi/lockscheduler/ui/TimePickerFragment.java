package it.ibashkimi.lockscheduler.ui;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import it.ibashkimi.lockscheduler.ui.recyclerview.TimeIntervalAdapter;

/**
 * Created by indrit on 19/11/16.
 */

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private TextView timeViewer;
    private TimeIntervalAdapter listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void setTimeViewer(TextView textView) {
        this.timeViewer = textView;
    }

    public void setListener(TimeIntervalAdapter listener) {
        this.listener = listener;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        if (listener != null) {
            listener.onTimeSet(hourOfDay, minute);
        }
    }
}
