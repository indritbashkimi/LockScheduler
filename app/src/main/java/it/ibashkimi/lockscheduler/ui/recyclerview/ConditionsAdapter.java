package it.ibashkimi.lockscheduler.ui.recyclerview;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

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
import it.ibashkimi.lockscheduler.addeditprofile.WifiAdapter;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.model.api.WifiListProvider;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.lockscheduler.util.Utils;
import it.ibashkimi.support.utils.ThemeUtils;


public class ConditionsAdapter extends RecyclerView.Adapter<ConditionsAdapter.BaseViewHolder> {

    private static final String TAG = "ConditionsAdapter";

    public interface Callbacks {
        void onConditionClicked(Condition condition);

        void onConditionRemoved(Condition condition);
    }

    private List<Condition> conditions;
    private Callbacks listener;
    private Context context;

    public ConditionsAdapter(Context context, List<Condition> conditions, Callbacks listener) {
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
                        inflate(R.layout.item_wifi_old, parent, false);
                return new WifiViewHolder(wifiItemView);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        final Condition condition = conditions.get(position);
        holder.init(context, this, condition, listener);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public int getItemViewType(int position) {
        return conditions.get(position).getType();
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
                        int index = adapter.getConditions().indexOf(condition);
                        adapter.getConditions().remove(condition);
                        adapter.notifyItemRemoved(index);
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
        //private int mapType;
        private Circle circle;
        private GoogleMap googleMap;
        private PlaceCondition placeCondition;
        private View mapCover;
        private TextView coordinates;
        private TextView address;
        private Context context;
        private float circleStrokeWidth;

        PlaceViewHolder(View itemView) {
            super(itemView);
            circlePadding = (int) ThemeUtils.dpToPx(itemView.getContext(), 8);
            circleColor = ThemeUtils.getColorFromAttribute(itemView.getContext(), R.attr.colorAccent);
            fillColor = ColorUtils.setAlphaComponent(circleColor, 0x25);
            //mapType = MapUtils.resolveMapStyle(itemView.getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE).getInt("map_style", 0));
            mapView = (MapView) itemView.findViewById(R.id.mapView);
            mapCover = itemView.findViewById(R.id.mapCover);
            coordinates = (TextView) itemView.findViewById(R.id.coordinates);
            address = (TextView) itemView.findViewById(R.id.address);
        }

        @Override
        public void init(final Context context, ConditionsAdapter adapter, Condition condition, final Callbacks listener) {
            super.init(context, adapter, condition, listener);
            this.context = context;
            this.placeCondition = (PlaceCondition) condition;
            mapView.onCreate(null);
            //mapView.onSaveInstanceState(null);
            mapView.onResume();
            mapView.onStart();
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    PlaceViewHolder.this.googleMap = googleMap;
                    //googleMap.setMapType(mapType);
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
                            .strokeWidth(Utils.dpToPx(itemView.getContext(), 2))
                            .strokeColor(circleColor));
                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            Log.d(TAG, "onMapLoaded: ");
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(MapUtils.calculateBounds(placeCondition.getPlace(), placeCondition.getRadius()), circlePadding);
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
            coordinates.setText(String.format(Locale.ENGLISH, "%f, %f", placeCondition.getPlace().latitude, placeCondition.getPlace().longitude));
            address.setText(placeCondition.getAddress());
        }

        private void updateMap() {
            if (circle == null) {
                if (googleMap != null && placeCondition.getPlace() != null) {
                    circle = googleMap.addCircle(new CircleOptions()
                            .center(placeCondition.getPlace())
                            .radius(placeCondition.getRadius())
                            .strokeColor(circleColor)
                            .strokeWidth(Utils.dpToPx(itemView.getContext(), 2))
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
        public void recycle() {
            super.recycle();
            mapView.onPause();
            mapView.onStop();
            mapView.onDestroy();
        }
    }

    private static class TimeViewHolder extends BaseViewHolder {
        CheckBox[] days = new CheckBox[7];

        TimeViewHolder(View itemView) {
            super(itemView);
            days[0] = (CheckBox) itemView.findViewById(R.id.day_0);
            days[1] = (CheckBox) itemView.findViewById(R.id.day_1);
            days[2] = (CheckBox) itemView.findViewById(R.id.day_2);
            days[3] = (CheckBox) itemView.findViewById(R.id.day_3);
            days[4] = (CheckBox) itemView.findViewById(R.id.day_4);
            days[5] = (CheckBox) itemView.findViewById(R.id.day_5);
            days[6] = (CheckBox) itemView.findViewById(R.id.day_6);
        }

        @Override
        public void init(Context context, ConditionsAdapter adapter, Condition condition, Callbacks listener) {
            super.init(context, adapter, condition, listener);
            final TimeCondition timeCondition = (TimeCondition) condition;
            CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int index = -1;
                    switch (buttonView.getId()) {
                        case R.id.day_0:
                            index = 0;
                            break;
                        case R.id.day_1:
                            index = 1;
                            break;
                        case R.id.day_2:
                            index = 2;
                            break;
                        case R.id.day_3:
                            index = 3;
                            break;
                        case R.id.day_4:
                            index = 4;
                            break;
                        case R.id.day_5:
                            index = 5;
                            break;
                        case R.id.day_6:
                            index = 6;
                            break;
                    }
                    timeCondition.getDaysActive()[index] = isChecked;
                }
            };
            for (int i = 0; i < 7; i++) {
                Log.d(TAG, "init: day_" + i + ": " + timeCondition.getDaysActive()[i]);
                days[i].setChecked(timeCondition.getDaysActive()[i]);
                days[i].setOnCheckedChangeListener(checkListener);
            }
        }
    }

    private static class WifiViewHolder extends BaseViewHolder {
        RecyclerView recyclerView;
        RecyclerView availableRecyclerView;

        WifiViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.recyclerView);
            availableRecyclerView = (RecyclerView) itemView.findViewById(R.id.availableRecyclerView);
        }

        @Override
        public void init(Context context, ConditionsAdapter adapter, final Condition condition, Callbacks listener) {
            super.init(context, adapter, condition, listener);

            WifiCondition wifiCondition = (WifiCondition) condition;
            final List<WifiItem> availableWifiItems = WifiListProvider.getNetworks(context);
            final List<WifiItem> conditionWifiItems = wifiCondition.getNetworks();

            for (WifiItem item : conditionWifiItems) {
                for (int i = 0; i < availableWifiItems.size(); i++) {
                    if (availableWifiItems.get(i).equals(item)) {
                        availableWifiItems.remove(i);
                        break;
                    }
                }
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(new WifiAdapter(conditionWifiItems, new WifiAdapter.Callbacks() {
                @Override
                public void onWifiItemSelectChange(WifiItem item, boolean selected) {

                }

                /*@Override
                public void onWifiItemClicked(WifiItem item) {

                }

                @Override
                public void onWifiItemRemoved(WifiItem item) {
                    conditionWifiItems.remove(item);
                    availableWifiItems.add(item);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    availableRecyclerView.getAdapter().notifyDataSetChanged();
                }*/
            }));

            availableRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            DividerItemDecoration dividerItemDecoration2 = new DividerItemDecoration(availableRecyclerView.getContext(),
                    LinearLayoutManager.VERTICAL);
            availableRecyclerView.addItemDecoration(dividerItemDecoration2);
            availableRecyclerView.setAdapter(new WifiAdapter(availableWifiItems, new WifiAdapter.Callbacks() {
                @Override
                public void onWifiItemSelectChange(WifiItem item, boolean selected) {

                }

                /*@Override
                public void onWifiItemClicked(WifiItem item) {
                    conditionWifiItems.add(item);
                    availableWifiItems.remove(item);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    availableRecyclerView.getAdapter().notifyDataSetChanged();
                }

                @Override
                public void onWifiItemRemoved(WifiItem item) {

                }*/
            }));
        }
    }
}
