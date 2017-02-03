package it.ibashkimi.lockscheduler.profiles;

import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.ui.BasePresenter;
import it.ibashkimi.lockscheduler.ui.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface ProfilesContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showProfiles(List<Profile> profiles);

        void showAddProfile();

        void showProfileDetailsUi(long profileId);

        void showLoadingProfilesError();

        void showNoProfiles();

        void showSwapProfile(int pos1, int pos2);

        void showSuccessfullySavedMessage();

        void showSuccessfullyRemovedMessage(int profilesRemoved);

        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadProfiles(boolean forceUpdate);

        void addNewProfile();

        void openProfileDetails(@NonNull Profile requestedProfile);

        void deleteProfile(long profileId);

        void swapProfiles(int pos1, int pos2);
/*
        void clearCompletedTasks();*/
    }
}
