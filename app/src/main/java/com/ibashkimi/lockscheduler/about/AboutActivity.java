package com.ibashkimi.lockscheduler.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.help.HelpActivity;
import com.ibashkimi.lockscheduler.ui.BaseActivity;
import com.ibashkimi.lockscheduler.util.PlatformUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;


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
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.fragment_about, container, false);
            ButterKnife.bind(this, root);
            return root;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            view.findViewById(R.id.email).setOnClickListener(this);
            view.findViewById(R.id.facebook).setOnClickListener(this);
            view.findViewById(R.id.google_plus).setOnClickListener(this);
            view.findViewById(R.id.twitter).setOnClickListener(this);
            view.findViewById(R.id.github).setOnClickListener(this);
        }

        @OnClick(R.id.help)
        public void onHelpClicked() {
            startActivity(new Intent(getContext(), HelpActivity.class));
        }

        @OnClick(R.id.feedback)
        public void onSendFeedbackClicked() {
            PlatformUtils.sendFeedback(getContext());
        }

        @OnClick(R.id.uninstall)
        public void onUninstallClicked() {
            PlatformUtils.uninstall(AboutFragment.this.getContext());
        }

        @OnClick(R.id.privacy_policy)
        public void onPrivacyPolicyClicked() {
            Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show();
        }

        @OnClick(R.id.licenses)
        public void onLicensesClicked() {
            ((AboutActivity) getActivity()).showLicenceFragment();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.email:
                    PlatformUtils.sendFeedback(getContext());
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
