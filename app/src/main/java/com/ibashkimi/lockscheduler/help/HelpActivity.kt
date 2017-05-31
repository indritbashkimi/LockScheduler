package com.ibashkimi.lockscheduler.help

import android.os.Bundle
import android.view.MenuItem
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.ui.BaseActivity
import com.ibashkimi.lockscheduler.extention.setUpToolbar

class HelpActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        setUpToolbar()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
