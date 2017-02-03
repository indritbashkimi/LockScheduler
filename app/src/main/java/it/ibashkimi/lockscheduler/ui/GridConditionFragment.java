package it.ibashkimi.lockscheduler.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.touchboarder.weekdaysbuttons.WeekdaysDataItem;
import com.touchboarder.weekdaysbuttons.WeekdaysDataSource;
import com.touchboarder.weekdaysbuttons.WeekdaysDrawableProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.lockscheduler.util.Utils;
import it.ibashkimi.support.utils.ThemeUtils;

import static android.app.Activity.RESULT_OK;


public class GridConditionFragment extends ConditionsFragment {
    private static final String TAG = "GridConditionFragment";

    private TextView place;
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
    private PlaceCondition placeCondition;
    private View mapCover;
    private TextView coordinates;
    private View radiusLayout;
    private View mapContainer;
    private float circleStrokeWidth;
    private TextView time;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_conditions_grid, container, false);
        place = (TextView) rootView.findViewById(R.id.place_text);
        place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showAddPlaceDialog();
            }
        });
        circlePadding = (int) ThemeUtils.dpToPx(rootView.getContext(), 8);
        circleColor = ThemeUtils.getColorFromAttribute(rootView.getContext(), R.attr.colorPrimary);
        fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
        mapType = MapUtils.resolveMapStyle(rootView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getInt("map_style", 0));
        mapView = (MapView) rootView.findViewById(R.id.mapView);
        mapCover = rootView.findViewById(R.id.mapCover);
        coordinates = (TextView) rootView.findViewById(R.id.coordinates);
        time = (TextView) rootView.findViewById(R.id.time);
        mapContainer = rootView.findViewById(R.id.map_container);
        radiusLayout = rootView.findViewById(R.id.radius_layout);
        rootView.findViewById(R.id.time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (time.getVisibility() == View.VISIBLE) {
                    time.setVisibility(View.GONE);
                    //rootView.findViewById(R.id.weekdays_stub).setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.time_delete).setVisibility(View.VISIBLE);
                    WeekdaysDataSource wds = new WeekdaysDataSource((AppCompatActivity) getActivity(), R.id.weekdays_stub)
                            .setFirstDayOfWeek(Calendar.MONDAY)
                            .setNumberOfLetters(3)
                            .setDrawableType(WeekdaysDrawableProvider.MW_ROUND)
                            .start(new WeekdaysDataSource.Callback() {
                                @Override
                                public void onWeekdaysItemClicked(int i, WeekdaysDataItem weekdaysDataItem) {
                                    weekdaysDataItem.getCalendarDayId();
                                }

                                @Override
                                public void onWeekdaysSelected(int i, ArrayList<WeekdaysDataItem> arrayList) {

                                }
                            });
                } else {
                    time.setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.weekdays_stub).setVisibility(View.GONE);
                    rootView.findViewById(R.id.time_delete).setVisibility(View.GONE);
                }

            }
        });
        rootView.findViewById(R.id.wifi_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiSelectFragment fragment = new WifiSelectFragment();
                //fragment.setWifiCondition((WifiCondition) getCondition(Condition.Type.WIFI));
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(android.R.id.content, fragment, "Wifi").commit();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        placeCondition = (PlaceCondition) getCondition(Condition.Type.PLACE);
        if (placeCondition != null) {
            //coordinates.setVisibility(View.VISIBLE);
            radiusLayout.setVisibility(View.VISIBLE);
            mapContainer.setVisibility(View.VISIBLE);
            mapView.onCreate(null);
            //mapView.onSaveInstanceState(null);
            mapView.onResume();
            mapView.onStart();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    GridConditionFragment.this.googleMap = googleMap;
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
                            onConditionClicked(placeCondition);
                        }
                    });
                    circle = googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .fillColor(fillColor)
                            .strokeWidth(Utils.dpToPx(getContext(), 2))
                            .strokeColor(circleColor));
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            Log.d(TAG, "onMapLoaded: ");
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                            googleMap.moveCamera(cameraUpdate);
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
            });
            radiusEditText = (EditText) view.findViewById(R.id.radiusInput);
            radiusEditText.setText(String.format(Locale.ENGLISH, "%d", placeCondition.getRadius()));
            radiusEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    placeCondition.setRadius(s.length() == 0 ? 0 : Integer.parseInt(s.toString()));
                    updateMap();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            coordinates.setText(String.format(Locale.ENGLISH, "%f, %f", placeCondition.getPlace().latitude, placeCondition.getPlace().longitude));
            place.setText(placeCondition.getAddress());
        } else {
            //coordinates.setVisibility(View.GONE);
            radiusLayout.setVisibility(View.GONE);
            mapContainer.setVisibility(View.GONE);
        }
    }

    private void updateMap() {
        if (circle == null) {
            if (googleMap != null && placeCondition.getPlace() != null) {
                circle = googleMap.addCircle(new CircleOptions()
                        .center(placeCondition.getPlace())
                        .radius(placeCondition.getRadius())
                        .strokeColor(circleColor)
                        .strokeWidth(Utils.dpToPx(getContext(), 2))
                        .fillColor(fillColor));
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                googleMap.moveCamera(cameraUpdate);
            }
        } else {
            if (placeCondition.getRadius() > 0) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                googleMap.animateCamera(cameraUpdate);
            }
            circle.setCenter(placeCondition.getPlace());
            circle.setRadius(placeCondition.getRadius());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       /* if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                PlaceCondition placeCondition = null;
                Condition condition = getCondition(Condition.Type.PLACE);
                if (condition != null) {
                    placeCondition = (PlaceCondition) condition;
                }
                if (placeCondition != null) {
                    placeCondition.setPlace(place.getLatLng());
                    placeCondition.setAddress(place.getAddress().toString());
                    this.place.setText(placeCondition.getAddress());
                    updateMap();
                    parent.onConditionChanged(placeCondition);
                } else {
                    placeCondition = new PlaceCondition("Place", place.getLatLng(), 300);
                    placeCondition.setAddress(place.getAddress().toString());
                    conditions.add(placeCondition);
                    parent.onConditionAdded(placeCondition);
                    this.place.setText(placeCondition.getAddress());
                    //coordinates.setVisibility(View.VISIBLE);
                    radiusLayout.setVisibility(View.VISIBLE);
                    mapContainer.setVisibility(View.VISIBLE);
                    updateMap();
                }
                parent.onPlacePicked(place);
            }
        }*/
    }
}
