package it.ibashkimi.lockscheduler.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.ProfileLoader;
import it.ibashkimi.lockscheduler.ui.recyclerview.ProfileAdapter;
import it.ibashkimi.lockscheduler.util.MapUtils;
import it.ibashkimi.support.utils.ThemeUtils;

/**
 * Fragment used to display profile list.
 */
public class ProfileListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, ProfileAdapter.ClickListener {
    private static final String TAG = "ProfileListFragment";

    private ProfileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mItemLayout;
    private int mMapStyle;
    private SharedPreferences mSettings;
    private ItemTouchHelper mItemTouchHelper;
    private ActionMode actionMode;

    private final LoaderManager.LoaderCallbacks<List<Profile>> mLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Profile>>() {

                @Override
                public Loader<List<Profile>> onCreateLoader(int id, Bundle args) {
                    return new ProfileLoader(getContext());
                }

                @Override
                public void onLoadFinished(
                        Loader<List<Profile>> loader, List<Profile> data) {
                    // Display our data, for instance updating our adapter
                    mAdapter.setData(data);
                   /* if (recyclerView != null)
                        recyclerView.scrollToPosition(data.size() - 1);*/
                }

                @Override
                public void onLoaderReset(Loader<List<Profile>> loader) {
                    // Loader reset, throw away our data, unregister any listeners, etc.
                    mAdapter.setData(null);
                    // Of course, unless you use destroyLoader(),this is called when everything
                    // is already dying so a completely empty onLoaderReset() is totally acceptable
                }
            };

    public ProfileListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mMapStyle = MapUtils.resolveMapStyle(mSettings.getInt("map_style", 0));
        int itemLayout = mSettings.getInt("item_layout", 1);
        mItemLayout = resolveLayout(itemLayout);

        getActivity().getSupportLoaderManager().initLoader(0, null, mLoaderCallbacks);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        //mRecyclerView.setItemAnimator(new SlideInRightAnimator(new LinearOutSlowInInterpolator()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new ProfileAdapter(getContext(), new ArrayList<Profile>(0), mItemLayout, mMapStyle, this);
        mRecyclerView.setAdapter(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
                /*if (mAdapter.isSelected(fromPos)) {
                    toggleSelection(fromPos);
                }
                if (mAdapter.isSelected(toPos)) {
                    toggleSelection(toPos);
                }*/
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: " + viewHolder.getAdapterPosition() + ", " + target.getAdapterPosition());
                int targetPosition = target.getAdapterPosition();
                App.getProfileApiHelper().swap(viewHolder.getAdapterPosition(), targetPosition);
                if (mAdapter.isSelected(viewHolder.getAdapterPosition()) != mAdapter.isSelected(targetPosition)) {
                    mAdapter.toggleSelection(viewHolder.getAdapterPosition());
                    mAdapter.toggleSelection(targetPosition);
                }
                /*if (mAdapter.isSelected(viewHolder.getAdapterPosition())) {
                    toggleSelection(viewHolder.getAdapterPosition());
                }
                if (mAdapter.isSelected(targetPosition)) {
                    toggleSelection(targetPosition);
                }*/
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), targetPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
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

    @Override
    public void onItemClicked(int position, ProfileAdapter.ProfileViewHolder viewHolder) {
        //Log.d(TAG, "onItemClicked: position = " + position);
        if (actionMode != null) {
            toggleSelection(position, viewHolder);
        } else {
            Profile profile = mAdapter.getData().get(position);
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.setAction(ProfileActivity.ACTION_VIEW);
            intent.putExtra("profile", profile.toJson());
            getActivity().startActivityForResult(intent, MainActivity.RESULT_PROFILE);
        }
    }

    @Override
    public boolean onItemLongClicked(int position, ProfileAdapter.ProfileViewHolder viewHolder) {
        //Log.d(TAG, "onItemLongClicked: position=" + position);
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(new ActionModeCallback());
        }
        toggleSelection(position, viewHolder);

        return true;
    }

    /**
     * Toggle the selection state of an item.
     * <p>
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position, ProfileAdapter.ProfileViewHolder viewHolder) {
        mAdapter.toggleSelection(position);
        viewHolder.setSelected(mAdapter.isSelected(position));
        //mAdapter.notifyDataSetChanged();
        //mAdapter.notifyItemChanged(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
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
                        App.getProfileApiHelper().removeProfile(mAdapter.getData().get(position));
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