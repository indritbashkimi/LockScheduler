package it.ibashkimi.lockscheduler.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.Profile;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileActivity extends BaseActivity {
    public static final String ACTION_NEW = "it.ibashkimi.lockscheduler.profile.new";
    public static final String ACTION_VIEW = "it.ibashkimi.lockscheduler.profile.view";

    private static final String TAG = "ProfileActivity";

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            String action = getIntent().getAction();
            String profileRep = null;
            switch (action) {
                case ACTION_NEW:
                    Profile profile = new Profile(System.currentTimeMillis());
                    profile.getTrueActions().add(new LockAction());
                    profile.getFalseActions().add(new LockAction());
                    profileRep = profile.toJson();
                    break;
                case ACTION_VIEW:
                    profileRep = getIntent().getStringExtra("profile");
                    break;
                default:
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, ProfileFragment.newInstance(profileRep, !action.equals(ACTION_NEW)))
                    .commit();
        }
    }

    public void cancel() {
        finish();
    }

    public void delete(Profile profile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profile", profile.toJson());
        resultIntent.setAction("delete");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void save(Profile profile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profile", profile.toJson().toString());
        String action = getIntent().getAction().equals(ACTION_NEW) ? "new" : "update";
        resultIntent.setAction(action);
        setResult(Activity.RESULT_OK, resultIntent);
        Log.d(TAG, "save: returning: " + profile.toString());
        finish();
    }
}
