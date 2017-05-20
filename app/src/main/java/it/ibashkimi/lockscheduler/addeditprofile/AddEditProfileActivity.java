package it.ibashkimi.lockscheduler.addeditprofile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
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

    public static final int REQUEST_ADD_PROFILE = 1;
    public static final int REQUEST_EDIT_PROFILE = 2;

    public static final String ENTER_ACTIONS_FRAGMENT = "enter_actions_fragment";
    public static final String EXIT_ACTIONS_FRAGMENT = "exit_actions_fragment";
    public static final String CONDITIONS_FRAGMENT = "conditions_fragment";

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
            Drawable icon = ContextCompat.getDrawable(this, R.drawable.ic_cancel_toolbar);
            /*TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.titleTextAppearance, typedValue, true);
            int color = ThemeUtils.getColorsFromStyle(this, typedValue.data, new int[]{R.attr.titleTextColor}, Color.GREEN)[0];
            //int color = ThemeUtils.getColorFromAttribute(toolbar.getContext(), android.R.attr.textColorPrimaryInverse);
            icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);*/

            actionBar.setHomeAsUpIndicator(icon);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String profileId = getIntent().getStringExtra(ARGUMENT_EDIT_PROFILE_ID);

        mPresenter = new AddEditProfilePresenter(
                profileId,
                ProfilesRepository.getInstance(),
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

    @OnClick(R.id.fab)
    public void onSave() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Action> enterActions = getEnterActionsFragment(fragmentManager).assembleData();
        List<Action> exitActions = getExitActionsFragment(fragmentManager).assembleData();
        List<Condition> conditions = getConditionsFragment(fragmentManager).assembleConditions();
        mPresenter.saveProfile(mProfileName.getText().toString(), conditions, enterActions, exitActions);
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
            actionsFragment = ActionsFragment.newInstance();
        }
        return actionsFragment;
    }

    private ActionsFragment getExitActionsFragment(FragmentManager fragmentManager) {
        ActionsFragment actionsFragment = (ActionsFragment) fragmentManager.findFragmentByTag(EXIT_ACTIONS_FRAGMENT);
        if (actionsFragment == null) {
            actionsFragment = ActionsFragment.newInstance();
        }
        return actionsFragment;
    }

    private ConditionsFragment getConditionsFragment(FragmentManager fragmentManager) {
        ConditionsFragment conditionsFragment = (ConditionsFragment) fragmentManager
                .findFragmentByTag(CONDITIONS_FRAGMENT);
        if (conditionsFragment == null) {
            conditionsFragment = ConditionsFragment.newInstance();
        }
        return conditionsFragment;
    }
}
