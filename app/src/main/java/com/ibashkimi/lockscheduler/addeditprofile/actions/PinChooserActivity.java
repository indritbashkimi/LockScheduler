package com.ibashkimi.lockscheduler.addeditprofile.actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.ui.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PinChooserActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.message_text)
    TextView mMessageText;

    @BindView(R.id.input_layout)
    TextInputLayout mInputLayout;

    @BindView(R.id.editText)
    EditText mEditText;

    @BindView(R.id.actionButton)
    Button mActionButton;

    private String mInput;

    private int mMinLength = 4;

    private String mInputType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pass_chooser);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            mMinLength = getIntent().getIntExtra("min_length", mMinLength);
            mInputType = getIntent().getStringExtra("type");
            setInitialState();
        } else {
            mMinLength = savedInstanceState.getInt("min_length", mMinLength);
            mInputType = savedInstanceState.getString("type");
            mInput = savedInstanceState.getString("input");
            String currInput = savedInstanceState.getString("curr_input");
            mEditText.setText(currInput);
            boolean isConfirmState = savedInstanceState.getBoolean("is_confirm_state");
            if (isConfirmState)
                setConfirmState();
            else
                setInitialState();
        }
        if (mInputType.equals("pin")) {
            mEditText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("min_length", mMinLength);
        outState.putString("type", mInputType);
        outState.putString("input", mInput);
        outState.putString("curr_input", mEditText.getText().toString());
        outState.putBoolean("is_confirm_state", mInput != null);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No op
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No op
    }

    @Override
    public void afterTextChanged(Editable s) {
        mActionButton.setEnabled(s.length() >= mMinLength);
    }

    private View.OnClickListener mFirstFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onContinuePressed();
        }
    };

    private View.OnClickListener mFinalFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onDonePressed();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onCancel();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onContinuePressed() {
        mInput = mEditText.getText().toString();
        setConfirmState();
    }

    public void onDonePressed() {
        if (mEditText.getText().toString().equals(mInput)) {
            onSave();
        } else {
            mInputLayout.setError(getString(R.string.password_mismatch_error));
        }
    }

    private void setInitialState() {
        mMessageText.setText(getResources().getQuantityString(mInputType.equals("pin") ? R.plurals.pin_digits : R.plurals.password_digits, mMinLength, mMinLength));
        mActionButton.setEnabled(false);
        mActionButton.setText(R.string.continue_text);
        mActionButton.setOnClickListener(mFirstFabListener);
        mEditText.addTextChangedListener(this);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_NEXT:
                        if (mEditText.getText().length() >= mMinLength)
                            onContinuePressed();
                        return true;
                    case EditorInfo.IME_ACTION_DONE:
                        onDonePressed();
                        return true;
                }
                return false;
            }
        });
    }

    private void setConfirmState() {
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditText.removeTextChangedListener(this);
        mEditText.setText("");
        mActionButton.setOnClickListener(mFinalFabListener);
        mActionButton.setText(R.string.ok);
        mMessageText.setText(mInputType.equals("pin") ? R.string.confirm_pin : R.string.confirm_password);
    }

    @OnClick(R.id.cancel)
    public void onCancel() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void onSave() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("input", mInput);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
