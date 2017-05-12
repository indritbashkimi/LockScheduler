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
import android.util.Log;
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
    private static final String TAG = "ActionsFragment";

    private static final int REQUEST_ENTER_PIN = 1;
    private static final int REQUEST_EXIT_PIN = 2;
    private static final int REQUEST_ENTER_PASSWORD = 3;
    private static final int REQUEST_EXIT_PASSWORD = 4;
    private static final int REQUEST_ADMIN_PERMISSION = 5;

    private SharedPreferences mSharedPrefs;

    @LockAction.LockType
    private int enterLockType = LockAction.LockType.UNCHANGED;
    @LockAction.LockType
    private int exitLockType = LockAction.LockType.UNCHANGED;

    private int whichSpinner;
    private int lockTypeIfGranted;

    private ViewGroup rootView;
    private TextView enterPinView;
    private TextView exitPinView;

    private boolean enterFirstTime = true;
    private boolean exitFirstTime = true;

    private String enterInput;
    private String exitInput;

    private Spinner enterSpinner;
    private Spinner exitSpinner;

    private AdminApiHelper adminApiHelper;

    @SuppressWarnings("unused")
    public static ActionsFragment newInstance() {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setData(List<Action> trueActions, List<Action> falseActions) {
        Log.d(TAG, "setData");
        LockAction enterAction = (LockAction) trueActions.get(0);
        enterLockType = enterAction.getLockType();

        LockAction exitAction = (LockAction) falseActions.get(0);
        exitLockType = exitAction.getLockType();
    }

    public List<List<Action>> assembleData() {
        List<Action> enterActions = new ArrayList<>(1);
        LockAction enterAction = new LockAction(enterLockType);
        if (enterLockType == LockAction.LockType.PIN || enterLockType == LockAction.LockType.PASSWORD)
            enterAction.setInput(enterPinView.getText().toString());
        enterActions.add(enterAction);

        List<Action> exitActions = new ArrayList<>(1);
        LockAction exitAction = new LockAction(exitLockType);
        if (exitLockType == LockAction.LockType.PIN || exitLockType == LockAction.LockType.PASSWORD)
            exitAction.setInput(exitPinView.getText().toString());
        exitActions.add(exitAction);

        ArrayList<List<Action>> data = new ArrayList<>(2);
        data.add(enterActions);
        data.add(exitActions);
        return data;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        enterLockType = LockAction.LockType.UNCHANGED;
        exitLockType = LockAction.LockType.UNCHANGED;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_actions, container, false);

        if (savedInstanceState != null) {
            enterFirstTime = savedInstanceState.getBoolean("enter_first_time");
            @LockAction.LockType int lockType1 = savedInstanceState.getInt("enter_lock_type", LockAction.LockType.UNCHANGED);
            enterLockType = lockType1;
            exitFirstTime = savedInstanceState.getBoolean("exit_first_time");
            @LockAction.LockType int lockType2 = savedInstanceState.getInt("exit_lock_type", LockAction.LockType.UNCHANGED);
            exitLockType = lockType2;
            enterInput = savedInstanceState.getString("enter_input");
            exitInput = savedInstanceState.getString("exit_input");

            whichSpinner = savedInstanceState.getInt("which_spinner");
            lockTypeIfGranted = savedInstanceState.getInt("lock_type_if_granted");
        }

        enterPinView = (TextView) rootView.findViewById(R.id.enter_pin_password_view);
        enterPinView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        enterPinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterLockType == LockAction.LockType.PIN)
                    showPinChooser(REQUEST_ENTER_PIN);
                else if (enterLockType == LockAction.LockType.PASSWORD)
                    showPasswordChooser(REQUEST_ENTER_PIN);
            }
        });
        if (enterLockType == LockAction.LockType.PIN || enterLockType == LockAction.LockType.PASSWORD) {
            enterPinView.setText(enterInput);
            enterPinView.setVisibility(View.VISIBLE);
        }

        exitPinView = (TextView) rootView.findViewById(R.id.exit_pin_password_view);
        exitPinView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        exitPinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exitLockType == LockAction.LockType.PIN)
                    showPinChooser(REQUEST_EXIT_PIN);
                else if (exitLockType == LockAction.LockType.PASSWORD)
                    showPasswordChooser(REQUEST_EXIT_PIN);
            }
        });
        if (exitLockType == LockAction.LockType.PIN || exitLockType == LockAction.LockType.PASSWORD) {
            exitPinView.setText(exitInput);
            exitPinView.setVisibility(View.VISIBLE);
        }

        ArrayList<StringWithTag> array = new ArrayList<>();
        array.add(new StringWithTag(getString(R.string.lock_mode_nothing), "nothing"));
        array.add(new StringWithTag(getString(R.string.lock_mode_password), "password"));
        array.add(new StringWithTag(getString(R.string.lock_mode_pin), "pin"));
        array.add(new StringWithTag(getString(R.string.lock_mode_swipe), "swipe"));

        enterSpinner = (Spinner) rootView.findViewById(R.id.lock_spinner);
        ArrayAdapter<StringWithTag> enterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        enterSpinner.setAdapter(enterSpinnerAdapter);
        enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockType));
        enterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
                @LockAction.LockType int selectedLockType = getLockTypeFromSpinnerTag(item.tag);

                if (selectedLockType != LockAction.LockType.UNCHANGED && !adminPermissionGranted()) {
                    whichSpinner = 0;
                    lockTypeIfGranted = selectedLockType;
                    if (shouldShowAdminPermissionRationale())
                        showAdminPermissionRationale();
                    else
                        askAdminPermission();
                    return;
                }

                if (enterFirstTime) {
                    enterFirstTime = false;
                    return;
                }

                if (selectedLockType != enterLockType) {
                    switch (selectedLockType) {
                        case LockAction.LockType.PIN:
                            showPinChooser(REQUEST_ENTER_PIN);
                            break;
                        case LockAction.LockType.PASSWORD:
                            showPasswordChooser(REQUEST_ENTER_PASSWORD);
                            break;
                        default:
                            enterLockType = selectedLockType;
                            enterPinView.setVisibility(View.GONE);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No op
            }
        });

        exitSpinner = (Spinner) rootView.findViewById(R.id.otherwise_spinner);
        ArrayAdapter<StringWithTag> exitSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        exitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exitSpinner.setAdapter(exitSpinnerAdapter);
        exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockType));
        exitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
                @LockAction.LockType int selectedLockType = getLockTypeFromSpinnerTag(item.tag);

                if (selectedLockType != LockAction.LockType.UNCHANGED && !adminPermissionGranted()) {
                    whichSpinner = 1;
                    lockTypeIfGranted = selectedLockType;
                    if (shouldShowAdminPermissionRationale())
                        showAdminPermissionRationale();
                    else
                        askAdminPermission();
                    return;
                }

                if (exitFirstTime) {
                    exitFirstTime = false;
                    return;
                }

                if (selectedLockType != exitLockType) {
                    exitLockType = selectedLockType;
                    switch (selectedLockType) {
                        case LockAction.LockType.PIN:
                            showPinChooser(REQUEST_EXIT_PIN);
                            break;
                        case LockAction.LockType.PASSWORD:
                            showPasswordChooser(REQUEST_EXIT_PASSWORD);
                            break;
                        default:
                            exitLockType = selectedLockType;
                            exitPinView.setVisibility(View.GONE);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
        enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockType));
        exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockType));
    }

    private void onAdminPermissionGranted() {
        Toast.makeText(getContext(), "Admin permission granted", Toast.LENGTH_SHORT).show();
        Spinner spinner = whichSpinner == 0 ? enterSpinner : exitSpinner;
        spinner.setSelection(whichSpinner == 0 ? enterLockType : exitLockType);
        spinner.setSelection(getSpinnerPositionFromLockType(lockTypeIfGranted));
        switch (lockTypeIfGranted) {
            case LockAction.LockType.PIN:
                showPinChooser(whichSpinner == 0 ? REQUEST_ENTER_PIN : REQUEST_EXIT_PIN);
                break;
            case LockAction.LockType.PASSWORD:
                showPasswordChooser(whichSpinner == 0 ? REQUEST_ENTER_PIN : REQUEST_EXIT_PIN);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("enter_first_time", enterFirstTime);
        outState.putInt("enter_lock_type", enterLockType);
        outState.putBoolean("exit_first_time", exitFirstTime);
        outState.putInt("exit_first_time", exitLockType);
        outState.putString("enter_input", enterInput);
        outState.putString("exit_input", exitInput);
        outState.putInt("which_spinner", whichSpinner);
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
            case REQUEST_ENTER_PIN:
                if (resultCode == Activity.RESULT_OK) {
                    enterLockType = LockAction.LockType.PIN;
                    enterInput = data.getStringExtra("input");
                    enterPinView.setText(enterInput);
                    TransitionManager.beginDelayedTransition(rootView);
                    enterPinView.setVisibility(View.VISIBLE);
                } else {
                    enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockType));
                }
                break;
            case REQUEST_ENTER_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    enterLockType = LockAction.LockType.PASSWORD;
                    enterInput = data.getStringExtra("input");
                    enterPinView.setText(enterInput);
                    TransitionManager.beginDelayedTransition(rootView);
                    enterPinView.setVisibility(View.VISIBLE);
                } else {
                    enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockType));
                }
                break;
            case REQUEST_EXIT_PIN:
                if (resultCode == Activity.RESULT_OK) {
                    exitLockType = LockAction.LockType.PIN;
                    exitInput = data.getStringExtra("input");
                    exitPinView.setText(exitInput);
                    TransitionManager.beginDelayedTransition(rootView);
                    exitPinView.setVisibility(View.VISIBLE);
                } else {
                    exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockType));
                }
                break;
            case REQUEST_EXIT_PASSWORD:
                if (resultCode == Activity.RESULT_OK) {
                    exitLockType = LockAction.LockType.PASSWORD;
                    exitInput = data.getStringExtra("input");
                    exitPinView.setText(exitInput);
                    TransitionManager.beginDelayedTransition(rootView);
                    exitPinView.setVisibility(View.VISIBLE);
                } else {
                    exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockType));
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
