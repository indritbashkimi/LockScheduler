package com.ibashkimi.lockscheduler.addeditprofile;

import java.util.List;

import com.ibashkimi.lockscheduler.model.Action;
import com.ibashkimi.lockscheduler.model.Condition;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.ui.BasePresenter;
import com.ibashkimi.lockscheduler.ui.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
interface AddEditProfileContract {

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