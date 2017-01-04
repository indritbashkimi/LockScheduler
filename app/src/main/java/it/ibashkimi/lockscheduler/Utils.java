package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

/**
 * Created by indrit on 22/12/16.
 */

public class Utils {

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

    public static float dpToPx(Context context, int px) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public static boolean hasNavBar (Context context) {
        //http://stackoverflow.com/questions/28983621/detect-soft-navigation-bar-availability-in-android-device-progmatically
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }
}
