package com.thuruthuru.contacts.ui.dialer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thuruthuru.contacts.R;

import java.io.StringWriter;

public class DialerFragment extends Fragment{

    private EditText phoneField;
    private TextView addNew;

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
                if(phoneNumber.length() > 0){
                    createIntent = new Intent(
                            ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                            Uri.fromParts("tel", phoneNumber, null));
                    createIntent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
                } else{
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
                String phoneNumber = phoneField.getText().toString();

                StringWriter sw = new StringWriter();
                sw.append(phoneNumber);
                sw.append(value);

                phoneField.setText(sw.toString());

                if(phoneField.getText().toString().length() == 0)
                    addNew.setText("NEW");
                else
                    addNew.setText("ADD");

                addNew.setTextColor(getResources().getColor(R.color.colorPrimaryDark, null));
            }
        };

        ((Button) root.findViewById(R.id.button0)).setOnClickListener(onClickListener);
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

        ((ImageButton) root.findViewById(R.id.delete)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneField.getText().toString();

                StringWriter sw = new StringWriter();
                sw.append(phoneNumber);

                if (phoneNumber != null && phoneNumber.length() > 0) {
                    phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                    phoneField.setText(phoneNumber);
                }

                if(phoneNumber.length() == 0)
                    addNew.setText("NEW");
                else
                    addNew.setText("ADD");
            }
        });

        ((ImageButton) root.findViewById(R.id.call)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPermissionGranted()) callAction();
            }

            public void callAction() {
                String phoneNumber = phoneField.getText().toString();

                if (phoneNumber.length() == 0){
                    Context context = getContext().getApplicationContext();
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, "Phone number is empty.!", duration);
                    toast.show();
                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.fromParts("tel", phoneNumber, null));
                    startActivity(callIntent);
                }
            };

            public  boolean isPermissionGranted() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Context context = getContext().getApplicationContext();
                    int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);

                    if ( permission == PackageManager.PERMISSION_GRANTED)
                        return true;
                    else {
                        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                        return false;
                    }
                }
                else return true;
            }
        });

        return root;
    }

}