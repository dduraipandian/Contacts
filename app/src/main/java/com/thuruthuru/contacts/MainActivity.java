package com.thuruthuru.contacts;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.thuruthuru.contacts.ui.about.About;
import com.thuruthuru.contacts.ui.settings.Setting;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String menuFragment = getIntent().getStringExtra("menu");

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_fav,
                R.id.navigation_dialer,
                R.id.navigation_history,
                R.id.navigation_contacts).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        if (menuFragment != null && menuFragment.equals("HistoryFragment"))
            navController.navigate(R.id.navigation_history);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSetting();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete_all_logs) {
            deleteAllCallLogs();
        }

        if (id == R.id.about) {
            showAbout();
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteAllCallLogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Deletion confirmation!");
        builder.setMessage("Please confirm to delete all the call log entries.");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                AlertDialog dialog = (AlertDialog) arg0;
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorBackspace, null));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorMissedCall, null));
            }
        });
        dialog.show();
    }

    void showAbout() {
        Intent intent = new Intent(this, About.class);
        startActivityForResult(intent, 0);
    }

    void showSetting() {
        Intent intent = new Intent(this, Setting.class);
        startActivityForResult(intent, 0);
    }

}