package com.ibashkimi.lockscheduler.addeditprofile.actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.databinding.ActivityPinPassChooserBinding;
import com.ibashkimi.lockscheduler.ui.BaseActivity;

public class PinChooserActivity extends BaseActivity implements TextWatcher {

    private String mInput;

    private int mMinLength = 4;

    private String mInputType;

    private ActivityPinPassChooserBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPinPassChooserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.cancel).setOnClickListener(view -> onCancel());

        if (savedInstanceState == null) {
            mMinLength = getIntent().getIntExtra("min_length", mMinLength);
            mInputType = getIntent().getStringExtra("type");
            setInitialState();
        } else {
            mMinLength = savedInstanceState.getInt("min_length", mMinLength);
            mInputType = savedInstanceState.getString("type");
            mInput = savedInstanceState.getString("input");
            String currInput = savedInstanceState.getString("curr_input");
            binding.editText.setText(currInput);
            boolean isConfirmState = savedInstanceState.getBoolean("is_confirm_state");
            if (isConfirmState)
                setConfirmState();
            else
                setInitialState();
        }
        if (mInputType.equals("pin")) {
            binding.editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            binding.editText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("min_length", mMinLength);
        outState.putString("type", mInputType);
        outState.putString("input", mInput);
        outState.putString("curr_input", binding.editText.getText().toString());
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
        binding.actionButton.setEnabled(s.length() >= mMinLength);
    }

    private View.OnClickListener mFirstFabListener = v -> onContinuePressed();

    private View.OnClickListener mFinalFabListener = v -> onDonePressed();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onCancel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onContinuePressed() {
        mInput = binding.editText.getText().toString();
        setConfirmState();
    }

    public void onDonePressed() {
        if (binding.editText.getText().toString().equals(mInput)) {
            onSave();
        } else {
            binding.inputLayout.setError(getString(R.string.password_mismatch_error));
        }
    }

    private void setInitialState() {
        binding.messageText.setText(getResources().getQuantityString(mInputType.equals("pin") ? R.plurals.pin_digits : R.plurals.password_digits, mMinLength, mMinLength));
        binding.actionButton.setEnabled(false);
        binding.actionButton.setText(R.string.continue_text);
        binding.actionButton.setOnClickListener(mFirstFabListener);
        binding.editText.addTextChangedListener(this);
        binding.editText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        binding.editText.setOnEditorActionListener((v, actionId, event) -> {
            switch (actionId) {
                case EditorInfo.IME_ACTION_NEXT:
                    if (binding.editText.getText().length() >= mMinLength)
                        onContinuePressed();
                    return true;
                case EditorInfo.IME_ACTION_DONE:
                    onDonePressed();
                    return true;
            }
            return false;
        });
    }

    private void setConfirmState() {
        binding.editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        binding.editText.removeTextChangedListener(this);
        binding.editText.setText("");
        binding.actionButton.setOnClickListener(mFinalFabListener);
        binding.actionButton.setText(android.R.string.ok);
        binding.messageText.setText(mInputType.equals("pin") ? R.string.confirm_pin : R.string.confirm_password);
    }

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
