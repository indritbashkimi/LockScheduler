package com.ibashkimi.lockscheduler.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.ibashkimi.lockscheduler.BuildConfig
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.manager.action.LockSchedulerAdmin
import com.ibashkimi.lockscheduler.manager.ProfileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun uninstall(context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setMessage(R.string.uninstall_message)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.action_uninstall) { _, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                ProfileManager.removeAll()
                if (LockSchedulerAdmin.isAdminActive(context))
                    LockSchedulerAdmin.removeAdmin(context);

                val packageUri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                context.startActivity(uninstallIntent)
            }
        }
    builder.create().show()
}