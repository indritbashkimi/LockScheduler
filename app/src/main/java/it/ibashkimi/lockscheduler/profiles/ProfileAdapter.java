package it.ibashkimi.lockscheduler.profiles;


import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.LockMode;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.ProfileUtils;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.support.utils.SelectableAdapter;

public class ProfileAdapter extends SelectableAdapter<ProfileAdapter.ProfileViewHolder> {
    private static final String TAG = "ProfileAdapter";

    public interface Callback {

        void onProfileClick(int position);

        void onProfileLongClick(int position);
    }

    private List<Profile> mProfiles;
    private Callback mItemListener;
    @LayoutRes
    private int mItemLayout;

    public ProfileAdapter(List<Profile> profiles, @LayoutRes int itemLayout, @NonNull Callback listener) {
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
        Log.d(TAG, "onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
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

    public Callback getItemListener() {
        return mItemListener;
    }

    public void setItemListener(Callback itemListener) {
        this.mItemListener = itemListener;
    }


    static PlaceCondition getPlaceCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.PLACE);
        if (condition != null)
            return (PlaceCondition) condition;
        return null;
    }

    static TimeCondition getTimeCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.TIME);
        if (condition != null)
            return (TimeCondition) condition;
        return null;
    }

    static WifiCondition getWifiCondition(Profile profile) {
        Condition condition = profile.getCondition(Condition.Type.WIFI);
        if (condition != null)
            return (WifiCondition) condition;
        return null;
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

        public void init(@NonNull Profile profile) {
            name.setText(profile.getName());
            enterLock.setText(LockMode.lockTypeToString(ProfileUtils.getLockAction(profile, true).getLockMode().getLockType()));
            exitLock.setText(LockMode.lockTypeToString(ProfileUtils.getLockAction(profile, false).getLockMode().getLockType()));
            PlaceCondition placeCondition = getPlaceCondition(profile);
            if (placeCondition != null) {
                place.setText(placeCondition.getAddress());
                placeLayout.setVisibility(View.VISIBLE);
            }
            TimeCondition timeCondition = getTimeCondition(profile);
            if (timeCondition != null) {
                days.setText("Mon, Tue, Wed, Thu, Fri, Sat, Sun TODO");
                interval.setText("00:00 - 00:00 TODO");
                timeLayout.setVisibility(View.VISIBLE);
            }
            WifiCondition wifiCondition = getWifiCondition(profile);
            if (wifiCondition != null) {
                wifi.setText("TODO");
                wifiLayout.setVisibility(View.VISIBLE);
            }
        }

        public void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        @Override
        @OnClick(R.id.root)
        public void onClick(View v) {
            listener.onProfileClick(getAdapterPosition());
        }

        @Override
        @OnLongClick(R.id.root)
        public boolean onLongClick(View v) {
            listener.onProfileLongClick(getAdapterPosition());
            return true;
        }
    }
}
