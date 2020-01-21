package com.ibashkimi.lockscheduler.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.ibashkimi.lockscheduler.R;

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

}
