package it.ibashkimi.lockscheduler;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import java.security.InvalidParameterException;

import it.ibashkimi.support.design.utils.ThemeUtils;

public class AboutActivity extends BaseActivity {

    public boolean licencesOpen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new AboutFragment(), "about_fragment_tag")
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (licencesOpen)
            closeLicenceFragment();
        else
            super.onBackPressed();
    }

    public void showLicenceFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                //.setCustomAnimations(android.R.anim.)
                .replace(android.R.id.content, new LicencesFragment(), "licences_fragment_tag")
                .commit();
        licencesOpen = true;
    }

    public void closeLicenceFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new AboutFragment(), "about_fragment_tag")
                .commit();
        licencesOpen = false;
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
                    MainActivity.sendFeedback(getContext());
                }
            });
        }
    }

    public static class LicencesFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_licences, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
            //toolbar.setTitle("Licences");
            toolbar.findViewById(R.id.cancel_view).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AboutActivity) getActivity()).closeLicenceFragment();
                }
            });
            RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new LibraryAdapter(getActivity()));
        }
    }

    private static class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_INTRO = 0;
        private static final int VIEW_TYPE_LIBRARY = 1;
        Library[] libs;

        //private final CircleTransform circleCrop;
        final Activity host;

        LibraryAdapter(Activity host) {
            this.host = host;
            populate();
        }

        private void populate() {
            libs = new Library[]{
                    new Library("Android support libraries",
                            "The Android support libraries offer a number of features that are not built into the framework.",
                            "https://developer.android.com/topic/libraries/support-library",
                            "https://developer.android.com/images/android_icon_125.png",
                            "Licence bla bla"),
                    new Library("ButterKnife",
                            "Bind Android views and callbacks to fields and methods.",
                            "http://jakewharton.github.io/butterknife/",
                            "https://avatars.githubusercontent.com/u/66577",
                            "Licence bla bla"),
                    new Library("Google Play Services",
                            "Skip the HTML, Bypass takes markdown and renders it directly.",
                            "https://github.com/Uncodin/bypass",
                            "https://avatars.githubusercontent.com/u/1072254",
                            GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(host)),
                    new Library("Glide",
                            "An image loading and caching library for Android focused on smooth scrolling.",
                            "https://github.com/bumptech/glide",
                            "https://avatars.githubusercontent.com/u/423539",
                            "Licence bla bla"),
                    new Library("JSoup",
                            "Java HTML Parser, with best of DOM, CSS, and jquery.",
                            "https://github.com/jhy/jsoup/",
                            "https://avatars.githubusercontent.com/u/76934",
                            "Licence bla bla"),
                    new Library("OkHttp",
                            "An HTTP & HTTP/2 client for Android and Java applications.",
                            "http://square.github.io/okhttp/",
                            "https://avatars.githubusercontent.com/u/82592",
                            "Licence bla bla"),
                    new Library("Retrofit",
                            "A type-safe HTTP client for Android and Java.",
                            "http://square.github.io/retrofit/",
                            "https://avatars.githubusercontent.com/u/82592",
                            "Licence bla bla")};
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_INTRO:
                    return new LibraryIntroHolder(LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.about_lib_intro, parent, false));
                case VIEW_TYPE_LIBRARY:
                    return createLibraryHolder(parent);
            }
            throw new InvalidParameterException();
        }

        @NonNull
        private LibraryHolder createLibraryHolder(ViewGroup parent) {
            final LibraryHolder holder = new LibraryHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.about_library, parent, false));
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ThemeUtils.getColorFromAttribute(host, R.attr.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(host, Uri.parse(libs[position - 1].link));
                }
            };
            holder.itemView.setOnClickListener(clickListener);
            holder.link.setOnClickListener(clickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_LIBRARY) {
                bindLibrary((LibraryHolder) holder, libs[position - 1]); // adjust for intro
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? VIEW_TYPE_INTRO : VIEW_TYPE_LIBRARY;
        }

        @Override
        public int getItemCount() {
            return libs.length + 1; // + 1 for the static intro view
        }

        private void bindLibrary(final LibraryHolder holder, final Library lib) {
            holder.name.setText(lib.name);
            Log.d("AboutActivity", "bindLibrary: " + lib.licence);
            holder.licence.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(host)
                            .setTitle("Licence")
                            .setMessage(lib.licence)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .show();
                }
            });
        }
    }

    static class LibraryHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;
        TextView description;
        Button link;
        Button licence;

        LibraryHolder(View itemView) {
            super(itemView);
            //image = (ImageView) itemView.findViewById(R.id.library_image);
            name = (TextView) itemView.findViewById(R.id.library_name);
            //description = (TextView) itemView.findViewById(R.id.library_description);
            link = (Button) itemView.findViewById(R.id.library_link);
            licence = (Button) itemView.findViewById(R.id.library_licence);
        }
    }

    static class LibraryIntroHolder extends RecyclerView.ViewHolder {
        TextView intro;

        LibraryIntroHolder(View itemView) {
            super(itemView);
            intro = (TextView) itemView;
        }
    }

    /**
     * Models an open source library we want to credit
     */
    private static class Library {
        final String name;
        final String link;
        final String description;
        final String imageUrl;
        final String licence;

        Library(String name, String description, String link, String imageUrl, String licence) {
            this.name = name;
            this.description = description;
            this.link = link;
            this.imageUrl = imageUrl;
            this.licence = licence;
        }
    }

}
