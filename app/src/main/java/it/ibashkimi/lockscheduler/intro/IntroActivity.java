package it.ibashkimi.lockscheduler.intro;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.api.AdminApiHelper;

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
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        final AdminApiHelper adminApiHelper = new AdminApiHelper(IntroActivity.this);
        if (!adminApiHelper.isAdminActive()) {
            SimpleSlide2 slide = new SimpleSlide2.Builder()
                    .title(R.string.app_name)
                    .description(R.string.device_admin_permission_rationale)
                    .image(android.R.drawable.ic_lock_idle_lock)
                    .background(R.color.colorAccent)
                    .backgroundDark(R.color.colorPrimaryDark)
                    .buttonCtaClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(adminApiHelper.buildAddAdminIntent(), 5);
                        }
                    })
                    .build();
            slide.setAdminApiHelper(adminApiHelper);
            addSlide(slide);
        }

        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.location_permission_rationale)
                .image(android.R.drawable.ic_menu_mylocation)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorAccent)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .build());

        /**
         * Custom fragment slide
         */
        /*addSlide(new FragmentSlide.Builder()
                .background(R.color.colorAccent)
                .backgroundDark(R.color.colorPrimary)
                .fragment(R.layout.fragment_2, R.style.FragmentTheme)
                .build());*/
    }

    @Override
    public void finish() {
        super.finish();
    }
}
