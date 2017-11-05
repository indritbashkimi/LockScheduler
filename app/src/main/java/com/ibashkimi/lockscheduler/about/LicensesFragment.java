package com.ibashkimi.lockscheduler.about;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.support.utils.ThemeUtils;

import java.security.InvalidParameterException;


public class LicensesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_licenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fragment_licences_title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new LibraryAdapter(getActivity()));
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
                    new Library(R.string.android_support_libraries_name,
                            R.string.android_support_libraries_link,
                            R.string.android_support_libraries_license,
                            R.string.apache_license_name,
                            R.string.apache_license_link),
                    new Library(R.string.material_dialogs_name,
                            R.string.material_dialogs_link,
                            R.string.material_dialogs_license,
                            R.string.mit_license_name,
                            R.string.mit_license_link),
                    new Library(R.string.material_intro_name,
                            R.string.material_intro_link,
                            R.string.material_intro_license,
                            R.string.apache_license_name,
                            R.string.apache_license_link)
            };
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
            return new LibraryHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.about_library, parent, false));
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
            holder.link.setText(lib.link);
            holder.licence.setText(lib.license);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ThemeUtils.obtainColor(host, R.attr.colorPrimary, Color.RED));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(host, Uri.parse(v.getContext().getString(libs[position - 1].link)));
                }
            };
            holder.itemView.setOnClickListener(clickListener);
            holder.website.setOnClickListener(clickListener);
            holder.link.setOnClickListener(clickListener);
            holder.licenseLink.setText(lib.licenseName);
            holder.licenseLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ThemeUtils.obtainColor(host, R.attr.colorPrimary, Color.RED));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(host, Uri.parse(v.getContext().getString(libs[position - 1].licenseLink)));
                }
            });
        }
    }

    private static class LibraryHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView link;
        TextView licence;
        Button licenseLink;
        Button website;

        LibraryHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.library_name);
            link = itemView.findViewById(R.id.library_link);
            link.setMovementMethod(LinkMovementMethod.getInstance());
            licence = itemView.findViewById(R.id.library_license);
            licenseLink = itemView.findViewById(R.id.library_full_license);
            website = itemView.findViewById(R.id.library_website);
        }
    }

    private static class LibraryIntroHolder extends RecyclerView.ViewHolder {
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
        @StringRes
        final int name;
        @StringRes
        final int link;
        @StringRes
        final int license;
        final int licenseName;
        final int licenseLink;

        Library(@StringRes int name, @StringRes int link, @StringRes int license, int licenseName, int licenseLink) {
            this.name = name;
            this.link = link;
            this.license = license;
            this.licenseName = licenseName;
            this.licenseLink = licenseLink;
        }
    }
}
