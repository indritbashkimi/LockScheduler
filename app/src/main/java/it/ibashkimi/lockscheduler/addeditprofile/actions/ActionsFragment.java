package it.ibashkimi.lockscheduler.addeditprofile.actions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.Action;
import it.ibashkimi.lockscheduler.model.LockAction;
import it.ibashkimi.lockscheduler.model.api.AdminApiHelper;


public class ActionsFragment extends Fragment {

    private static final int REQUEST_PIN = 1;
    private static final int REQUEST_PASSWORD = 2;
    private static final int REQUEST_ADMIN_PERMISSION = 3;

    private SharedPreferences mSharedPrefs;

    @LockAction.LockType
    private int lockType = LockAction.LockType.UNCHANGED;
    private String input;

    private ViewGroup rootView;
    private TextView inputView;
    private Spinner spinner;

    private boolean firstTime = true;
    private int lockTypeIfGranted;

    private AdminApiHelper adminApiHelper;

    @SuppressWarnings("unused")
    public static ActionsFragment newInstance() {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setData(List<Action> actions) {
        LockAction action = (LockAction) actions.get(0);
        lockType = action.getLockType();
        if (lockType == LockAction.LockType.PIN || lockType == LockAction.LockType.PASSWORD)
            input = action.getInput();
    }

    public List<Action> assembleData() {
        List<Action> actions = new ArrayList<>(1);
        LockAction action = new LockAction(lockType);
        if (lockType == LockAction.LockType.PIN || lockType == LockAction.LockType.PASSWORD)
            action.setInput(inputView.getText().toString());
        actions.add(action);
        return actions;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_actions, container, false);

        if (savedInstanceState != null) {
            firstTime = savedInstanceState.getBoolean("enter_first_time");
            @LockAction.LockType int lockType1 = savedInstanceState.getInt("enter_lock_type", LockAction.LockType.UNCHANGED);
            lockType = lockType1;
            input = savedInstanceState.getString("enter_input");
            lockTypeIfGranted = savedInstanceState.getInt("lock_type_if_granted");
        }

        inputView = (TextView) rootView.findViewById(R.id.input_view);
        inputView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lockType == LockAction.LockType.PIN)
                    showPinChooser(REQUEST_PIN);
                else if (lockType == LockAction.LockType.PASSWORD)
                    showPasswordChooser(REQUEST_PIN);
            }
        });
        if (lockType == LockAction.LockType.PIN || lockType == LockAction.LockType.PASSWORD) {
            inputView.setText(input);
            inputView.setVisibility(View.VISIBLE);
        }

        ArrayList<StringWithTag> array = new ArrayList<>();
        array.add(new StringWithTag(getString(R.string.lock_mode_nothing), "nothing"));
        array.add(new StringWithTag(getString(R.string.lock_mode_password), "password"));
        array.add(new StringWithTag(getString(R.string.lock_mode_pin), "pin"));
        array.add(new StringWithTag(getString(R.string.lock_mode_swipe), "swipe"));

        spinner = (Spinner) rootView.findViewById(R.id.lock_spinner);
        ArrayAdapter<StringWithTag> enterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(enterSpinnerAdapter);
        spinner.setSelection(getSpinnerPositionFromLockType(lockType));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
                @LockAction.LockType int selectedLockType = getLockTypeFromSpinnerTag(item.tag);

                if (selectedLockType != LockAction.LockType.UNCHANGED && !adminPermissionGranted()) {
                    lockTypeIfGranted = selectedLockType;
                    if (shouldShowAdminPermissionRationale())
                        showAdminPermissionRationale();
                    else
                        askAdminPermission();
                    return;
                }

                if (firstTime) {
                    firstTime = false;
                    return;
                }

                if (selectedLockType != lockType) {
                    if (selectedLockType == LockAction.LockType.PIN) {
                        showPinChooser(REQUEST_PIN);
                    } else if (selectedLockType == LockAction.LockType.PASSWORD) {
                        showPasswordChooser(REQUEST_PASSWORD);
                    } else {
                        lockType = selectedLockType;
                        inputView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });
        return rootView;
    }

    private AdminApiHelper getAdminApiHelper() {
        if (adminApiHelper == null)
            adminApiHelper = new AdminApiHelper(getContext());
        return adminApiHelper;
    }

    private boolean adminPermissionGranted() {
        return getAdminApiHelper().isAdminActive();
    }

    private boolean shouldShowAdminPermissionRationale() {
        return getSharedPreferences().getBoolean("show_admin_permission_rationale", false);
    }

    private void showAdminPermissionRationale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.admin_permission_rationale_title)
                .setMessage(R.string.admin_permission_rationale)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        askAdminPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onAdminPermissionDenied();
                    }
                });
        builder.create().show();
    }

    private void askAdminPermission() {
        startActivityForResult(adminApiHelper.buildAddAdminIntent(), REQUEST_ADMIN_PERMISSION);
    }

    private void onAdminPermissionDenied() {
        Toast.makeText(getContext(), "Admin permission denied", Toast.LENGTH_SHORT).show();
        spinner.setSelection(getSpinnerPositionFromLockType(lockType));
    }

    private void onAdminPermissionGranted() {
        Toast.makeText(getContext(), "Admin permission granted", Toast.LENGTH_SHORT).show();
        spinner.setSelection(getSpinnerPositionFromLockType(lockTypeIfGranted));
        switch (lockTypeIfGranted) {
            case LockAction.LockType.PIN:
                showPinChooser(REQUEST_PIN);
                break;
            case LockAction.LockType.PASSWORD:
                showPasswordChooser(REQUEST_PASSWORD);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("enter_first_time", firstTime);
        outState.putInt("enter_lock_type", lockType);
        outState.putString("enter_input", input);
        outState.putInt("lock_type_if_granted", lockTypeIfGranted);
    }

    private void showPasswordChooser(int request) {
        Intent intent = new Intent(getContext(), PinChooserActivity.class);
        intent.putExtra("type", "password");
        intent.putExtra("min_length", getSharedPreferences().getInt("min_password_length", 4));
        startActivityForResult(intent, request);
    }

    private void showPinChooser(int request) {
        Intent intent = new Intent(getContext(), PinChooserActivity.class);
        intent.putExtra("type", "pin");
        intent.putExtra("min_length", getSharedPreferences().getInt("min_pin_length", 4));
        startActivityForResult(intent, request);
    }

    protected SharedPreferences getSharedPreferences() {
        if (mSharedPrefs == null) {
            mSharedPrefs = getContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        }
        return mSharedPrefs;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PIN:
                if (resultCode == Activity.RESULT_OK) {
                    lockType = LockAction.LockType.PIN;
                    input = data.getStringExtra("input");
                    inputView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    inputView.setVisibility(View.VISIBLE);
                } else {
                    spinner.setSelection(getSpinnerPositionFromLockType(lockType));
                }
                break;
            case REQUEST_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    lockType = LockAction.LockType.PASSWORD;
                    input = data.getStringExtra("input");
                    inputView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    inputView.setVisibility(View.VISIBLE);
                } else {
                    spinner.setSelection(getSpinnerPositionFromLockType(lockType));
                }
                break;
            case REQUEST_ADMIN_PERMISSION:
                if (resultCode == Activity.RESULT_CANCELED) {
                    getSharedPreferences().edit().putBoolean("show_admin_permission_rationale", true).apply();
                    onAdminPermissionDenied();
                } else if (resultCode == Activity.RESULT_OK) {
                    onAdminPermissionGranted();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static int getSpinnerPositionFromLockType(@LockAction.LockType int lockType) {
        switch (lockType) {
            case LockAction.LockType.UNCHANGED:
                return 0;
            case LockAction.LockType.PASSWORD:
                return 1;
            case LockAction.LockType.PIN:
                return 2;
            case LockAction.LockType.SWIPE:
                return 3;
            default:
                throw new RuntimeException("Unsupported lockType: " + lockType);
        }
    }

    @LockAction.LockType
    private static int getLockTypeFromSpinnerTag(String tag) {
        switch (tag) {
            case "nothing":
                return LockAction.LockType.UNCHANGED;
            case "password":
                return LockAction.LockType.PASSWORD;
            case "pin":
                return LockAction.LockType.PIN;
            case "swipe":
                return LockAction.LockType.SWIPE;
            default:
                throw new RuntimeException("Unknown tag: " + tag);
        }
    }

    private static class StringWithTag implements CharSequence {
        public String string;
        public String tag;

        StringWithTag(String stringPart, String tagPart) {
            string = stringPart;
            tag = tagPart;
        }

        @Override
        public int length() {
            return string.length();
        }

        @Override
        public char charAt(int index) {
            return string.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return string.subSequence(start, end);
        }

        @NonNull
        @Override
        public String toString() {
            return string;
        }
    }
}
