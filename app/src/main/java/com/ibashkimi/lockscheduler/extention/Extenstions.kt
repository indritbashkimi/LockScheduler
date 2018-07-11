package com.ibashkimi.lockscheduler.extention

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ibashkimi.lockscheduler.R


fun AppCompatActivity.setUpToolbar(toolbar: Toolbar = findViewById(R.id.toolbar)) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
}

fun Activity.isPermissionGranted(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.isPermissionRationaleNeeded(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

fun Activity.requestPermission(permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

inline fun AppCompatActivity.checkPermission(permission: String,
                                             whenGranted: (AppCompatActivity.() -> Unit),
                                             whenExplanationNeed: (AppCompatActivity.() -> Unit),
                                             whenDenied: (AppCompatActivity.() -> Unit)) {
    when {
        isPermissionGranted(permission) -> whenGranted()
        isPermissionRationaleNeeded(permission) -> whenExplanationNeed()
        else -> whenDenied()
    }
}

inline fun AppCompatActivity.handlePermissionResult(permission: String,
                                                    permissions: Array<String>,
                                                    grantResults: IntArray,
                                                    whenGranted: (AppCompatActivity.() -> Unit),
                                                    whenDenied: (AppCompatActivity.() -> Unit)) {
    for (i in permission.indices) {
        if (permissions[i] == permission) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) whenGranted() else whenDenied()
            return
        }
    }
}
