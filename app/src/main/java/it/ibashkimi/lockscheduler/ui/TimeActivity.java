package it.ibashkimi.lockscheduler.ui;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.ui.recyclerview.TimeIntervalAdapter;

@Deprecated
public class TimeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TimeIntervalAdapter(this));
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimeDialogFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
}
