package com.ibashkimi.lockscheduler.help;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.util.PlatformUtils;


public class HelpFragment extends Fragment {

    private HelpAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HelpItem[] items = new HelpItem[]{
                new HelpItem(R.string.uninstall_title, R.string.uninstall_content),
                new HelpItem(R.string.forgot_password_title, R.string.forgot_password_content),
                new HelpItem(R.string.device_admin_title, R.string.device_admin_content),
                new HelpItem(R.string.location_permission_title, R.string.location_permission_content),
                new HelpItem(R.string.access_to_wifi_title, R.string.access_to_wifi_content)
        };
        adapter = new HelpAdapter(items);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        rootView.findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlatformUtils.sendFeedback(getContext());
            }
        });
        rootView.findViewById(R.id.uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlatformUtils.uninstall(HelpFragment.this.getContext());
            }
        });

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    private static class HelpItem {
        @StringRes
        public final int title;
        @StringRes
        public final int content;

        HelpItem(int title, int content) {
            this.title = title;
            this.content = content;
        }
    }

    static class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {

        private HelpItem[] items;

        HelpAdapter(HelpItem[] items) {
            this.items = items;
        }

        @Override
        public HelpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.item_help, parent, false);
            return new HelpViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(HelpAdapter.HelpViewHolder holder, int position) {
            holder.title.setText(items[position].title);
            holder.content.setText(items[position].content);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }


        static class HelpViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView content;

            HelpViewHolder(View itemView) {
                super(itemView);
                title = (TextView) itemView.findViewById(R.id.help_title);
                content = (TextView) itemView.findViewById(R.id.help_content);
            }
        }
    }
}
