package com.thuruthuru.contacts.receiver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.TaskStackBuilder;

import java.util.HashMap;

import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.common.phoneContact;
import com.thuruthuru.contacts.MainActivity;


public class IncomingCallReceiver extends BroadcastReceiver {
    TelephonyManager telephonyManager;


    @Override
    public void onReceive(Context context, Intent intent) {

        phoneStateListener phoneListener = new phoneStateListener(context);
        telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

    }

    public void onDestroy() {
        telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
    }
}

class phoneStateListener extends PhoneStateListener {

    private String NOTIFICATION_CHANNEL_ID = "MISSED CALL";
    private String GROUP_ID = "com.thuruthuru.contacts";
    String TITLE = "Missed call";
    String CHANNEL_TITLE = "Missed calls";
    int GROUP_ID_NUM = 2345;
    int NOTIFICATION_ID = 2345;

    private Context mContext;
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;

    PendingIntent resultPendingIntent;


    //Constructor
    public phoneStateListener(Context context) {
        mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        if (lastState == state) return;


        boolean isValidNumber = incomingNumber != null && !incomingNumber.isEmpty() && !incomingNumber.equals("null");
        String name = incomingNumber;
        if (isValidNumber) {
            HashMap<String, String> values = phoneContact.getDisplayName(mContext, incomingNumber);
            name = values.getOrDefault("name", "" + incomingNumber);
        }
        try {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        notificationDialog(mContext, name, incomingNumber);
                    }
                    break;
            }
            lastState = state;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationDialog(Context context, String name, String number) {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("menu", "HistoryFragment");

        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setContentIntent(resultPendingIntent);
        notificationBuilder.setGroup(GROUP_ID)
                .setAutoCancel(true)
                .setTicker(TITLE)
                .setContentTitle(name)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.sym_call_missed)
                .setPriority(Notification.PRIORITY_HIGH);

        // NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription(CHANNEL_TITLE);
            notificationChannel.setLightColor(context.getResources().getColor(R.color.colorFabFG, null));
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(++NOTIFICATION_ID, notificationBuilder.build());
    }
}