package it.ibashkimi.lockscheduler.about;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.ui.BaseActivity;


public class AboutActivity extends BaseActivity {

    public static final String ACTION_HELP = "it.ibashkimi.lockscheduler.settings.help";
    public static final String ACTION_ABOUT = "it.ibashkimi.lockscheduler.settings.about";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            if (getIntent().getAction() != null && getIntent().getAction().equals(ACTION_HELP)) {
                fragmentTransaction
                        .replace(android.R.id.content, getHelpFragment(), "help_fragment_tag");
            } else {
                fragmentTransaction
                        .replace(android.R.id.content, getAboutFragment(), "about_fragment_tag");
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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

    public void showHelpFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(android.R.id.content, getHelpFragment(), "help_fragment_tag")
                .addToBackStack("help")
                .commit();
    }

    public AboutFragment getAboutFragment() {
        AboutFragment fragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag("about_fragment_tag");
        if (fragment == null) {
            fragment = new AboutFragment();
        }
        return fragment;
    }

    public HelpFragment getHelpFragment() {
        HelpFragment fragment = (HelpFragment) getSupportFragmentManager().findFragmentByTag("help_fragment_tag");
        if (fragment == null) {
            fragment = new HelpFragment();
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

    public static void sendFeedback(Context context) {
        // http://stackoverflow.com/a/16217921
        // https://developer.android.com/guide/components/intents-common.html#Email
        String address = context.getString(R.string.developer_email);
        String subject = context.getString(R.string.feedback_subject);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + address));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        String chooserTitle = context.getString(R.string.feedback_chooser_title);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    public static class AboutFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_about, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }

            Button licences = (Button) view.findViewById(R.id.licences);
            licences.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AboutActivity) getActivity()).showLicenceFragment();
                }
            });

            view.findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AboutActivity.sendFeedback(getContext());
                }
            });

            view.findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AboutActivity) getActivity()).showHelpFragment();
                }
            });
        }
    }
}
