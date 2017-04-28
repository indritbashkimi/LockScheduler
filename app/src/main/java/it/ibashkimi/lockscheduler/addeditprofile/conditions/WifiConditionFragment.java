package it.ibashkimi.lockscheduler.addeditprofile.conditions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

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
        return null;
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
            String[] itemReps = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                itemReps[i] = items.get(i).toJson();
            }
            intent.putExtra("items", itemReps);
        }

        startActivityForResult(intent, REQUEST_WIFI_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_WIFI_PICKER && resultCode == Activity.RESULT_OK) {
            int[] ids = data.getIntArrayExtra("ids");
            String[] ssids = data.getStringArrayExtra("ssid");
            if (condition == null) {
                condition = new WifiCondition("Wifi");
            }
            List<WifiItem> items = new ArrayList<>(ids.length);
            for (int i = 0; i < ids.length; i++)
                items.add(new WifiItem(ids[i], ssids[i]));
        }
    }
}
