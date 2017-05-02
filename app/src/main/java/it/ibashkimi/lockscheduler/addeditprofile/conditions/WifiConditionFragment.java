package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.addeditprofile.conditions.picker.WifiPickerActivity;
import it.ibashkimi.lockscheduler.model.WifiCondition;
import it.ibashkimi.lockscheduler.model.WifiItem;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
public class WifiConditionFragment extends Fragment {

    public static final int REQUEST_WIFI_PICKER = 5;

    private WifiCondition condition;

    @BindView(R.id.networks_summary)
    TextView networksSummary;

    public void setData(WifiCondition condition) {
        this.condition = condition;
    }

    public WifiCondition assembleCondition() {
        return condition;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_condition_wifi, container, false);
        ButterKnife.bind(this, root);

        if (savedInstanceState == null)
            if (condition != null)
                showWifiItems(condition.getNetworks());
        return root;
    }

    public void showWifiItems(List<WifiItem> networks) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < networks.size() -1; i++)
            text.append(networks.get(i).SSID).append(", ");
        if (networks.size() > 0)
            text.append(networks.get(networks.size()-1).SSID);
        networksSummary.setText(text.toString());
        networksSummary.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.wifi_body)
    public void showWifiPicker() {
        Intent intent = new Intent(getContext(), WifiPickerActivity.class);

        if (condition != null) {
            List<WifiItem> items = condition.getNetworks();
            String[] ssids = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                ssids[i] = items.get(i).SSID;
            }
            intent.putExtra("ssids", ssids);
        }

        startActivityForResult(intent, REQUEST_WIFI_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WIFI_PICKER && resultCode == Activity.RESULT_OK) {
            String[] ssids = data.getStringArrayExtra("ssids");
            if (condition == null) {
                condition = new WifiCondition();
            }
            List<WifiItem> items = new ArrayList<>(ssids.length);
            for (int i = 0; i < ssids.length; i++)
                items.add(new WifiItem(ssids[i]));
            condition.setNetworks(items);
        }
    }
}
