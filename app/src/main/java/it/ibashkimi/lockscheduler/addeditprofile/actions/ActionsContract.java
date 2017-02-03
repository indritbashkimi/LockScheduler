package it.ibashkimi.lockscheduler.addeditprofile.actions;


import java.util.List;

import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.ui.BasePresenter;
import it.ibashkimi.lockscheduler.ui.BaseView;

public class ActionsContract {

    public interface View extends BaseView<ActionsContract.Presenter> {

        void showEmptyProfileError();

        void setPlace(String address, int radius);

        void setTitle(String title);

        boolean isActive();
    }

    public interface Presenter extends BasePresenter {

        List<Action> saveTrueConditions();

        List<Action> saveFalseConditions();

        void deleteCondition(int conditionId);

        void populateConditions();

        boolean isDataMissing();
    }

}
