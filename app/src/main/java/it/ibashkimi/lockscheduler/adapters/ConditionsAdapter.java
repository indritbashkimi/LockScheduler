package it.ibashkimi.lockscheduler.adapters;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.Utils;
import it.ibashkimi.lockscheduler.domain.Condition;
import it.ibashkimi.lockscheduler.domain.PlaceCondition;
import it.ibashkimi.lockscheduler.domain.WifiCondition;
import it.ibashkimi.support.design.utils.ThemeUtils;


public class ConditionsAdapter extends RecyclerView.Adapter<ConditionsAdapter.BaseViewHolder> {

    private static final String TAG = "ConditionsAdapter";

    public interface Callbacks {
        void onConditionClicked(Condition condition);

        void onConditionRemoved(Condition condition);
    }

    private SparseArray<Condition> conditions;
    private Callbacks listener;
    private Context context;

    public ConditionsAdapter(Context context, SparseArray<Condition> conditions, Callbacks listener) {
        this.context = context;
        this.conditions = conditions;
        this.listener = listener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Condition.Type.PLACE:
                View placeItemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_place, parent, false);
                return new PlaceViewHolder(placeItemView);
            case Condition.Type.TIME:
                View timeItemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_time, parent, false);
                return new TimeViewHolder(timeItemView);
            case Condition.Type.WIFI:
                View wifiItemView = LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.item_wifi, parent, false);
                return new WifiViewHolder(wifiItemView);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final Condition condition = conditions.valueAt(position);
        holder.init(context, this, condition, listener);
    }

    public SparseArray<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(SparseArray<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public int getItemViewType(int position) {
        return conditions.keyAt(position);
    }

    @Override
    public int getItemCount() {
        return conditions.size();
    }

    @Override
    public void onViewRecycled(BaseViewHolder holder) {
        super.onViewRecycled(holder);
        holder.recycle();
    }


    static class BaseViewHolder extends RecyclerView.ViewHolder {
        public View rootView;
        public Toolbar toolbar;

        BaseViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            this.toolbar = (Toolbar) itemView.findViewById(R.id.toolbar);
        }

        public void init(Context context, final ConditionsAdapter adapter, final Condition condition, final Callbacks listener) {
            toolbar.setTitle(condition.getName());
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_condition);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_delete) {
                        adapter.getConditions().remove(condition.getType());
                        adapter.notifyDataSetChanged();
                        if (listener != null)
                            listener.onConditionRemoved(condition);
                        return true;
                    }
                    return false;
                }
            });
        }

        public void recycle() {
        }
    }

    private static class PlaceViewHolder extends BaseViewHolder {
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

        PlaceViewHolder(View itemView) {
            super(itemView);
            circlePadding = (int) ThemeUtils.dpToPx(itemView.getContext(), 8);
            circleColor = ThemeUtils.getColorFromAttribute(itemView.getContext(), R.attr.colorAccent);
            fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
            mapType = Utils.resolveMapStyle(itemView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    .getString("map_style", "hybrid"));
            mapView = (MapView) itemView.findViewById(R.id.mapView);
            mapCover = itemView.findViewById(R.id.mapCover);
        }

        @Override
        public void init(final Context context, ConditionsAdapter adapter, Condition condition, final Callbacks listener) {
            super.init(context, adapter, condition, listener);
            this.placeCondition = (PlaceCondition) condition;
            mapView.onCreate(null);
            //mapView.onSaveInstanceState(null);
            mapView.onResume();
            mapView.onStart();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    PlaceViewHolder.this.googleMap = googleMap;
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
                            if (listener != null)
                                listener.onConditionClicked(placeCondition);
                        }
                    });
                    circle = googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .fillColor(fillColor)
                            .strokeColor(circleColor));
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            Log.d(TAG, "onMapLoaded: ");
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                            googleMap.moveCamera(cameraUpdate);
                            if (mapCover.getVisibility() == View.VISIBLE) {
                                Animation fadeAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
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
            radiusEditText = (EditText) itemView.findViewById(R.id.radiusInput);
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
        }

        private void updateMap() {
            if (circle == null) {
                if (googleMap != null && placeCondition.getPlace() != null) {
                    circle = googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .strokeColor(circleColor)
                            .fillColor(fillColor));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                    googleMap.moveCamera(cameraUpdate);
                }
            } else {
                if (placeCondition.getRadius() > 0) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(Utils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
                    googleMap.animateCamera(cameraUpdate);
                }
                circle.setCenter(placeCondition.getPlace());
                circle.setRadius(placeCondition.getRadius());
            }
        }

        @Override
        public void recycle() {
            super.recycle();
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
        }
    }

    private static class TimeViewHolder extends BaseViewHolder {
        TimeViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class WifiViewHolder extends BaseViewHolder {
        Spinner spinner;

        WifiViewHolder(View itemView) {
            super(itemView);
            spinner = (Spinner) itemView.findViewById(R.id.spinner);
        }

        @Override
        public void init(Context context, ConditionsAdapter adapter, Condition condition, Callbacks listener) {
            super.init(context, adapter, condition, listener);
            WifiCondition wifiCondition = (WifiCondition) condition;
            List<WifiConfiguration> networks = wifiCondition.getNetworks();
            ArrayList<String> array = new ArrayList<>(networks.size());
            for (WifiConfiguration wifi : networks) {
                array.add(wifi.SSID);
            }
            ArrayAdapter<String> enterSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
            enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(enterSpinnerAdapter);
            //spinner.setOnItemSelectedListener(new SpinnerListener(mProfile.getEnterLockMode(), mEnterPasswordLayout, rootView, getSharedPreferences()));
            //spinner.setSelection(getSpinnerPositionFromLockType(mProfile.getEnterLockMode().getLockType()));
        }
    }
}
