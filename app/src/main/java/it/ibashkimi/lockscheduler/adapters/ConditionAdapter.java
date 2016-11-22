package it.ibashkimi.lockscheduler.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.ibashkimi.lockscheduler.domain.Condition;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ConditionAdapter extends RecyclerView.Adapter<ConditionAdapter.ViewHolder> {
    private List<Condition> conditions;

    public ConditionAdapter(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
