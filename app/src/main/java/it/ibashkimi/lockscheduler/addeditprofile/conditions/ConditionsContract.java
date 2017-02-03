package it.ibashkimi.lockscheduler.addeditprofile.conditions;


import java.util.List;

import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.ui.BasePresenter;
import it.ibashkimi.lockscheduler.ui.BaseView;

public class ConditionsContract {

    public interface View extends BaseView<ConditionsContract.Presenter> {

        void showPlaceCondition(PlaceCondition placeCondition);

        void showTimeCondition(TimeCondition timeCondition);

        void showWifiCondition(WifiCondition wifiCondition);

        //void showConditions(List<Condition> conditions);

        void showEmptyConditions();

        void hidePlaceCondition();

        void hideTimeCondition();

        void hideWifiCondition();

        boolean isActive();
    }

    public interface Presenter extends BasePresenter {

        void saveConditions(List<Condition> conditions);

        void deleteCondition(int conditionId);

        boolean isDataMissing();
    }
}
