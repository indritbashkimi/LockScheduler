package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.LockMode;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapterImpl2 extends ProfileAdapter<ProfileAdapterImpl2.ProfileViewHolder> {

    private static final String TAG = "ProfileAdapterImpl";

    private static final int DEFAULT_MAP_STYLE = GoogleMap.MAP_TYPE_HYBRID;
    private static final int DEFAULT_ITEM_LAYOUT = R.layout.item_profile_6;

    private int mItemLayout;

    @SuppressWarnings("unused")
    public ProfileAdapterImpl2(List<Profile> profiles, @NonNull ClickListener clickListener) {
        this(profiles, DEFAULT_ITEM_LAYOUT, clickListener);
    }

    public ProfileAdapterImpl2(List<Profile> profiles, int itemLayout, @NonNull ClickListener clickListener) {
        super(profiles, clickListener);
        this.mItemLayout = itemLayout;
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ProfileViewHolder(itemView, getClickListener());
    }

    @Override
    public void onBindViewHolder(final ProfileViewHolder holder, int position) {
        final Profile profile = getData().get(position);
        holder.setSelected(isSelected(position));
        holder.init(profile);
    }

    @Override
    public void onViewRecycled(ProfileViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }


    static class ProfileViewHolder extends ProfileAdapter.ProfileViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Context context;
        //View rootView;
        //CardView cardView;
        TextView name;
        TextView enterLock;
        TextView exitLock;
        TextView place;
        View cover;

        ProfileViewHolder(View itemView, ClickListener listener) {
            super(itemView, listener);
            this.context = itemView.getContext();
            cover = itemView.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            View rootView = itemView.findViewById(R.id.rootView);
            //cardView = (CardView) itemView.findViewById(R.id.cardView);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
            name = (TextView) itemView.findViewById(R.id.name_view);
            enterLock = (TextView) itemView.findViewById(R.id.enter_lock_mode);
            exitLock = (TextView) itemView.findViewById(R.id.exit_lock_mode);
            place = (TextView) itemView.findViewById(R.id.place);
        }

        public void init(Profile profile) {
            setName(profile.getName());
            setLock(profile.getLockAction(true), profile.getLockAction(false));
            PlaceCondition placeCondition = profile.getPlaceCondition();
            if (placeCondition != null) {
                setPlaceVisibility(true);
                setPlace(placeCondition);
            }
            TimeCondition timeCondition = profile.getTimeCondition();
            if (timeCondition != null) {
                setTimeVisibility(true);
                setTime(timeCondition);
            }
            WifiCondition wifiCondition = profile.getWifiCondition();
            if (wifiCondition != null) {
                setWifiVisibility(true);
                setWifi(wifiCondition);
            }
        }

        private void setPlaceVisibility(boolean visible) {
            int visibility = visible ? View.VISIBLE : View.GONE;
            itemView.findViewById(R.id.place_icon).setVisibility(visibility);
            place.setVisibility(visibility);
        }

        private void setTimeVisibility(boolean visible) {
            int visibility = visible ? View.VISIBLE : View.GONE;
            itemView.findViewById(R.id.time_icon).setVisibility(visibility);
        }

        private void setWifiVisibility(boolean visible) {
            int visibility = visible ? View.VISIBLE : View.GONE;
            itemView.findViewById(R.id.wifi_icon).setVisibility(visibility);
        }

        public void setLock(LockAction enterLockAction, LockAction exitLockAction) {
            enterLock.setText(LockMode.lockTypeToString(enterLockAction.getLockMode().getLockType()));
            exitLock.setText(LockMode.lockTypeToString(exitLockAction.getLockMode().getLockType()));
        }

        @Override
        public void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public void setPlace(PlaceCondition placeCondition) {
            place.setText(placeCondition.getAddress() + "\n" + placeCondition.getRadius() + " m");
        }

        public void setTime(TimeCondition timeCondition) {

        }

        public void setWifi(WifiCondition wifiCondition) {

        }

        @Override
        public void onClick(View v) {
            //Log.d(TAG, "onClick: pos = " + getAdapterPosition());
            if (getListener() != null) {
                getListener().onItemClicked(getAdapterPosition(), this);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            //Log.d(TAG, "onLongClick: pos = " + getAdapterPosition() + ", ProfileViewHolder = " + this);
            return getListener() != null && getListener().onItemLongClicked(getAdapterPosition(), this);
        }

        public void recycle() {

        }
    }
}
