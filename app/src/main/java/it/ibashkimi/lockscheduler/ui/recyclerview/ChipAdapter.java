package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.R;


public class ChipAdapter extends RecyclerView.Adapter<ChipAdapter.ChipViewHolder> {

    public interface Callbacks {
        void onChipClicked(ChipItem chipItem);
    }

    private ArrayList<ChipItem> items;
    private Callbacks listener;

    public ChipAdapter(ArrayList<ChipItem> items, Callbacks listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ChipViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.chip_alternative, parent, false);
        return new ChipViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChipViewHolder holder, int position) {
        final ChipItem chipItem = items.get(position);
        holder.icon.setImageResource(chipItem.iconRes);
        holder.title.setText(chipItem.title);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onChipClicked(chipItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ChipItem {
        public int id;
        public int iconRes;
        public String title;

        public ChipItem(int id, int iconRes, String title) {
            this.id = id;
            this.iconRes = iconRes;
            this.title = title;
        }
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup rootView;
        public ImageView icon;
        public TextView title;

        public ChipViewHolder(View itemView) {
            super(itemView);
            rootView = (ViewGroup) itemView;
            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }
}
