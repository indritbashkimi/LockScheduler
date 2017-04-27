package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;

/**
 * Main entry point for accessing tasks data.
 * <p>
 * For simplicity, only getProfiles() and getProfile() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new task is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface ProfilesDataSource {

    List<Profile> getProfiles();

    Profile getProfile(long profileId);

    void saveProfile(@NonNull Profile profile);

    void deleteAllProfiles();

    void deleteProfile(long profileId);

    void updateProfile(Profile profile);

    void swapProfiles(int pos1, int pos2);
}
