package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.Context;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;


public class WifiConditionView {

    private static final String TAG = WifiConditionView.class.getSimpleName();
    private Context context;
    private ConditionsFragment parent;
    private WifiCondition condition;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private ImageView delete;
    private ViewGroup body;
    private ViewGroup root;
    private WifiCondition wifiCondition;

    public WifiConditionView(ConditionsFragment parent, WifiCondition condition, ViewGroup root, Bundle savedInstanceState) {
        this.parent = parent;
        this.condition = condition;
        this.root = root;

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick() called with: view = [" + view + "]");
                showWifiDialog();
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.wifi_body);
        //recyclerView.setLayoutManager(new FlexboxLayoutManager());
        //adapter = new Adapter(condition.getNetworks());
        //recyclerView.setAdapter(adapter);
        body = recyclerView;
        delete = (ImageView) root.findViewById(R.id.wifi_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiCondition = null;
                showWifiEmpty();
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("wifiBodyVisible", body.getVisibility());
    }

    public void onConditionUpdate() {
        adapter.notifyDataSetChanged();
    }

    public void showWifiEmpty() {
        TransitionManager.beginDelayedTransition(root);
        body.setVisibility(View.GONE);
        delete.setVisibility(View.GONE);
    }

    public void showWifiConnections(List<WifiItem> connections) {
        TransitionManager.beginDelayedTransition(root);
        body.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
    }

    public void showWifiDialog() {
        Log.d(TAG, "showWifiDialog() called");
        /*TransitionManager.beginDelayedTransition(root);
        body.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);*/
        parent.showWifiPicker(new ArrayList<WifiItem>(0));
    }

    public void setWifiCondition(WifiCondition wifiCondition) {
        this.wifiCondition = wifiCondition;
    }

    public WifiCondition getWifiCondition() {
        return wifiCondition;
    }


    static class Adapter extends RecyclerView.Adapter<WifiViewHolder> {
        private List<WifiItem> connections;

        Adapter(List<WifiItem> connections) {
            this.connections = connections;
        }

        @Override
        public WifiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView view = new TextView(parent.getContext());
            return new WifiViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WifiViewHolder holder, int position) {
            holder.textView.setText(connections.get(position).SSID);
        }

        @Override
        public int getItemCount() {
            return connections.size();
        }
    }

    static class WifiViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        WifiViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }
}
