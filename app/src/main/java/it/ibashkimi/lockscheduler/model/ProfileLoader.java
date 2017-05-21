package it.ibashkimi.lockscheduler.model;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

public class ProfileLoader extends AsyncTaskLoader<List<Profile>> {
    private List<Profile> mData;

    public ProfileLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Use cached data
            deliverResult(mData);
        }
        if (takeContentChanged() || mData == null) {
            // Something has changed or we have no data, so kick off loading it
            forceLoad();
        }
    }

    @Override
    public List<Profile> loadInBackground() {
        mData = ProfileManager.Companion.getInstance().getAll();
        return mData;
    }

    @Override
    public void deliverResult(List<Profile> data) {
        // We’ll save the data for later retrieval
        mData = data;
        // We can do any pre-processing we want here
        // Just remember this is on the UI thread so nothing lengthy!
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        // Stop watching for changes
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
    }
}
