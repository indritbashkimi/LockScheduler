package it.ibashkimi.lockscheduler.addeditprofile.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
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
import it.ibashkimi.lockscheduler.model.LockMode;


public class ActionsFragment extends Fragment {
    private static final String TAG = "ActionsFragment";

    private static final int REQUEST_ENTER_PIN = 1;
    private static final int REQUEST_EXIT_PIN = 2;
    private static final int REQUEST_ENTER_PASSWORD = 3;
    private static final int REQUEST_EXIT_PASSWORD = 4;

    private SharedPreferences mSharedPrefs;
    private LockMode enterLockMode;
    private LockMode exitLockMode;

    private int enterSelectedItem = 0;
    private int exitSelectedItem = 0;

    private ViewGroup rootView;
    private TextView enterPinView;
    private TextView exitPinView;

    private @LockMode.LockType
    int enterPreviousChoice = -1;
    private @LockMode.LockType
    int exitPreviousChoice = -1;

    private Spinner enterSpinner;
    private Spinner exitSpinner;

    @SuppressWarnings("unused")
    public static ActionsFragment newInstance() {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setData(List<Action> trueActions, List<Action> falseActions) {

        LockAction enterAction = (LockAction) trueActions.get(0);
        enterLockMode = enterAction.getLockMode();

        LockAction exitAction = (LockAction) falseActions.get(0);
        exitLockMode = exitAction.getLockMode();

        enterSelectedItem = getSpinnerPositionFromLockType(enterLockMode.getLockType());
        exitSelectedItem = getSpinnerPositionFromLockType(exitLockMode.getLockType());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enterLockMode = new LockMode(LockMode.LockType.UNCHANGED);
        exitLockMode = new LockMode(LockMode.LockType.UNCHANGED);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_actions, container, false);

        enterPinView = (TextView) rootView.findViewById(R.id.enter_pin_password_view);
        enterPinView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        enterPinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (enterLockMode.getLockType() == LockMode.LockType.PIN)
                    showPinChooser(REQUEST_ENTER_PIN);
                else if (enterLockMode.getLockType() == LockMode.LockType.PASSWORD)
                    showPasswordChooser(REQUEST_ENTER_PIN);
            }
        });
        exitPinView = (TextView) rootView.findViewById(R.id.exit_pin_password_view);
        exitPinView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        exitPinView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (exitLockMode.getLockType() == LockMode.LockType.PIN)
                    showPinChooser(REQUEST_EXIT_PIN);
                else if (exitLockMode.getLockType() == LockMode.LockType.PASSWORD)
                    showPasswordChooser(REQUEST_EXIT_PIN);
            }
        });

        ArrayList<StringWithTag> array = new ArrayList<>();
        array.add(new StringWithTag(getString(R.string.lock_mode_nothing), "nothing"));
        array.add(new StringWithTag(getString(R.string.lock_mode_password), "password"));
        array.add(new StringWithTag(getString(R.string.lock_mode_pin), "pin"));
        array.add(new StringWithTag(getString(R.string.lock_mode_swipe), "swipe"));

        enterSpinner = (Spinner) rootView.findViewById(R.id.lock_spinner);
        ArrayAdapter<StringWithTag> enterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        enterSpinner.setAdapter(enterSpinnerAdapter);
        enterSpinner.setSelection(enterSelectedItem);
        enterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
                @LockMode.LockType int selectedLockType = getLockTypeFromSpinnerTag(item.tag);
                if (enterPreviousChoice == -1) {
                    enterPreviousChoice = selectedLockType;
                    return;
                }
                Toast.makeText(getContext(), item.tag, Toast.LENGTH_SHORT).show();
                if (enterLockMode == null) {
                    enterLockMode = new LockMode(LockMode.LockType.UNCHANGED);
                }
                if (selectedLockType != enterLockMode.getLockType()) {
                    switch (selectedLockType) {
                        case LockMode.LockType.PIN:
                            showPinChooser(REQUEST_ENTER_PIN);
                            break;
                        case LockMode.LockType.PASSWORD:
                            showPasswordChooser(REQUEST_ENTER_PIN);
                            break;
                        default:
                            enterLockMode.setLockType(selectedLockType);
                            enterPreviousChoice = selectedLockType;
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
        exitSpinner.setSelection(exitSelectedItem);
        exitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
                @LockMode.LockType int selectedLockType = getLockTypeFromSpinnerTag(item.tag);
                if (exitPreviousChoice == -1) {
                    exitPreviousChoice = selectedLockType;
                    return;
                }
                Toast.makeText(getContext(), item.tag, Toast.LENGTH_SHORT).show();
                if (exitLockMode == null) {
                    exitLockMode = new LockMode(LockMode.LockType.UNCHANGED);
                }
                if (selectedLockType != exitLockMode.getLockType()) {
                    exitLockMode.setLockType(selectedLockType);
                    switch (selectedLockType) {
                        case LockMode.LockType.PIN:
                            showPinChooser(REQUEST_EXIT_PIN);
                            break;
                        case LockMode.LockType.PASSWORD:
                            showPasswordChooser(REQUEST_EXIT_PASSWORD);
                            break;
                        default:
                            exitPreviousChoice = selectedLockType;
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

    private void showPasswordChooser(int request) {
        Intent intent = new Intent(getContext(), PinChooserActivity.class);
        intent.putExtra("type", "password");
        intent.putExtra("min_length", 4);
        startActivityForResult(intent, request);
    }

    private void showPinChooser(int request) {
        Intent intent = new Intent(getContext(), PinChooserActivity.class);
        intent.putExtra("type", "pin");
        intent.putExtra("min_length", 4);
        startActivityForResult(intent, request);
    }

    protected SharedPreferences getSharedPreferences() {
        if (mSharedPrefs == null) {
            mSharedPrefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        }
        return mSharedPrefs;
    }

    public List<List<Action>> assembleData() {
        List<Action> enterActions = new ArrayList<>(1);
        if (enterLockMode == null) {
            enterActions.add(new LockAction());
        } else {
            enterActions.add(new LockAction(enterLockMode));
        }
        List<Action> exitActions = new ArrayList<>(1);
        if (exitLockMode == null) {
            exitActions.add(new LockAction());
        } else {
            exitActions.add(new LockAction(exitLockMode));
        }
        List<List<Action>> data = new ArrayList<>(2);
        data.add(enterActions);
        data.add(exitActions);
        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            enterPreviousChoice = -1;
            enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockMode.getLockType()));
            exitPreviousChoice = -1;
            exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockMode.getLockType()));
        } else if (resultCode == Activity.RESULT_OK) {
            String input = data.getStringExtra("input");
            switch (requestCode) {
                case REQUEST_ENTER_PIN:
                    enterPinView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    enterPinView.setVisibility(View.VISIBLE);
                    enterPreviousChoice = LockMode.LockType.PIN;
                    enterLockMode.setLockType(LockMode.LockType.PIN);
                    enterLockMode.setPin(input);
                    break;
                case REQUEST_ENTER_PASSWORD:
                    enterPinView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    enterPinView.setVisibility(View.VISIBLE);
                    enterPreviousChoice = LockMode.LockType.PASSWORD;
                    enterLockMode.setLockType(LockMode.LockType.PASSWORD);
                    enterLockMode.setPassword(input);
                    break;
                case REQUEST_EXIT_PIN:
                    exitPinView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    exitPinView.setVisibility(View.VISIBLE);
                    exitPreviousChoice = LockMode.LockType.PIN;
                    exitLockMode.setLockType(LockMode.LockType.PIN);
                    exitLockMode.setPin(input);
                    break;
                case REQUEST_EXIT_PASSWORD:
                    exitPinView.setText(input);
                    TransitionManager.beginDelayedTransition(rootView);
                    exitPinView.setVisibility(View.VISIBLE);
                    exitPreviousChoice = LockMode.LockType.PASSWORD;
                    exitLockMode.setLockType(LockMode.LockType.PASSWORD);
                    exitLockMode.setPassword(input);
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private static int getSpinnerPositionFromLockType(@LockMode.LockType int lockType) {
        switch (lockType) {
            case LockMode.LockType.UNCHANGED:
                return 0;
            case LockMode.LockType.PASSWORD:
                return 1;
            case LockMode.LockType.PIN:
                return 2;
            case LockMode.LockType.SEQUENCE:
                return 0;
            case LockMode.LockType.SWIPE:
                return 3;
            case LockMode.LockType.FINGERPRINT:
                return 0;
            default:
                return 0;
        }
    }

    private static int getLockTypeFromSpinnerTag(String tag) {
        switch (tag) {
            case "nothing":
                return LockMode.LockType.UNCHANGED;
            case "password":
                return LockMode.LockType.PASSWORD;
            case "pin":
                return LockMode.LockType.PIN;
            case "sequence":
                return LockMode.LockType.SEQUENCE;
            case "swipe":
                return LockMode.LockType.SWIPE;
            default:
                return LockMode.LockType.UNCHANGED;
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
