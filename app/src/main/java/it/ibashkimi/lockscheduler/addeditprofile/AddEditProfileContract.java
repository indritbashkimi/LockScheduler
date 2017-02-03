package it.ibashkimi.lockscheduler.addeditprofile;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.ui.BasePresenter;
import it.ibashkimi.lockscheduler.ui.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface AddEditProfileContract {

    interface View extends BaseView<Presenter> {

        void showEmptyProfileError();

        void showProfileList();

        void showProfile(Profile profile);

        void showEmptyProfile();

        void save();

        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        /**
         * Called by the view when data is ready to be saved.
         * @param title
         * @param conditions
         * @param trueActions
         * @param falseActions
         */
        void saveProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions);

        void deleteProfile();

        void populateProfile();

        void requestSave();

        boolean isDataMissing();
    }
}