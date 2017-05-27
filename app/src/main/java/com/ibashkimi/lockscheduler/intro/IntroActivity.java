package com.ibashkimi.lockscheduler.intro;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import com.ibashkimi.lockscheduler.R;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // https://github.com/HeinrichReimer/material-intro
        setFullscreen(true);

        super.onCreate(savedInstanceState);
        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.app_description)
                .image(R.mipmap.ic_launcher)
                .background(R.color.indigo_500)
                .backgroundDark(R.color.indigo_700)
                .build());
        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description("Additional description coming soon. Start using the app. I hope you will like it.")
                .image(R.mipmap.ic_launcher)
                .background(R.color.indigo_500)
                .backgroundDark(R.color.indigo_700)
                .build());
    }
}
