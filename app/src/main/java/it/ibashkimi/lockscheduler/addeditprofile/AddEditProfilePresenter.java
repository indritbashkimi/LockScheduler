package it.ibashkimi.lockscheduler.addeditprofile;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

public class AddEditProfilePresenter implements AddEditProfileContract.Presenter, ProfilesDataSource.GetProfileCallback {

    private static final String TAG = "AddEditProfilePresenter";

    @NonNull
    private final ProfilesDataSource mProfilesRepository;

    @NonNull
    private final AddEditProfileContract.View mAddProfileView;

    private long mProfileId;

    private boolean mIsDataMissing;

    public AddEditProfilePresenter(long profileId, @NonNull ProfilesDataSource mProfilesRepository, @NonNull AddEditProfileContract.View mAddProfileView) {
        this.mProfileId = profileId;
        this.mProfilesRepository = mProfilesRepository;
        this.mAddProfileView = mAddProfileView;
    }

    @Override
    public void start() {
        if (!isNewProfile())
            mProfilesRepository.getProfile(mProfileId, this);
        else
            mAddProfileView.showEmptyProfile();
    }

    @Override
    public void saveProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions) {
        if (isNewProfile()) {
            createProfile(title, conditions, trueActions, falseActions);
        } else {
            updateProfile(title, conditions, trueActions, falseActions);
        }
    }

    @Override
    public void deleteProfile() {
        mProfilesRepository.deleteProfile(mProfileId);
        mAddProfileView.showProfileList();
    }

    @Override
    public void populateProfile() {
        if (isNewProfile()) {
            throw new RuntimeException("populateProfile() was called but profile is new.");
        }
        mProfilesRepository.getProfile(mProfileId, this);
    }

    @Override
    public void requestSave() {
        mAddProfileView.save();
    }

    @Override
    public boolean isDataMissing() {
        return mIsDataMissing;
    }

    @Override
    public void onProfileLoaded(Profile profile) {
        // The view may not be able to handle UI updates anymore
        if (mAddProfileView.isActive()) {
            mAddProfileView.showProfile(profile);
        }
        mIsDataMissing = false;
    }

    @Override
    public void onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddProfileView.isActive()) {
            mAddProfileView.showEmptyProfileError();
        }
    }


    private boolean isNewProfile() {
        return mProfileId == -1;
    }

    private void createProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions) {
        Profile newProfile = new Profile(
                System.currentTimeMillis(),
                title,
                conditions,
                trueActions,
                falseActions);
        if (newProfile.isEmpty()) {
            mAddProfileView.showEmptyProfileError();
        } else {
            mProfilesRepository.saveProfile(newProfile);
            mAddProfileView.showProfileList();
        }
    }

    private void updateProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions) {
        if (isNewProfile()) {
            throw new RuntimeException("updateProfile() was called but task is new.");
        }
        Profile newProfile = new Profile(
                mProfileId,
                title,
                conditions,
                trueActions,
                falseActions);
        if (newProfile.isEmpty()) {
            mAddProfileView.showEmptyProfileError();
        } else {
            mProfilesRepository.saveProfile(newProfile);
            mAddProfileView.showProfileList();
        }
    }
}
