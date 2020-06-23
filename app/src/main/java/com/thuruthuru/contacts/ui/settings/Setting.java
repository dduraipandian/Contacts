package com.thuruthuru.contacts.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thuruthuru.contacts.R;

public class Setting extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.settings);

//        Switch aSwitch = (Switch) findViewById(R.id.switch1);
        CheckBox phoneCheckbox = (CheckBox) findViewById(R.id.phone);
        CheckBox simCheckbox = (CheckBox) findViewById(R.id.sim);
        CheckBox emailCheckbox = (CheckBox) findViewById(R.id.email);
        CheckBox whatsppCheckbox = (CheckBox) findViewById(R.id.whatspp);

        SharedPreferences sharedPref = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

//        aSwitch.setChecked(sharedPref.getBoolean("show_contact", true));
        phoneCheckbox.setChecked(sharedPref.getBoolean("phone_contact", true));
        simCheckbox.setChecked(sharedPref.getBoolean("sim_contact", true));
        emailCheckbox.setChecked(sharedPref.getBoolean("mail_contact", true));
        whatsppCheckbox.setChecked(sharedPref.getBoolean("whatsapp_contact", false));


//        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                savePreference("show_contact", isChecked);
//            }
//        });

        phoneCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference("phone_contact", isChecked);
            }
        });

        simCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference("sim_contact", isChecked);
            }
        });

        emailCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference("mail_contact", isChecked);
            }
        });

        whatsppCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreference("whatsapp_contact", isChecked);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void savePreference(String prop, boolean isChecked){
        SharedPreferences settings = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(prop, isChecked);
        editor.apply();
    }
}
