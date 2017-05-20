package it.ibashkimi.lockscheduler.profiles;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

/**
 * Listens to user actions from the UI ({@link ProfilesFragment}), retrieves the data and updates the
 * UI as required.
 */
class ProfilesPresenter implements ProfilesContract.Presenter {
    private final ProfilesDataSource profilesRepository;
    private final ProfilesContract.View profilesView;

    ProfilesPresenter(@NonNull ProfilesDataSource profilesRepository, @NonNull ProfilesContract.View profilesView) {
        this.profilesRepository = profilesRepository;
        this.profilesView = profilesView;
    }

    @Override
    public void start() {
        loadProfiles();
    }

    @Override
    public void result(int requestCode, int resultCode, String extras) {
        // If a profile was successfully added, show snackbar
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        if (AddEditProfileActivity.REQUEST_ADD_PROFILE == requestCode) {
            profilesView.showSuccessfullySavedMessage();
        } else if (AddEditProfileActivity.REQUEST_EDIT_PROFILE == requestCode) {
            if (extras != null) {
                switch (extras) {
                    case "deleted":
                        profilesView.showSuccessfullyRemovedMessage();
                        break;
                    case "updated":
                        profilesView.showSuccessfullyUpdatedMessage();
                        break;
                    default:
                        throw new RuntimeException("Unhandled case.");
                }
            } else {
                throw new RuntimeException("This should not be possible.");
            }
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
            if (profiles.size() == 0)
                profilesView.showNoProfiles();
            else
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
        profilesRepository.swap(pos1, pos2);
    }

}
