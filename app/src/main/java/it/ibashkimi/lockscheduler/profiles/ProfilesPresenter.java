package it.ibashkimi.lockscheduler.profiles;


import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;

/**
 * Listens to user actions from the UI ({@link ProfilesFragment}), retrieves the data and updates the
 * UI as required.
 */
public class ProfilesPresenter implements ProfilesContract.Presenter {

    private final ProfilesDataSource profilesRepository;

    private final ProfilesContract.View profilesView;

    private boolean mFirstLoad = true;

    public ProfilesPresenter(@NonNull ProfilesDataSource profilesRepository, @NonNull ProfilesContract.View profilesView) {
        this.profilesRepository = profilesRepository;
        this.profilesView = profilesView;

        profilesView.setPresenter(this);
    }

    @Override
    public void start() {
        loadProfiles(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
        if (AddEditProfileActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
            profilesView.showSuccessfullySavedMessage();
        }
    }

    @Override
    public void loadProfiles(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadProfiles(false, true);
        //loadProfiles(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link ProfilesDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadProfiles(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            profilesView.setLoadingIndicator(true);
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        //EspressoIdlingResource.increment(); // App is busy until further notice

        List<Profile> profiles = profilesRepository.getProfiles();
        if (!profilesView.isActive()) {
            return;
        }
        if (profiles == null) {
            profilesView.showLoadingProfilesError();
        } else {
            profilesView.showProfiles(profiles);
        }

        profilesView.showProfiles(profiles);
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
