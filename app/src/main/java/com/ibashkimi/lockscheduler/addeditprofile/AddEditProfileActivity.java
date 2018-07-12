package com.ibashkimi.lockscheduler.addeditprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.actions.ActionsFragment;
import com.ibashkimi.lockscheduler.addeditprofile.conditions.ConditionsFragment;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.model.action.Action;
import com.ibashkimi.lockscheduler.model.condition.Condition;
import com.ibashkimi.lockscheduler.ui.BaseActivity;

import java.util.List;

public class AddEditProfileActivity extends BaseActivity implements AddEditProfileContract.View, ConditionsFragment.ConditionChangeListener {

    public static final int REQUEST_ADD_PROFILE = 1;
    public static final int REQUEST_EDIT_PROFILE = 2;

    public static final String ENTER_ACTIONS_FRAGMENT = "enter_actions_fragment";
    public static final String EXIT_ACTIONS_FRAGMENT = "exit_actions_fragment";
    public static final String CONDITIONS_FRAGMENT = "conditions_fragment";

    public static final String ARGUMENT_EDIT_PROFILE_ID = "ARGUMENT_EDIT_PROFILE_ID";

    Toolbar toolbar;

    EditText mProfileName;

    private boolean mShowDelete;

    private AddEditProfileContract.Presenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_profile);

        toolbar = findViewById(R.id.toolbar);
        mProfileName = findViewById(R.id.profile_name);
        findViewById(R.id.fab).setOnClickListener(view -> onSave());

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
                ProfileManager.INSTANCE,
                this,
                savedInstanceState != null);
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

    @Override
    public void setPresenter(AddEditProfileContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoadProfileError() {

    }

    @Override
    public void showProfileList(boolean success, String extra) {
        if (extra != null) {
            Intent intent = new Intent();
            intent.putExtra("extra", extra);
            setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED, intent);
        } else {
            setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public void showProfile(Profile profile) {
        mShowDelete = true;
        mProfileName.setText(profile.getName());

        FragmentManager fragmentManager = getSupportFragmentManager();

        ActionsFragment enterActionsFragment = getEnterActionsFragment(fragmentManager);
        enterActionsFragment.setData(profile.getEnterActions());

        ActionsFragment exitActionsFragment = getEnterActionsFragment(fragmentManager);
        exitActionsFragment.setData(profile.getExitActions());

        ConditionsFragment conditionsFragment = getConditionsFragment(fragmentManager);
        conditionsFragment.setData(profile.getConditions());

        showFragments(fragmentManager, enterActionsFragment, exitActionsFragment, conditionsFragment);
    }

    @Override
    public void showEmptyProfile() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        showFragments(fragmentManager,
                getEnterActionsFragment(fragmentManager),
                getExitActionsFragment(fragmentManager),
                getConditionsFragment(fragmentManager));
    }

    @Override
    public void showTitle(int title) {
        toolbar.setTitle(title);
    }

    public void onSave() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Action> enterActions = getEnterActionsFragment(fragmentManager).assembleData();
        List<Action> exitActions = getExitActionsFragment(fragmentManager).assembleData();
        List<Condition> conditions = getConditionsFragment(fragmentManager).assembleConditions();
        String title = mProfileName.getText().toString();
        if (title.equals(""))
            title = getString(R.string.profile_name_hint);
        mPresenter.saveProfile(title, conditions, enterActions, exitActions);
    }

    public void showFragments(FragmentManager fragmentManager, Fragment enterActions, Fragment exitActions, Fragment conditions) {
        fragmentManager
                .beginTransaction()
                .replace(R.id.enter_actions_container, enterActions, ENTER_ACTIONS_FRAGMENT)
                .replace(R.id.exit_actions_container, exitActions, EXIT_ACTIONS_FRAGMENT)
                .replace(R.id.conditions_container, conditions, CONDITIONS_FRAGMENT)
                .commit();
    }

    @Override
    public boolean isActive() {
        return true;
    }

    private ActionsFragment getEnterActionsFragment(FragmentManager fragmentManager) {
        ActionsFragment actionsFragment = (ActionsFragment) fragmentManager.findFragmentByTag(ENTER_ACTIONS_FRAGMENT);
        if (actionsFragment == null) {
            actionsFragment = ActionsFragment.Companion.newInstance(true);
        }
        return actionsFragment;
    }

    private ActionsFragment getExitActionsFragment(FragmentManager fragmentManager) {
        ActionsFragment actionsFragment = (ActionsFragment) fragmentManager.findFragmentByTag(EXIT_ACTIONS_FRAGMENT);
        if (actionsFragment == null) {
            actionsFragment = ActionsFragment.Companion.newInstance(false);
        }
        return actionsFragment;
    }

    private ConditionsFragment getConditionsFragment(FragmentManager fragmentManager) {
        ConditionsFragment conditionsFragment = (ConditionsFragment) fragmentManager
                .findFragmentByTag(CONDITIONS_FRAGMENT);
        if (conditionsFragment == null) {
            conditionsFragment = ConditionsFragment.Companion.newInstance();
        }
        return conditionsFragment;
    }

    @Override
    public void onConditionChanged(@NonNull Condition condition) {
        Toast.makeText(this, "Condition changed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConditionRemoved(int type) {
        Toast.makeText(this, "Condition removed", Toast.LENGTH_SHORT).show();
    }
}
