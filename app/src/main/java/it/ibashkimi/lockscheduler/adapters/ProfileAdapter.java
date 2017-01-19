package it.ibashkimi.lockscheduler.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.lockscheduler.domain.LockMode;
import it.ibashkimi.lockscheduler.domain.PlaceCondition;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.domain.TimeCondition;
import it.ibashkimi.lockscheduler.domain.WifiCondition;
import it.ibashkimi.lockscheduler.domain.WifiItem;
import it.ibashkimi.support.design.utils.ThemeUtils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends SelectableAdapter<ProfileAdapter.BaseViewHolder> {

    private static final String TAG = "ProfileAdapter";

    public interface ClickListener {
        void onItemClicked(int position);

        boolean onItemLongClicked(int position);
    }

    /*public interface ItemTouchHelperAdapter {
        void onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }*/

    private static final int DEFAULT_MAP_STYLE = GoogleMap.MAP_TYPE_HYBRID;
    private static final int DEFAULT_ITEM_LAYOUT = R.layout.item_profile_6;

    private Context mContext;
    private List<Profile> mProfiles;
    private int mItemLayout;
    private ClickListener clickListener;
    private int mapType;

    @SuppressWarnings("unused")
    public ProfileAdapter(Context context, List<Profile> profiles, @NonNull ClickListener clickListener) {
        this(context, profiles, DEFAULT_ITEM_LAYOUT, DEFAULT_MAP_STYLE, clickListener);
    }

    public ProfileAdapter(Context context, List<Profile> profiles, int itemLayout, int mapType, @NonNull ClickListener clickListener) {
        super();
        this.mContext = context;
        this.mProfiles = profiles;
        this.clickListener = clickListener;
        this.mItemLayout = itemLayout;
        this.mapType = mapType;
    }

    public void setData(List<Profile> data) {
        this.mProfiles = data;
        notifyDataSetChanged();
    }

    public List<Profile> getData() {
        return mProfiles;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mProfiles.size() ? -1 : mItemLayout;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == -1) {
            View itemView = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_space, parent, false);
            return new SpaceViewHolder(itemView);
        }
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ViewHolder(itemView, mapType, clickListener);
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, int position) {
        if (getItemViewType(position) == -1) {
            return;
        }

        //holder.setPos(position);
        final Profile profile = mProfiles.get(position);
        Log.d(TAG, "onBindViewHolder: position = " + position + ", profile = " + profile);
        holder.setSelected(isSelected(position));
        holder.init(profile);
    }

    @Override
    public void onViewRecycled(BaseViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }

    @Override
    public int getItemCount() {
        return mProfiles.size() + 1;
    }

    /**
     * Here is the key method to apply the animation
     */
    private void setAnimation(View viewToAnimate) {
        // If the bound view wasn't previously displayed on screen, it's animated
        Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
        viewToAnimate.startAnimation(animation);
    }

    static class SpaceViewHolder extends BaseViewHolder {
        SpaceViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void init(Profile profile) {

        }

        @Override
        public void setSelected(boolean selected) {

        }

        @Override
        public void recycle() {

        }
    }

    static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        int pos;

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void init(Profile profile);

        public abstract void setSelected(boolean selected);

        public abstract void recycle();

        public int getPos() {
            return getLayoutPosition();
        }
    }

    static abstract class SimpleViewHolder extends BaseViewHolder implements View.OnLongClickListener, View.OnClickListener {

        ClickListener listener;

        public SimpleViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void init(Profile profile) {
            setName(profile.getName());
            setLock(profile);
            setPlace(profile.getPlaceCondition());
            setTime(profile.getTimeCondition());
            setWifi(profile.getWifiCondition());
        }

        public abstract void setLock(Profile profile);

        public abstract void setName(String name);

        public abstract void setPlace(PlaceCondition placeCondition);

        public abstract void setTime(TimeCondition timeCondition);

        public abstract void setWifi(WifiCondition wifiCondition);

        public abstract void setSelected(boolean selected);

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: pos = " + getPos());
            if (listener != null) {
                listener.onItemClicked(getPos());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "onLongClick: pos = " + getPos() + ", ViewHolder = " + this);
            return listener != null && listener.onItemLongClicked(getPos());
        }
    }

    static class ViewHolder extends SimpleViewHolder {
        View rootView;
        CardView cardView;
        TextView name;
        MapView mapView;
        TextView enterLock;
        TextView exitLock;
        View mapCover;
        View mapContainer;
        View wifiLayout;
        View weekLayout;
        TextView wifiConnections;
        View lockLayout;
        ImageView[] week;
        TextView place;
        View placeLayout;
        ClickListener listener;
        private int mCirclePadding;
        private int mMapType;
        @ColorInt
        private int mCircleColor;
        @ColorInt
        private int mFillColor;
        private boolean mapActive;

        ViewHolder(View itemView, int mapType, ClickListener listener) {
            super(itemView, listener);

            this.mCirclePadding = (int) Utils.dpToPx(itemView.getContext(), 8);
            this.mMapType = mapType;
            this.mCircleColor = ThemeUtils.getColorFromAttribute(itemView.getContext(), R.attr.colorAccent);
            this.mFillColor = ColorUtils.setAlphaComponent(mCircleColor, 0x25);
            this.mCircleColor = ColorUtils.setAlphaComponent(mCircleColor, 200);

            this.listener = listener;
            rootView = itemView;
            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                cardView = null;
            }
            name = (TextView) itemView.findViewById(R.id.name_view);
            mapContainer = itemView.findViewById(R.id.map_container);
            mapView = (MapView) itemView.findViewById(R.id.mapView);
            mapCover = itemView.findViewById(R.id.mapCover);
            lockLayout = itemView.findViewById(R.id.lock_layout);
            enterLock = (TextView) itemView.findViewById(R.id.enter_lock_mode);
            exitLock = (TextView) itemView.findViewById(R.id.exit_lock_mode);
            placeLayout = itemView.findViewById(R.id.place_layout);
            place = (TextView) itemView.findViewById(R.id.place);
            weekLayout = itemView.findViewById(R.id.week_layout);
            if (weekLayout != null) {
                week = new ImageView[7];
                week[0] = (ImageView) weekLayout.findViewById(R.id.monday_circle);
                week[1] = (ImageView) weekLayout.findViewById(R.id.tuesday_circle);
                week[2] = (ImageView) weekLayout.findViewById(R.id.wednesday_circle);
                week[3] = (ImageView) weekLayout.findViewById(R.id.thursday_circle);
                week[4] = (ImageView) weekLayout.findViewById(R.id.friday_circle);
                week[5] = (ImageView) weekLayout.findViewById(R.id.saturday_circle);
                week[6] = (ImageView) weekLayout.findViewById(R.id.sunday_circle);
            } else {
                week = null;
            }
            wifiLayout = itemView.findViewById(R.id.wifi_layout);
            if (wifiLayout != null) {
                wifiConnections = (TextView) wifiLayout.findViewById(R.id.wifi_connections);
            } else {
                wifiConnections = null;
            }
        }

        @Override
        public void setLock(Profile profile) {
            enterLock.setText(LockMode.lockTypeToString(profile.getLockAction(true).getLockMode().getLockType()));
            exitLock.setText(LockMode.lockTypeToString(profile.getLockAction(false).getLockMode().getLockType()));
        }

        @Override
        public void setSelected(boolean selected) {
            Log.d(TAG, "setSelected() called with: selected = [" + selected + "]");
            if (cardView != null) {
                int color = ContextCompat.getColor(itemView.getContext(), selected ? R.color.card_background_color_selected : R.color.card_background_color);
                cardView.setBackgroundColor(color);
            } else {
                int color = Color.WHITE; // TODO: 17/01/17
                itemView.setBackgroundColor(color);
            }
        }

        protected boolean hasLockLayout() {
            return lockLayout != null;
        }

        protected boolean hasNameField() {
            return name != null;
        }

        protected boolean hasPlaceField() {
            return mapContainer != null || placeLayout != null;
        }

        protected boolean hasTimeFiled() {
            return weekLayout != null;
        }

        protected boolean hasWifiField() {
            return wifiLayout != null;
        }

        @Override
        public void setName(String name) {
            if (hasNameField())
                this.name.setText(name);
        }

        @Override
        public void setPlace(PlaceCondition placeCondition) {
            if (placeCondition == null || !hasPlaceField())
                return;
            if (containsMap()) {
                configureMap(placeCondition);
            } else {
                place.setText(placeCondition.getAddress() + "\n" + placeCondition.getRadius() + " m");
            }
        }

        @Override
        public void setTime(TimeCondition timeCondition) {
            if (timeCondition == null || !hasTimeFiled())
                return;
            weekLayout.setVisibility(View.VISIBLE);
            if (weekLayout != null) {
                boolean[] week = timeCondition.getDaysActive();
                for (int i = 0; i < 7; i++) {
                    if (week[i] && this.week[i] != null) {
                        this.week[i].setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        public void setWifi(WifiCondition wifiCondition) {
            if (!hasWifiField() || wifiCondition == null)
                return;
            wifiLayout.setVisibility(View.VISIBLE);
            StringBuilder text = new StringBuilder();
            for (WifiItem wifi : wifiCondition.getNetworks()) {
                text.append(wifi.SSID).append(" ");
            }
            wifiConnections.setText(text.toString());
        }

        protected void configureMap(final PlaceCondition placeCondition) {
            mapActive = true;
            mapContainer.setVisibility(View.VISIBLE);
            mapView.onCreate(null);
            mapView.onResume();
            mapView.onStart();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    googleMap.setMapType(mMapType);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.getUiSettings().setZoomGesturesEnabled(false);
                    googleMap.getUiSettings().setScrollGesturesEnabled(false);
                    googleMap.getUiSettings().setRotateGesturesEnabled(false);
                    googleMap.getUiSettings().setTiltGesturesEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            if (listener != null)
                                listener.onItemClicked(getPos());
                        }
                    });
                    googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .strokeColor(mCircleColor)
                            .strokeWidth(Utils.dpToPx(itemView.getContext(), 2))
                            .fillColor(mFillColor));
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), mCirclePadding);
                            googleMap.moveCamera(cameraUpdate);
                            if (mapCover.getVisibility() == View.VISIBLE) {
                                Animation fadeAnimation = AnimationUtils.loadAnimation(itemView.getContext(), android.R.anim.fade_out);
                                fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        mapCover.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                mapCover.startAnimation(fadeAnimation);
                            }
                        }
                    });
                }
            });
        }

        private boolean containsMap() {
            return mapView != null;
        }

        @Override
        public void recycle() {
            if (mapActive) {
                mapView.onPause();
                mapView.onStop();
                mapView.onDestroy();
                mapCover.setVisibility(View.VISIBLE);
                mapActive = false;
            }
        }
    }
}
