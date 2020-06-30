package com.thuruthuru.contacts.ui.history;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.thuruthuru.contacts.R;

import java.util.Date;

public class CustomAdapter extends SimpleCursorAdapter {

    private LayoutInflater cursorInflater;
    private Context context;
    private static Cursor cursor;
    private int layout;
    private int simSlots;
    public static boolean isCallGroup;
    private final int missedCall = android.R.drawable.sym_call_missed;
    private long LIMIT_IN_MILL_3DAY = 3 * 24 * 60 * 60 * 1000;

    int SECONDS_IN_MINUTE = 60;
    int MINUTES_IN_HOURS = 60;
    int SECONDS_IN_HOUR = (MINUTES_IN_HOURS * SECONDS_IN_MINUTE);
    int PERMISSION_REQUEST_CALL_CONTACT = 1;


    public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag, int simSlots, boolean isCallGroup) {
        super(context, layout, c, from, to, flag);
        cursorInflater = LayoutInflater.from(context);
        cursor = c;
        this.context = context;
        this.layout = layout;
        this.simSlots = simSlots;
        this.isCallGroup = isCallGroup;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(layout, null);
    }

    private boolean isCallGrouped() {
        SharedPreferences sharedPref = this.context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        boolean isCallGroup = sharedPref.getBoolean("call_group", true);
        return isCallGroup;
    }

    private void getCurrentCursor(View v, Cursor cursor) {
        int position = (int) v.getTag();
        cursor.moveToPosition(position);
    }

    private String getPhoneNumber() {
        String phNumber = getCursor().getString(getCursor().getColumnIndex(CallLog.Calls.NUMBER));
        return phNumber;
    }

    private int phoneTypeResource() {
        int type = getCursor().getInt(getCursor().getColumnIndex(CallLog.Calls.TYPE));
        int phResource;
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                phResource = android.R.drawable.sym_call_incoming;
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                phResource = android.R.drawable.sym_call_outgoing;
                break;
            case CallLog.Calls.VOICEMAIL_TYPE:
                phResource = android.R.drawable.stat_notify_voicemail;
                break;
            case CallLog.Calls.REJECTED_TYPE:
                phResource = R.drawable.ic_cancel_black_24dp;
                break;
            case CallLog.Calls.BLOCKED_TYPE:
                phResource = R.drawable.ic_app_blocking_black_24dp;
                break;
            default:
                phResource = android.R.drawable.sym_call_missed;
        }
        return phResource;
    }

    private View findViewById(View view, int viewId, Cursor cursor) {
        View resourceView = view.findViewById(viewId);
        resourceView.setTag(cursor.getPosition());
        return resourceView;
    }

    private String getDisplayName() {
        String name = getCursor().getString(getCursor().getColumnIndex("name"));
        return name;
    }

    private String getPhotoURI() {
        String photoURI = getCursor().getString(getCursor().getColumnIndex("photo_uri"));
        return photoURI;
    }

    private int isCallRead() {
        int isRead = getCursor().getInt(getCursor().getColumnIndex("IS_READ"));
        return isRead;
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

        TextView displayNameField = (TextView) findViewById(view, R.id.displayName, cursor);
        TextView phoneNumberField = (TextView) findViewById(view, R.id.phoneNumber, cursor);
        ImageView callField = (ImageView) findViewById(view, R.id.call, cursor);
        ImageView profileField = (ImageView) findViewById(view, R.id.icon, cursor);
        ImageView newMessageField = (ImageView) findViewById(view, R.id.newMessage, cursor);
        ImageView phTypeImgField = (ImageView) findViewById(view, R.id.phTypeImage, cursor);
        TextView callTimeField = (TextView) findViewById(view, R.id.callTime, cursor);
        TextView callDurationField = (TextView) findViewById(view, R.id.callDuration, cursor);
        CardView simField = (CardView) findViewById(view, R.id.sim, cursor);
        TextView simTextField = (TextView) findViewById(view, R.id.sim1, cursor);
        TextView repeatedCallsField = (TextView) findViewById(view, R.id.repeatedCalls, cursor);

        long callTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
        long callDuration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));

        int isRead = isCallRead();

        int type = getCursor().getInt(getCursor().getColumnIndex(CallLog.Calls.TYPE));

        int deviceNum = 0;
        try {
            long phoneAccount = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            if (phoneAccount == -1) deviceNum = 0;
        } catch (Exception e) {
            deviceNum = 0;
        } finally {
            deviceNum = deviceNum + 1;
        }

        if (this.simSlots > 1) {
            simTextField.setText("S" + deviceNum);
            simField.setVisibility(View.VISIBLE);
        } else {
            simField.setVisibility(View.GONE);
        }

        if (isCallGroup) {
            int index;
            try {
                index = cursor.getColumnIndexOrThrow("repeated_calls");
                int repeatedCalls = cursor.getInt(index);
                if (repeatedCalls > 1) {
                    repeatedCallsField.setVisibility(View.VISIBLE);
                    repeatedCallsField.setText("(" + repeatedCalls + ")");
                } else repeatedCallsField.setVisibility(View.GONE);
            } catch (Exception e) {
                repeatedCallsField.setVisibility(View.GONE);
            }
        } else repeatedCallsField.setVisibility(View.GONE);

        String callRelativeTime = DateUtils.getRelativeDateTimeString(context, callTime,
                DateUtils.MINUTE_IN_MILLIS,
                LIMIT_IN_MILL_3DAY,
                0).toString();
        callTimeField.setText(callRelativeTime);

        int typeRes = phoneTypeResource();
        phTypeImgField.setImageResource(typeRes);

        if (missedCall == typeRes
                || R.drawable.ic_cancel_black_24dp == typeRes
                || R.drawable.ic_app_blocking_black_24dp == typeRes) {
            // TextView phoneNumberField = (TextView) findViewById(view, R.id.phoneNumber, cursor);
            // phoneNumberField.setTextColor(context.getResources().getColor(R.color.colorMissedCall, null));
            phTypeImgField.setColorFilter(context.getResources().getColor(R.color.colorMissedCall, null));
        } else {
            phTypeImgField.setColorFilter(null);
        }

        if (callDuration == -1) {
            callDurationField.setVisibility(View.GONE);
        } else {
            String callRelativeDuration = getFormattedCallDuration(callDuration);
            callDurationField.setText(callRelativeDuration + ",");
            callDurationField.setVisibility(View.VISIBLE);
        }

        String displayName = getDisplayName();
        String photoUri = getPhotoURI();

        if (isRead == 0 && type == CallLog.Calls.MISSED_TYPE) {
            TextView field = (displayName == null || displayName.length() == 0) ? phoneNumberField : displayNameField;
            field.setTextColor(context.getResources().getColor(R.color.colorMissedCall, null));
        }

        if (displayName == null || displayName.length() == 0) {
            displayNameField.setVisibility(View.GONE);
        } else {
            displayNameField.setVisibility(View.VISIBLE);
            displayNameField.setText(displayName);
        }

        if (photoUri != null && photoUri.length() > 0) {
            Uri myUri = Uri.parse(photoUri);
            profileField.setImageURI(myUri);
        } else {
            profileField.setImageResource(R.drawable.ic_person_24dp);
        }

        newMessageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentCursor(v, cursor);
                String phoneNumber = getPhoneNumber();
                composeSmsMessage(phoneNumber);
            }
        });

        callField.setClickable(true);
        callField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentCursor(v, cursor);
                String phoneNumber = getPhoneNumber();
                if (isPermissionGranted(v, Manifest.permission.CALL_PHONE))
                    callAction(phoneNumber);
                else
                    showToast("Please provide permissions to make call from main_menu menu.");
            }
        });
    }

    private void callAction(String phoneNumber) {
        if (phoneNumber.length() == 0) {
            showToast("Phone number is empty.!");
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.fromParts("tel", phoneNumber, null));
            this.context.startActivity(callIntent);
        }
    }

    private boolean isPermissionGranted(View v, @NonNull String permission) {
        Context context = v.getContext().getApplicationContext();
        int perm = ActivityCompat.checkSelfPermission(context, permission);

        if (perm == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions((Activity) v.getContext(), new String[]{permission}, PERMISSION_REQUEST_CALL_CONTACT);
            return false;
        }
    }

    private void showToast(String message) {
        Context context = this.context.getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    public void composeSmsMessage(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber)); // This ensures only SMS apps respond
        this.context.startActivity(intent);
    }
}