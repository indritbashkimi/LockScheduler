package it.ibashkimi.lockscheduler.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import it.ibashkimi.lockscheduler.BuildConfig;
import it.ibashkimi.lockscheduler.R;
import it.ibashkimi.lockscheduler.model.api.AdminApiHelper;
import it.ibashkimi.lockscheduler.model.ProfileManager;

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
                .setPositiveButton(R.string.action_uninstall, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProfileManager.Companion.getInstance().removeAll();

                        AdminApiHelper adminApiHelper = new AdminApiHelper(context);
                        if (adminApiHelper.isAdminActive())
                            adminApiHelper.removeAdmin();

                        Uri packageUri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        Intent uninstallIntent =
                                new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                        context.startActivity(uninstallIntent);
                    }
                });
        builder.create().show();
    }
}
