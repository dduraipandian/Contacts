package com.thuruthuru.contacts.ui.frag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.thuruthuru.contacts.ui.common.phoneContact;
import com.thuruthuru.contacts.R;

public abstract class BaseContactsFragment extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static boolean resumeActivity = true;

    @SuppressLint("InlinedApi")
    private static final String[] FROM_COLUMNS = {
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.STARRED
    };

    private static final String order_by = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY;


    private static final String ORDER_BY = "CAST( " + order_by + " as INT) COLLATE NOCASE ASC, " + order_by + " COLLATE NOCASE ASC ";

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.STARRED
    };

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static String selection_display_name =
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";

    private String originalSELECTION = "(" + selection_display_name + " or " +
            ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? )";

    private String SELECTION = "";

    // Defines a variable for the search string
    private String searchString = "";
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {searchString, searchString};

    private static final int[] TO_IDS = {
            0,
            R.id.displayName,
            0,
            R.id.phoneNumber
    };

    private boolean DISABLE_CALL = false;
    private boolean DISABLE_FAV = false;

    // The column index for the _ID column
    private static final int CONTACT_ID_INDEX = 0;

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 0;
    private static final int PERMISSION_REQUEST_WRITE_CONTACT = 1;
    private static final int PERMISSION_REQUEST_CALL_CONTACT = 2;

    // The column index for the CONTACT_KEY column
    private static final int CONTACT_KEY_INDEX = 1;
    private static final int CONTACT_PH_NUMBER_INDEX = 5;

    // Define global mutable variables
    // Define a ListView object
    private ListView contactsList;

    // An adapter that binds the result Cursor to the ListView
    private SimpleCursorAdapter cursorAdapter;

    public boolean SEARCH_ENABLED = true;
    public boolean ONLY_FAV = false;

    public BaseContactsFragment() {
        SEARCH_ENABLED = true;
        ONLY_FAV = false;
    }

    // Called just before the Fragment displays its UI
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Always call the super method first
        super.onCreate(savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(20);
    }

    public abstract View getCustomView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState);

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = getCustomView(inflater, container, savedInstanceState);
        contactsList = (ListView) root.findViewById(R.id.contactListView);

        EditText searchField = (EditText) root.findViewById(R.id.searchField);

        if (ONLY_FAV) {
            originalSELECTION = originalSELECTION + " and " + ContactsContract.Contacts.STARRED + " = '1' ";
        }

        SELECTION = phoneContact.getSelection(getActivity(), originalSELECTION);

        if (SEARCH_ENABLED) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String searchString = s.toString();
                    onQueryTextChange(searchString);
                }

                @Override
                public void afterTextChanged(Editable searchField) {
                }
            });
        } else {
            searchField.setVisibility(View.GONE);
        }

        return root;
    }

    @SuppressLint("ResourceType")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Gets the ListView from the View list of the parent activity

        cursorAdapter = new CustomAdapter(
                getActivity(),
                R.layout.contact_item,
                null,
                FROM_COLUMNS,
                TO_IDS, 0, DISABLE_CALL, DISABLE_FAV);
        contactsList.setAdapter(cursorAdapter);
        contactsList.setOnItemClickListener(this);

        isPermissionGrantedCall();
    }

    private void isPermissionGrantedCall() {
        String write_permission = Manifest.permission.WRITE_CONTACTS;
        String read_permission = Manifest.permission.READ_CONTACTS;
        String call_permission = Manifest.permission.CALL_PHONE;

        boolean read_granted = isPermissionGranted(read_permission, PERMISSION_REQUEST_READ_CONTACTS);

        if (read_granted)
            showContacts();

        boolean write_ranted = isPermissionGranted(write_permission, PERMISSION_REQUEST_WRITE_CONTACT);
        boolean call_granted = isPermissionGranted(call_permission, PERMISSION_REQUEST_CALL_CONTACT);
    }

    public void onQueryTextChange(String newText) {
        searchString = !TextUtils.isEmpty(newText) ? newText : "";
        String permission = Manifest.permission.READ_CONTACTS;
        boolean granted = isPermissionGranted(permission, PERMISSION_REQUEST_READ_CONTACTS);
        if (granted) LoaderManager.getInstance(this).restartLoader(0, null, this);
        else
            Toast.makeText(getContext().getApplicationContext(),
                    "Permission is not provided to read contacts.!",
                    Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        SELECTION = phoneContact.getSelection(getActivity(), originalSELECTION);

        String permission = Manifest.permission.READ_CONTACTS;

        boolean granted = isPermissionGranted(permission, PERMISSION_REQUEST_READ_CONTACTS);
        if (granted) LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private void showContacts() {
        // Initializes the loader
        LoaderManager.getInstance(this).initLoader(0, null, this);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {
        Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        long contactId = cursor.getLong(CONTACT_ID_INDEX);
        String contactKey = cursor.getString(CONTACT_KEY_INDEX);

        Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(contactUri);
        startActivity(intent);
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
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                SELECTION,
                selectionArgs,
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS:
                boolean showAllContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (showAllContacts) showContacts();
                return;

            case PERMISSION_REQUEST_CALL_CONTACT:
                boolean callContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                DISABLE_CALL = !callContacts;
                return;

            case PERMISSION_REQUEST_WRITE_CONTACT:
                boolean writeContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                DISABLE_FAV = !writeContacts;
        }
    }
}
