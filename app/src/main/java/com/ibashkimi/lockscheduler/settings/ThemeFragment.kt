package com.ibashkimi.lockscheduler.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.ui.BaseActivity
import com.ibashkimi.theme.preference.PremiumThemeAdapter
import com.ibashkimi.theme.theme.Theme

class ThemeFragment : Fragment(), PremiumThemeAdapter.ThemeSelectedListener {

    private val themes: Array<Theme> = Theme.values()

    private var baseActivity: BaseActivity? = null

    private lateinit var themeAdapter: PremiumThemeAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity?
                ?: throw IllegalStateException("Activity must be BaseActivity.")
    }

    override fun onDetach() {
        super.onDetach()
        baseActivity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_theme, container, false)

        root.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView).apply {
            layoutManager = GridLayoutManager(requireContext(), resources.getInteger(com.ibashkimi.theme.R.integer.theme_columns))
            val theme = baseActivity!!.themePreferences.getTheme(Theme.INDIGO_PINK)
            themeAdapter = PremiumThemeAdapter(requireContext(), themes, themes.indexOf(theme), { isThemePremium(it) }, this@ThemeFragment)
            adapter = themeAdapter
        }

        return root
    }

    override fun onThemeSelected(theme: Theme) {
        baseActivity!!.apply {
            themePreferences.setTheme(theme)
            recreate()
        }
    }

    private fun isThemePremium(theme: Theme): Boolean {
        return false//!isPremium && theme.name.toLowerCase().startsWith("full")
    }
}
