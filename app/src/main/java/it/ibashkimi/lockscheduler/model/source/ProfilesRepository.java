package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.scheduler.ProfileScheduler;
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

    private final ProfileScheduler mScheduler;

    // Prevent direct instantiation.
    private ProfilesRepository(@NonNull ProfilesDataSource profilesLocalDataSource, @NonNull ProfileScheduler scheduler) {
        mProfilesLocalDataSource = profilesLocalDataSource;
        mScheduler = scheduler;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link ProfilesRepository} instance
     */
    public static ProfilesRepository getInstance() {
        if (INSTANCE == null) {
            ProfilesDataSource profilesLocalDataSource = ProfilesLocalDataSource.getInstance(App.getInstance());
            ProfileScheduler scheduler = ProfileScheduler.Companion.getInstance();
            INSTANCE = new ProfilesRepository(profilesLocalDataSource, scheduler);
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
    @Nullable
    public Profile get(@NonNull String profileId) {
        return mProfilesLocalDataSource.get(profileId);
    }

    @Override
    public void save(@NonNull Profile profile) {
        mProfilesLocalDataSource.save(profile);
        mScheduler.register(profile);
    }

    @Override
    public void deleteAll() {
        for (Profile profile : getProfiles())
            mScheduler.unregister(profile);
        mProfilesLocalDataSource.deleteAll();
    }

    @Override
    public void delete(@NonNull String profileId) {
        Profile profile = get(profileId);
        if (profile != null) {
            mScheduler.unregister(profile);
            mProfilesLocalDataSource.delete(profileId);
        }
    }

    @Override
    public void substitute(@NonNull Profile newProfile, @Nullable Profile oldProfile) {
        if (oldProfile == null)
            oldProfile = get(newProfile.getId());
        mProfilesLocalDataSource.substitute(newProfile, oldProfile);
        mScheduler.unregister(oldProfile);
        mScheduler.register(newProfile);
        //mScheduler.substitute(newProfile, oldProfile);
    }

    @Override
    public void update(@NonNull Profile profile) {
       mProfilesLocalDataSource.update(profile);
    }

    @Override
    public void swap(int pos1, int pos2) {
        mProfilesLocalDataSource.swap(pos1, pos2);
    }
}
