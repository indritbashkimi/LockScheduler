package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.model.WifiItem;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class WifiPickerPresenter implements WifiPickerContract.Presenter, WifiInfoProvider.Callback {

    private WifiInfoProvider provider;

    private WifiPickerContract.View view;

    private List<WifiItem> selectedItems;

    public WifiPickerPresenter(WifiInfoProvider provider, WifiPickerContract.View view, List<WifiItem> selectedItems) {
        this.provider = provider;
        this.view = view;
        this.selectedItems = selectedItems;
    }

    @Override
    public void start() {
        provider.getWifiList(this);
    }

    @Override
    public void setSelected(WifiItem item, boolean selected) {

    }

    @Override
    public void onDataLoaded(List<WifiItem> items) {
        view.showWifiList(filter(items), selectedItems.size());
    }

    @Override
    public void onDataNotAvailable() {
        view.showWiFiDisabled();
    }

    private List<WifiItem> filter(List<WifiItem> items) {
        for (WifiItem item : selectedItems) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).equals(item)) {
                    items.remove(i);
                    break;
                }
            }
        }
        ArrayList<WifiItem> result = new ArrayList<>();
        result.addAll(selectedItems);
        result.addAll(items);
        return result;
    }

//    private int[] asdjfk(List<WifiItem> items) {
//        int[] res = new int[selectedItems.size()];
//        int j = 0;
//        for (int i = 0; i < items.size(); i++) {
//            if (items.get(i))
//        }
//    }
}
