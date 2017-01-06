package it.ibashkimi.lockscheduler.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.ConditionAdder;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.adapters.ChipAdapter;
import it.ibashkimi.lockscheduler.domain.Condition;


public class ChipConditionAdder extends RecyclerView implements ConditionAdder {

    private ArrayList<ChipAdapter.ChipItem> chips;

    public ChipConditionAdder(Context context) {
        this(context, null);
    }

    public ChipConditionAdder(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.style.AppTheme_DayNight_Indigo);
    }

    public ChipConditionAdder(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        chips = new ArrayList<>();
    }

    @Override
    public void add(@Condition.Type int conditionType) {
        ChipAdapter.ChipItem chip = null;
        switch (conditionType) {
            case Condition.Type.PLACE:
                chip = new ChipAdapter.ChipItem(Condition.Type.PLACE, R.drawable.ic_wifi, "Place");
                break;
            case Condition.Type.TIME:
                chip = new ChipAdapter.ChipItem(Condition.Type.TIME, R.drawable.ic_wifi, "Time");
                break;
            case Condition.Type.WIFI:
                chip = new ChipAdapter.ChipItem(Condition.Type.WIFI, R.drawable.ic_wifi, "Wifi");
                break;
        }
        chips.add(chip);
    }

    @Override
    public void remove(@Condition.Type int conditionType) {
        for (ChipAdapter.ChipItem chipItem : chips) {
            if (chipItem.id == conditionType) {
                chips.remove(chipItem);
                //chipAdapter.notifyDataSetChanged();
                break;
            }
        }
    }
}
