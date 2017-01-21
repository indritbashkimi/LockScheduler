package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import it.ibashkimi.lockscheduler.R;


public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {

    private HelpItem[] items;

    public HelpAdapter(HelpItem[] items) {
        this.items = items;
    }

    @Override
    public HelpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_help, parent, false);
        return new HelpViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HelpViewHolder holder, int position) {
        holder.title.setText(items[position].title);
        holder.content.setText(items[position].content);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }


    static class HelpViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView content;

        HelpViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.help_title);
            content = (TextView) itemView.findViewById(R.id.help_content);
        }
    }


    public static class HelpItem {
        @StringRes
        public final int title;
        @StringRes
        public final int content;

        public HelpItem(int title, int content) {
            this.title = title;
            this.content = content;
        }
    }
}
