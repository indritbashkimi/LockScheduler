package com.ibashkimi.lockscheduler.model.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;

public class GoogleApiHelper extends BaseGoogleApiHelper {

    private Handler mHandler;
    private ArrayList<Runnable> mJobs;

    public GoogleApiHelper(Context context) {
        super(context);
        mHandler = new Handler();
        mJobs = new ArrayList<>();
    }

    @Override
    public void onConnected(Bundle bundle) {
        while (mJobs.size() > 0) {
            mHandler.post(mJobs.get(0));
            mJobs.remove(0);
        }
    }

    public void doJob(Runnable job) {
        if (isConnected())
            mHandler.post(job);
        else
            mJobs.add(job);
    }
}
