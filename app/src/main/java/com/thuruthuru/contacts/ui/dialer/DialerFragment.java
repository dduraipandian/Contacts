package com.thuruthuru.contacts.ui.dialer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.thuruthuru.contacts.MainActivity;
import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.common.phoneContact;

import java.io.StringWriter;
import java.util.HashMap;

public class DialerFragment extends Fragment {

    private EditText phoneField;
    private TextView addNew;

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 0;
    private static final int PERMISSION_REQUEST_CALL = 1;
    private static final int PERMISSION_REQUEST_RECEIVER = 2;
    private static final int REQUEST_OVERLAY_PERMISSION = 3;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dialer, container, false);

        phoneField = (EditText) root.findViewById(R.id.phoneField);
        addNew = (TextView) root.findViewById(R.id.add);

        View.OnClickListener onClickListenerTV = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneField.getText().toString();
                Intent createIntent;
                if (phoneNumber.length() > 0) {
                    createIntent = new Intent(
                            ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                            Uri.fromParts("tel", phoneNumber, null));
                    createIntent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
                } else {
                    createIntent = new Intent(Intent.ACTION_INSERT);
                    createIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                }
                startActivity(createIntent);
            }
        };

        addNew.setOnClickListener(onClickListenerTV);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                String value = btn.getText().toString();
                setNumber(value);
            }
        };

        ((LinearLayout) root.findViewById(R.id.button0)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNumber("0");
            }
        });
        ((Button) root.findViewById(R.id.button1)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button2)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button3)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button4)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button5)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button6)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button7)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button8)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.button9)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.buttonStar)).setOnClickListener(onClickListener);
        ((Button) root.findViewById(R.id.buttonHash)).setOnClickListener(onClickListener);

        ((LinearLayout) root.findViewById(R.id.button0)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setNumber("+");
                return true;
            }
        });

        ((ImageButton) root.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneField.getText().toString();

                StringWriter sw = new StringWriter();
                sw.append(phoneNumber);

                if (phoneNumber.length() > 0) {
                    phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                    phoneField.setText(phoneNumber);
                }

                if (phoneNumber.length() == 0)
                    addNew.setText("NEW");
                else
                    addNew.setText("ADD");
            }
        });

        ((ImageButton) root.findViewById(R.id.call)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean rc_granted = isPermissionGranted(Manifest.permission.CALL_PHONE, PERMISSION_REQUEST_CALL);
                if (rc_granted) callAction();
                else
                    Toast.makeText(getContext().getApplicationContext(),
                            "Permission is not provided to call.!",
                            Toast.LENGTH_SHORT).show();
            }
        });

        String[] permissions = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CONTACTS
        };

        boolean granted = true;
        for (String permission : permissions) {
            int perm = ActivityCompat.checkSelfPermission(getActivity(), permission);

            if (perm == PackageManager.PERMISSION_GRANTED)
                granted = granted && true;
            else
                granted = granted && false;
        }

        if (!granted) {
            requestPermissions(permissions, PERMISSION_REQUEST_RECEIVER);
        }

//        if (!Settings.canDrawOverlays(getActivity())) {
//            // ask for setting
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getActivity().getPackageName()));
//            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
//        }
        return root;
    }


    public void callAction() {
        String phoneNumber = phoneField.getText().toString();

        if (phoneNumber.length() == 0) {
            Context context = getContext().getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, "Phone number is empty.!", duration);
            toast.show();
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.fromParts("tel", phoneNumber, null));
            startActivity(callIntent);
        }
    }

    private void setNumber(String num) {
        String phoneNumber = phoneField.getText().toString();

        StringWriter sw = new StringWriter();
        sw.append(phoneNumber);
        sw.append(num);

        phoneField.setText(sw.toString());

        if (phoneField.getText().toString().length() == 0) {
            addNew.setText("NEW");
            addNew.setTextColor(getResources().getColor(R.color.colorBackspace, null));
        } else {
            String phoneNum = phoneField.getText().toString();
            boolean rc_granted = isPermissionGranted("Manifest.permission.READ_CONTACTS", PERMISSION_REQUEST_READ_CONTACTS);
            String name;

            if (rc_granted) {
                HashMap<String, String> val = phoneContact.getDisplayName(getActivity(), phoneNum);
                name = val.get("name");
            } else name = null;

            if (name != null)
                addNew.setText(name);
            else
                addNew.setText("ADD");
            addNew.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
        }
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CALL) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) callAction();
        }

        if (requestCode == PERMISSION_REQUEST_RECEIVER) {
            boolean granted = grantResults.length > 0;
            for (int grantResult : grantResults)
                granted = granted && grantResult == PackageManager.PERMISSION_GRANTED;
        }
    }
}