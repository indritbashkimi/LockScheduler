package it.ibashkimi.lockscheduler.addeditprofile.actions;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import it.ibashkimi.lockscheduler.R;

import static it.ibashkimi.lockscheduler.addeditprofile.actions.PinInputFragment.InputType.INPUT_TYPE_PASSWORD;
import static it.ibashkimi.lockscheduler.addeditprofile.actions.PinInputFragment.InputType.INPUT_TYPE_PIN;

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
public class PinInputFragment extends Fragment {
    private static final String TAG = "PinInputFragment";

    @IntDef({INPUT_TYPE_PASSWORD, INPUT_TYPE_PIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface InputType {
        int INPUT_TYPE_PASSWORD = 0;
        int INPUT_TYPE_PIN = 1;
    }

    private static final String ARGUMENT_INPUT_TYPE = "argument_input_type";
    private static final String ARGUMENT_MIN_LENGTH = "argument_min_length";

    private TextInputLayout mTextInputLayout;
    private TextInputLayout mConfirmTextInputLayout;
    private EditText mEditText;
    private EditText mConfirmEditText;
    private String mError;
    private int mMinLength = 0; // 0 means no check

    private @StringRes int mHint;
    private @StringRes int mShortInputError;
    private @StringRes int mInputMismatchError;

    private TextWatcher mEditTextTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setPasswordTooShortError(editable.length() < mMinLength);
            setPasswordMismatchError(!editable.toString().equals(mConfirmEditText.getText().toString()));
        }
    };

    public static PinInputFragment newInstance(@InputType int inputType, int minLength) {
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_INPUT_TYPE, inputType);
        args.putInt(ARGUMENT_MIN_LENGTH, minLength);
        PinInputFragment fragment = new PinInputFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public PinInputFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        int inputType = args.getInt(ARGUMENT_INPUT_TYPE);

        mMinLength = args.getInt(ARGUMENT_MIN_LENGTH);

        if (inputType == INPUT_TYPE_PIN) {
            mHint = R.string.pin_input_hint;
            mInputMismatchError = R.string.pin_mismatch_error;
            mShortInputError = R.string.pin_short_error;
        } else {
            mHint = R.string.password_input_hint;
            mInputMismatchError = R.string.password_mismatch_error;
            mShortInputError = R.string.password_short_error;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_pin_input, container, false);

        mTextInputLayout = (TextInputLayout) rootView.findViewById(R.id.input_layout);
        mConfirmTextInputLayout = (TextInputLayout) rootView.findViewById(R.id.confirm_input_layout);
        mConfirmTextInputLayout.setEnabled(false);
        mEditText = (EditText) rootView.findViewById(R.id.editText);
        mConfirmEditText = (EditText) rootView.findViewById(R.id.confirm_editText);
        //mEditText.setHint(mHint);
        //mConfirmEditText.setHint(mHint);
        mConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setPasswordMismatchError(!s.toString().equals(mEditText.getText().toString()));
            }
        });
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !passwordMatch()) {
                    setPasswordMismatchError(true);
                }
            }
        });

        return rootView;
    }

    /**
     * Set the type of the content with a constant as defined for {@link EditorInfo#inputType}.
     *
     * @attr ref android.R.styleable#TextView_inputType
     * @see android.text.InputType
     */
    public void setInputType(int type) {
        mEditText.setInputType(type);
        mConfirmEditText.setInputType(type);
    }

    public String getPassword() {
        return passwordMatch() ? mEditText.getText().toString() : null;
    }

    public void setPassword(CharSequence password) {
        Log.d(TAG, "setPassword() called with: password = [" + password + "]");
        mEditText.setText(password);
        mConfirmEditText.setText(password);
        mEditText.setSelection(mEditText.getText().length());
        mConfirmEditText.setSelection(mConfirmEditText.getText().length());
    }

    public boolean passwordMatch() {
        return mEditText.getText().toString().equals(mConfirmEditText.getText().toString());
    }

    public boolean hasValidLength() {
        return mEditText.getText().length() >= mMinLength;
    }

    public void setMinLength(int length) {
        if (length > 0) {
            mMinLength = length;
            mEditText.addTextChangedListener(mEditTextTextWatcher);
        } else {
            mEditText.removeTextChangedListener(mEditTextTextWatcher);
            mTextInputLayout.setErrorEnabled(false);
        }
    }

    public boolean isValid() {
        return hasValidLength() && passwordMatch();
    }

    public String getError() {
        return mError;
    }

    private void setPasswordTooShortError(boolean tooShort) {
        if (tooShort) {
            mError = "Password too short";
            mTextInputLayout.setError(mError);
        } else {
            mError = null;
            mTextInputLayout.setErrorEnabled(false);
        }
    }

    private void setPasswordMismatchError(boolean mismatch) {
        if (mismatch) {
            mError = "Password doesn't match";
            mConfirmTextInputLayout.setError(mError);
        } else {
            mError = null;
            mConfirmTextInputLayout.setErrorEnabled(false);
        }
    }
}
