package com.ibashkimi.lockscheduler.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;

import com.ibashkimi.lockscheduler.R;

import androidx.annotation.RequiresApi;
import androidx.preference.DialogPreference;

public class ThemePreference extends DialogPreference {

    private int mDialogLayoutResId = R.layout.pref_dialog_color;


    public ThemePreference(Context context) {
        this(context, null);
    }

    public ThemePreference(Context context, AttributeSet attrs) {
        // Why this magic trick: http://stackoverflow.com/a/23432239
        this(context, attrs, R.style.Preference_DialogPreference);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setPersistent(false);
        setDialogLayoutResource(mDialogLayoutResId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setPersistent(false);
        setDialogLayoutResource(mDialogLayoutResId);
    }

    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
    }
}

