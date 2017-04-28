package it.ibashkimi.lockscheduler.addeditprofile.conditions;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.transition.TransitionManager;
import android.support.v4.graphics.ColorUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.lockscheduler.util.Utils;
import it.ibashkimi.support.utils.ThemeUtils;


public class PlaceConditionView {

    private static final String TAG = PlaceConditionView.class.getSimpleName();

    private ConditionsFragment parent;
    private TextView address;
    private EditText radiusEditText;
    private MapView mapView;
    @ColorInt
    private int circleColor;
    @ColorInt
    private int fillColor;
    private int circlePadding;
    private int mapType;
    private Circle circle;
    private GoogleMap googleMap;
    @NonNull
    private View mapCover;
    private View radiusLayout;
    private View mapContainer;
    private float circleStrokeWidth;

    private ViewGroup root;
    private View body;
    private ImageView delete;

    private PlaceCondition mPlaceCondition;


    public PlaceConditionView(ConditionsFragment parent, ViewGroup root, Bundle savedInstanceState) {
        this.parent = parent;
        this.root = root;
        circlePadding = (int) ThemeUtils.dpToPx(parent.getContext(), 8);
        circleColor = ThemeUtils.getColorFromAttribute(parent.getContext(), R.attr.colorPrimary);
        fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
        mapType = MapUtils.resolveMapStyle(parent.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("map_style", 0));

        delete = (ImageView) root.findViewById(R.id.place_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPlaceCondition(null);
                updateView();
            }
        });
        body = root.findViewById(R.id.place_body);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlacePicker();

            }
        });
        mapView = (MapView) root.findViewById(R.id.mapView);
        mapCover = root.findViewById(R.id.mapCover);
        mapContainer = root.findViewById(R.id.map_container);
        radiusLayout = root.findViewById(R.id.radius_layout);
        radiusEditText = (EditText) root.findViewById(R.id.radiusInput);
        address = (TextView) root.findViewById(R.id.address_text);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceEmpty();
            }
        });

        radiusEditText.setText(String.format(Locale.ENGLISH, "%d", 0));
        radiusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO: 19/04/17
                //presenter.setRadius(s.length() == 0 ? 0 : Integer.parseInt(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("placeBodyVisible", body.getVisibility());
    }

    public void setPlaceCondition(PlaceCondition placeCondition) {
        mPlaceCondition = placeCondition;
        updateView();
    }

    public PlaceCondition getPlaceCondition() {
        return mPlaceCondition;
    }

    public void updateView() {
        if (mPlaceCondition != null) {
            showMap(mPlaceCondition.getPlace(), mPlaceCondition.getRadius(), mPlaceCondition.getAddress());
        } else {
            showPlaceEmpty();
        }
    }

    public void showPlaceEmpty() {
        mPlaceCondition = null;
        TransitionManager.beginDelayedTransition(root);
        body.setVisibility(View.GONE);
        mapCover.setVisibility(View.VISIBLE);
        delete.setVisibility(View.GONE);
    }

    public void showPlacePicker() {
        //parent.showPlacePicker();
    }

    public void showMap(LatLng center, int radius, String address) {
        TransitionManager.beginDelayedTransition(root);
        body.setVisibility(View.VISIBLE);
        delete.setVisibility(View.VISIBLE);
        initMap(center, radius, address);
        updateMap(center, radius, address);
        this.address.setText(address);
    }

    private void initMap(final LatLng center, final int radius, String address) {
        mapView.onCreate(null);
        mapView.onResume();
        mapView.onStart();
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                PlaceConditionView.this.googleMap = googleMap;
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
                        //parent.showPlacePicker();
                    }
                });
                circle = googleMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(radius)
                        .fillColor(fillColor)
                        .strokeWidth(Utils.dpToPx(getContext(), 2))
                        .strokeColor(circleColor));
                googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        //Log.d(TAG, "onMapLoaded: ");
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(center, radius), circlePadding);
                        googleMap.moveCamera(cameraUpdate);
                        if (mapCover.getVisibility() == View.VISIBLE) {
                            Animation fadeAnimation = AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.fade_out);
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

    private Context getContext() {
        return parent.getContext();
    }

    public void updateMap(LatLng center, int radius, String address) {
        Log.d(TAG, "updateMap: ");
        if (circle == null) {
            if (googleMap != null && center != null) {
                circle = googleMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(radius)
                        .strokeColor(circleColor)
                        .strokeWidth(Utils.dpToPx(parent.getContext(), 2))
                        .fillColor(fillColor));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(center, radius), circlePadding);
                googleMap.moveCamera(cameraUpdate);
            }
        } else {
            if (radius > 0) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(center, radius), circlePadding);
                googleMap.animateCamera(cameraUpdate);
            }
            circle.setCenter(center);
            circle.setRadius(radius);
        }
        this.address.setText(address);
    }
}
