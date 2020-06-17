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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.thuruthuru.contacts.R;

public abstract class BaseContactsFragment extends Fragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressLint("InlinedApi")
    private static final String[] FROM_COLUMNS = {
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY :
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.STARRED
    };

    private static final String ORDER_BY = Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY :
    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;

    @SuppressLint("InlinedApi")
    private static final String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY :
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.STARRED
    };

    // Defines the text expression
    @SuppressLint("InlinedApi")
    private static String selection_display_name =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE ?" :
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?";

    private String SELECTION = "(" + selection_display_name + " or " +
            ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? )";

    // Defines a variable for the search string
    private String searchString = "";
    // Defines the array to hold values that replace the ?
    private String[] selectionArgs = {searchString, searchString};

    private static final int[] TO_IDS = {
            R.id.icon,
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
    }

    public abstract View getCustomView(LayoutInflater inflater,ViewGroup parent, Bundle savedInstanceState);

    // A UI Fragment must inflate its View
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = getCustomView(inflater, container, savedInstanceState);
        contactsList = (ListView) root.findViewById(R.id.contactListView);

        EditText searchField = (EditText) root.findViewById(R.id.searchField);


        if (ONLY_FAV) {
            SELECTION = SELECTION + " and " +
                    ContactsContract.CommonDataKinds.Phone.STARRED + " = '1' ";
        }

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
        isPermissionGrantedContacts();
        isPermissionGrantedCall();
        isPermissionGrantedContactWrite();
    }

    public void onQueryTextChange(String newText) {
        searchString = !TextUtils.isEmpty(newText) ? newText : "";
        LoaderManager.getInstance(this).restartLoader(0, null, this);
    }


    private void showContacts() {
        // Gets the ListView from the View list of the parent activity

        cursorAdapter = new CustomAdapter(
                getActivity(),
                R.layout.contact_item,
                null,
                FROM_COLUMNS,
                TO_IDS, 0, DISABLE_CALL, DISABLE_FAV);
        contactsList.setAdapter(cursorAdapter);
        contactsList.setOnItemClickListener(this);


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

    private void isPermissionGrantedContacts() {
        String permission = Manifest.permission.READ_CONTACTS;
        int requestCode = PERMISSION_REQUEST_READ_CONTACTS;
        boolean granted = isPermissionGranted(permission, requestCode);
        if (granted) showContacts();
    }


    private void isPermissionGrantedContactWrite() {
        String permission = Manifest.permission.WRITE_CONTACTS;
        boolean granted = isPermissionGranted(permission, PERMISSION_REQUEST_WRITE_CONTACT);
        DISABLE_FAV = !granted;
    }

    private void isPermissionGrantedCall() {
        String permission = Manifest.permission.CALL_PHONE;
        boolean granted = isPermissionGranted(permission, PERMISSION_REQUEST_CALL_CONTACT);
        DISABLE_CALL = !granted;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View item, int position, long rowID) {
        // Get the Cursor
        Cursor cursor = ((SimpleCursorAdapter) parent.getAdapter()).getCursor();
        // Move to the selected contact
        cursor.moveToPosition(position);
        // Get the _ID value
        long contactId = cursor.getLong(CONTACT_ID_INDEX);
        // Get the selected LOOKUP KEY
        String contactKey = cursor.getString(CONTACT_KEY_INDEX);

        // Create the contact's content Uri
        Uri contactUri = ContactsContract.Contacts.getLookupUri(contactId, contactKey);
        /*
         * You can use contactUri as the content URI for retrieving
         * the details for a contact.
         */

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

    public void onRequestPermissionsResult(int permission, String[] permissions, int[] grantResults) {
        switch (permission) {
            case PERMISSION_REQUEST_READ_CONTACTS:
                boolean showAllContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (showAllContacts) showContacts();

            case PERMISSION_REQUEST_CALL_CONTACT:
                boolean callContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (callContacts) DISABLE_CALL = false;
                else DISABLE_CALL = true;

            case PERMISSION_REQUEST_WRITE_CONTACT:
                boolean writeContacts = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (writeContacts) DISABLE_FAV = false;
                else DISABLE_FAV = true;
        }
    }
}
