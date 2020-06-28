package com.thuruthuru.contacts.ui.policy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.thuruthuru.contacts.R;

public class PolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_policy);

        String policyType = getIntent().getStringExtra("policy_type");

        if(policyType.equals("privacy-policy"))
            getSupportActionBar().setTitle("Privacy Policy");
        else
            getSupportActionBar().setTitle("Terms and Condition");

        WebView web = (WebView) findViewById(R.id.webView);
        web.setWebViewClient(new WebViewClient());
        web.loadUrl("file:///android_asset/" + policyType + ".html");
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