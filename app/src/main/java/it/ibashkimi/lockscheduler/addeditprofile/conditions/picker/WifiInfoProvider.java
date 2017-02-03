package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import java.util.List;

import it.ibashkimi.lockscheduler.model.WifiItem;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public interface WifiInfoProvider {

    interface Callback {

        void onDataLoaded(List<WifiItem> items);

        void onDataNotAvailable();
    }

    void getWifiList(Callback callback);
}
