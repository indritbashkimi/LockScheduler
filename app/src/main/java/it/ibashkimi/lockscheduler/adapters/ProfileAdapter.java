package it.ibashkimi.lockscheduler.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import it.ibashkimi.lockscheduler.MainActivity;
import it.ibashkimi.lockscheduler.ProfileActivity;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.lockscheduler.domain.LockMode;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_SPACE = 1;

    private static final String TAG = "ProfileAdapter";
    private Activity activity;
    private List<Profile> profiles;
    private int circlePadding;
    private int mapType;

    public ProfileAdapter(Activity activity, List<Profile> profiles) {
        this(activity, profiles, GoogleMap.MAP_TYPE_HYBRID);
    }

    public ProfileAdapter(Activity activity, List<Profile> profiles, int mapType) {
        this.activity = activity;
        this.profiles = profiles;
        this.circlePadding = (int) Utils.dpToPx(activity, 8);
        this.mapType = mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public int getMapType() {
        return mapType;
    }

    @Override
    public int getItemViewType(int position) {
        return position == profiles.size() ? VIEW_TYPE_SPACE : VIEW_TYPE_PROFILE;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() viewType = [" + viewType + "]");
        if (viewType == VIEW_TYPE_SPACE) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_space, parent, false);
            return new ViewHolder(itemView, VIEW_TYPE_SPACE);
        }
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_profile2, parent, false);
        return new ViewHolder(itemView, VIEW_TYPE_PROFILE);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (holder.viewType == VIEW_TYPE_SPACE) {
            return;
        }
        final Profile profile = profiles.get(position);
        holder.name.setText(profile.getName());
        holder.enabledView.setChecked(profile.isEnabled());
        holder.enabledView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.setEnabled(isChecked);
            }
        });
        holder.enterLock.setText(LockMode.lockTypeToString(profile.getEnterLockMode().getLockType()));
        if (profile.getEnterLockMode().getLockType() == LockMode.LockType.PASSWORD) {
            holder.enterLock.append(": " + hidePassword(profile.getEnterLockMode().getPassword()));
        } else if (profile.getEnterLockMode().getLockType() == LockMode.LockType.PIN) {
            holder.enterLock.append(": " + hidePassword(profile.getEnterLockMode().getPin()));
        }
        holder.exitLock.setText(LockMode.lockTypeToString(profile.getExitLockMode().getLockType()));
        if (profile.getExitLockMode().getLockType() == LockMode.LockType.PASSWORD) {
            holder.exitLock.append(": " + hidePassword(profile.getExitLockMode().getPassword()));
        } else if (profile.getExitLockMode().getLockType() == LockMode.LockType.PIN) {
            holder.exitLock.append(": " + hidePassword(profile.getExitLockMode().getPin()));
        }
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.setAction(ProfileActivity.ACTION_VIEW);
                intent.putExtra("profile", profile);
                activity.startActivityForResult(intent, MainActivity.RESULT_PROFILE);
            }
        });
        holder.mapView.onCreate(null);
        //holder.mapView.onSaveInstanceState(null);
        holder.mapView.onResume();
        holder.mapView.onStart();
        holder.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                //MapsInitializer.initialize(activity);
                googleMap.setMapType(mapType);

                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setZoomGesturesEnabled(false);
                googleMap.getUiSettings().setScrollGesturesEnabled(false);
                googleMap.getUiSettings().setRotateGesturesEnabled(false);
                googleMap.getUiSettings().setTiltGesturesEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.setAction(ProfileActivity.ACTION_VIEW);
                        intent.putExtra("profile", profile);
                        activity.startActivityForResult(intent, MainActivity.RESULT_PROFILE);
                    }
                });
                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(profile.getPlace())
                        .radius(profile.getRadius())
                        .strokeColor(Color.RED));
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(profile.getPlace(), profile.getRadius()), circlePadding);
                        googleMap.moveCamera(cameraUpdate);
                    }
                });
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.viewType == VIEW_TYPE_PROFILE) {
            holder.mapView.onPause();
            holder.mapView.onStop();
            holder.mapView.onDestroy();
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return profiles.size() + 1;
    }

    private String hidePassword(String password) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            s.append("*");
        }
        return s.toString();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView name;
        CompoundButton enabledView;
        MapView mapView;
        TextView enterLock;
        TextView exitLock;
        int viewType;

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
            }
        }

        void initializeMapView() {

        }
    }
}
