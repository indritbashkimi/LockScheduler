package com.ibashkimi.lockscheduler.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.theme.R
import com.ibashkimi.theme.preference.ThemeAdapter
import com.ibashkimi.theme.theme.Theme


class ThemePreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat(), ThemeAdapter.ThemeSelectedListener {

    private var mSelectedTheme: Theme? = null
    private var mThemeChanged: Boolean = false

    private val sharedPreferences: SharedPreferences
        get() = context!!.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    override fun onBindDialogView(view: View) {
        val recyclerView: RecyclerView = view.findViewById<RecyclerView>(R.id.recyclerView) // recyclerViewColor
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
        val themes = Theme.values()
        val theme = Theme.valueOf(sharedPreferences
                .getString("theme", Theme.INDIGO_PINK.name))
        recyclerView.adapter = ThemeAdapter(view.context, themes, -1, this)
        super.onBindDialogView(view)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            Log.d(TAG, "onDialogClosed: positiveResult")
            //getSharedPreferences().edit().putInt("theme_id", mSelectedTheme).apply();
            //persistInt(mSelectedTheme);
        }
        Log.e(TAG, "onDialogClosed: no positiveResult")
        if (mThemeChanged)
            sharedPreferences.edit().putString("theme", mSelectedTheme!!.name).apply()
    }

    override fun onThemeSelected(theme: Theme) {
        mSelectedTheme = theme
        mThemeChanged = true
        dialog.dismiss()
    }

    companion object {

        private val TAG = "ThemePreferenceDialogFr"

        fun newInstance(key: String): ThemePreferenceDialogFragmentCompat {
            val fragment = ThemePreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b

            return fragment
        }
    }
}
