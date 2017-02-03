package it.ibashkimi.lockscheduler.addeditprofile.conditions;


import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.addeditprofile.ConditionsProvider;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.PlaceCondition;
import it.ibashkimi.lockscheduler.model.TimeCondition;
import it.ibashkimi.lockscheduler.model.WifiCondition;


public class ConditionsPresenter implements ConditionsContract.Presenter {

    private static final String TAG = ConditionsPresenter.class.getSimpleName();

    @NonNull
    private ConditionsContract.View mView;

    @NonNull
    private ConditionsProvider mProvider;

    public ConditionsPresenter(@NonNull ConditionsProvider provider, @NonNull ConditionsContract.View view) {
        mProvider = provider;
        mView = view;
    }

    @Override
    public void start() {
        List<Condition> conditions = mProvider.getConditions();
        if (conditions != null && mView.isActive()) {
            for (Condition condition : conditions) {
                switch (condition.getType()) {
                    case Condition.Type.PLACE:
                        mView.showPlaceCondition((PlaceCondition) condition);
                        break;
                    case Condition.Type.TIME:
                        mView.showTimeCondition((TimeCondition) condition);
                        break;
                    case Condition.Type.WIFI:
                        mView.showWifiCondition((WifiCondition) condition);
                        break;
                }
            }
        }
    }

    @Override
    public void saveConditions(List<Condition> conditions) {
        mProvider.updateConditions(conditions);
    }

    @Override
    public void deleteCondition(int conditionId) {
        switch (conditionId) {
            case Condition.Type.PLACE:
                mView.hidePlaceCondition();
                break;
            case Condition.Type.TIME:
                mView.hideTimeCondition();
                break;
            case Condition.Type.WIFI:
                mView.hideWifiCondition();
                break;
        }
    }

    @Override
    public boolean isDataMissing() {
        return false;
    }
}
