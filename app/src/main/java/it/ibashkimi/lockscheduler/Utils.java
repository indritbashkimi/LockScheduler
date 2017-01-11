package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;


public class Utils {

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

    public static void sendFeedback(Context context) {
        // http://stackoverflow.com/a/16217921
        // https://developer.android.com/guide/components/intents-common.html#Email
        String address = context.getString(R.string.developer_email);
        String subject = context.getString(R.string.feedback_subject);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + address));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        String chooserTitle = context.getString(R.string.feedback_chooser_title);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
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
