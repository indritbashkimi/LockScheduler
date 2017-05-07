package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;

/**
 * Main entry point for accessing tasks data.
 * <p>
 * For simplicity, only getProfiles() and get() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new task is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface ProfilesDataSource {

    List<Profile> getProfiles();

    @Nullable
    Profile get(@NonNull String profileId);

    void save(@NonNull Profile profile);

    void deleteAll();

    void delete(@NonNull String profileId);

    void substitute(@NonNull Profile newProfile, @Nullable Profile oldProfile);

    void update(@NonNull Profile profile);

    void swap(int pos1, int pos2);
}
