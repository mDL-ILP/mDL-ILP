package com.ul.ts.products.mdlreader;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(android.R.id.content, new SettingsFragment(), "settings_fragment").commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // the only options item is the back icon from "setDisplayHomeAsUpEnabled(true)"
        onBackPressed();
        return true;
    }
}
