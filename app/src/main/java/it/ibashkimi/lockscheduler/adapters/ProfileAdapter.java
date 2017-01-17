package it.ibashkimi.lockscheduler.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import it.ibashkimi.lockscheduler.domain.WifiItem;
import it.ibashkimi.support.design.utils.ThemeUtils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends SelectableAdapter<ProfileAdapter.ViewHolder> {

    public interface ClickListener {
        void onItemClicked(int position);

        boolean onItemLongClicked(int position);
    }

    private static final int DEFAULT_MAP_STYLE = GoogleMap.MAP_TYPE_HYBRID;
    private static final int DEFAULT_ITEM_LAYOUT = R.layout.item_profile_1;

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_SPACE = 1;

    private Context mContext;
    private List<Profile> mProfiles;
    private int mCirclePadding;
    private int mMapType;
    private int mItemLayout;
    @ColorInt
    private int mCircleColor;
    @ColorInt
    private int mFillColor;
    private ClickListener clickListener;
    Vibrator vb;// = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    private boolean selectedModeActive;

    @SuppressWarnings("unused")
    public ProfileAdapter(Context context, List<Profile> profiles, @NonNull ClickListener clickListener) {
        this(context, profiles, DEFAULT_ITEM_LAYOUT, DEFAULT_MAP_STYLE, clickListener);
    }

    public ProfileAdapter(Context context, List<Profile> profiles, int itemLayout, int mapType, @NonNull ClickListener clickListener) {
        super();
        this.mContext = context;
        this.mProfiles = profiles;
        this.mCirclePadding = (int) Utils.dpToPx(context, 8);
        this.mItemLayout = itemLayout;
        this.mMapType = mapType;
        this.clickListener = clickListener;
        this.mCircleColor = ThemeUtils.getColorFromAttribute(context, R.attr.colorAccent);
        this.mFillColor = ColorUtils.setAlphaComponent(mCircleColor, 0x25);
        this.mCircleColor = ColorUtils.setAlphaComponent(mCircleColor, 200);
        vb = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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
        return position == mProfiles.size() ? VIEW_TYPE_SPACE : VIEW_TYPE_PROFILE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SPACE) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_space, parent, false);
            return new ViewHolder(itemView, VIEW_TYPE_SPACE, clickListener);
        }
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ViewHolder(itemView, VIEW_TYPE_PROFILE, clickListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (holder.viewType == VIEW_TYPE_SPACE) {
            return;
        }
        //setAnimation(holder.rootView);
        final Profile profile = mProfiles.get(position);

        /*final PopupMenu popup = new PopupMenu(mContext, holder.settingsView);
        popup.getMenuInflater().inflate(R.menu.menu_profile_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        mCallback.onProfileRemoved(profile, holder.getAdapterPosition());
                        return true;
                    default:
                        return true;
                }
            }
        });

        holder.settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.show();
            }
        });*/
        if (holder.rootView instanceof CardView) {
            CardView cardView = (CardView) holder.rootView;
            int color = ContextCompat.getColor(mContext, isSelected(position) ? R.color.card_background_color_selected : R.color.card_background_color);
            cardView.setBackgroundColor(color);
            //cardView.setOnLongClickListener(holder);
        }
        holder.name.setText(profile.getName());
        holder.enterLock.setText(LockMode.lockTypeToString(profile.getLockAction(true).getLockMode().getLockType()));
        holder.exitLock.setText(LockMode.lockTypeToString(profile.getLockAction(false).getLockMode().getLockType()));
       /* holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onProfileClicked(profile);
            }
        });*/

        final PlaceCondition placeCondition = profile.getPlaceCondition();
        if (placeCondition != null) {
            if (holder.place != null) {
                holder.place.setText(placeCondition.getAddress() + "\n" + placeCondition.getRadius() + " m");
            }
            if (holder.mapView != null) {
                holder.mapActive = true;
                holder.mapView.onCreate(null);
                holder.mapView.onResume();
                holder.mapView.onStart();
                holder.mapView.getMapAsync(new OnMapReadyCallback() {
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
                                if (clickListener != null)
                                    clickListener.onItemClicked(position);
                            }
                        });
                        googleMap.addCircle(new CircleOptions()
                                .center(placeCondition.getPlace())
                                .radius(placeCondition.getRadius())
                                .strokeColor(mCircleColor)
                                .strokeWidth(Utils.dpToPx(mContext, 2))
                                .fillColor(mFillColor));
                        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), mCirclePadding);
                                googleMap.moveCamera(cameraUpdate);
                                if (holder.mapCover.getVisibility() == View.VISIBLE) {
                                    Animation fadeAnimation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
                                    fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            holder.mapCover.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });
                                    holder.mapCover.startAnimation(fadeAnimation);
                                }
                            }
                        });
                    }
                });
            }
        } else {
            if (holder.mapContainer != null)
                holder.mapContainer.setVisibility(View.GONE);
            if (holder.placeLayout != null)
                holder.placeLayout.setVisibility(View.GONE);
        }
        if (profile.getTimeCondition() != null) {
            if (holder.weekLayout != null) {
                boolean[] week = profile.getTimeCondition().getDaysActive();
                for (int i = 0; i < 7; i++) {
                    if (week[i] && holder.week[i] != null) {
                        holder.week[i].setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {
            if (holder.weekLayout != null) {
                holder.weekLayout.setVisibility(View.GONE);
            }
        }
        if (profile.getWifiCondition() != null) {
            if (holder.wifiLayout != null && holder.wifiConnections != null) {
                StringBuilder text = new StringBuilder();
                for (WifiItem wifi : profile.getWifiCondition().getNetworks()) {
                    text.append(wifi.SSID).append(" ");
                }
                holder.wifiConnections.setText(text.toString());
            }
        } else {
            if (holder.wifiLayout != null) holder.wifiLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.viewType == VIEW_TYPE_PROFILE && holder.mapActive) {
            holder.mapView.onPause();
            holder.mapView.onStop();
            holder.mapView.onDestroy();
            holder.mapCover.setVisibility(View.VISIBLE);
        }
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        View rootView;
        TextView name;
        MapView mapView;
        TextView enterLock;
        TextView exitLock;
        int viewType;
        View settingsView;
        View mapCover;
        boolean mapActive;
        View mapContainer;
        View wifiLayout;
        View weekLayout;
        TextView wifiConnections;
        View lockLayout;
        ImageView[] week;
        TextView place;
        View placeLayout;
        ClickListener listener;

        ViewHolder(View itemView, int viewType, ClickListener listener) {
            super(itemView);
            this.viewType = viewType;
            this.listener = listener;
            if (viewType == VIEW_TYPE_PROFILE) {
                this.rootView = itemView;
                this.name = (TextView) itemView.findViewById(R.id.name_view);
                this.mapView = (MapView) itemView.findViewById(R.id.mapView);
                this.enterLock = (TextView) itemView.findViewById(R.id.enter_lock_mode);
                this.exitLock = (TextView) itemView.findViewById(R.id.exit_lock_mode);
                this.settingsView = itemView.findViewById(R.id.options_menu);
                this.mapCover = itemView.findViewById(R.id.mapCover);
                this.mapContainer = itemView.findViewById(R.id.map_container);
                this.placeLayout = itemView.findViewById(R.id.place_layout);
                this.place = (TextView) itemView.findViewById(R.id.place);
                this.weekLayout = itemView.findViewById(R.id.week_layout);
                if (weekLayout != null) {
                    week = new ImageView[7];
                    week[0] = (ImageView) weekLayout.findViewById(R.id.monday_circle);
                    week[1] = (ImageView) weekLayout.findViewById(R.id.tuesday_circle);
                    week[2] = (ImageView) weekLayout.findViewById(R.id.wednesday_circle);
                    week[3] = (ImageView) weekLayout.findViewById(R.id.thursday_circle);
                    week[4] = (ImageView) weekLayout.findViewById(R.id.friday_circle);
                    week[5] = (ImageView) weekLayout.findViewById(R.id.saturday_circle);
                    week[6] = (ImageView) weekLayout.findViewById(R.id.sunday_circle);
                }
                wifiLayout = itemView.findViewById(R.id.wifi_layout);
                if (wifiLayout != null) {
                    wifiConnections = (TextView) wifiLayout.findViewById(R.id.wifi_connections);
                }
                lockLayout = itemView.findViewById(R.id.lock_layout);
            }
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(this.getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            //vb.vibrate();
            return listener != null && listener.onItemLongClicked(this.getAdapterPosition());
        }
    }
}
