package it.ibashkimi.lockscheduler.profiles;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.ProfileRepository;

/**
 * Listens to user actions from the UI ({@link ProfilesFragment}), retrieves the data and updates the
 * UI as required.
 */
class ProfilesPresenter implements ProfilesContract.Presenter {
    private final ProfileRepository repository;
    private final ProfilesContract.View profilesView;

    ProfilesPresenter(@NonNull ProfileRepository repository, @NonNull ProfilesContract.View profilesView) {
        this.repository = repository;
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
        List<Profile> profiles = repository.getAll();
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
    public void swapProfiles(Profile profile1, Profile profile2) {
        repository.swap(profile1, profile2);
    }

}
