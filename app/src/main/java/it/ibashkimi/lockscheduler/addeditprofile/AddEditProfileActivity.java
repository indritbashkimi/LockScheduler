package it.ibashkimi.lockscheduler.addeditprofile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;

import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.actions.ActionsContract;
import it.ibashkimi.lockscheduler.addeditprofile.actions.ActionsFragment;
import it.ibashkimi.lockscheduler.addeditprofile.actions.ActionsPresenter;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsContract;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsFragment;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsPresenter;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.ui.BaseActivity;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class AddEditProfileActivity extends BaseActivity implements AddEditProfileContract.View, ConditionsProvider {

    public static final int REQUEST_ADD_TASK = 1;

    public static final String ACTION_NEW = "it.ibashkimi.lockscheduler.profile.new";
    public static final String ACTION_VIEW = "it.ibashkimi.lockscheduler.profile.view";

    private static final String TAG = "AddEditProfileActivity";

    public static final String EXTRA_PROFILE_ID = "extra_profile_id";

    private static final String ARGUMENT_EDIT_TASK_ID = "edit_task_id";

    private EditText mTitle;

    private AddEditProfileContract.Presenter mPresenter;

    private ConditionsContract.Presenter mConditionsPresenter;

    private ActionsContract.Presenter mActionsPresenter;

    private ActionsFragment mActionsFragment;

    private ConditionsFragment mConditionsFragment;

    private Profile mProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel);
        }

        mTitle = (EditText) findViewById(R.id.input_name);

        String profileId = getIntent().getStringExtra(ARGUMENT_EDIT_TASK_ID);

        mPresenter = new AddEditProfilePresenter(
                profileId,
                ProfilesRepository.getInstance(),
                this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        ConditionsFragment conditionsFragment = (ConditionsFragment) fragmentManager
                .findFragmentByTag(ConditionsFragment.class.getName());
        if (conditionsFragment == null) {
            conditionsFragment = ConditionsFragment.newInstance();
        }
        //ConditionsPresenter mConditionsPresenter = new ConditionsPresenter(conditions, conditionsFragment);
        //conditionsFragment.setPresenter(mConditionsPresenter);

        fragmentManager
                .beginTransaction()
                .replace(R.id.conditions_container, conditionsFragment, ConditionsFragment.class.getName())
                .commit();

        /*FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ConditionsFragment conditionsFragment = (ConditionsFragment) fragmentManager.findFragmentByTag(ConditionsFragment.class.getName());
        if (conditionsFragment == null) {
            conditionsFragment = ConditionsFragment.newInstance();
            fragmentTransaction.replace(R.id.conditions_container, conditionsFragment, ConditionsFragment.class.getName());
        }
        mConditionsPresenter = new ConditionsPresenter(conditionsFragment);
        conditionsFragment.setPresenter(mConditionsPresenter);
        ActionsFragment actionsFragment = (ActionsFragment) fragmentManager.findFragmentByTag(ActionsFragment.class.getName());
        if (actionsFragment == null) {
            actionsFragment = ActionsFragment.newInstance();
            fragmentTransaction.replace(R.id.actions_container, actionsFragment, ActionsFragment.class.getName());
        }
        mActionsPresenter = new ActionsPresenter(actionsFragment);
        fragmentTransaction.commit();*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        mPresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Deprecated
    public void cancel() {
        finish();
    }

    @Deprecated
    public void delete(Profile profile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profile", profile.toJson());
        resultIntent.setAction("delete");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Deprecated
    public void save(Profile profile) {
        mConditionsFragment.requestSave();

        /*Intent resultIntent = new Intent();
        resultIntent.putExtra("profile", profile.toJson());
        String action = getIntent().getAction().equals(ACTION_NEW) ? "new" : "update";
        resultIntent.setAction(action);
        setResult(Activity.RESULT_OK, resultIntent);
        Log.d(TAG, "save: returning: " + profile.toString());
        finish();*/
    }

    @Override
    public void setPresenter(AddEditProfileContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showEmptyProfileError() {

    }

    @Override
    public void showProfileList() {

    }

    @Override
    public void showProfile(Profile profile) {
        mProfile = profile;
        showFragments();
    }

    @Override
    public void showEmptyProfile() {
        showFragments();
    }

    @Override
    public void save() {

    }

    public void showFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        mConditionsFragment = (ConditionsFragment) fragmentManager
                .findFragmentByTag(ConditionsFragment.class.getName());
        if (mConditionsFragment == null) {
            mConditionsFragment = ConditionsFragment.newInstance();
        }
        mConditionsPresenter = new ConditionsPresenter(this, mConditionsFragment);
        mConditionsFragment.setPresenter(mConditionsPresenter);

        mActionsFragment = (ActionsFragment) fragmentManager
                .findFragmentByTag(ActionsFragment.class.getName());
        if (mActionsFragment == null) {
            mActionsFragment = ActionsFragment.newInstance();
        }
        mActionsPresenter = new ActionsPresenter(mActionsFragment);
        mActionsFragment.setPresenter(mActionsPresenter);

        fragmentManager
                .beginTransaction()
                .replace(R.id.actions_container, mActionsFragment, ActionsFragment.class.getName())
                .replace(R.id.conditions_container, mConditionsFragment, ConditionsFragment.class.getName())
                .commit();
    }

    @Override
    public List<Condition> getConditions() {
        return mProfile != null ? mProfile.getConditions() : null;
    }

    @Override
    public void updateConditions(List<Condition> conditions) {
        getProfile().setConditions(conditions);
    }

    private Profile getProfile() {
        if (mProfile == null) {
            mProfile = new Profile(Long.toString(System.currentTimeMillis()));
            mProfile.getTrueActions().add(new LockAction());
            mProfile.getFalseActions().add(new LockAction());
        }
        return mProfile;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    private ConditionsFragment getConditionsFragment(FragmentManager fragmentManager) {
        ConditionsFragment conditionsFragment = (ConditionsFragment) fragmentManager
                .findFragmentByTag(ConditionsFragment.class.getName());
        if (conditionsFragment == null) {
            conditionsFragment = ConditionsFragment.newInstance();
        }
        return conditionsFragment;
    }
}
