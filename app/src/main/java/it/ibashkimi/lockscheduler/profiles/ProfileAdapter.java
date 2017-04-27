package it.ibashkimi.lockscheduler.profiles;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.support.utils.SelectableAdapter;

public abstract class ProfileAdapter<VH extends ProfileAdapter.ProfileViewHolder> extends SelectableAdapter<VH> {

    private List<Profile> mProfiles;
    private ProfilesFragment.ProfileItemListener itemListener;

    public ProfileAdapter(List<Profile> profiles, ProfilesFragment.ProfileItemListener itemListener) {
        this.mProfiles = profiles;
        this.itemListener = itemListener;
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

    public void setData(List<Profile> data) {
        this.mProfiles = data;
        notifyDataSetChanged();
    }

    public List<Profile> getProfiles() {
        return mProfiles;
    }

    public ProfilesFragment.ProfileItemListener getItemListener() {
        return itemListener;
    }

    public void setItemListener(ProfilesFragment.ProfileItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public static abstract class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ProfilesFragment.ProfileItemListener listener;

        ProfileViewHolder(View itemView, ProfilesFragment.ProfileItemListener listener) {
            super(itemView);
            this.listener = listener;

        }

        public void init(Profile profile) {

        }

        public ProfilesFragment.ProfileItemListener getListener() {
            return listener;
        }

        public void setListener(ProfilesFragment.ProfileItemListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onProfileClick(getAdapterPosition(), null); // // TODO: 08/03/17
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null)
                listener.onProfileLongClick(getAdapterPosition(), null); // TODO: 08/03/17
            return true;
        }

        public abstract void setSelected(boolean selected);

        public void recycle() {

        }
    }
}
