package it.ibashkimi.lockscheduler.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.model.source.local.ProfilesLocalDataSource;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileActivity extends BaseActivity {

    public static final String ARGUMENT_EDIT_PROFILE_ID = "EDIT_PROFILE_ID";

    private static final String TAG = "ProfileActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            long profileId = getIntent().getLongExtra(ARGUMENT_EDIT_PROFILE_ID, -1);
            Log.d(TAG, "onCreate: profileId = " + profileId);
            if (profileId != -1) {
                Profile profile = ProfilesRepository.getInstance().getProfile(profileId);
                attachFragment(profile.toJson(), true);
            } else {
                Profile profile = new Profile(System.currentTimeMillis());
                profile.getTrueActions().add(new LockAction());
                profile.getFalseActions().add(new LockAction());
                attachFragment(profile.toJson(), false);
            }
        }
    }

    private void attachFragment(String profileRep, boolean showDelete) {
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, ProfileFragment.newInstance(profileRep, showDelete))
                .commit();
    }

    public void cancel() {
        finish();
    }

    public void delete(Profile profile) {
        ProfilesRepository.getInstance().deleteProfile(profile.getId());
        finish();
    }

    public void save(Profile profile) {
        ProfilesRepository.getInstance().saveProfile(profile);
        Log.d(TAG, "save: returning: " + profile.toString());
        finish();
    }
}
