package com.ibashkimi.lockscheduler.addeditprofile;

import com.ibashkimi.lockscheduler.model.Actions;
import com.ibashkimi.lockscheduler.model.Conditions;
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

        void saveProfile(String title, Conditions conditions, Actions trueActions, Actions falseActions);

        void deleteProfile();

        void discard();
    }
}