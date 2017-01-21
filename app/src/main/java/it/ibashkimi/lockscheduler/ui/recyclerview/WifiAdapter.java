package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.support.utils.ThemeUtils;


public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.ViewHolder> {

    public interface Callbacks {
        void onWifiItemClicked(WifiItem item);

        void onWifiItemRemoved(WifiItem item);
    }

    private List<WifiItem> wifiList;
    private Callbacks listener;
    private boolean showCancel;

    public WifiAdapter(List<WifiItem> wifiList, boolean showCancel, Callbacks listener) {
        this.wifiList = wifiList;
        this.listener = listener;
        this.showCancel = showCancel;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_wifi_connection, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final WifiItem wifiItem = wifiList.get(position);
        holder.title.setText(wifiItem.SSID);
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onWifiItemClicked(wifiItem);
                }
            }
        });
        if (showCancel) {
            holder.cancel.setVisibility(View.VISIBLE);
            holder.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onWifiItemRemoved(wifiItem);
                    }
                }
            });
        } else {
            holder.cancel.setVisibility(View.GONE);
        }
        /*if (position == getItemCount() -1) {
            holder.divider.setVisibility(View.GONE);
        }*/
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView title;
        ImageView cancel;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            cancel = (ImageView) itemView.findViewById(R.id.cancel);
            cancel.setColorFilter(ThemeUtils.getColorFromAttribute(itemView.getContext(), android.R.attr.textColorPrimary));
        }
    }
}
