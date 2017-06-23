package com.ul.ts.products.mdlreader;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.DrivingLicence;
import com.ul.ts.products.mdlreader.data.ReadDataScript;
import com.ul.ts.products.mdlreader.utils.ByteUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadAgeActivity extends AbstractLicenseActivity {

    private final String TAG = getClass().getName();

    @BindView(R.id.license_image) ImageView headshot;
    @BindView(R.id.ageView) Button ageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read_age);
        ButterKnife.bind(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            connection.runSetupSteps();
            connection.resume();
            connection.findPeers();
        } catch (RemoteConnectionException e) {
            Log.d(TAG, "Could not setup connection", e);
            if (e.retry) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            } else {
                fail(e.getMessage());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        connection.pause();
        connection.shutdown();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void loadLicense(APDUInterface apduInterface) {
        progressDialog.dismiss();
        new LoadAgeTask(this, apduInterface).execute();
    }

    @OnClick(R.id.license_image)
    public void licensePressed() {
        final Intent intent = new Intent(this, PictureZoomActivity.class);
        final Bundle extras = new Bundle();

        extras.putByteArray("picture", licence.getPhoto());
        intent.putExtras(extras);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, headshot, "zoom");
        startActivity(intent, options.toBundle());
    }

    private class LoadAgeTask extends LoadLicenceTask {
        public LoadAgeTask(AbstractLicenseActivity activity, APDUInterface apduInterface) {
            super(activity, apduInterface);
        }

        @Override
        protected DrivingLicence doInBackground(Void... params) {
            try {
                final ReadDataScript script = new ReadDataScript(apduInterface, ReadDataScript.AID_MDL);
                publishProgress(PROGRESS_CONNECTING);
                byte[] EFSOd = script.readFile(0x0001D, new PPC(PROGRESS_EFSOD, 512, 1, 2));
                Log.d("EFSOd", ByteUtils.bytesToHex(EFSOd));
                publishProgress(PROGRESS_DG6, 3);
                byte[] DG6 = script.readFile(0x0006, new PPC(PROGRESS_DG6, 20000, 3, 95));
                Log.d("DG6", ByteUtils.bytesToHex(DG6));
                publishProgress(PROGRESS_DG13, 97);
                byte[] DG15 = script.readFile(0x000F);
                Log.d("DG15", ByteUtils.bytesToHex(DG15));
                byte[] DG16 = script.readFile(0x0010);
                Log.d("DG16", ByteUtils.bytesToHex(DG16));
                byte[] rand = new byte[8];
                new Random().nextBytes(rand);
                Log.d("Random", ByteUtils.bytesToHex(rand));
                byte[] responseToAuth = script.internalAuthenticate(rand);
                Log.d("AA response", ByteUtils.bytesToHex(responseToAuth));

                licence = new DrivingLicence(DG6, DG15, DG16, EFSOd, rand, responseToAuth);
                activity.setLicence(licence);

                publishProgress(PROGRESS_DONE, 100);

                final Bitmap photobm = BitmapFactory.decodeByteArray(licence.getPhoto(), 0, licence.getPhoto().length);

                runOnUiThread(new Runnable() {
                    public void run() {

                        headshot.setImageBitmap(photobm);

                        int age = dataMinimizationParameter.getAgeLimit();

                        if (age != 18 && age != 21) {
                            Log.w(TAG, "Target age " + age + " not supported; falling back to 21.");
                            age = 21;
                        }

                        if (age == 18 && licence.is18()) {
                            ageView.setText(" "+age+"+ ");
                            ageView.setBackground(getDrawable(R.drawable.green_circle));
                        }
                        else if (age == 21 && licence.is21()) {
                            ageView.setText(" "+age+"+ ");
                            ageView.setBackground(getDrawable(R.drawable.green_circle));
                        }
                        else {
                            ageView.setText(" "+age+"- ");
                            ageView.setBackground(getDrawable(R.drawable.red_circle));
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error occurred:", e);
                final Exception _e = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.fail(_e.getMessage());
                    }
                });

                return null;
            }

            return licence;
        }
    }
}
