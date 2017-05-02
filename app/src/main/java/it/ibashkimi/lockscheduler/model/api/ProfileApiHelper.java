package it.ibashkimi.lockscheduler.model.api;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.receiver.AlarmReceiver;


public class ProfileApiHelper {

    private static final String TAG = "ProfileApiHelper";

    private static ProfileApiHelper sInstance;

    private GeofenceApiHelper mGeofenceApiHelper;

    private ProfileApiHelper(GeofenceApiHelper geofenceApiHelper) {
        mGeofenceApiHelper = geofenceApiHelper;
    }

    public static ProfileApiHelper getInstance(GeofenceApiHelper geofenceApiHelper) {
        if (sInstance == null) {
            sInstance = new ProfileApiHelper(geofenceApiHelper);
        }
        return sInstance;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    /**
     * Save a new profile and initialize conditions.
     *
     * @param profile Profile to add
     */
    public void addProfile(@NonNull Profile profile) {
        for (Condition condition : profile.getConditions()) {
            onRegisterCondition(profile, condition);
        }
    }

    /**
     * Method called on phone reboot
     */
    public void initProfiles(List<Profile> profiles) {
        for (Profile profile : profiles) {
            for (Condition condition : profile.getConditions()) {
                condition.setTrue(false);
            }
        }
        mGeofenceApiHelper.initGeofences(profiles);
        for (Profile profile : profiles) {
            for (Condition condition : profile.getConditions()) {
                switch (condition.getType()) {
                    case Condition.Type.PLACE:
                        break;
                    case Condition.Type.TIME:
                        onRegisterCondition(profile, condition);
                        break;
                    case Condition.Type.WIFI:
                        onRegisterCondition(profile, condition);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Remove a profile and remove conditions.
     *
     * @param profile Profile to remove.
     */
    public void removeProfile(@NonNull Profile profile) {
        for (Condition condition : profile.getConditions()) {
            onUnregisterCondition(profile, condition);
        }
    }

    public void removeAll(List<Profile> profiles) {
        for (Profile profile : profiles)
            removeProfile(profile);
    }

    public void update(@NonNull Profile oldProfile, @NonNull Profile newProfile) {
        // Unregister removed conditions
        for (Condition condition : oldProfile.getConditions()) {
            if (newProfile.getCondition(condition.getType()) == null) {
                onUnregisterCondition(oldProfile, condition);
            }
        }
        // Register newly added conditions and update existing ones.
        for (Condition condition : newProfile.getConditions()) {
            Condition oldCondition = oldProfile.getCondition(condition.getType());
            if (oldCondition == null) {
                onRegisterCondition(newProfile, condition);
            } else {
                condition.setTrue(oldCondition.isTrue());
                onUpdateCondition(newProfile, oldCondition, condition);
            }
        }
        newProfile.notifyUpdated();
    }

    private void onRegisterCondition(Profile profile, Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                mGeofenceApiHelper.initGeofences(ProfilesRepository.getInstance().getProfiles());
                break;
            case Condition.Type.TIME:
                TimeCondition timeCondition = (TimeCondition) condition;
                timeCondition.checkNow();
                long nextAlarm = timeCondition.getNextAlarm();
                if (nextAlarm != -1) {
                    AlarmReceiver.setAlarm(App.getInstance(), nextAlarm);
                }
                if (timeCondition.isTrue())
                    profile.notifyConditionChanged(timeCondition);
                break;
            case Condition.Type.WIFI:
                WifiItem wifiItem = null;
                WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo != null) {
                        String ssid = wifiInfo.getSSID();
                        wifiItem = new WifiItem(ssid.substring(1, ssid.length() - 1));
                    }
                }
                ((WifiCondition) condition).onWifiStateChanged(wifiItem);
                profile.notifyConditionChanged(condition);
                break;
        }
    }

    private void onUnregisterCondition(Profile profile, Condition condition) {
        switch (condition.getType()) {
            case Condition.Type.PLACE:
                mGeofenceApiHelper.removeGeofence(profile.getId());
                break;
            case Condition.Type.TIME:
                AlarmReceiver.cancelAlarm(App.getInstance());
                break;
            case Condition.Type.WIFI:
                break;
        }
    }

    private void onUpdateCondition(Profile profile, Condition oldCondition, Condition newCondition) {
        if (oldCondition.equals(newCondition)) {
            newCondition.setTrue(oldCondition.isTrue());
            return;
        }
        switch (oldCondition.getType()) {
            case Condition.Type.PLACE:
                onUnregisterCondition(profile, oldCondition);
                onRegisterCondition(profile, newCondition);
                break;
            case Condition.Type.TIME:
                onUnregisterCondition(profile, oldCondition);
                onRegisterCondition(profile, newCondition);
                break;
            case Condition.Type.WIFI:
                break;
        }
    }
}
