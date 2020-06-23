package com.thuruthuru.contacts.ui.history;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.thuruthuru.contacts.ui.call_details.CallDetailFragment;
import com.thuruthuru.contacts.R;

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
    };


    private static String[] PROJECTION = new String[PROJECTION_KEYS.length +
            FROM_COLUMNS.length];

    private String ORDER_BY = CallLog.Calls.DATE + " DESC ";

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;
    private static final int PERMISSION_REQUEST_READ_CALL_LOG = 0;
    private static final int PERMISSION_REQUEST_WRITE_CALL_LOG = 1;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 2;

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

    private String SELECTION = "(" + selection_display_name + " or " +
            CallLog.Calls.NUMBER + " LIKE ? )";

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

        return root;
    }

    @SuppressLint("ResourceType")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        isPermissionGrantedCallLogs();
    }

    public void onQueryTextChange(String newText) {
        searchString = !TextUtils.isEmpty(newText) ? newText : "";
        LoaderManager.getInstance(this).restartLoader(0, null, this);
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

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private boolean isPermissionGranted(@NonNull String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = getContext().getApplicationContext();
            int perm = ActivityCompat.checkSelfPermission(context, permission);

            if (perm == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{permission}, requestCode);
                return false;
            }
        } else return true;
    }

    private void isPermissionGrantedCallLogs() {
        String permission = Manifest.permission.READ_CALL_LOG;
        String w_permission = Manifest.permission.WRITE_CALL_LOG;
        String rs_permission = Manifest.permission.READ_PHONE_STATE;

        boolean granted = isPermissionGranted(permission, PERMISSION_REQUEST_READ_CALL_LOG);
        boolean w_granted = isPermissionGranted(w_permission, PERMISSION_REQUEST_WRITE_CALL_LOG);
        boolean rs_granted = isPermissionGranted(rs_permission, PERMISSION_REQUEST_READ_PHONE_STATE);
//        if (granted && w_granted && rs_granted) showCallLogs();
        if (granted) showCallLogs();
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
    public void onResume() {
        super.onResume();
        cursorAdapter.isCallGroup = isCallGrouped();
        LoaderManager.getInstance(this).restartLoader(0, null, this);
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
        return new CursorLoader(
                getActivity(),
                CallLog.Calls.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ORDER_BY
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView

        boolean isCallGroup = isCallGrouped();
        if (isCallGroup) {
            MatrixCursor newCursor = getAggregatedCursor(cursor);
            cursorAdapter.swapCursor(newCursor);
        } else {
            MatrixCursor newCursor = getNewCursor(cursor);
            cursorAdapter.swapCursor(newCursor);
        }
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
            String id = cursor.getString(0);
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            String simSlot = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            String[] values = getDisplayName(number);
            String name = values[0] != null ? values[0] : number;
            String nameNull = values[0];
            String photoUri = values[1];
            String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));

            String newEntry = name + type + simSlot;

            while (cursor.moveToNext()) {
                String number1 = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String type1 = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String simSlot1 = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));

                String[] values1 = getDisplayName(number1);
                String name1 = values1[0] != null ? values1[0] : number1;
                String currentLog = name1 + type1 + simSlot1;

                if (newEntry.equalsIgnoreCase(currentLog)) {
                    repeatedCalls++;
                } else {
                    newCursor.addRow(new Object[]{
                                    id,
                                    photoUri,
                                    nameNull == null ? null : name,
                                    number,
                                    type,
                                    date,
                                    -1,
                                    simSlot,
                                    repeatedCalls
                            }
                    );

                    id = cursor.getString(0);
                    number = number1;
                    type = type1;
                    simSlot = simSlot1;
                    values1 = getDisplayName(number1);
                    name = values1[0] != null ? values1[0] : number1;
                    nameNull = values1[0];
                    photoUri = values1[1];
                    date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                    repeatedCalls = 1;
                    newEntry = currentLog;
                }
            }
            newCursor.addRow(new Object[]{
                            id,
                            photoUri,
                            nameNull == null ? null : name,
                            number,
                            type,
                            date,
                            -1,
                            simSlot,
                            repeatedCalls
                    }
            );
        }
        return newCursor;
    }


    private MatrixCursor getNewCursor(Cursor cursor) {

        MatrixCursor newCursor = new MatrixCursor(PROJECTION); // Same projection used in loader
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String simSlot = cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
                String[] values = getDisplayName(number);
                String name = values[0] != null ? values[0] : number;
                String nameNull = values[0];
                String photoUri = values[1];
                String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

                newCursor.addRow(new Object[]{
                                id,
                                photoUri,
                                nameNull == null ? null : name,
                                number,
                                type,
                                date,
                                duration,
                                simSlot,
                        }
                );

            } while (cursor.moveToNext());
        }
        return newCursor;
    }

    public String[] getDisplayName(String number) {
        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] mPhoneNumberProjection = {
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
        };
        Cursor cur = getActivity().getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        String[] values = {null, null};
        try {
            if (cur.moveToFirst()) {
                String name = null, puri = null;
                do {
                    name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    puri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    if (name != null) break;
                } while (cur.moveToNext());
                values[0] = name;
                values[1] = puri;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return values;
    }
}