package com.thuruthuru.contacts.ui.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;

import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.thuruthuru.contacts.ui.call_details.CallDetailFragment;
import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.common.phoneContact;

public class HistoryFragment extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static int simSlots;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION_KEYS = {
            CallLog.Calls._ID
    };

    private static final String[] FROM_COLUMNS = {
            CallLog.Calls.CACHED_PHOTO_URI,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.PHONE_ACCOUNT_ID,
            CallLog.Calls.IS_READ,
    };

    private static final String[] R_FROM_COLUMN = new String[]{"repeated_calls"};

    private static final int[] TO_IDS = {
            0,
            R.id.displayName,
            R.id.phoneNumber,
            0,
            R.id.callTime,
            0,
            0,
            0
    };


    private static String[] PROJECTION = new String[PROJECTION_KEYS.length +
            FROM_COLUMNS.length];

    private String ORDER_BY = CallLog.Calls.DATE + " DESC ";

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    private static final int PERMISSION_REQUEST_READ_CALL_LOG = 0;
    private static final int PERMISSION_REQUEST_WRITE_CALL_LOG = 1;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 2;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 3;
    private static final int PERMISSION_REQUEST_HISTORY = 4;

    // The column index for the CONTACT_KEY column
    private static final int CONTACT_KEY_INDEX = 1;

    // Defines a variable for the search string
    private String searchString = "";
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {searchString, searchString};

    // Define global mutable variables
    // Define a ListView object
    private ListView callList;

    // An adapter that binds the result Cursor to the ListView
    private CustomAdapter cursorAdapter;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static String selection_display_name = CallLog.Calls.CACHED_NAME + " LIKE ?";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        List<String> list = new ArrayList<String>(Arrays.asList(PROJECTION_KEYS)); //returns a list view of an array
        list.addAll(Arrays.asList(FROM_COLUMNS));
        list.toArray(PROJECTION);
        callList = (ListView) root.findViewById(R.id.callListView);
        TextView em = (TextView) root.findViewById(R.id.empty);
        em.setText("No new call details to display");
        callList.setEmptyView(em);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(20);
        return root;
    }

    @SuppressLint("ResourceType")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Gets the ListView from the View list of the parent activity
        simSlots = getSimSlots();
        boolean isCallGroup = isCallGrouped();

        cursorAdapter = new CustomAdapter(
                getActivity(),
                R.layout.recent_contact_item,
                null,
                FROM_COLUMNS,
                TO_IDS, 0,
                simSlots,
                isCallGroup);
        callList.setAdapter(cursorAdapter);
        callList.setOnItemClickListener(this);

        isPermissionGrantedCallLogs(PERMISSION_REQUEST_HISTORY);
    }

    private int getSimSlots() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        int simSlots = sharedPref.getInt("simSlots", -1);

        if (simSlots == -1) {
            TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            int slots = manager.getPhoneCount();
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("simSlots", slots);
            editor.apply();
        }
        return simSlots;
    }

    private void showCallLogs() {
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        clearMissedCalls();
    }

    private void clearMissedCalls() {
        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NEW, 0);
        values.put(CallLog.Calls.IS_READ, 1);
        StringBuilder where = new StringBuilder();
        where.append(CallLog.Calls.NEW);
        where.append(" = 1 AND ");
        where.append(CallLog.Calls.TYPE);
        where.append(" = ?");
        getActivity().getContentResolver().update(CallLog.Calls.CONTENT_URI, values, where.toString(),
                new String[]{Integer.toString(CallLog.Calls.MISSED_TYPE)});
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private boolean isPermissionGranted(@NonNull String permission, int requestCode) {
        Context context = getContext().getApplicationContext();
        int perm = ActivityCompat.checkSelfPermission(context, permission);

        if (perm == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions(new String[]{permission}, requestCode);
            return false;
        }
    }

    private void isPermissionGrantedCallLogs(int requestCode) {
        String permission = Manifest.permission.READ_CALL_LOG;
        String rs_permission = Manifest.permission.READ_PHONE_STATE;
        String rc_permission = Manifest.permission.READ_CONTACTS;
        String w_permission = Manifest.permission.WRITE_CALL_LOG;

        String[] permissions = {permission, rs_permission, rc_permission, w_permission};

        boolean granted = true;
        for (String perms : permissions) {
            Context context = getContext().getApplicationContext();
            int perm = ActivityCompat.checkSelfPermission(context, perms);

            if (perm == PackageManager.PERMISSION_GRANTED)
                granted = granted && true;
            else
                granted = granted && false;
        }

        if (!granted)
            requestPermissions(permissions, PERMISSION_REQUEST_HISTORY);
        else
            showCallLogs();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {

        Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

        // display sheet
        CallDetailFragment fragment = new CallDetailFragment(phoneNumber, simSlots);
        fragment.show(getActivity().getSupportFragmentManager(), fragment.getTag());
    }

    private boolean isCallGrouped() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        boolean isCallGroup = sharedPref.getBoolean("call_group", true);
        return isCallGroup;
    }

    @Override
    public void onStart() {
        super.onStart();
        cursorAdapter.isCallGroup = isCallGrouped();
        isPermissionGrantedCallLogs(PERMISSION_REQUEST_HISTORY);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */

        selectionArgs[0] = "%" + searchString + "%";
        selectionArgs[1] = "%" + searchString + "%";

        // Starts the query
        CursorLoader cursorLoader = new CursorLoader(
                getActivity(),
                CallLog.Calls.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ORDER_BY
        );
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView

        boolean isCallGroup = isCallGrouped();
        MatrixCursor newCursor = isCallGroup ? getAggregatedCursor(cursor) : getNewCursor(cursor);
        cursorAdapter.swapCursor(newCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        cursorAdapter.swapCursor(null);

    }

    private MatrixCursor getAggregatedCursor(Cursor cursor) {
        String[] PROJECTION_1 = new String[PROJECTION.length + R_FROM_COLUMN.length];

        List<String> list = new ArrayList<String>(Arrays.asList(PROJECTION)); //returns a list view of an array
        list.addAll(Arrays.asList(R_FROM_COLUMN));
        list.toArray(PROJECTION_1);

        MatrixCursor newCursor = new MatrixCursor(PROJECTION_1); // Same projection used in loader
        if (cursor.moveToFirst()) {

            int repeatedCalls = 1;

            HashMap<String, String> previousDataHash = getDataFromCursor(cursor);

            while (cursor.moveToNext()) {
                String number = previousDataHash.get("number");
                String name = previousDataHash.getOrDefault("name", number);
                String type = previousDataHash.get("type");
                String simSlot = previousDataHash.get("simSlot");

                String newEntry = name + type + simSlot;

                HashMap<String, String> currentDataHash = getDataFromCursor(cursor);
                String currentNumber = currentDataHash.get("number");
                String CurrentName = currentDataHash.getOrDefault("name", currentNumber);
                String currentType = currentDataHash.get("type");
                String currentSimSlot = currentDataHash.get("simSlot");

                String currentLog = CurrentName + currentType + currentSimSlot;

                if (newEntry.equalsIgnoreCase(currentLog)) {
                    repeatedCalls++;
                } else {
                    Object[] newData = getNewCallData(previousDataHash, repeatedCalls);
                    newCursor.addRow(newData);

                    repeatedCalls = 1;
                    previousDataHash = currentDataHash;
                }
            }

            Object[] newData = getNewCallData(previousDataHash, repeatedCalls);
            newCursor.addRow(newData);
        }
        return newCursor;
    }


    private MatrixCursor getNewCursor(Cursor cursor) {
        MatrixCursor newCursor = new MatrixCursor(PROJECTION); // Same projection used in loader
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> values = getDataFromCursor(cursor);
                Object[] newData = newCursorDataObject(values);
                newCursor.addRow(newData);

            } while (cursor.moveToNext());
        }
        return newCursor;
    }

    private Object[] getNewCallData(HashMap<String, String> previousDataHash,
                                    int repeatedCalls) {
        previousDataHash.put("duration", "-1");
        previousDataHash.put("repeatedCalls", "" + repeatedCalls);
        return newCursorDataObject(previousDataHash);
    }

    private HashMap<String, String> getDataFromCursor(Cursor cursor) {
        HashMap<String, String> values = new HashMap<>();

        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));

        String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
        String photoUri = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_PHOTO_URI));
        HashMap<String, String> val = new HashMap<>();

        if (name == null)
            val = phoneContact.getDisplayName(getActivity(), number);
        else {
            val.put("name", name);
            val.put("photoUri", photoUri);
        }


        values.put("id", cursor.getString(0));
        values.put("number", number);
        values.put("type", cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)));
        values.put("simSlot", cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)));

        values.put("name", val.get("name"));
        values.put("nameNull", val.get("name"));
        values.put("photoUri", val.get("photoUri"));
        values.put("is_read", cursor.getString(cursor.getColumnIndex(CallLog.Calls.IS_READ)));
        values.put("date", cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
        values.put("duration", cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));

        return values;
    }

    private Object[] newCursorDataObject(HashMap<String, String> values) {
        String id = values.get("id");
        String name = values.get("name");
        String photoUri = values.get("photoUri");
        String nameNull = values.get("nameNull");
        String number = values.get("number");
        String type = values.get("type");
        String date = values.get("date");
        String duration = values.get("duration");
        String simSlot = values.get("simSlot");
        String isRead = values.get("is_read");
        String repeatedCalls = values.get("repeatedCalls");

        nameNull = nameNull == null ? null : name;

        if (repeatedCalls == null || repeatedCalls.equals(""))
            return new Object[]{
                    id,
                    photoUri,
                    nameNull,
                    number,
                    type,
                    date,
                    duration,
                    simSlot,
                    isRead
            };
        else
            return new Object[]{
                    id,
                    photoUri,
                    nameNull,
                    number,
                    type,
                    date,
                    duration,
                    simSlot,
                    isRead,
                    repeatedCalls
            };
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_HISTORY) {
            boolean granted = grantResults.length > 0;
            for (int grantResult : grantResults)
                granted = granted && grantResult == PackageManager.PERMISSION_GRANTED;
            if (granted) showCallLogs();
        }
    }
}