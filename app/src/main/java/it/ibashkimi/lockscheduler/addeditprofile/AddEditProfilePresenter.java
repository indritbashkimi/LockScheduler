package it.ibashkimi.lockscheduler.addeditprofile;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;

public class AddEditProfilePresenter implements AddEditProfileContract.Presenter {

    private static final String TAG = "AddEditProfilePresenter";

    @NonNull
    private final ProfilesDataSource mProfilesRepository;

    @NonNull
    private final AddEditProfileContract.View mAddProfileView;

    private String mProfileId;

    private boolean mDataLoaded = false;

    public AddEditProfilePresenter(String profileId, @NonNull ProfilesDataSource mProfilesRepository, @NonNull AddEditProfileContract.View mAddProfileView) {
        this.mProfileId = profileId;
        this.mProfilesRepository = mProfilesRepository;
        this.mAddProfileView = mAddProfileView;
    }

    @Override
    public void start() {
        if (mAddProfileView.isActive()) {

        }
        mAddProfileView.showTitle(isNewProfile() ? R.string.new_profile : R.string.edit_profile);
        if (!mDataLoaded) {
            if (!isNewProfile()) {
                Profile profile = mProfilesRepository.get(mProfileId);
                Log.d(TAG, "start: profile = " + profile);
                if (mAddProfileView.isActive()) {
                    mAddProfileView.showProfile(profile);
                }
            } else {
                Log.d(TAG, "start: here");
                mAddProfileView.showEmptyProfile();
            }
        }
        mDataLoaded = true;
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
        mProfilesRepository.delete(mProfileId);
        mAddProfileView.showProfileList(true, true);
    }

    @Override
    public void discard() {
        mAddProfileView.showProfileList(false, false);
    }

    private boolean isNewProfile() {
        return mProfileId == null;
    }

    private void createProfile(String title, List<Condition> conditions, List<Action> trueActions, List<Action> falseActions) {
        Profile newProfile = new Profile(
                Long.toString(System.currentTimeMillis()),
                title,
                conditions,
                trueActions,
                falseActions);
        if (!isValid(newProfile)) {
            mAddProfileView.showLoadProfileError();
        } else {
            mProfilesRepository.save(newProfile);
            mAddProfileView.showProfileList(true, false);
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
        if (!isValid(newProfile)) {
            mAddProfileView.showLoadProfileError();
        } else {
            mProfilesRepository.substitute(newProfile, null);
            mAddProfileView.showProfileList(true, false);
        }
    }

    public boolean isValid(Profile profile) {
        return profile.getConditions().size() > 0;
    }
}
