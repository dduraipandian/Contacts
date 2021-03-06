package com.thuruthuru.contacts.ui.frag;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.thuruthuru.contacts.R;

public class CustomAdapter extends SimpleCursorAdapter {

    private LayoutInflater cursorInflater;
    private Context context;
    private static Cursor cursor;
    private int layout;
    private boolean DISABLE_CALL;
    private boolean DISABLE_FAV;


    public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flag,
                         boolean DISABLE_CALL, boolean DISABLE_FAV) {
        super(context, layout, c, from, to, flag);
        cursorInflater = LayoutInflater.from(context);
        cursor = c;
        this.context = context;
        this.layout = layout;
        this.DISABLE_CALL = DISABLE_CALL;
        this.DISABLE_FAV = DISABLE_FAV;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(layout, null);
    }

    private void getCurrentCursor(View v, Cursor cursor) {
        int position = (int) v.getTag();
        cursor.moveToPosition(position);
    }

    private String getPhoneNumber() {
        String phNumber = getCursor().getString(5);
        return phNumber;
    }

    private boolean isFavorite() {
        String starred = getCursor().getString(6);
        return starred.equals("1");
    }

    private String getPhotoUri() {
        String photoUri = getCursor().getString(getCursor().getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
        return photoUri;
    }

    private int phoneTypeResource() {
        int type = getCursor().getInt(4);
        int phResource;
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                phResource = R.drawable.ic_home_black_24dp;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                phResource = R.drawable.ic_phone_android_black_24dp;
                break;
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                phResource = R.drawable.ic_business_black_24dp;
                break;
            default:
                phResource = R.drawable.ic_point_of_sale_black_24dp;
        }
        return phResource;
    }

    private View findViewById(View view, int viewId, Cursor cursor) {
        View resourceView = view.findViewById(viewId);
        resourceView.setTag(cursor.getPosition());
        return resourceView;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        // R.layout.list_row is your xml layout for each row
        super.bindView(view, context, cursor);
        ImageView callField = (ImageView) findViewById(view, R.id.call, cursor);
        ImageView profileField = (ImageView) findViewById(view, R.id.icon, cursor);
        ImageView addFavField = (ImageView) findViewById(view, R.id.addFav, cursor);
        ImageView newMessageField = (ImageView) findViewById(view, R.id.newMessage, cursor);
        ImageView phTypeImgField = (ImageView) findViewById(view, R.id.phTypeImage, cursor);

        int typeRes = phoneTypeResource();

        boolean starred = isFavorite();
        String photoUri = getPhotoUri();
        int colorR = starred ? R.color.colorFabFG : R.color.colorFabBG;
        addFavField.setColorFilter(context.getResources().getColor(colorR, null));
        phTypeImgField.setImageResource(typeRes);

        newMessageField.setTag(cursor.getPosition());
        newMessageField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentCursor(v, cursor);
                String phoneNumber = getPhoneNumber();
                composeSmsMessage(phoneNumber);
            }
        });

        if (photoUri != null && photoUri.length() > 0) {
            Uri myUri = Uri.parse(photoUri);
            profileField.setImageURI(myUri);
        } else {
            profileField.setImageResource(R.drawable.ic_person_24dp);
        }

        if (DISABLE_CALL) {
            callField.setVisibility(View.GONE);
        } else {
            callField.setClickable(true);
            callField.setTag(cursor.getPosition());
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

        if (DISABLE_FAV) {
            addFavField.setVisibility(View.GONE);
        } else {
            addFavField.setTag(cursor.getPosition());
            addFavField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPermissionGranted(v, Manifest.permission.WRITE_CONTACTS)) {
                        getCurrentCursor(v, cursor);
                        setFav(v, cursor);
                    } else
                        showToast("Please provide permissions to make call from main_menu menu.");
                }
            });
        }
    }

    private void setFav(View v, Cursor cursor) {
        boolean starred = !isFavorite();

        ContentValues values = new ContentValues();
        values.put(ContactsContract.Contacts.STARRED, starred ? 1 : 0);
        String contactKey = cursor.getString(1);

        this.context.getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,
                values, ContactsContract.Contacts.LOOKUP_KEY + "= ?", new String[]{contactKey});

        ImageView addFavField = (ImageView) v;
        int colorR = starred ? R.color.colorFabFG : R.color.colorFabBG;
        addFavField.setColorFilter(context.getResources().getColor(colorR, null));

        if (starred) showToast("Added to favorites.!");
        else showToast("Removed from favorites.!");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Context context = v.getContext().getApplicationContext();
            int perm = ActivityCompat.checkSelfPermission(context, permission);
            return perm == PackageManager.PERMISSION_GRANTED;
        } else return true;
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