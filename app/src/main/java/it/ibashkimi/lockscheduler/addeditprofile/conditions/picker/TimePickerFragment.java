package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;
import com.touchboarder.weekdaysbuttons.WeekdaysDrawableProvider;

import java.util.ArrayList;
import java.util.Calendar;

import it.ibashkimi.lockscheduler.R;


public class TimePickerFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_time_picker, container, false);
        WeekdaysDataSource wds = new WeekdaysDataSource((AppCompatActivity) getActivity(), R.id.weekdays_stub_frag, rootView)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setNumberOfLetters(3)
                .setDrawableType(WeekdaysDrawableProvider.MW_ROUND)
                .start(new WeekdaysDataSource.Callback() {
                    @Override
                    public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
                        weekdaysDataItem.getCalendarDayId();
                    }

                    @Override
                    public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {

                    }
                });
        return rootView;
    }
}
