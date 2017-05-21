package it.ibashkimi.lockscheduler.model.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;

/**
 * Main entry point for accessing tasks data.
 * <p>
 * For simplicity, only getRegisteredProfiles() and get() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new task is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface ProfilesDataSource {

    void beginTransaction();

    void endTransaction();

    List<Profile> getProfiles();

    @Nullable
    Profile getProfile(@NonNull String profileId);

    List<Profile> getConditionProfiles(@Condition.Type int conditionType);

    void saveProfile(@NonNull Profile profile);

    void saveCondition(@NonNull String profileId, @Condition.Type int conditionType);

    void deleteProfiles();

    void deleteConditions();

    void deleteProfile(@NonNull String profileId);

    void deleteCondition(@NonNull String profileId, @Condition.Type int conditionType);

    void updateProfile(@NonNull Profile profile);

    void swapProfiles(@NonNull String id1, @NonNull String id2);
}
