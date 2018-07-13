package com.ibashkimi.lockscheduler.profiles;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import com.ibashkimi.lockscheduler.model.Profile;
import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.util.PlatformUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Fragment used to display profile list.
 */
public class ProfilesFragment extends Fragment implements ProfilesContract.View, ProfileAdapter.Callback {

    private ViewGroup mRootView;

    private RecyclerView mRecyclerView;

    private View mNoTasksView;

    private ProfilesContract.Presenter mPresenter;

    private ProfileAdapter mAdapter;

    private ActionMode mActionMode;

    public ProfilesFragment() {
        // Requires empty public constructor
    }

    @Override
    public void setPresenter(ProfilesContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        setPresenter(new ProfilesPresenter(ProfileManager.INSTANCE, this));

        mAdapter = new ProfileAdapter(new ArrayList<>(0), R.layout.item_profile, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode, data != null ? data.getStringExtra("extra") : null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment);
        if (NavigationUI.onNavDestinationSelected(item, navController)) {
            return true;
        } else if (item.getItemId() == R.id.action_uninstall) {
            PlatformUtils.uninstall(requireContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profiles, container, false);
        mRootView = rootView.findViewById(R.id.root);
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mNoTasksView = rootView.findViewById(R.id.no_profiles);

        RecyclerView.LayoutManager layoutManager;
        int columnCount = getResources().getInteger(R.integer.profiles_column_count);
        if (columnCount == 1)
            layoutManager = new LinearLayoutManager(getContext());
        else
            layoutManager = new GridLayoutManager(getContext(), columnCount);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        rootView.findViewById(R.id.fab).setOnClickListener(v -> mPresenter.addNewProfile());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    private ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int targetPosition = target.getAdapterPosition();
            int pos1 = viewHolder.getAdapterPosition();
            mPresenter.swapProfiles(mAdapter.getProfiles().get(pos1), mAdapter.getProfiles().get(targetPosition));

            if (mAdapter.isSelected(viewHolder.getAdapterPosition()) != mAdapter.isSelected(targetPosition)) {
                mAdapter.toggleSelection(viewHolder.getAdapterPosition());
                mAdapter.toggleSelection(targetPosition);
            }
            // TODO: Move adapter data swap to presenter
            List<Profile> profiles = mAdapter.getProfiles();
            Profile profile = profiles.get(viewHolder.getAdapterPosition());
            profiles.set(viewHolder.getAdapterPosition(), profiles.get(targetPosition));
            profiles.set(targetPosition, profile);

            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), targetPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    });

    /**
     * Toggle the selection state of an item.
     * <p>
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        mAdapter.notifyDataSetChanged();
        // Bug: It's better to call mAdapter.notifyItemChanged(position) but it shows the wrong item.

        int count = mAdapter.getSelectedItemCount();
        if (count == 0) {
            mActionMode.finish();
        } else {
            mActionMode.setTitle(String.valueOf(count));
            mActionMode.invalidate();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showProfiles(List<Profile> profiles) {
        mAdapter.setData(profiles);

        mRecyclerView.setVisibility(View.VISIBLE);
        mNoTasksView.setVisibility(View.GONE);
    }

    @Override
    public void showAddProfile() {
        Intent intent = new Intent(getContext(), AddEditProfileActivity.class);
        startActivityForResult(intent, AddEditProfileActivity.REQUEST_ADD_PROFILE);
    }

    @Override
    public void showProfileDetailsUi(String profileId) {
        Intent intent = new Intent(getContext(), AddEditProfileActivity.class);
        intent.putExtra(AddEditProfileActivity.ARGUMENT_EDIT_PROFILE_ID, profileId);
        startActivityForResult(intent, AddEditProfileActivity.REQUEST_EDIT_PROFILE);
    }

    @Override
    public void showLoadingProfilesError() {
        showMessage(getString(R.string.loading_profiles_error));
    }

    @Override
    public void showNoProfiles() {
        mRecyclerView.setVisibility(View.GONE);
        mNoTasksView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSwapProfile(int pos1, int pos2) {
        if (mAdapter.isSelected(pos1) != mAdapter.isSelected(pos2)) {
            mAdapter.toggleSelection(pos1);
            mAdapter.toggleSelection(pos2);
        }
        mAdapter.notifyItemMoved(pos1, pos2);
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_profile_message));
    }

    @Override
    public void showSuccessfullyRemovedMessage() {
        showMessage(getString(R.string.successfully_removed_profile_message));
    }

    @Override
    public void showSuccessfullyUpdatedMessage() {
        showMessage("Profile updated.");
    }

    private void showMessage(String message) {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onProfileClick(int position) {
        if (mActionMode != null) {
            toggleSelection(position);
        } else {
            Profile p = mAdapter.getProfiles().get(position);
            mPresenter.openProfileDetails(p);
        }
    }

    @Override
    public void onProfileLongClick(int position) {
        if (mActionMode == null) {
            mActionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(new ActionModeCallback());
        }
        toggleSelection(position);
    }


    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.profile_selected, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    List<Integer> items = mAdapter.getSelectedItems();
                    for (int i = items.size() - 1; i > -1; i--) {
                        int position = items.get(i);
                        ProfileManager.INSTANCE.remove(mAdapter.getProfiles().get(position).getId());
                    }
                    mAdapter.clearSelection();
                    mPresenter.loadProfiles();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            mActionMode = null;
        }
    }
}
