package it.ibashkimi.lockscheduler.util;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;


public class MapUtils {

    public static LatLngBounds calculateBounds(LatLng center, double radius) {
        return new LatLngBounds.Builder().
                include(SphericalUtil.computeOffset(center, radius, 0)).
                include(SphericalUtil.computeOffset(center, radius, 90)).
                include(SphericalUtil.computeOffset(center, radius, 180)).
                include(SphericalUtil.computeOffset(center, radius, 270)).build();
    }

    public static int resolveMapStyle(String mapStyle) {
        switch (mapStyle) {
            case "normal":
                return GoogleMap.MAP_TYPE_NORMAL;
            case "satellite":
                return GoogleMap.MAP_TYPE_SATELLITE;
            case "hybrid":
                return GoogleMap.MAP_TYPE_HYBRID;
            case "terrain":
                return GoogleMap.MAP_TYPE_TERRAIN;
            default:
                return GoogleMap.MAP_TYPE_HYBRID;
        }
    }
}
