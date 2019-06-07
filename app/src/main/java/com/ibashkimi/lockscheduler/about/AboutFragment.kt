package com.ibashkimi.lockscheduler.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.util.PlatformUtils

class AboutFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.help).setOnClickListener(this)
        view.findViewById<View>(R.id.feedback).setOnClickListener(this)
        view.findViewById<View>(R.id.privacy_policy).setOnClickListener(this)
        view.findViewById<View>(R.id.licenses).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val navController = findNavController(v)
        when (v.id) {
            R.id.help -> navController.navigate(R.id.action_about_to_help)
            R.id.feedback -> PlatformUtils.sendFeedback(requireContext())
            R.id.privacy_policy -> Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
            R.id.licenses -> navController.navigate(R.id.action_about_to_licenses)
        }
    }

    private fun openUrl(@StringRes link: Int) {
        openUrl(getString(link))
    }

    private fun openUrl(url: String) {
        val builder = CustomTabsIntent.Builder()
        //builder.setToolbarColor(ContextCompat.getColor(this, R.color.primary));
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}