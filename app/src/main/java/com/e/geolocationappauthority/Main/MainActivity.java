package com.e.geolocationappauthority.Main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.e.geolocationappauthority.R;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//Welcome name and email
        TextView txtname = (TextView) findViewById(R.id.Welcome_name);
        TextView txtemail = (TextView) findViewById(R.id.Welcome_email);
        TextView txtuserid = (TextView) findViewById(R.id.User_id);

        Intent intent = getIntent();

        String loginName = intent.getStringExtra("fullname");
        String loginEmail = intent.getStringExtra("email");
        int UserID = intent.getIntExtra("userid",0);


        txtname.setText("Welcome, " +loginName);
        txtemail.setText(loginEmail);
        txtuserid.setText(String.valueOf(UserID));

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        String title = "";
                        switch (item.getItemId()) {
                            case R.id.navigation_unapproved:
                                selectedFragment = UnapprovedFragment.newInstance();
                                title ="New Emergencies";
                                break;

                            case R.id.navigation_approved:
                                selectedFragment = ApprovedFragment.newInstance();
                                title ="Approved Emergencies";
                                break;

                            case R.id.navigation_archive:
                                selectedFragment = ArchiveFragment.newInstance();
                                title ="Archived Emergencies";
                                break;

                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.container, selectedFragment);
                        transaction.commit();
                        getSupportActionBar().setTitle(title);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_format_list_bulleted_black_24dp);
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, UnapprovedFragment.newInstance());
        transaction.commit();


        //Used to select an item programmatically
        //bottomNavigationView.getMenu().getItem(2).setChecked(true);
    }


}
