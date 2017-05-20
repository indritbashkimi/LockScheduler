package it.ibashkimi.lockscheduler.profiles;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibashkimi.support.utils.SelectableAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.ProfileUtils;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.util.ConditionUtils;

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

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ProfileViewHolder(itemView, getItemListener());
    }

    @Override
    public void onBindViewHolder(final ProfileViewHolder holder, int position) {
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

    static class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Callback listener;

        @BindView(R.id.name)
        TextView name;

        @BindView(R.id.enter_lock_mode)
        TextView enterLock;

        @BindView(R.id.exit_lock_mode)
        TextView exitLock;

        @BindView(R.id.place_summary)
        TextView place;

        @BindView(R.id.days)
        TextView days;

        @BindView(R.id.interval)
        TextView interval;

        @BindView(R.id.wifi_summary)
        TextView wifi;

        @BindView(R.id.place_layout)
        View placeLayout;

        @BindView(R.id.time_layout)
        View timeLayout;

        @BindView(R.id.wifi_layout)
        View wifiLayout;

        @BindView(R.id.cover)
        View cover;

        ProfileViewHolder(View itemView, @NonNull Callback listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.listener = listener;
        }

        void init(@NonNull Profile profile) {
            if (profile.getName() != null && !profile.getName().equals("")) {
                name.setText(profile.getName());
            } else {
                name.setVisibility(View.GONE);
            }
            LockAction enterLockAction = ProfileUtils.getLockAction(profile, true);
            if (enterLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an enter lock action.");
            enterLock.setText(LockAction.lockTypeToString(enterLockAction.getLockType()));
            LockAction exitLockAction = ProfileUtils.getLockAction(profile, false);
            if (exitLockAction == null)
                throw new IllegalArgumentException("Profile " + profile.getName() + " doesn't contain an exit lock action.");
            exitLock.setText(LockAction.lockTypeToString(exitLockAction.getLockType()));
            PlaceCondition placeCondition = ProfileUtils.getPlaceCondition(profile);
            if (placeCondition != null) {
                place.setText(placeCondition.getAddress());
                placeLayout.setVisibility(View.VISIBLE);
            } else {
                placeLayout.setVisibility(View.GONE);
            }
            TimeCondition timeCondition = ProfileUtils.getTimeCondition(profile);
            if (timeCondition != null) {
                days.setText(ConditionUtils.daysToString(itemView.getContext(), timeCondition));
                interval.setText(ConditionUtils.internvalToString(timeCondition.getStartTime(), timeCondition.getEndTime()));
                timeLayout.setVisibility(View.VISIBLE);
            } else {
                timeLayout.setVisibility(View.GONE);
            }
            WifiCondition wifiCondition = ProfileUtils.getWifiCondition(profile);
            if (wifiCondition != null) {
                CharSequence[] wifiList = new CharSequence[wifiCondition.getWifiList().size()];
                for (int i = 0; i < wifiList.length; i++)
                    wifiList[i] = wifiCondition.getWifiList().get(i).getSsid();
                wifi.setText(ConditionUtils.concatenate(wifiList, ", "));
                wifiLayout.setVisibility(View.VISIBLE);
            } else {
                wifiLayout.setVisibility(View.GONE);
            }
        }

        void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.root)
        @Override
        public void onClick(View v) {
            listener.onProfileClick(getAdapterPosition());
        }

        @OnLongClick(R.id.root)
        @Override
        public boolean onLongClick(View v) {
            listener.onProfileLongClick(getAdapterPosition());
            return true;
        }
    }
}
