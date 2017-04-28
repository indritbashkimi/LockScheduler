package it.ibashkimi.lockscheduler.profiles;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

/**
 * Listens to user actions from the UI ({@link ProfilesFragment}), retrieves the data and updates the
 * UI as required.
 */
public class ProfilesPresenter implements ProfilesContract.Presenter {
    private static final String TAG = "ProfilesPresenter";
    private final ProfilesDataSource profilesRepository;

    private final ProfilesContract.View profilesView;

    public ProfilesPresenter(@NonNull ProfilesDataSource profilesRepository, @NonNull ProfilesContract.View profilesView) {
        this.profilesRepository = profilesRepository;
        this.profilesView = profilesView;
    }

    @Override
    public void start() {
        loadProfiles();
    }

    @Override
    public void result(int requestCode, int resultCode, Bundle extras) {
        // If a profile was successfully added, show snackbar
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        if (AddEditProfileActivity.REQUEST_ADD_PROFILE == requestCode) {
            profilesView.showSuccessfullySavedMessage();
        } else if (AddEditProfileActivity.REQUEST_EDIT_PROFILE == requestCode){
            if (extras != null && extras.containsKey("deleted"))
                profilesView.showSuccessfullyUpdatedMessage();
            else
                profilesView.showSuccessfullyRemovedMessage(1);
        }
    }

    @Override
    public void loadProfiles() {
        if (!profilesView.isActive()) {
            return;
        }
        List<Profile> profiles = profilesRepository.getProfiles();
        if (profiles == null) {
            profilesView.showLoadingProfilesError();
        } else {
            profilesView.showProfiles(profiles);
        }
    }

    @Override
    public void addNewProfile() {
        profilesView.showAddProfile();
    }

    @Override
    public void openProfileDetails(@NonNull Profile requestedProfile) {
        profilesView.showProfileDetailsUi(requestedProfile.getId());
    }

    @Override
    public void deleteProfile(long profileId) {

    }

    @Override
    public void swapProfiles(int pos1, int pos2) {
        profilesRepository.swapProfiles(pos1, pos2);
    }

}
