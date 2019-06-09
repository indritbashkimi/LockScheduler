package com.ibashkimi.lockscheduler.profiles;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi.SelectableAdapter;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.action.LockAction;
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition;
import com.ibashkimi.lockscheduler.model.condition.PowerCondition;
import com.ibashkimi.lockscheduler.model.condition.TimeCondition;
import com.ibashkimi.lockscheduler.model.condition.WifiCondition;
import com.ibashkimi.lockscheduler.util.ConditionUtils;

import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


class ProfileAdapter extends SelectableAdapter<ProfileAdapter.ProfileViewHolder> {

    interface Callback {

        void onProfileClick(int position);

        void onProfileLongClick(int position);
    }

    private List<Profile> mProfiles;
    private Callback mItemListener;
    @LayoutRes
    private int mItemLayout;

    ProfileAdapter(List<Profile> profiles, @LayoutRes int itemLayout, @NonNull Callback listener) {
        this.mProfiles = profiles;
        this.mItemLayout = itemLayout;
        this.mItemListener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ProfileViewHolder(itemView, getItemListener());
    }

    @Override
    public void onBindViewHolder(@NonNull final ProfileViewHolder holder, int position) {
        final Profile profile = getProfiles().get(position);
        holder.init(profile);
        holder.setSelected(isSelected(position));
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

    public void setData(List<Profile> data) {
        this.mProfiles = data;
        notifyDataSetChanged();
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    private Callback getItemListener() {
        return mItemListener;
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        Callback listener;

        TextView name;

        TextView enterLock;

        TextView exitLock;

        TextView place;

        TextView days;

        TextView interval;

        TextView wifi;

        View placeLayout;

        View timeLayout;

        View wifiLayout;

        View powerLayout;

        TextView powerSummary;

        View cover;

        ProfileViewHolder(View itemView, @NonNull Callback listener) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            enterLock = itemView.findViewById(R.id.enter_lock_mode);
            exitLock = itemView.findViewById(R.id.exit_lock_mode);
            place = itemView.findViewById(R.id.place_summary);
            days = itemView.findViewById(R.id.days);
            interval = itemView.findViewById(R.id.interval);
            wifi = itemView.findViewById(R.id.wifi_summary);
            placeLayout = itemView.findViewById(R.id.locationLayout);
            timeLayout = itemView.findViewById(R.id.time_layout);
            wifiLayout = itemView.findViewById(R.id.wifi_layout);
            powerLayout = itemView.findViewById(R.id.power_layout);
            powerSummary = itemView.findViewById(R.id.power_summary);
            cover = itemView.findViewById(R.id.cover);
            itemView.findViewById(R.id.root).setOnClickListener(view -> listener.onProfileClick(getAdapterPosition()));
            itemView.findViewById(R.id.root).setOnLongClickListener(view -> {
                listener.onProfileLongClick(getAdapterPosition());
                return true;
            });

            this.listener = listener;
        }

        void init(@NonNull Profile profile) {
            if (!profile.getName().equals("")) {
                name.setText(profile.getName());
            } else {
                name.setVisibility(View.GONE);
            }
            LockAction enterLockAction = profile.getEnterExitActions().getEnterActions().getLockAction();
            if (enterLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an enter lock action.");
            enterLock.setText(enterLockAction.getLockMode().getLockType().getValue());
            LockAction exitLockAction = profile.getEnterExitActions().getExitActions().getLockAction();
            if (exitLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an exit lock action.");
            exitLock.setText(exitLockAction.getLockMode().getLockType().getValue());
            PlaceCondition placeCondition = profile.getConditions().getPlaceCondition();
            if (placeCondition != null) {
                place.setText(placeCondition.getAddress());
                placeLayout.setVisibility(View.VISIBLE);
            } else {
                placeLayout.setVisibility(View.GONE);
            }
            TimeCondition timeCondition = profile.getConditions().getTimeCondition();
            if (timeCondition != null) {
                days.setText(ConditionUtils.daysToString(itemView.getContext(), timeCondition));
                interval.setText(ConditionUtils.internvalToString(timeCondition.getStartTime(), timeCondition.getEndTime()));
                timeLayout.setVisibility(View.VISIBLE);
            } else {
                timeLayout.setVisibility(View.GONE);
            }
            WifiCondition wifiCondition = profile.getConditions().getWifiCondition();
            if (wifiCondition != null) {
                CharSequence[] wifiList = new CharSequence[wifiCondition.getWifiList().size()];
                for (int i = 0; i < wifiList.length; i++)
                    wifiList[i] = wifiCondition.getWifiList().get(i).getSsid();
                wifi.setText(ConditionUtils.concatenate(wifiList, ", "));
                wifiLayout.setVisibility(View.VISIBLE);
            } else {
                wifiLayout.setVisibility(View.GONE);
            }
            PowerCondition powerCondition = profile.getConditions().getPowerCondition();
            if (powerCondition != null) {
                powerLayout.setVisibility(View.VISIBLE);
                powerSummary.setText(powerCondition.getPowerConnected() ? R.string.power_connected : R.string.power_disconnected);
            }
        }

        void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
