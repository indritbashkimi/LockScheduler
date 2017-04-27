package it.ibashkimi.lockscheduler.profiles;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.MapsInitializer;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.AddEditProfileActivity;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.source.ProfilesDataSource;
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository;
import it.ibashkimi.lockscheduler.model.source.local.ProfilesLocalDataSource;
import it.ibashkimi.lockscheduler.ui.ProfileActivity;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.support.utils.ThemeUtils;

/**
 * Fragment used to display profile list.
 */
public class ProfilesFragment extends Fragment implements ProfilesContract.View, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "ProfilesFragment";

    private ProfilesContract.Presenter mPresenter;

    private ProfileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mItemLayout;
    private int mMapStyle;
    private SharedPreferences mSettings;

    private ActionMode actionMode;

    private View mNoTasksView;

    private ImageView mNoTaskIcon;

    private TextView mNoTaskMainView;

    private TextView mNoTaskAddView;

    private View mProfileView;

    public static ProfilesFragment newInstance() {
        return new ProfilesFragment();
    }

    public ProfilesFragment() {
        // Requires empty public constructor
    }

    @Override
    public void setPresenter(ProfilesContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        mMapStyle = MapUtils.resolveMapStyle(mSettings.getInt("map_style", 0));
        int itemLayout = mSettings.getInt("item_layout", 1);
        mItemLayout = resolveLayout(itemLayout);

        if (mItemLayout == R.layout.item_profile_7) {
            mAdapter = new ProfileAdapterImpl2(new ArrayList<Profile>(0), mItemLayout, mItemListener);
        } else {
            MapsInitializer.initialize(getActivity());
            mAdapter = new ProfileAdapterImpl(getContext(), new ArrayList<Profile>(0), mItemLayout, mMapStyle, mItemListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(mAdapter);

        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.addNewProfile();
            }
        });

        mProfileView = mRecyclerView;//(LinearLayout) rootView.findViewById(R.id.recyclerView);

        // Set up  no tasks view
        mNoTasksView = rootView.findViewById(R.id.noTasks);
        mNoTaskIcon = (ImageView) rootView.findViewById(R.id.noTasksIcon);
        mNoTaskMainView = (TextView) rootView.findViewById(R.id.noTasksMain);
        mNoTaskAddView = (TextView) rootView.findViewById(R.id.noTasksAdd);
        mNoTaskAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProfile();
            }
        });

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettings.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        mSettings.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    public ProfileAdapter getAdapter() {
        return mAdapter;
    }

    private ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Log.d(TAG, "onMove: " + viewHolder.getAdapterPosition() + ", " + target.getAdapterPosition());
            int targetPosition = target.getAdapterPosition();
            mPresenter.swapProfiles(viewHolder.getAdapterPosition(), targetPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    });

    private static int resolveLayout(int itemLayout) {
        switch (itemLayout) {
            case 0:
                return R.layout.item_profile_0;
            case 1:
                return R.layout.item_profile_1;
            case 2:
                return R.layout.item_profile_2;
            case 3:
                return R.layout.item_profile_3;
            case 4:
                return R.layout.item_profile_4;
            case 5:
                return R.layout.item_profile_5;
            case 6:
                return R.layout.item_profile_6;
            case 7:
                return R.layout.item_profile_7;
            default:
                return R.layout.item_profile_6;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("item_layout")) {
            mItemLayout = resolveLayout(Integer.parseInt(sharedPreferences.getString("item_layout", "0")));
        } else if (s.equals("map_style")) {
            mMapStyle = MapUtils.resolveMapStyle(sharedPreferences.getInt("map_style", 0));
        }
    }

    private ProfileItemListener mItemListener = new ProfileItemListener() {
        @Override
        public void onProfileClick(int position, Profile profile) {
            if (actionMode != null) {
                toggleSelection(position);
            } else {
                Profile p = (Profile) mAdapter.getProfiles().get(position);
                mPresenter.openProfileDetails(p);
            }
        }

        @Override
        public void onProfileLongClick(int position, Profile profile) {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
            }
            toggleSelection(position);

            mAdapter.toggleSelection(position);
            mAdapter.notifyItemChanged(position);
        }
    };

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
        mAdapter.notifyItemChanged(position);

        int count = mAdapter.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showProfiles(List<Profile> profiles) {
        Log.d(TAG, "showProfiles() called with: profiles = [" + profiles + "]");
        mAdapter.setData(profiles);

        mProfileView.setVisibility(View.VISIBLE);
        mNoTasksView.setVisibility(View.GONE);
    }

    @Override
    public void showAddProfile() {
        // TODO: 27/04/17
        //Intent intent = new Intent(getContext(), AddEditProfileActivity.class);
        //startActivityForResult(intent, AddEditProfileActivity.REQUEST_ADD_TASK);

        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        getActivity().startActivityForResult(intent, 0);
    }

    @Override
    public void showProfileDetailsUi(String profileId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        // TODO: 27/04/17
        /*Intent intent = new Intent(getContext(), AddEditProfileActivity.class);
        intent.putExtra(AddEditProfileActivity.EXTRA_PROFILE_ID, profileId);
        startActivity(intent);*/

        Profile profile = ProfilesRepository.getInstance().getProfile(profileId);
        Log.d(TAG, "onProfileLoaded() called with: profile = [" + profile + "]");
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.ARGUMENT_EDIT_PROFILE_ID, profile.getId());
        getActivity().startActivityForResult(intent, ProfilesActivity.RESULT_PROFILE);
    }

    @Override
    public void showLoadingProfilesError() {
        showMessage(getString(R.string.loading_profiles_error));
    }

    @Override
    public void showNoProfiles() {
        showNoTasksViews(
                getResources().getString(R.string.no_profiles_all),
                R.drawable.ic_no_profiles,
                false
        );
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
    public void showSuccessfullyRemovedMessage(int profilesRemoved) {
        showMessage(getString(R.string.successfully_removed_profile_message));
    }

    private void showNoTasksViews(String mainText, int iconRes, boolean showAddView) {
        mProfileView.setVisibility(View.GONE);
        mNoTasksView.setVisibility(View.VISIBLE);

        mNoTaskMainView.setText(mainText);
        mNoTaskIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), iconRes));
        mNoTaskAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }


    public interface ProfileItemListener {

        void onProfileClick(int position, Profile profile);

        void onProfileLongClick(int position, Profile profile);
    }


    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.profile_selected, menu);
            // Tint drawable
            Drawable drawable = menu.findItem(R.id.action_delete).getIcon();
            drawable = DrawableCompat.wrap(drawable);
            // It works but it's a strange dependency
            Context context = getActivity().findViewById(R.id.toolbar).getContext();
            DrawableCompat.setTint(drawable, ThemeUtils.getColorFromAttribute(context, android.R.attr.textColorPrimary));
            menu.findItem(R.id.action_delete).setIcon(drawable);
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
                        //Log.d(TAG, "onActionItemClicked: i = " + i);
                        int position = items.get(i);
                        //ProfilesRepository.getInstance().deleteProfile((Profile) mAdapter.getProfiles().get(position));
                        mAdapter.notifyItemRemoved(position);
                    }
                    mAdapter.clearSelection();
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.clearSelection();
            actionMode = null;
        }
    }
}
