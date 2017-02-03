package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.ibashkimi.lockscheduler.model.Profile;

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class ProfilesRepository implements ProfilesDataSource {

    private static ProfilesRepository INSTANCE = null;

    private final ProfilesDataSource mProfilesLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */

    Map<Long, Profile> mCachedProfiles;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private ProfilesRepository(@NonNull ProfilesDataSource tasksLocalDataSource) {
        mProfilesLocalDataSource = tasksLocalDataSource;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param profilesLocalDataSource the device storage data source
     * @return the {@link ProfilesRepository} instance
     */
    public static ProfilesRepository getInstance(ProfilesDataSource profilesLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new ProfilesRepository(profilesLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(ProfilesDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link ProfilesDataSource.LoadProfilesCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getProfiles(@NonNull final LoadProfilesCallback callback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedProfiles != null && !mCacheIsDirty) {
            callback.onProfilesLoaded(new ArrayList<>(mCachedProfiles.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            //getTasksFromRemoteDataSource(callback);
            throw new RuntimeException("Da faq do I do now");
        } else {
            // Query the local storage if available. If not, query the network.
            mProfilesLocalDataSource.getProfiles(new LoadProfilesCallback() {
                @Override
                public void onProfilesLoaded(List<Profile> profiles) {
                    refreshCache(profiles);
                    callback.onProfilesLoaded(new ArrayList<>(mCachedProfiles.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    //getTasksFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void getProfile(long profileId, @NonNull final GetProfileCallback callback) {
        Profile cachedProfile = getProfileWithId(profileId);

        // Respond immediately with cache if available
        if (cachedProfile != null) {
            callback.onProfileLoaded(cachedProfile);
            return;
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mProfilesLocalDataSource.getProfile(profileId, new GetProfileCallback() {
            @Override
            public void onProfileLoaded(Profile profile) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedProfiles == null) {
                    mCachedProfiles = new LinkedHashMap<>();
                }
                mCachedProfiles.put(profile.getId(), profile);
                callback.onProfileLoaded(profile);
            }

            @Override
            public void onDataNotAvailable() {
               /* mTasksRemoteDataSource.getTask(taskId, new GetTaskCallback() {
                    @Override
                    public void onTaskLoaded(Task task) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedTasks == null) {
                            mCachedTasks = new LinkedHashMap<>();
                        }
                        mCachedTasks.put(task.getId(), task);
                        callback.onConditionLoaded(task);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });*/
            }
        });
    }

    @Override
    public void saveProfile(@NonNull Profile profile) {
        mProfilesLocalDataSource.saveProfile(profile);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedProfiles == null) {
            mCachedProfiles = new LinkedHashMap<>();
        }
        mCachedProfiles.put(profile.getId(), profile);
    }

    @Override
    public void refreshProfiles() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllProfiles() {
        mProfilesLocalDataSource.deleteAllProfiles();

        if (mCachedProfiles == null) {
            mCachedProfiles = new LinkedHashMap<>();
        }
        mCachedProfiles.clear();
    }

    @Override
    public void deleteProfile(@NonNull long profileId) {
        mProfilesLocalDataSource.deleteProfile(profileId);

        mCachedProfiles.remove(profileId);
    }

    @Override
    public void swapProfiles(int pos1, int pos2) {
        mProfilesLocalDataSource.swapProfiles(pos1, pos2);
    }

    private void refreshCache(List<Profile> profiles) {
        if (mCachedProfiles == null) {
            mCachedProfiles = new LinkedHashMap<>();
        }
        mCachedProfiles.clear();
        for (Profile task : profiles) {
            mCachedProfiles.put(task.getId(), task);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Profile> profiles) {
        mProfilesLocalDataSource.deleteAllProfiles();
        for (Profile profile : profiles) {
            mProfilesLocalDataSource.saveProfile(profile);
        }
    }

    @Nullable
    private Profile getProfileWithId(long id) {
        if (mCachedProfiles == null || mCachedProfiles.isEmpty()) {
            return null;
        } else {
            return mCachedProfiles.get(id);
        }
    }
}
