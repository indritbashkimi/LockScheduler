package it.ibashkimi.lockscheduler.profiles;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.LockMode;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.support.utils.SelectableAdapter;

public class ProfileAdapter extends SelectableAdapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> mProfiles;
    private ProfilesFragment.ProfileItemListener itemListener;
    private @LayoutRes
    int mItemLayout;

    public ProfileAdapter(List<Profile> profiles, @LayoutRes int itemLayout, @NonNull ProfilesFragment.ProfileItemListener listener) {
        this.mProfiles = profiles;
        this.mItemLayout = itemLayout;
        this.itemListener = listener;
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

    public ProfilesFragment.ProfileItemListener getItemListener() {
        return itemListener;
    }

    public void setItemListener(ProfilesFragment.ProfileItemListener itemListener) {
        this.itemListener = itemListener;
    }


    static class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ProfilesFragment.ProfileItemListener listener;
        Context context;

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

        ProfileViewHolder(View itemView, ProfilesFragment.ProfileItemListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.listener = listener;
            this.context = itemView.getContext();
        }

        public void init(Profile profile) {
            name.setText(profile.getName());
            enterLock.setText(LockMode.lockTypeToString(profile.getLockAction(true).getLockMode().getLockType()));
            exitLock.setText(LockMode.lockTypeToString(profile.getLockAction(false).getLockMode().getLockType()));
            PlaceCondition placeCondition = profile.getPlaceCondition();
            if (placeCondition != null) {
                place.setText(placeCondition.getAddress());
                placeLayout.setVisibility(View.VISIBLE);
            }
            TimeCondition timeCondition = profile.getTimeCondition();
            if (timeCondition != null) {
                days.setText("Mon, Tue, Wed, Thu, Fri, Sat, Sun TODO");
                interval.setText("00:00 - 00:00 TODO");
                timeLayout.setVisibility(View.VISIBLE);
            }
            WifiCondition wifiCondition = profile.getWifiCondition();
            if (wifiCondition != null) {
                wifi.setText("TODO");
                wifiLayout.setVisibility(View.VISIBLE);
            }
        }

        public void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public ProfilesFragment.ProfileItemListener getListener() {
            return listener;
        }

        public void setListener(ProfilesFragment.ProfileItemListener listener) {
            this.listener = listener;
        }

        @Override
        @OnClick(R.id.root)
        public void onClick(View v) {
            if (getListener() != null) {
                getListener().onProfileClick(getAdapterPosition(), null);
            }
        }

        @Override
        @OnLongClick(R.id.root)
        public boolean onLongClick(View v) {
            if (getListener() != null) {
                getListener().onProfileLongClick(getAdapterPosition(), null);
            }
            return true;
        }
    }
}
