package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.api.ProfileApiHelper;
import it.ibashkimi.lockscheduler.model.source.local.ProfilesLocalDataSource;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class ProfilesRepository implements ProfilesDataSource {

    private static final String TAG = "ProfilesRepository";

    private static ProfilesRepository INSTANCE = null;

    private final ProfilesDataSource mProfilesLocalDataSource;

    private final ProfileApiHelper mProfileApiHelper;

    // Prevent direct instantiation.
    private ProfilesRepository(@NonNull ProfilesDataSource profilesLocalDataSource, @NonNull ProfileApiHelper profileApiHelper) {
        mProfilesLocalDataSource = profilesLocalDataSource;
        mProfileApiHelper = profileApiHelper;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link ProfilesRepository} instance
     */
    public static ProfilesRepository getInstance() {
        if (INSTANCE == null) {
            ProfilesDataSource profilesLocalDataSource = ProfilesLocalDataSource.getInstance(App.getInstance());
            ProfileApiHelper profileApiHelper = ProfileApiHelper.getInstance(App.getGeofenceApiHelper());
            INSTANCE = new ProfilesRepository(profilesLocalDataSource, profileApiHelper);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public List<Profile> getProfiles() {
        return mProfilesLocalDataSource.getProfiles();
    }

    @Override
    public Profile getProfile(long profileId) {
        return mProfilesLocalDataSource.getProfile(profileId);
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        mProfilesLocalDataSource.saveProfile(profile);
        mProfileApiHelper.addProfile(profile);
    }

    @Override
    public void deleteAllProfiles() {
        mProfileApiHelper.removeAll(getProfiles());
        mProfilesLocalDataSource.deleteAllProfiles();
    }

    @Override
    public void deleteProfile(long profileId) {
        mProfileApiHelper.removeProfile(getProfile(profileId));
        mProfilesLocalDataSource.deleteProfile(profileId);
    }

    @Override
    public void updateProfile(Profile profile) {
        mProfileApiHelper.update(getProfile(profile.getId()), profile);
        mProfilesLocalDataSource.updateProfile(profile);
    }

    @Override
    public void swapProfiles(int pos1, int pos2) {
        mProfilesLocalDataSource.swapProfiles(pos1, pos2);
    }
}
