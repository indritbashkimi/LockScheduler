package it.ibashkimi.lockscheduler.ui.recyclerview;


import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import it.ibashkimi.lockscheduler.model.Profile;

public abstract class ProfileAdapter<VH extends ProfileAdapter.ProfileViewHolder> extends SelectableAdapter<VH> {

    public interface ClickListener {
        void onItemClicked(int position, ProfileViewHolder viewHolder);

        boolean onItemLongClicked(int position, ProfileViewHolder viewHolder);
    }

    private List<Profile> mProfiles;
    private ProfileAdapterImpl.ClickListener clickListener;

    public ProfileAdapter(List<Profile> profiles, ProfileAdapterImpl.ClickListener clickListener) {
        this.mProfiles = profiles;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

    public void setData(List<Profile> data) {
        this.mProfiles = data;
        notifyDataSetChanged();
    }

    public List<Profile> getData() {
        return mProfiles;
    }

    public ClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public static abstract class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ClickListener listener;

        ProfileViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            this.listener = listener;

        }

        public void init(Profile profile) {

        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getAdapterPosition(), this);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            return listener != null && listener.onItemLongClicked(getAdapterPosition(), this);
        }

        public ClickListener getListener() {
            return listener;
        }

        public void setListener(ClickListener listener) {
            this.listener = listener;
        }

        public abstract void setSelected(boolean selected);

        public void recycle() {

        }
    }
}
