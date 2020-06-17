package com.thuruthuru.contacts.ui.call_details;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.thuruthuru.contacts.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends SimpleCursorAdapter {

    private LayoutInflater cursorInflater;
    private Context context;
    private static Cursor cursor;
    private int layout;
    private final int missedCall = android.R.drawable.sym_call_missed;
    private long LIMIT_IN_MILL_3DAY = 3 * 24 * 60 * 60 * 1000;

    int SECONDS_IN_MINUTE = 60;
    int MINUTES_IN_HOURS = 60;
    int SECONDS_IN_HOUR = (MINUTES_IN_HOURS * SECONDS_IN_MINUTE);


    public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag) {
        super(context, layout, c, from, to, flag);
        cursorInflater = LayoutInflater.from(context);
        cursor = c;
        this.context = context;
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(layout, null);
    }

    private void getCurrentCursor(View v, Cursor cursor) {
        int position = (int) v.getTag();
        cursor.moveToPosition(position);
    }

    private ArrayList phoneTypeResource() {
        String type = getCursor().getString(1);
        int phResource;
        String callType = "";
        switch (type) {
            case "1":
                phResource = android.R.drawable.sym_call_incoming;
                callType = "Incoming call";
                break;
            case "2":
                phResource = android.R.drawable.sym_call_outgoing;
                callType = "Outgoing call";
                break;
            case "3":
                phResource = android.R.drawable.sym_call_missed;
                callType = "Missed call";
                break;
            case "4":
                phResource = android.R.drawable.stat_notify_voicemail;
                callType = "Voice mail";
                break;
            case "5":
                phResource = R.drawable.ic_cancel_black_24dp;
                break;
            case "6":
                phResource = R.drawable.ic_app_blocking_black_24dp;
                break;
            default:
                phResource = android.R.drawable.sym_call_missed;
                callType = "Missed mail";
        }
        ArrayList l = new ArrayList();
        l.add(phResource);
        l.add(callType);
        return l;
    }

    private View findViewById(View view, int viewId, Cursor cursor) {
        View resourceView = view.findViewById(viewId);
        resourceView.setTag(cursor.getPosition());
        return resourceView;
    }

    private String getFormattedCallDuration(long callDuration) {
        int inHours = (int) callDuration / SECONDS_IN_HOUR;
        int inMinutes = (int) callDuration / SECONDS_IN_MINUTE;
        long inSecs = callDuration;

        String formattedDuration = "";
        if (inHours > 0) {
            formattedDuration = formattedDuration + inHours + " hrs ";
            inMinutes = (int) (callDuration % SECONDS_IN_HOUR);
        }

        if (inMinutes > 0) {
            formattedDuration = formattedDuration + inMinutes + " mins ";
            inSecs = (int) (inMinutes % SECONDS_IN_MINUTE);
        }

        formattedDuration = formattedDuration + inSecs + " secs";
        return formattedDuration;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // R.layout.list_row is your xml layout for each row
        super.bindView(view, context, cursor);

        Date dt = new Date();

        ImageView phTypeImgField = (ImageView) findViewById(view, R.id.callTypeImage, cursor);
        TextView callTypeField = (TextView) findViewById(view, R.id.callType, cursor);
        TextView callTimeField = (TextView) findViewById(view, R.id.callTime, cursor);
        TextView callDurationField = (TextView) findViewById(view, R.id.callDuration, cursor);

        long callTime = cursor.getLong(2);
        long callDuration = cursor.getLong(3);

        String callRelativeTime = DateUtils.getRelativeDateTimeString(context, callTime,
                DateUtils.MINUTE_IN_MILLIS,
                LIMIT_IN_MILL_3DAY,
                0).toString();
        callTimeField.setText(callRelativeTime);

        ArrayList typeRes = phoneTypeResource();
        phTypeImgField.setImageResource((int)typeRes.get(0));
        callTypeField.setText((String)typeRes.get(1) + ",");

        int phResource = (int)typeRes.get(0);
        if (missedCall == phResource || R.drawable.ic_cancel_black_24dp == phResource ||
                R.drawable.ic_app_blocking_black_24dp == phResource) {
            // TextView phoneNumberField = (TextView) findViewById(view, R.id.phoneNumber, cursor);
            // phoneNumberField.setTextColor(context.getResources().getColor(R.color.colorMissedCall, null));
            callTypeField.setTextColor(context.getResources().getColor(R.color.colorMissedCall, null));
        }


        String callRelativeDuration = getFormattedCallDuration(callDuration);
        callDurationField.setText(callRelativeDuration);
    }
}