package com.ibashkimi.lockscheduler.profiles;

import android.support.annotation.NonNull;

import java.util.List;

import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.ui.BasePresenter;
import com.ibashkimi.lockscheduler.ui.BaseView;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface ProfilesContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showProfiles(List<Profile> profiles);

        void showAddProfile();

        void showProfileDetailsUi(String profileId);

        void showLoadingProfilesError();

        void showNoProfiles();

        void showSwapProfile(int pos1, int pos2);

        void showSuccessfullySavedMessage();

        void showSuccessfullyRemovedMessage();

        void showSuccessfullyUpdatedMessage();

        boolean isActive();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode, String extras);

        void loadProfiles();

        void addNewProfile();

        void openProfileDetails(@NonNull Profile requestedProfile);

        void deleteProfile(long profileId);

        void swapProfiles(Profile profile1, Profile profile2);
/*
        void clearCompletedTasks();*/
    }
}
