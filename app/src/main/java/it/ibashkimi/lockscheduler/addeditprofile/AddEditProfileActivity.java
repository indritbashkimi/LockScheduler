package it.ibashkimi.lockscheduler.addeditprofile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.actions.ActionsFragment;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsFragment;
import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.ui.BaseActivity;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class AddEditProfileActivity extends BaseActivity implements AddEditProfileContract.View {

    private static final String TAG = "AddEditProfileActivity";

    public static final int REQUEST_ADD_PROFILE = 1;
    public static final int REQUEST_EDIT_PROFILE = 2;

    public static final String ARGUMENT_EDIT_PROFILE_ID = "ARGUMENT_EDIT_PROFILE_ID";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.profile_name)
    EditText mProfileName;

    private boolean mShowDelete;

    private AddEditProfileContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profile);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String profileId = getIntent().getStringExtra(ARGUMENT_EDIT_PROFILE_ID);

        mPresenter = new AddEditProfilePresenter(
                profileId,
                ProfilesRepository.getInstance(),
                this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteMenu = menu.findItem(R.id.action_delete);
        deleteMenu.setVisible(mShowDelete);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mPresenter.discard();
                return true;
            case R.id.action_delete:
                mPresenter.deleteProfile();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Deprecated
    public void delete(Profile profile) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("profile", profile.toJson());
        resultIntent.setAction("delete");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void setPresenter(AddEditProfileContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoadProfileError() {

    }

    @Override
    public void showProfileList(boolean success, boolean deleted) {
        Log.d(TAG, "showProfileList() called");
        setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        if (deleted) {
            Intent intent = new Intent();
            intent.putExtra("deleted", true);
            setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
        }
        finish();
    }

    @Override
    public void showProfile(Profile profile) {
        mShowDelete = true;
        mProfileName.setText(profile.getName());

        FragmentManager fragmentManager = getSupportFragmentManager();

        ActionsFragment actionsFragment = getActionsFragment(fragmentManager);
        actionsFragment.setData(profile.getEnterActions(), profile.getExitActions());

        ConditionsFragment conditionsFragment = getConditionsFragment(fragmentManager);
        conditionsFragment.setData(profile.getConditions());

        showFragments(fragmentManager, actionsFragment, conditionsFragment);
    }

    @Override
    public void showEmptyProfile() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        showFragments(fragmentManager,
                getActionsFragment(fragmentManager),
                getConditionsFragment(fragmentManager));
    }

    @Override
    public void showTitle(int title) {
        getSupportActionBar().setTitle(title);
        //toolbar.setTitle(title);
    }

    @OnClick(R.id.fab)
    public void onSave() {
        Log.d(TAG, "onSave: ");
        List<List<Action>> actionData = getActionsFragment(getSupportFragmentManager()).assembleData();
        List<Action> enterActions = actionData.get(0);
        for (Action action : enterActions)
            Log.d(TAG, action.toJson());

        List<Condition> conditions = getConditionsFragment(getSupportFragmentManager()).assembleConditions();
        for (Condition condition : conditions)
            Log.d(TAG, condition.toJson());
        mPresenter.saveProfile(mProfileName.getText().toString(), conditions, actionData.get(0), actionData.get(1));
    }

    public void showFragments(FragmentManager fragmentManager, Fragment actions, Fragment conditions) {
        fragmentManager
                .beginTransaction()
                .replace(R.id.actions_container, actions, ActionsFragment.class.getName())
                .replace(R.id.conditions_container, conditions, ConditionsFragment.class.getName())
                .commit();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    private ActionsFragment getActionsFragment(FragmentManager fragmentManager) {
        ActionsFragment actionsFragment = (ActionsFragment) fragmentManager.findFragmentByTag(ActionsFragment.class.getName());
        if (actionsFragment == null) {
            actionsFragment = ActionsFragment.newInstance();
        }
        return actionsFragment;
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
