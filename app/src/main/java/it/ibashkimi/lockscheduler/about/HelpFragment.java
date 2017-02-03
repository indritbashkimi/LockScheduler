package it.ibashkimi.lockscheduler.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.support.utils.ThemeUtils;


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
        ImageView cancel = (ImageView) rootView.findViewById(R.id.cancel_view);
        cancel.setColorFilter(ThemeUtils.getColorFromAttribute(rootView.findViewById(R.id.toolbar).getContext(), android.R.attr.textColorPrimary));
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        TextView title = (TextView) rootView.findViewById(R.id.title_view);
        title.setText(getString(R.string.fragment_help_title));
        rootView.findViewById(R.id.feedback_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.sendFeedback(getContext());
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
