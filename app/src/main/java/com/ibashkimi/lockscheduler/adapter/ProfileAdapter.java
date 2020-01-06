package com.ibashkimi.lockscheduler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.wifi.SelectableAdapter;
import com.ibashkimi.lockscheduler.databinding.ItemProfileBinding;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.action.LockAction;
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition;
import com.ibashkimi.lockscheduler.model.condition.PowerCondition;
import com.ibashkimi.lockscheduler.model.condition.TimeCondition;
import com.ibashkimi.lockscheduler.model.condition.WifiCondition;
import com.ibashkimi.lockscheduler.util.ConditionUtils;

import java.util.List;


public class ProfileAdapter extends SelectableAdapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> mProfiles;
    private Callback mItemListener;

    public ProfileAdapter(List<Profile> profiles, @NonNull Callback listener) {
        this.mProfiles = profiles;
        this.mItemListener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileBinding binding = ItemProfileBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProfileViewHolder(binding, getItemListener());
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

    public interface Callback {

        void onProfileClick(int position);

        void onProfileLongClick(int position);
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ItemProfileBinding binding;

        Callback listener;

        ProfileViewHolder(ItemProfileBinding binding, @NonNull Callback listener) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(view -> listener.onProfileClick(getAdapterPosition()));
            binding.getRoot().setOnLongClickListener(view -> {
                listener.onProfileLongClick(getAdapterPosition());
                return true;
            });

            this.listener = listener;
        }

        void init(@NonNull Profile profile) {
            if (!profile.getName().equals("")) {
                binding.name.setText(profile.getName());
            } else {
                binding.name.setVisibility(View.GONE);
            }
            LockAction enterLockAction = profile.getEnterExitActions().getEnterActions().getLockAction();
            if (enterLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an enter lock action.");
            binding.enterLockMode.setText(enterLockAction.getLockMode().getLockType().getValue());
            LockAction exitLockAction = profile.getEnterExitActions().getExitActions().getLockAction();
            if (exitLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an exit lock action.");
            binding.exitLockMode.setText(exitLockAction.getLockMode().getLockType().getValue());
            PlaceCondition placeCondition = profile.getConditions().getPlaceCondition();
            if (placeCondition != null) {
                binding.placeSummary.setText(placeCondition.getAddress());
                binding.locationLayout.setVisibility(View.VISIBLE);
            } else {
                binding.locationLayout.setVisibility(View.GONE);
            }
            TimeCondition timeCondition = profile.getConditions().getTimeCondition();
            if (timeCondition != null) {
                binding.days.setText(ConditionUtils.daysToString(itemView.getContext(), timeCondition));
                binding.interval.setText(ConditionUtils.internvalToString(timeCondition.getStartTime(), timeCondition.getEndTime()));
                binding.timeLayout.setVisibility(View.VISIBLE);
            } else {
                binding.timeLayout.setVisibility(View.GONE);
            }
            WifiCondition wifiCondition = profile.getConditions().getWifiCondition();
            if (wifiCondition != null) {
                CharSequence[] wifiList = new CharSequence[wifiCondition.getWifiList().size()];
                for (int i = 0; i < wifiList.length; i++)
                    wifiList[i] = wifiCondition.getWifiList().get(i).getSsid();
                binding.wifiSummary.setText(ConditionUtils.concatenate(wifiList, ", "));
                binding.wifiLayout.setVisibility(View.VISIBLE);
            } else {
                binding.wifiLayout.setVisibility(View.GONE);
            }
            PowerCondition powerCondition = profile.getConditions().getPowerCondition();
            if (powerCondition != null) {
                binding.powerLayout.setVisibility(View.VISIBLE);
                binding.powerSummary.setText(powerCondition.getPowerConnected() ? R.string.power_connected : R.string.power_disconnected);
            }
        }

        void setSelected(boolean selected) {
            binding.cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }
    }
}
