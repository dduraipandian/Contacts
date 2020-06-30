package com.thuruthuru.contacts.ui.call_details;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.common.phoneContact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class CallDetailFragment extends BottomSheetDialogFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION_KEYS = {
            CallLog.Calls._ID
    };

    private static final String[] FROM_COLUMNS = {
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.PHONE_ACCOUNT_ID,
    };

    private static final int[] TO_IDS = {
            0,
            R.id.callTime,
            0,
            0
    };


    private static String[] PROJECTION = new String[PROJECTION_KEYS.length +
            FROM_COLUMNS.length];

    private String ORDER_BY = CallLog.Calls.DATE + " DESC ";


    // Defines a variable for the search string
    private String searchString = "";
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {searchString, searchString};

    // Define global mutable variables
    // Define a ListView object
    private ListView BottomSheetListView;

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter cursorAdapter;

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static String selection_display_name = CallLog.Calls.CACHED_NAME + " LIKE ?";

    private String SELECTION = "(" + selection_display_name + " or " +
            CallLog.Calls.NUMBER + " LIKE ? )";

    private BottomSheetBehavior mBehavior;
    private BottomSheetListView callList;
    private String phoneNumber;
    private int simSlots;
    private String userName = null;
    private String photoUri = null;

    public CallDetailFragment(String phoneNumber, int simSlots) {
        this.phoneNumber = phoneNumber;
        this.simSlots = simSlots;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        final View view = View.inflate(getContext(), R.layout.fragment_call_details, null);

        TextView title = (TextView) view.findViewById(R.id.title);

        HashMap<String, String> values = phoneContact.getDisplayName(getActivity(), phoneNumber);

        this.userName = values.get("name");
        this.photoUri = values.get("photoUri");

        int deviceNum = 0;
        try {
            Cursor cursor = cursorAdapter.getCursor();
            long phoneAccount = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID));
            if (phoneAccount == -1) deviceNum = 0;
        } catch (Exception e) {
            deviceNum = 0;
        } finally {
            deviceNum = deviceNum + 1;
        }

        if (simSlots > 1)
            title.setText("SIM " + deviceNum + ": " + this.phoneNumber);
        else title.setText(this.phoneNumber);

//        Bundle extras = this.get;
//        String phoneNumber = extras.getString("phoneNumber");

        List<String> list = new ArrayList<String>(Arrays.asList(PROJECTION_KEYS)); //returns a list view of an array
        list.addAll(Arrays.asList(FROM_COLUMNS));
        list.toArray(PROJECTION);

        callList = (BottomSheetListView) view.findViewById(R.id.callDetailsListView);
        TextView userName = (TextView) view.findViewById(R.id.userName);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);

        CardView addContactField = (CardView) view.findViewById(R.id.addContact);
        ImageView addContactIconField = (ImageView) view.findViewById(R.id.addContactIcon);
        CardView newMessageField = (CardView) view.findViewById(R.id.newMessage);
        CardView callField = (CardView) view.findViewById(R.id.call);
        CardView deleteField = (CardView) view.findViewById(R.id.delete);

        if (this.userName == null || this.userName.length() == 0) {
            // userName.setText(this.phoneNumber);
            userName.setVisibility(View.GONE);
        } else {
            userName.setText(this.userName);
            userName.setVisibility(View.VISIBLE);
            addContactIconField.setImageResource(R.drawable.ic_edit_black_24dp);
        }

        if (this.photoUri != null && this.photoUri.length() > 0) {
            icon.setImageURI(Uri.parse(this.photoUri));
        }

        dialog.setContentView(view);
        mBehavior = BottomSheetBehavior.from((View) view.getParent());
        mBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    // View is expended
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    // View is collapsed
                }

                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });


        String selectedContactUri = values.get("contactUri");
        addContactField.setTag(selectedContactUri);
        addContactField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedContactUri = (String) v.getTag();
                addContact(selectedContactUri);
            }
        });
        newMessageField.setTag(this.phoneNumber);
        newMessageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phNumber = (String) v.getTag();
                composeSmsMessage(phNumber);
            }
        });

        callField.setTag(this.phoneNumber);
        callField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phNumber = (String) v.getTag();
                call(phNumber);
            }
        });

        deleteField.setTag(this.phoneNumber);
        deleteField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phNumber = (String) v.getTag();
                deleteAllCallLog(phNumber);
            }
        });

        showCallDetailsLogs();
        return dialog;
    }

    private void call(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.fromParts("tel", phoneNumber, null));
        startActivity(callIntent);
    }

    private void composeSmsMessage(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("smsto:" + phoneNumber)); // This ensures only SMS apps respond
        startActivity(intent);
    }

    public void deleteAllCallLog(final String number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        builder.setTitle("Deletion confirmation!");
        builder.setMessage("Please confirm to delete all the call log entries for " + number + ".");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getContentResolver().delete(CallLog.Calls.CONTENT_URI,
                                CallLog.Calls.NUMBER + " = ? ", new String[]{number});
                        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                AlertDialog dialog = (AlertDialog) arg0;
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBackspace, null));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorMissedCall, null));
            }
        });
        dialog.show();
    }

    private void addContact(String selectedContactUri) {

        if (phoneNumber.length() > 0) {

            Intent createIntent;

            if (selectedContactUri != null) {
                createIntent = new Intent(Intent.ACTION_EDIT);
                createIntent.setDataAndType(Uri.parse(selectedContactUri), ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            } else {
                createIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
                createIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                createIntent.putExtra("phone", phoneNumber);
            }

            startActivity(createIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void showCallDetailsLogs() {
        // Gets the ListView from the View list of the parent activity

        cursorAdapter = new CustomAdapter(
                getActivity(),
                R.layout.contact_details_item,
                null,
                FROM_COLUMNS,
                TO_IDS, 0);
        callList.setAdapter(cursorAdapter);

        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        /*
         * Makes search string into pattern and
         * stores it in the selection array
         */

        Uri lookupUri = Uri.withAppendedPath(CallLog.Calls.CONTENT_FILTER_URI,
                Uri.encode(this.phoneNumber));

        // Starts the query
        return new CursorLoader(
                getActivity(),
                lookupUri,
                PROJECTION,
                null,
                null,
                ORDER_BY
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Put the result Cursor in the adapter for the ListView
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Delete the reference to the existing Cursor
        cursorAdapter.swapCursor(null);

    }
}