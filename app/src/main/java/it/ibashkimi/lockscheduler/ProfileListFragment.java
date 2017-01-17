package it.ibashkimi.lockscheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.adapters.ProfileAdapter;
import it.ibashkimi.lockscheduler.domain.Profile;

/**
 * Fragment used to display profile list.
 */
public class ProfileListFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, ProfileAdapter.Callback {
    private static final String TAG = "ProfileListFragment";

    private ProfileAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private int mItemLayout;
    private int mMapStyle;
    private SharedPreferences mSettings;
    private ItemTouchHelper mItemTouchHelper;

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
        mMapStyle = Utils.resolveMapStyle(mSettings.getInt("map_style", 0));
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
        mAdapter = new ProfileAdapter(getContext(), new ArrayList<Profile>(0), mItemLayout, mMapStyle, this);
        mRecyclerView.setAdapter(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.d(TAG, "onMove: " + viewHolder.getAdapterPosition() + ", " + target.getAdapterPosition());
                int targetPosition = target.getAdapterPosition();
                if (targetPosition == mAdapter.getData().size()) {
                    targetPosition--;
                }
                App.getProfileApiHelper().swap(viewHolder.getAdapterPosition(), targetPosition);
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
            case 8:
                return R.layout.item_profile_8;
            case 9:
                return R.layout.item_profile_9;
            case 10:
                return R.layout.item_profile_a;
            default:
                return R.layout.item_profile_a;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("item_layout")) {
            mItemLayout = resolveLayout(Integer.parseInt(sharedPreferences.getString("item_layout", "0")));
            //mAdapter = new ProfileAdapter(getContext(), getProfiles(), mItemLayout, mMapStyle, this);
            //mRecyclerView.setAdapter(mAdapter);
        } else if (s.equals("map_style")) {
            mMapStyle = Utils.resolveMapStyle(sharedPreferences.getInt("map_style", 0));
            //mAdapter = new ProfileAdapter(getContext(), getProfiles(), mItemLayout, mMapStyle, this);
            //mRecyclerView.setAdapter(mAdapter);
        }
    }


    @Override
    public void onProfileRemoved(Profile profile, int position) {
        App.getProfileApiHelper().removeProfile(profile);
        mAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onProfileClicked(Profile profile) {
        Intent intent = new Intent(getActivity(), ProfileActivity.class);
        intent.setAction(ProfileActivity.ACTION_VIEW);
        intent.putExtra("profile", profile.toJson());
        getActivity().startActivityForResult(intent, MainActivity.RESULT_PROFILE);
    }
}
