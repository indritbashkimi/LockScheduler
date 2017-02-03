package it.ibashkimi.lockscheduler.addeditprofile.actions;

import android.content.Context;
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
import android.widget.Toast;

import java.util.ArrayList;

import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.LockMode;
import it.ibashkimi.lockscheduler.ui.widget.PasswordInputLayout;


public class ActionsFragment extends Fragment implements ActionsContract.View {

    private SharedPreferences mSharedPrefs;
    private LockMode enterLockMode;
    private LockMode exitLockMode;
    private PasswordInputLayout mEnterPasswordLayout;
    private PasswordInputLayout mExitPasswordLayout;

    @SuppressWarnings("unused")
    public static ActionsFragment newInstance() {
        ActionsFragment fragment = new ActionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: 09/03/17
        //Profile profile = parent.getProfile();
        //enterLockMode = profile.getLockAction(true).getLockMode();
        //exitLockMode = profile.getLockAction(false).getLockMode();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_actions, container, false);
        /*mEnterPasswordLayout = (PasswordInputLayout) rootView.findViewById(R.id.enter_password_layout);
        mExitPasswordLayout = (PasswordInputLayout) rootView.findViewById(R.id.exit_password_layout);

        ArrayList<StringWithTag> array = new ArrayList<>();
        array.add(new StringWithTag(getString(R.string.lock_mode_nothing), "nothing"));
        array.add(new StringWithTag(getString(R.string.lock_mode_password), "password"));
        array.add(new StringWithTag(getString(R.string.lock_mode_pin), "pin"));
        array.add(new StringWithTag(getString(R.string.lock_mode_swipe), "swipe"));
        Spinner enterSpinner = (Spinner) rootView.findViewById(R.id.lock_spinner);
        ArrayAdapter<StringWithTag> enterSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        enterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        enterSpinner.setAdapter(enterSpinnerAdapter);
        enterSpinner.setOnItemSelectedListener(new SpinnerListener(enterLockMode, mEnterPasswordLayout, rootView, getSharedPreferences()));
        enterSpinner.setSelection(getSpinnerPositionFromLockType(enterLockMode.getLockType()));

        Spinner exitSpinner = (Spinner) rootView.findViewById(R.id.otherwise_spinner);
        ArrayAdapter<StringWithTag> exitSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, array);
        exitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exitSpinner.setAdapter(exitSpinnerAdapter);
        exitSpinner.setOnItemSelectedListener(new SpinnerListener(exitLockMode, mExitPasswordLayout, rootView, getSharedPreferences()));
        exitSpinner.setSelection(getSpinnerPositionFromLockType(exitLockMode.getLockType()));*/
        return rootView;
    }

    protected SharedPreferences getSharedPreferences() {
        if (mSharedPrefs == null) {
            mSharedPrefs = getContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        }
        return mSharedPrefs;
    }

    public void showError() {

    }

    public boolean saveData() {
        @LockMode.LockType int lockType = enterLockMode.getLockType();
        if (lockType == LockMode.LockType.PASSWORD) {
            if (mEnterPasswordLayout.isValid()) {
                enterLockMode.setPassword(mEnterPasswordLayout.getPassword());
            } else {
                Toast.makeText(getContext(), mEnterPasswordLayout.getError(), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (lockType == LockMode.LockType.PIN) {
            if (mEnterPasswordLayout.isValid()) {
                enterLockMode.setPin(mEnterPasswordLayout.getPassword());
            } else {
                Toast.makeText(getContext(), mEnterPasswordLayout.getError(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        lockType = exitLockMode.getLockType();
        if (lockType == LockMode.LockType.PASSWORD) {
            if (mExitPasswordLayout.isValid()) {
                exitLockMode.setPassword(mExitPasswordLayout.getPassword());
            } else {
                Toast.makeText(getContext(), mExitPasswordLayout.getError(), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (lockType == LockMode.LockType.PIN) {
            if (mExitPasswordLayout.isValid()) {
                exitLockMode.setPin(mExitPasswordLayout.getPassword());
            } else {
                Toast.makeText(getContext(), mExitPasswordLayout.getError(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
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

    @Override
    public void setPresenter(ActionsContract.Presenter presenter) {

    }

    @Override
    public void showEmptyProfileError() {

    }

    @Override
    public void setPlace(String address, int radius) {

    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public boolean isActive() {
        return false;
    }

    private static class SpinnerListener implements AdapterView.OnItemSelectedListener {
        private LockMode lockMode;
        private ViewGroup rootView;
        private PasswordInputLayout mPasswordInputLayout;
        private SharedPreferences mPrefs;

        SpinnerListener(LockMode lockMode, PasswordInputLayout passwordInputLayout, ViewGroup rootView, SharedPreferences prefs) {
            this.lockMode = lockMode;
            this.rootView = rootView;
            this.mPasswordInputLayout = passwordInputLayout;
            this.mPrefs = prefs;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            StringWithTag item = (StringWithTag) parent.getItemAtPosition(position);
            @LockMode.LockType int lockType = getLockTypeFromSpinnerTag(item.tag);
            lockMode.setLockType(lockType);
            TransitionManager.beginDelayedTransition(rootView);
            if (lockType == LockMode.LockType.PASSWORD) {
                int len = Integer.parseInt(mPrefs.getString("min_password_length", "4"));
                mPasswordInputLayout.setMinLength(len);
                mPasswordInputLayout.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPasswordInputLayout.setPassword(lockMode.getPassword());
                mPasswordInputLayout.setVisibility(View.VISIBLE);
            } else if (lockType == LockMode.LockType.PIN) {
                int len = Integer.parseInt(mPrefs.getString("min_pin_length", "4"));
                mPasswordInputLayout.setMinLength(len);
                mPasswordInputLayout.setInputType(InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                mPasswordInputLayout.setPassword(lockMode.getPin());
                mPasswordInputLayout.setVisibility(View.VISIBLE);
            } else {
                mPasswordInputLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

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
