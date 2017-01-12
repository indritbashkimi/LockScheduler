package it.ibashkimi.lockscheduler.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.adapters.HelpAdapter;
import it.ibashkimi.lockscheduler.adapters.HelpAdapter.HelpItem;


public class HelpFragment extends Fragment {

    private HelpAdapter.HelpItem[] items;
    private HelpAdapter adapter;
    private boolean exitOnClose;

    public static HelpFragment newInstance(boolean exitOnClose) {
        HelpFragment helpFragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putBoolean("exit_on_close", exitOnClose);
        helpFragment.setArguments(args);
        return helpFragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("exit_on_close")) {
            exitOnClose = getArguments().getBoolean("exit_on_close", false);
        } else {
            exitOnClose = false;
        }
        items = new HelpItem[]{
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
        rootView.findViewById(R.id.cancel_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exitOnClose) {
                    ((AboutActivity) getActivity()).finish();
                } else {
                    ((AboutActivity) getActivity()).getSupportFragmentManager().popBackStack();
                }
            }
        });
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        return rootView;
    }
}
