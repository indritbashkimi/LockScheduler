package it.ibashkimi.lockscheduler.adapters;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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
import it.ibashkimi.support.design.utils.ThemeUtils;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    public interface Callback {
        void onProfileRemoved(Profile profile, int position);

        void onProfileClicked(Profile profile);

        void onProfileEnabled(Profile profile, boolean enabled);
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
    private Callback mCallback;
    @ColorInt
    private int mCircleColor;

    @SuppressWarnings("unused")
    public ProfileAdapter(Context context, List<Profile> profiles, @NonNull Callback callback) {
        this(context, profiles, DEFAULT_ITEM_LAYOUT, DEFAULT_MAP_STYLE, callback);
    }

    public ProfileAdapter(Context context, List<Profile> profiles, int itemLayout, int mapType, @NonNull Callback callback) {
        this.mContext = context;
        this.mProfiles = profiles;
        this.mCirclePadding = (int) Utils.dpToPx(context, 8);
        this.mItemLayout = itemLayout;
        this.mMapType = mapType;
        this.mCallback = callback;
        this.mCircleColor = ThemeUtils.getColorFromAttribute(context, R.attr.colorAccent);
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
            return new ViewHolder(itemView, VIEW_TYPE_SPACE);
        }
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(mItemLayout, parent, false);
        return new ViewHolder(itemView, VIEW_TYPE_PROFILE);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (holder.viewType == VIEW_TYPE_SPACE) {
            return;
        }

        final Profile profile = mProfiles.get(position);

        final PopupMenu popup = new PopupMenu(mContext, holder.settingsView);
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
        });

        holder.name.setText(profile.getName());
        holder.enabledView.setChecked(profile.isEnabled());
        holder.enabledView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCallback.onProfileEnabled(profile, isChecked);
            }
        });
        holder.enterLock.setText(LockMode.lockTypeToString(profile.getEnterLockMode().getLockType()));
        holder.exitLock.setText(LockMode.lockTypeToString(profile.getExitLockMode().getLockType()));
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onProfileClicked(profile);
            }
        });

        final PlaceCondition placeCondition = profile.getPlaceCondition();
        if (placeCondition != null) {
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
                            mCallback.onProfileClicked(profile);
                        }
                    });
                    googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .strokeColor(mCircleColor));
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), mCirclePadding);
                            googleMap.moveCamera(cameraUpdate);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.viewType == VIEW_TYPE_PROFILE && holder.mapActive) {
            holder.mapView.onPause();
            holder.mapView.onStop();
            holder.mapView.onDestroy();
        }
    }

    @Override
    public int getItemCount() {
        return mProfiles.size() + 1;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView name;
        CompoundButton enabledView;
        MapView mapView;
        TextView enterLock;
        TextView exitLock;
        int viewType;
        View settingsView;
        boolean mapActive;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            if (viewType == VIEW_TYPE_PROFILE) {
                this.rootView = itemView;
                this.name = (TextView) itemView.findViewById(R.id.name_view);
                this.enabledView = (CompoundButton) itemView.findViewById(R.id.switchView);
                this.mapView = (MapView) itemView.findViewById(R.id.mapView);
                this.enterLock = (TextView) itemView.findViewById(R.id.enter_lock_mode);
                this.exitLock = (TextView) itemView.findViewById(R.id.exit_lock_mode);
                this.settingsView = itemView.findViewById(R.id.options_menu);
            }
        }
    }
}
