package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
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
import com.ibashkimi.support.utils.ThemeUtils;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.source.serializer.ConditionSerializer;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.lockscheduler.util.Utils;


public class PlaceConditionFragment extends Fragment implements OnMapReadyCallback {

    public static final int PLACE_PICKER_REQUEST = 1;

    @BindView(R.id.address_text)
    TextView addressView;

    @BindView(R.id.map_view)
    MapView mapView;

    @BindView(R.id.map_cover)
    View mapCover;

    @BindView(R.id.radius)
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

    public PlaceConditionFragment() {
    }

    public static PlaceConditionFragment newInstance(int mapStyle) {
        Bundle args = new Bundle();
        args.putInt("map_style", mapStyle);
        PlaceConditionFragment fragment = new PlaceConditionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        circlePadding = (int) ThemeUtils.dpToPx(getContext(), 8);
        circleColor = ThemeUtils.getColorFromAttribute(getContext(), R.attr.colorPrimary);
        fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
        mapStyle = MapUtils.resolveMapStyle(getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getString("map_style", "normal"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_condition_place, container, false);
        ButterKnife.bind(this, root);

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
    public void onSaveInstanceState(Bundle outState) {
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
        circle = googleMap.addCircle(new CircleOptions()
                .center(condition.getPlace())
                .radius(condition.getRadius())
                .fillColor(fillColor)
                .strokeWidth(Utils.dpToPx(getContext(), 2))
                .strokeColor(circleColor));
        LatLngBounds bounds = MapUtils.calculateBounds(condition.getPlace(), condition.getRadius());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, circlePadding);
        PlaceConditionFragment.this.googleMap.moveCamera(cameraUpdate);
    }
}
