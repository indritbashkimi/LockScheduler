package it.ibashkimi.lockscheduler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();

    private static final int RESULT_ENABLE = 1;

    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceManger = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (deviceManger.isAdminActive(compName)) {
                    setLockPin();
                } else {
                    addAdmin();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addAdmin() {
        Log.d(TAG, "addAdmin() called");
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        startActivityForResult(intent, RESULT_ENABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Log.i(TAG, "Admin enabled!");
                } else {
                    Log.i(TAG, "Admin enable FAILED!");
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeAdmin() {
        deviceManger.removeActiveAdmin(compName);
    }

    private void setLockPin() {
        Log.d(TAG, "setLockPin() called");
        //deviceManger.setPasswordQuality(compName, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        //deviceManger.setPasswordMinimumLength(compName, 4);

        boolean result = deviceManger.resetPassword("3475",
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

        //deviceManger.lockNow();
    }

    private void removeLockPin() {
        Log.d(TAG, "removeLockPin() called");
        boolean result = deviceManger.resetPassword("",
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password removed successfully" : "Password remove failed.";
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
