package com.ibashkimi.lockscheduler.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.help.HelpActivity;
import com.ibashkimi.lockscheduler.ui.BaseActivity;
import com.ibashkimi.lockscheduler.util.PlatformUtils;


public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, getAboutFragment(), "about_fragment_tag")
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showLicenceFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(android.R.id.content, getLicensesFragment(), "licences_fragment_tag")
                .addToBackStack("licenses")
                .commit();
    }

    public AboutFragment getAboutFragment() {
        AboutFragment fragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("about_fragment_tag");
        if (fragment == null) {
            fragment = new AboutFragment();
        }
        return fragment;
    }

    public LicensesFragment getLicensesFragment() {
        LicensesFragment fragment = (LicensesFragment) getSupportFragmentManager().findFragmentByTag("licenses_fragment_tag");
        if (fragment == null) {
            fragment = new LicensesFragment();
        }
        return fragment;
    }

    public static class AboutFragment extends Fragment implements View.OnClickListener {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            view.findViewById(R.id.help).setOnClickListener(this);
            view.findViewById(R.id.feedback).setOnClickListener(this);
            view.findViewById(R.id.privacy_policy).setOnClickListener(this);
            view.findViewById(R.id.licenses).setOnClickListener(this);
            view.findViewById(R.id.email).setOnClickListener(this);
            view.findViewById(R.id.facebook).setOnClickListener(this);
            view.findViewById(R.id.google_plus).setOnClickListener(this);
            view.findViewById(R.id.twitter).setOnClickListener(this);
            view.findViewById(R.id.github).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.help:
                    startActivity(new Intent(getContext(), HelpActivity.class));
                    break;
                case R.id.feedback:
                    PlatformUtils.sendFeedback(requireContext());
                    break;
                case R.id.privacy_policy:
                    Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.licenses:
                    ((AboutActivity) getActivity()).showLicenceFragment();
                    break;
                case R.id.email:
                    PlatformUtils.sendFeedback(requireContext());
                    break;
                case R.id.facebook:
                    openUrl(R.string.social_facebook);
                    break;
                case R.id.twitter:
                    openUrl(R.string.social_twitter);
                    break;
                case R.id.google_plus:
                    openUrl(R.string.social_google_plus);
                    break;
                case R.id.github:
                    openUrl(R.string.social_github);
                    break;
            }
        }

        private void openUrl(@StringRes int link) {
            openUrl(getString(link));
        }

        private void openUrl(String url) {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            //builder.setToolbarColor(ContextCompat.getColor(this, R.color.primary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(getContext(), Uri.parse(url));
        }
    }
}
