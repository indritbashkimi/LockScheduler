package com.ibashkimi.lockscheduler.help;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.databinding.FragmentHelpBinding;
import com.ibashkimi.lockscheduler.databinding.ItemHelpBinding;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentHelpBinding binding = FragmentHelpBinding.inflate(inflater, container, false);

        binding.feedback.setOnClickListener(v -> PlatformUtils.sendFeedback(requireActivity()));
        binding.uninstall.setOnClickListener(v -> PlatformUtils.uninstall(requireActivity()));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerView.setNestedScrollingEnabled(false);
        binding.recyclerView.setAdapter(adapter);
        return binding.getRoot();
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

        @NonNull
        @Override
        public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new HelpViewHolder(ItemHelpBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull HelpAdapter.HelpViewHolder holder, int position) {
            holder.binding.title.setText(items[position].title);
            holder.binding.content.setText(items[position].content);
        }

        @Override
        public int getItemCount() {
            return items.length;
        }


        static class HelpViewHolder extends RecyclerView.ViewHolder {
            ItemHelpBinding binding;

            HelpViewHolder(ItemHelpBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
