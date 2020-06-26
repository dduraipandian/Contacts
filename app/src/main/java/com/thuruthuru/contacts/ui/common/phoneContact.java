package com.thuruthuru.contacts.ui.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.HashMap;

public class phoneContact {

    public static String getSelection(Context context, String OriginalSELECTION) {
        boolean showContact;
        boolean phoneContact;
        boolean simContact;
        boolean mailContact;
        boolean whatsAppContact;

        SharedPreferences sharedPref = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

        showContact = sharedPref.getBoolean("show_contact", true);
        phoneContact = sharedPref.getBoolean("phone_contact", true);
        simContact = sharedPref.getBoolean("sim_contact", true);
        mailContact = sharedPref.getBoolean("mail_contact", true);
        whatsAppContact = sharedPref.getBoolean("whatsapp_contact", false);

        String SELECTION = OriginalSELECTION;
        SELECTION += !SELECTION.equals("") ? " and " : " ";

        if (showContact) {
            SELECTION = SELECTION + ContactsContract.Contacts.HAS_PHONE_NUMBER + " != 0 and " +
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " is not null ";
        } else {
            SELECTION = SELECTION + " (" + ContactsContract.Contacts.HAS_PHONE_NUMBER + " >= 0 or " +
                    ContactsContract.Contacts.HAS_PHONE_NUMBER + " is null ) ";
        }

        String phoneSelection = "";

        if (phoneContact) {
            phoneSelection = phoneSelection + ContactsContract.RawContacts.ACCOUNT_TYPE + " like '%account.phone' ";
        }

        if (simContact) {
            phoneSelection += !phoneSelection.equals("") ? " or " : "";
            phoneSelection = phoneSelection + ContactsContract.RawContacts.ACCOUNT_TYPE + " like '%account.usim' ";
        }

        if (mailContact) {
            phoneSelection += !phoneSelection.equals("") ? " or " : "";
            phoneSelection = phoneSelection + ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.google' ";
        }

        if (whatsAppContact) {
            phoneSelection += !phoneSelection.equals("") ? " or " : "";
            phoneSelection = phoneSelection + ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.whatsapp' ";
        }

        if (!phoneSelection.equals("")) {
            SELECTION = SELECTION + " and (" + phoneSelection + ")";
        }

        return SELECTION;
    }

    public static HashMap<String, String> getDisplayName(Context context, String number) {
        /// number is the phone number

        HashMap<String, String> values = new HashMap<>();
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] mPhoneNumberProjection = {
                ContactsContract.PhoneLookup._ID,
                ContactsContract.PhoneLookup.LOOKUP_KEY,
                ContactsContract.PhoneLookup.NUMBER,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        };

        try (Cursor cur = context.getContentResolver().query(lookupUri,
                mPhoneNumberProjection,
                null,
                null,
                "raw_contacts." + ContactsContract.PhoneLookup.STARRED + " DESC ")) {
            if (cur.moveToFirst()) {
                String displayName = null, photoUri = null, uri = null;
                do {
                    displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    photoUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));

                    long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String key = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    uri = ContactsContract.Contacts.getLookupUri(id, key).toString();

                    if (displayName != null) break;
                } while (cur.moveToNext());
                values.put("name", displayName);
                values.put("photoUri", photoUri);
                values.put("contactUri", uri);
            }
        }
        return values;
    }
}
