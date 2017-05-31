package com.ibashkimi.lockscheduler.extention

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.ibashkimi.lockscheduler.R

fun AppCompatActivity.setUpToolbar(toolbar: Toolbar = findViewById(R.id.toolbar) as Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
}
