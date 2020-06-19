package com.thuruthuru.contacts.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thuruthuru.contacts.R;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.about);

        TextView rate_us = (TextView) findViewById(R.id.rate_us);
        TextView share_us = (TextView) findViewById(R.id.share_us);

        rate_us.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String pkName = getPackageName();
                intent.setData(Uri.parse
                        ("market://details?id=" + pkName));
                startActivity(intent);
            }
        });

        share_us.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("smsto:"));
                String pkName = getPackageName();
                intent.putExtra("sms_body", "market://details?id=" + pkName);
                startActivity(intent);
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
}
