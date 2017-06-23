package com.ul.ts.products.mdlreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.buttonQR) Button buttonQR;
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

    @OnClick(R.id.buttonQR)
    public void QRButtonClicked() {

        // check camera permission and start activity
        final boolean allowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        if (allowed) {
            startActivity(new Intent(this, ContinuousCaptureActivity.class));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
        }
        else {
            Toast.makeText(this, "Camera permission disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.buttonNFC)
    public void NFCButtonClicked() {
        // check camera permission and start activity
        final boolean allowed = ContextCompat.checkSelfPermission(this, Manifest.permission.NFC) == PackageManager.PERMISSION_GRANTED;
        if (allowed) {
            startActivity(new Intent(this, NFCTagActivity.class));
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.NFC}, 0);
        }
        else {
            Toast.makeText(this, "NFC permission disabled", Toast.LENGTH_SHORT).show();
        }
    }

}
