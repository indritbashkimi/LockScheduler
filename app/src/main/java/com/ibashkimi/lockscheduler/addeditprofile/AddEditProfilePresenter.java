package com.ibashkimi.lockscheduler.addeditprofile;

import android.support.annotation.NonNull;

import java.util.List;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.model.action.Action;
import com.ibashkimi.lockscheduler.model.condition.Condition;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.ProfileRepository;

public class AddEditProfilePresenter implements AddEditProfileContract.Presenter {

    @NonNull
    private final ProfileRepository mRepository;

    @NonNull
    private final AddEditProfileContract.View mAddProfileView;

    private String mProfileId;

    public boolean mDataLoaded = false;

    public AddEditProfilePresenter(String profileId, @NonNull ProfileRepository repository, @NonNull AddEditProfileContract.View mAddProfileView, boolean dataLoaded) {
        this.mProfileId = profileId;
        this.mRepository = repository;
        this.mAddProfileView = mAddProfileView;
        this.mDataLoaded = dataLoaded;
    }

    @Override
    public void start() {
        if (!mDataLoaded && mAddProfileView.isActive()) {
            mAddProfileView.showTitle(isNewProfile() ? R.string.new_profile : R.string.edit_profile);
            if (!mDataLoaded) {
                if (!isNewProfile()) {
                    Profile profile = mRepository.get(mProfileId);
                    if (mAddProfileView.isActive()) {
                        mAddProfileView.showProfile(profile);
                    }
                } else {
                    mAddProfileView.showEmptyProfile();
                }
            }
            mDataLoaded = true;
        }
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
        mRepository.remove(mProfileId);
        mAddProfileView.showProfileList(true, "deleted");
    }

    @Override
    public void discard() {
        mAddProfileView.showProfileList(false, null);
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
            mRepository.add(newProfile);
            mAddProfileView.showProfileList(true, null);
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
            mRepository.update(newProfile);
            mAddProfileView.showProfileList(true, "updated");
        }
    }

    public boolean isValid(Profile profile) {
        return profile.getConditions().size() > 0;
    }
}
