package it.ibashkimi.lockscheduler.profiles;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;


public class PresenterTest {

    @Test
    public void shouldPassProfilesToView() {
        // given
        ProfilesContract.View view = new MockView();
        ProfilesDataSource repository = new MockProfilesRepository(false);

        // when
        ProfilesPresenter presenter = new ProfilesPresenter(repository, view);
        presenter.loadProfiles();

        // then
        Assert.assertEquals(true, ((MockView) view).showProfilesCalled);
    }

    private class MockProfilesRepository implements ProfilesDataSource {
        boolean showEmptyProfiles;
        List<Profile> profiles;

        public MockProfilesRepository(boolean showEmptyProfiles) {
            if (showEmptyProfiles)
                profiles = new ArrayList<>();
            else {
                profiles = new ArrayList<>();
                profiles.add(new Profile("0"));
                profiles.add(new Profile("1"));
            }
        }

        @Override
        public List<Profile> getProfiles() {
            return profiles;
        }

        @Override
        public Profile get(String profileId) {
            return null;
        }

        @Override
        public void save(@NonNull Profile profile) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public void delete(String profileId) {

        }

        @Override
        public void substitute(@NonNull Profile newProfile, @Nullable Profile oldProfile) {

        }

        @Override
        public void update(Profile profile) {

        }

        @Override
        public void swap(int pos1, int pos2) {

        }
    }

    private class MockView implements ProfilesContract.View {
        ProfilesContract.Presenter presenter;
        boolean showProfilesCalled;

        @Override
        public void setPresenter(ProfilesContract.Presenter presenter) {
            this.presenter = presenter;
        }

        @Override
        public void setLoadingIndicator(boolean active) {

        }

        @Override
        public void showProfiles(List<Profile> profiles) {
            showProfilesCalled = true;
        }

        @Override
        public void showAddProfile() {

        }

        @Override
        public void showProfileDetailsUi(String profileId) {

        }

        @Override
        public void showLoadingProfilesError() {

        }

        @Override
        public void showNoProfiles() {

        }

        @Override
        public void showSwapProfile(int pos1, int pos2) {

        }

        @Override
        public void showSuccessfullySavedMessage() {

        }

        @Override
        public void showSuccessfullyRemovedMessage(int profilesRemoved) {

        }

        @Override
        public void showSuccessfullyUpdatedMessage() {

        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
}