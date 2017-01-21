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

    public static int resolveMapStyle(int mapStyle) {
        switch (mapStyle) {
            case 0:
                return GoogleMap.MAP_TYPE_NORMAL;
            case 1:
                return GoogleMap.MAP_TYPE_SATELLITE;
            case 2:
                return GoogleMap.MAP_TYPE_HYBRID;
            case 3:
                return GoogleMap.MAP_TYPE_TERRAIN;
            default:
                return GoogleMap.MAP_TYPE_HYBRID;
        }
    }
}
