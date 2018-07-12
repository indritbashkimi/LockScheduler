package com.ibashkimi.lockscheduler.addeditprofile.conditions.location;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsFragment;
import com.ibashkimi.lockscheduler.model.condition.PlaceCondition;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;
import com.ibashkimi.lockscheduler.model.source.serializer.ConditionSerializer;
import com.ibashkimi.lockscheduler.util.MapUtils;
import com.ibashkimi.lockscheduler.util.Utils;
import com.ibashkimi.theme.utils.ThemeUtils;

import org.json.JSONException;


public class LocationConditionFragment extends Fragment implements OnMapReadyCallback {

    public static final int PLACE_PICKER_REQUEST = 1;

    TextView addressView;

    MapView mapView;

    View mapCover;

    TextView radiusView;

    private GoogleMap googleMap;

    private PlaceCondition condition;
    @ColorInt
    private int circleColor;
    @ColorInt
    private int fillColor;
    private int circlePadding;
    private int mapStyle;
    @Nullable
    private Circle circle;

    public void setData(@NonNull PlaceCondition condition) {
        this.condition = condition;
        if (googleMap != null)
            updateMap();
    }

    public PlaceCondition assembleCondition() {
        return condition;
    }

    public LocationConditionFragment() {
    }

    public static LocationConditionFragment newInstance(int mapStyle) {
        Bundle args = new Bundle();
        args.putInt("map_style", mapStyle);
        LocationConditionFragment fragment = new LocationConditionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        circlePadding = (int) ThemeUtils.dpToPx(requireContext(), 8);
        circleColor = ThemeUtils.obtainColor(requireContext(), R.attr.colorPrimary, Color.RED);
        fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
        mapStyle = MapUtils.resolveMapStyle(AppPreferencesHelper.INSTANCE.getMapStyle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_condition_place, container, false);

        addressView = root.findViewById(R.id.address_text);
        mapView = root.findViewById(R.id.map_view);
        mapCover = root.findViewById(R.id.map_cover);
        radiusView = root.findViewById(R.id.radius);

        if (savedInstanceState != null) {
            try {
                condition = ConditionSerializer.parsePlaceConditionJson(savedInstanceState.getString("place_condition"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        outState.putString("place_condition", ConditionSerializer.toJson(condition));
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(mapStyle);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomGesturesEnabled(false);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                showPlacePicker();
            }
        });
        updateMap();
        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (mapCover.getVisibility() == View.VISIBLE) {
                    Animation fadeAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
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

    private void showPlacePicker() {
        ((ConditionsFragment) getParentFragment()).showPlacePicker(condition);
    }

    public void updateMap() {
        addressView.setText(condition.getAddress());
        radiusView.setText(condition.getRadius() + " m"); // TODO: 11/05/17
        if (circle != null)
            circle.remove();
        LatLng place = new LatLng(condition.getLatitude(), condition.getLongitude());
        circle = googleMap.addCircle(new CircleOptions()
                .center(place)
                .radius(condition.getRadius())
                .fillColor(fillColor)
                .strokeWidth(Utils.dpToPx(requireContext(), 2))
                .strokeColor(circleColor));
        LatLngBounds bounds = MapUtils.calculateBounds(place, condition.getRadius());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, circlePadding);
        LocationConditionFragment.this.googleMap.moveCamera(cameraUpdate);
    }
}