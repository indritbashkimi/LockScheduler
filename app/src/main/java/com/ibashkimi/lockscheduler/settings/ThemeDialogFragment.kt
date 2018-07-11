package com.ibashkimi.lockscheduler.settings

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.theme.preference.ThemeAdapter
import com.ibashkimi.theme.theme.Theme

class ThemeDialogFragment : DialogFragment(), ThemeAdapter.ThemeSelectedListener {

    var listener: ThemeAdapter.ThemeSelectedListener? = null

    override fun onThemeSelected(theme: Theme) {
        dialog.dismiss()
        if (listener != null)
            listener!!.onThemeSelected(theme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val alertDialogBuilder = AlertDialog.Builder(activity!!)
        alertDialogBuilder.setTitle(R.string.pref_title_theme)

        val inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.fragment_theme, null)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(view.context, 2)
        val themes = Theme.values()
        val theme = Theme.valueOf(arguments!!.getString("theme"))
        recyclerView.adapter = ThemeAdapter(view.context, themes, themes.indexOf(theme), this)

        alertDialogBuilder.setView(view)

        alertDialogBuilder.setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }

        return alertDialogBuilder.create()
    }

    companion object {

        fun newInstance(theme: Theme): ThemeDialogFragment {
            val args = Bundle()
            args.putString("theme", theme.name)
            val fragment = ThemeDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
