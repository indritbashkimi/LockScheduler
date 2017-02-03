package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.ui.TimeActivity;
import it.ibashkimi.lockscheduler.ui.TimeDialogFragment;

/**
 * @author Indrit Bashkimi <indrit.bashkimi@gmail.com>
 */
@Deprecated
public class TimeIntervalAdapter extends RecyclerView.Adapter<TimeIntervalAdapter.ViewHolder> {
    private static final String TAG = "ProfileAdapterImpl";
    private TimeActivity activity;
    private List<int[]> intervals;

    public TimeIntervalAdapter(TimeActivity activity) {
        this(activity, new ArrayList<int[]>());
    }

    public TimeIntervalAdapter(TimeActivity activity, List<int[]> intervals) {
        this.activity = activity;
        this.intervals = intervals;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() viewType = [" + viewType + "]");
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.time_interval, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
        holder.end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog();
            }
        });
    }

    public void onTimeSet(int hourOfDay, int minute) {
        intervals.add(new int[]{hourOfDay, minute});
        notifyDataSetChanged();
    }

    public void showTimePickerDialog() {
        TimeDialogFragment newFragment = new TimeDialogFragment();
        newFragment.setListener(this);
        newFragment.show(activity.getSupportFragmentManager(), "timePicker");
    }

    @Override
    public int getItemCount() {
        return intervals.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView begin;
        TextView end;

        ViewHolder(View itemView) {
            super(itemView);
            begin = (TextView) itemView.findViewById(R.id.begin);
            end = (TextView) itemView.findViewById(R.id.end);
        }
    }
}
