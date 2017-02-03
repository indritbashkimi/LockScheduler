package it.ibashkimi.lockscheduler.profiles;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.LockMode;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.lockscheduler.util.Utils;
import it.ibashkimi.support.utils.ThemeUtils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapterImpl extends ProfileAdapter<ProfileAdapterImpl.ProfileViewHolder> {

    private static final String TAG = "ProfileAdapterImpl";

    private static final int DEFAULT_MAP_STYLE = GoogleMap.MAP_TYPE_HYBRID;
    private static final int DEFAULT_ITEM_LAYOUT = R.layout.item_profile_6;

    private int mItemLayout;
    private int mapType;

    @SuppressWarnings("unused")
    public ProfileAdapterImpl(Context context, List<Profile> profiles, @NonNull ProfilesFragment.ProfileItemListener listener) {
        this(context, profiles, DEFAULT_ITEM_LAYOUT, DEFAULT_MAP_STYLE, listener);
    }

    public ProfileAdapterImpl(Context context, List<Profile> profiles, int itemLayout, int mapType, @NonNull ProfilesFragment.ProfileItemListener listener) {
        super(profiles, listener);
        this.mItemLayout = itemLayout;
        this.mapType = mapType;
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ProfileViewHolder(itemView, mapType, getItemListener());
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
        FlexboxLayout wifiFlexbox;
        int mCirclePadding;
        int mMapType;
        @ColorInt
        int mCircleColor;
        @ColorInt
        int mFillColor;
        boolean mapActive;
        View cover;

        ProfileViewHolder(View itemView, int mapType, ProfilesFragment.ProfileItemListener listener) {
            super(itemView, listener);
            this.mCirclePadding = (int) Utils.dpToPx(itemView.getContext(), 8);
            this.mMapType = mapType;
            this.mCircleColor = ThemeUtils.getColorFromAttribute(itemView.getContext(), R.attr.colorAccent);
            this.mFillColor = ColorUtils.setAlphaComponent(mCircleColor, 0x25);
            this.mCircleColor = ColorUtils.setAlphaComponent(mCircleColor, 200);

            this.context = itemView.getContext();
            rootView = itemView.findViewById(R.id.rootView);
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            cover = itemView.findViewById(R.id.cover);
            cover.setVisibility(View.GONE);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);
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
            wifiFlexbox = (FlexboxLayout) itemView.findViewById(R.id.wifi_items);
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

        public void init(Profile profile) {
            setName(profile.getName());
            setLock(profile);
            setPlace(profile.getPlaceCondition());
            setTime(profile.getTimeCondition());
            setWifi(profile.getWifiCondition());
        }

        public void setLock(Profile profile) {
            enterLock.setText(LockMode.lockTypeToString(profile.getLockAction(true).getLockMode().getLockType()));
            exitLock.setText(LockMode.lockTypeToString(profile.getLockAction(false).getLockMode().getLockType()));
        }

        @Override
        public void setSelected(boolean selected) {
            cover.setVisibility(selected ? View.VISIBLE : View.GONE);
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

        public void setName(String name) {
            if (hasNameField())
                this.name.setText(name);
        }

        public void setPlace(PlaceCondition placeCondition) {
            if (placeCondition == null || !hasPlaceField())
                return;
            if (containsMap()) {
                configureMap(placeCondition);
            } else {
                place.setText(placeCondition.getAddress() + "\n" + placeCondition.getRadius() + " m");
            }
        }

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

        public void setWifi(WifiCondition wifiCondition) {
            if (!hasWifiField() || wifiCondition == null)
                return;
            wifiLayout.setVisibility(View.VISIBLE);
            //wifiFlexbox.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);
            FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
            int margin = (int) Utils.dpToPx(context, 4);
            lp.setMargins(margin, 0, margin, margin);
            for (WifiItem wifi: wifiCondition.getNetworks()) {
                TextView wifiItem = (TextView) View.inflate(context, R.layout.wifi_item, null);
                wifiItem.setText(wifi.SSID);
                wifiItem.setLayoutParams(lp);
                wifiFlexbox.addView(wifiItem);
            }
           /* View view = flexboxLayout.getChildAt(0);
            FlexboxLayout.LayoutParams lp = (FlexboxLayout.LayoutParams) view.getLayoutParams();
            lp.order = -1;
            lp.flexGrow = 2;
            view.setLayoutParams(lp);*/
            /*StringBuilder text = new StringBuilder();
            for (WifiItem wifi : wifiCondition.getNetworks()) {
                text.append(wifi.SSID).append(" ");
            }
            wifiConnections.setText(text.toString());*/
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
                            if (getListener() != null)
                                getListener().onProfileClick(getLayoutPosition(), null);
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
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), mCirclePadding);
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
        public void onClick(View v) {
            if (getListener() != null) {
                getListener().onProfileClick(getAdapterPosition(), null);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getListener() != null) {
                getListener().onProfileLongClick(getAdapterPosition(), null);
            }
            return true;
        }

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
