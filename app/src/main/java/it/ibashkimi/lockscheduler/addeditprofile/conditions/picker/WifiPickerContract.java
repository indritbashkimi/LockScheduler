package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import java.util.List;

import it.ibashkimi.lockscheduler.model.WifiItem;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public interface WifiPickerContract {

    interface View {

        void setPresenter(Presenter presenter);

        void showWifiList(List<WifiItem> wifiItems, int selectedItems);

        void setSelected(int item, boolean selected);

        void showWiFiDisabled();
    }

    interface Presenter {

        void start();

        void setSelected(WifiItem item, boolean selected);
    }
}
