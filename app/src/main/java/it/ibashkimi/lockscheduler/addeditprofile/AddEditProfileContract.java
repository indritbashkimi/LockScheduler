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

        void showLoadProfileError();

        void showProfileList(boolean success, String extra);

        void showProfile(Profile profile);

        void showEmptyProfile();

        void showTitle(int title);

        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        void saveProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions);

        void deleteProfile();

        void discard();
    }
}