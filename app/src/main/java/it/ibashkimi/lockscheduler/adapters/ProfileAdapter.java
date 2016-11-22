package it.ibashkimi.lockscheduler.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import it.ibashkimi.lockscheduler.ProfileModifierActivity;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.domain.Profile;
import it.ibashkimi.lockscheduler.views.ProfileView;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = "ProfileAdapter";
    private Activity activity;
    private List<Profile> profiles;

    public ProfileAdapter(Activity activity, List<Profile> profiles) {
        this.activity = activity;
        this.profiles = profiles;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() viewType = [" + viewType + "]");
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.profile_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.profileView.setProfile(profiles.get(position));
        holder.profileView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    @Override
    public void onClick(View view) {
        Profile profile = ((ProfileView) view).getProfile();
        Intent intent = new Intent(activity, ProfileModifierActivity.class);
        // putExtra
        activity.startActivityForResult(intent, 0);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ProfileView profileView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.profileView = (ProfileView) itemView;
        }
    }
}
