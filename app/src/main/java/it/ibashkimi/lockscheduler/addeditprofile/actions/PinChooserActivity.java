package it.ibashkimi.lockscheduler.addeditprofile.actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.ui.BaseActivity;

public class PinChooserActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.message_text)
    TextView mMessageText;

    @BindView(R.id.input_layout)
    TextInputLayout mInputLayout;

    @BindView(R.id.editText)
    EditText mEditText;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private String mInput;

    private int mMinLength = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_chooser);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mFab.setVisibility(View.GONE);
        mFab.setImageResource(R.drawable.ic_pin_next);
        mFab.setOnClickListener(mFirstFabListener);
        mEditText.addTextChangedListener(this);

        if (savedInstanceState == null) {
            mMessageText.setText("PIN must be 4 digits");
        }
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
        mFab.setVisibility(s.length() >= mMinLength ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener mFirstFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mInput = mEditText.getText().toString();
            mEditText.removeTextChangedListener(PinChooserActivity.this);
            mEditText.setText("");
            mFab.setImageResource(R.drawable.ic_done);
            mFab.setOnClickListener(mFinalFabListener);
            mMessageText.setText("Confirm PIN");
        }
    };

    private View.OnClickListener mFinalFabListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mEditText.getText().toString().equals(mInput)) {
                onSave();
            } else {
                mInputLayout.setError(getString(R.string.password_mismatch_error));
            }
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

    private void onCancel() {
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
