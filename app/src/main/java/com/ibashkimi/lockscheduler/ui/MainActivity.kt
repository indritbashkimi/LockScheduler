package com.ibashkimi.lockscheduler.ui

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.util.createNotificationChannels

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = preferences
        if (prefs.getBoolean("first_run", true)) {
            this.createNotificationChannels()
            prefs.edit().putBoolean("first_run", false).apply()
        }

        setContentView(R.layout.activity_profiles)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    private val navController: NavController
        get() = Navigation.findNavController(this, R.id.main_nav_host_fragment)

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()
}