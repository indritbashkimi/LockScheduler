package com.ibashkimi.lockscheduler.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.ibashkimi.lockscheduler.BuildConfig;
import com.ibashkimi.lockscheduler.R;
import com.ibashkimi.lockscheduler.data.ProfileManager;
import com.ibashkimi.lockscheduler.api.LockSchedulerAdmin;

public class PlatformUtils {

    public static void sendFeedback(Context context) {
        // http://stackoverflow.com/a/16217921
        // https://developer.android.com/guide/components/intents-common.html#Email
        String address = context.getString(R.string.developer_email);
        String subject = context.getString(R.string.feedback_subject);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + address));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        String chooserTitle = context.getString(R.string.feedback_chooser_title);
        context.startActivity(Intent.createChooser(emailIntent, chooserTitle));
    }

    public static void uninstall(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.uninstall_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.action_uninstall, (dialog, which) -> {
                    ProfileManager.INSTANCE.removeAll();

                    if (LockSchedulerAdmin.isAdminActive(context))
                        LockSchedulerAdmin.removeAdmin(context);

                    Uri packageUri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                    Intent uninstallIntent =
                            new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                    context.startActivity(uninstallIntent);
                });
        builder.create().show();
    }
}
