package com.thuruthuru.contacts.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.thuruthuru.contacts.R;
import com.thuruthuru.contacts.ui.policy.PolicyActivity;

public class About extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.about);

        TextView rate_us = (TextView) findViewById(R.id.rate_us);
        TextView write_us = (TextView) findViewById(R.id.write_us);
        TextView share_us = (TextView) findViewById(R.id.share_us);
        TextView privacy = (TextView) findViewById(R.id.privacy);
        TextView terms = (TextView) findViewById(R.id.terms);

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

        write_us.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String subject = "Contact Dialer - Suggestion";

                Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
                selectorIntent.setData(Uri.parse("mailto:sevenishidreams@gmail.com" ));

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.setSelector( selectorIntent );

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    //TODO: Handle case where no email app is available
                }
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

        privacy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PolicyActivity.class);
                intent.putExtra("policy_type", "privacy-policy");
                startActivityForResult(intent, 0);
            }
        });

        terms.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PolicyActivity.class);
                intent.putExtra("policy_type", "terms");
                startActivityForResult(intent, 0);
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
