package it.ibashkimi.lockscheduler.addeditprofile;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.support.utils.SelectableAdapter;


public class WifiAdapter extends SelectableAdapter<WifiAdapter.ViewHolder> {

    public interface Callbacks {

        void onWifiItemSelectChange(WifiItem item, boolean selected);
    }

    private List<WifiItem> wifiList;

    private Callbacks listener;

    public WifiAdapter(List<WifiItem> wifiList, Callbacks listener) {
        super();
        this.wifiList = wifiList;
        this.listener = listener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_wifi_connection, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final WifiItem wifiItem = wifiList.get(position);
        holder.title.setText(wifiItem.SSID);
        //holder.checkBox.setChecked(wifiItem.selected);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.onWifiItemSelectChange(wifiItem, isChecked);
                }
            }
        });
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.checkBox.performClick();
            }
        });
    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView title;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            rootView = itemView;
            title = (TextView) itemView.findViewById(R.id.title);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
