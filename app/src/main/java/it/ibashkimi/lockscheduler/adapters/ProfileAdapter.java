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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;

import java.util.List;

import it.ibashkimi.lockscheduler.MainActivity;
import it.ibashkimi.lockscheduler.ProfileActivity;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private static final String TAG = "ProfileAdapter";
    private Activity activity;
    private List<Profile> profiles;
    private int circlePadding;

    public ProfileAdapter(Activity activity, List<Profile> profiles) {
        this.activity = activity;
        this.profiles = profiles;
        this.circlePadding = (int) Utils.dpToPx(activity, 8);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() viewType = [" + viewType + "]");
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Profile profile = profiles.get(position);
        holder.name.setText(profile.getName());
        holder.enabledView.setChecked(profile.isEnabled());
        holder.enabledView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.setEnabled(isChecked);
            }
        });
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
        holder.mapView.onResume();
        holder.mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(activity);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(profile.getPlace())
                        .radius(profile.getRadius())
                        .strokeColor(Color.RED));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(profile.getPlace(), profile.getRadius()), circlePadding);
                googleMap.animateCamera(cameraUpdate);
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mapView.onPause();
        holder.mapView.onStop();
        holder.mapView.onDestroy();
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView name;
        CompoundButton enabledView;
        MapView mapView;

        ViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.name = (TextView) itemView.findViewById(R.id.name_view);
            this.enabledView = (CompoundButton) itemView.findViewById(R.id.switchView);
            this.mapView = (MapView) itemView.findViewById(R.id.mapView);
        }
    }
}
