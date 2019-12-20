package com.ibashkimi.lockscheduler.addeditprofile.conditions.time

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.ui.BaseActivity

class TimePickerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_picker)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_cancel_toolbar)
        }

        findViewById<View>(R.id.fab).setOnClickListener {
            val fragment =
                supportFragmentManager.findFragmentById(R.id.time_fragment) as TimeConditionFragment
            val condition = fragment.assembleCondition()
            val intent = Intent()
            intent.putExtra("time_condition", condition)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
}
