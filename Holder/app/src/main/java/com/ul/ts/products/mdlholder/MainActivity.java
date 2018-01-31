package com.ul.ts.products.mdlholder;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.utils.ConnectionPreference;
import com.ul.ts.products.mdlholder.utils.StorageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.transfer_settings_current) TextView currentTransferSettings;
    @BindView(R.id.main_view_valid_to) TextView validTo;
    @BindView(R.id.main_18_valid_to) TextView validTo18;
    @BindView(R.id.main_21_valid_to) TextView validTo21;
    @BindView(R.id.software_version) TextView softwareVersion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        softwareVersion.setText(
                        " v" + BuildConfig.VERSION_NAME +
                        " r" + BuildConfig.SVN_VERSION
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        // NOTE(JS): we check if a delete has been performed, if so we reset the delete state and return to the entry point
        // A delete can be performed from the settings or as a revoke from FirebaseMessaging
        Boolean deleted = StorageUtils.getBooleanPref(this, getString(R.string.license_data_deleted_key));
        if (deleted) {
            StorageUtils.removePref(this, getString(R.string.license_data_deleted_key));
            final Intent intent = new Intent(this, EntryPointActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        updateConnectionPreference();

        // Set valid to date in OnResume in the case it was set from the settings
        String validto = StorageUtils.getStringPref(this, getString(R.string.license_valid_to_key));
        if (!validto.equals("")) {
            validTo.setText(getString(R.string.main_description_valid_to)+" "+validto);
        } else {
            validTo.setText(R.string.main_description_valid_to_missing);
        }

        boolean validto18 = StorageUtils.getBooleanPref(this, getString(R.string.license_is_18_key));
        if (validto18) {
            validTo18.setText(getString(R.string.main_description_age_valid));
        } else {
            validTo18.setText(R.string.main_description_age_default);
        }

        boolean validto21 = StorageUtils.getBooleanPref(this, getString(R.string.license_is_21_key));
        if (validto21) {
            validTo21.setText(getString(R.string.main_description_age_valid));
        } else {
            validTo21.setText(R.string.main_description_age_default);
        }
    }

    private void updateConnectionPreference() {
        currentTransferSettings.setText(getCurrentConnectionText());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mdl_options_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.transfer_settings_button)
    public void changeTransferSetting() {
        ConnectionPreference.toggle(this);
        updateConnectionPreference();
    }

    @OnClick(R.id.main_view_share_icon)
    public void transfer() {
        if (checkPerso()) {
            final Intent intent = new Intent(this, TransferLicenseActivity.class);
            Bundle b = new Bundle();
            b.putInt("age", 0);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @OnClick(R.id.main_18_share_icon)
    public void transferAge18() {
        if (checkPerso()) {
            final Intent intent = new Intent(this, TransferLicenseActivity.class);
            Bundle b = new Bundle();
            b.putInt("age", 18);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @OnClick(R.id.main_21_share_icon)
    public void transferAge21() {
        if (checkPerso()) {
            final Intent intent = new Intent(this, TransferLicenseActivity.class);
            Bundle b = new Bundle();
            b.putInt("age", 21);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    @OnClick(R.id.card_view_license)
    public void viewLicense() {
        if (checkPerso()) {
            final Intent intent = new Intent(this, ViewLicenseActivity.class);
            startActivity(intent);
        }
    }

    private boolean checkPerso() {
        if (StorageUtils.getBooleanPref(this, getString(R.string.perso_complete_key))) {
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.perso_problem_title);
            builder.setMessage(R.string.perso_problem_not_done);
            builder.setPositiveButton("OK", null);
            builder.create().show();
        }

        return false;
    }

    public String getCurrentConnectionText() {
        switch(ConnectionPreference.getCurrentConnectionPreference(this)) {
            case ConnectionPreference.WIFI_DIRECT:
                return getString(R.string.connection_method_wifi_direct);
            case ConnectionPreference.BLUETOOTH:
                return getString(R.string.connection_method_bluetooth);
            case ConnectionPreference.NFC:
                return "Near-Field Communication";
            case ConnectionPreference.ONLINE:
                return "Online";
            default:
                return "Unknown";
        }
    }
}
