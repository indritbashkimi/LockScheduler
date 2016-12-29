package it.ibashkimi.lockscheduler.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import it.ibashkimi.lockscheduler.R;


public class PasswordInputLayout extends LinearLayout {

    private TextInputLayout mTextInputLayout;
    private TextInputLayout mConfirmTextInputLayout;
    private EditText mEditText;
    private EditText mConfirmEditText;
    private int mMinLength = 0; // 0 means no check
    private TextWatcher mEditTextTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() < mMinLength) {
                mTextInputLayout.setError("Too short");
                mError = "Too short";
            }
        }
    };
    private String mError;

    public PasswordInputLayout(Context context) {
        this(context, null);
    }

    public PasswordInputLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PasswordInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.text_input_layout, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PasswordInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflate(context, R.layout.text_input_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextInputLayout = (TextInputLayout) findViewById(R.id.input_layout);
        mConfirmTextInputLayout = (TextInputLayout) findViewById(R.id.confirm_input_layout);
        mEditText = (EditText) findViewById(R.id.editText);
        mConfirmEditText = (EditText) findViewById(R.id.confirm_editText);
        mEditText.setHint("Enter password");
        mConfirmEditText.setHint("Enter password");
        mConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(mEditText.getText().toString())) {
                    setPasswordMismatchError();
                } else {
                    mConfirmTextInputLayout.setErrorEnabled(false);
                }
            }
        });
        mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !passwordMatch()) {
                    setPasswordMismatchError();
                }
            }
        });
    }

    /**
     * Set the type of the content with a constant as defined for {@link EditorInfo#inputType}.
     *
     * @see android.text.InputType
     * @attr ref android.R.styleable#TextView_inputType
     */
    public void setInputType(int type) {
        mEditText.setInputType(type);
        mConfirmEditText.setInputType(type);
    }

    public String getPassword() {
        return passwordMatch() ? mEditText.getText().toString() : null;
    }

    private static final String TAG = "PasswordInputLayout";
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

    public boolean isMinimumLong() {
        return mMinLength != 0 && mEditText.getText().length() > mMinLength;
    }


    private void showEmptyInputError() {
        mConfirmTextInputLayout.setError("Password is empty");
    }

    private void setPasswordMismatchError() {
        mConfirmTextInputLayout.setError("Password doesn't match");
        mError = "Password doesn't match";
    }

    public void setMinLength(int length) {
        if (length > 0) {
            mMinLength = length;
            mEditText.addTextChangedListener(mEditTextTextWatcher);
        } else {
            mEditText.removeTextChangedListener(mEditTextTextWatcher);
        }
    }

    public boolean isCorrect() {
        return mError == null;
    }

    public String getError() {
        return mError;
    }
}
