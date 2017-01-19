package it.ibashkimi.lockscheduler.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;

import it.ibashkimi.lockscheduler.domain.Profile;


public class LayoutSelectorAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> implements ProfileAdapter.ClickListener {
    private int[] layouts;
    private Profile profile;
    private ProfileAdapter.ClickListener listener;

    public LayoutSelectorAdapter(int[] layouts, Profile profile, ProfileAdapter.ClickListener listener) {
        super();
        this.layouts = layouts;
        this.profile = profile;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    @Override
    public ProfileAdapter.ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(viewType, parent, false);
        return new ProfileAdapter.ProfileViewHolder(itemView, GoogleMap.MAP_TYPE_HYBRID, this);
    }

    @Override
    public void onBindViewHolder(ProfileAdapter.ProfileViewHolder holder, int position) {
        //holder.setPos(position);
        holder.init(profile);
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public void onItemClicked(int position, ProfileAdapter.ProfileViewHolder viewholder) {
        /*if (listener != null)
            listener.onItemClicked(position);*/
    }

    @Override
    public boolean onItemLongClicked(int position, ProfileAdapter.ProfileViewHolder viewholder) {
        /*if (listener != null) {
            return listener.onItemLongClicked(position, null);
        }*/
        return false;
    }
}
