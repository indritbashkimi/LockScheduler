package com.ibashkimi.lockscheduler.util

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.addeditprofile.actions.PinChooserActivity
import com.ibashkimi.lockscheduler.model.action.LockAction
import com.ibashkimi.lockscheduler.model.api.AdminUtils
import com.ibashkimi.lockscheduler.model.api.LockSchedulerAdmin
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper


fun Fragment.showPasswordDialog(lockType: Int, onSelected: (Int) -> Unit) {
    val builder = AlertDialog.Builder(context)
    val items = resources.getStringArray(R.array.lock_types)
    val selectedItem = when (lockType) {
        LockAction.LockType.UNCHANGED -> 0
        LockAction.LockType.SWIPE -> 1
        LockAction.LockType.PIN -> 2
        LockAction.LockType.PASSWORD -> 3
        else -> throw IllegalArgumentException("Cannot determine selected item.")
    }
    builder.setTitle(R.string.dialog_lock_settings_title)
            .setSingleChoiceItems(items, selectedItem, { dialog, which ->
                onSelected(which)
                dialog.dismiss()
            })
    builder.create().show()
}

fun lockTypeToTextRes(lockType: Int) = when (lockType) {
    LockAction.LockType.UNCHANGED -> R.string.lock_mode_nothing
    LockAction.LockType.PIN -> R.string.lock_mode_pin
    LockAction.LockType.PASSWORD -> R.string.lock_mode_password
    LockAction.LockType.SWIPE -> R.string.lock_mode_swipe
    else -> throw IllegalStateException("Cannot determine summary.")
}

fun positionToLockType(position: Int) = when (position) {
    0 -> LockAction.LockType.UNCHANGED
    1 -> LockAction.LockType.SWIPE
    2 -> LockAction.LockType.PIN
    3 -> LockAction.LockType.PASSWORD
    else -> throw IllegalStateException("Cannot determine lock type. position=$position.")
}

fun Fragment.isAdminPermissionGranted(): Boolean {
    return LockSchedulerAdmin.isAdminActive(context)
}

fun isAdminRationaleNeeded(): Boolean {
    return AppPreferencesHelper.isAdminRationaleNeeded
}


fun Fragment.showAdminPermissionRationale(onOk: () -> Unit, onCancel: () -> Unit) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(R.string.admin_permission_rationale_title)
            .setMessage(R.string.admin_permission_rationale)
            .setPositiveButton(R.string.ok) { _, _ -> onOk() }
            .setNegativeButton(R.string.cancel) { _, _ -> onCancel() }
    builder.create().show()
}

fun Fragment.checkAdminPermission(onGranted: () -> Unit, onRationaleNeeded: () -> Unit, onDenied: () -> Unit) {
    when {
        isAdminPermissionGranted() -> onGranted()
        isAdminRationaleNeeded() -> onRationaleNeeded()
        else -> onDenied()
    }
}

fun Fragment.showPasswordChooser(requestCode: Int) {
    val intent = Intent(context, PinChooserActivity::class.java)
    intent.putExtra("type", "password")
    intent.putExtra("min_length", AppPreferencesHelper.minPasswordLength)
    startActivityForResult(intent, requestCode)
}

fun Fragment.showPinChooser(requestCode: Int) {
    val intent = Intent(context, PinChooserActivity::class.java)
    intent.putExtra("type", "pin")
    intent.putExtra("min_length", AppPreferencesHelper.minPinLength)
    startActivityForResult(intent, requestCode)
}

fun Fragment.askAdminPermission(requestCode: Int) {
    startActivityForResult(AdminUtils.buildAddAdminIntent(context), requestCode)
}

fun handleAdminPermissionResult(resultCode: Int, onGranted: () -> Unit, onDenied: () -> Unit) {
    if (resultCode == Activity.RESULT_CANCELED) {
        AppPreferencesHelper.isAdminRationaleNeeded = true
        onDenied()
    } else if (resultCode == Activity.RESULT_OK) {
        onGranted()
    }
}