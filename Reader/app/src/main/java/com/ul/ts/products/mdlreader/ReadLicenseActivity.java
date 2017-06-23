package com.ul.ts.products.mdlreader;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.Category;
import com.ul.ts.products.mdlreader.data.DrivingLicence;
import com.ul.ts.products.mdlreader.data.ReadDataScript;
import com.ul.ts.products.mdlreader.utils.ByteUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.widget.Toast;

public class ReadLicenseActivity extends AbstractLicenseActivity {
    @BindView(R.id.license_firstname) TextView firstname;
    @BindView(R.id.license_surname) TextView surname;
    @BindView(R.id.license_dob) TextView dob;
    @BindView(R.id.license_PlaceOfBirth) TextView bithplace;
    @BindView(R.id.license_DateOfIssue) TextView validFrom;
    @BindView(R.id.license_DateOfExpiry) TextView validTo;
    @BindView(R.id.license_DocumentNumber) TextView licenseNumber;
    //@BindView(R.id.license_RestrictionsSummary) TextView restrictions;
    //@BindView(R.id.license_Bsn) TextView bsn;
    @BindView(R.id.license_image) ImageView headshot;
    //@BindView(R.id.license_QrCodeContents) TextView qrCodeContents;
    @BindView(R.id.license_aa) TextView activeAuth;
    @BindView(R.id.license_pa) TextView passiveAuth;
    @BindView(R.id.license_oc) TextView onlineCheck;
    @BindView(R.id.categoryList) LinearLayout categoryList;
    @BindView(R.id.view_title) ImageView view_title;
    @BindView(R.id.license_image_flag) ImageView licenseFlag;

    private final String TAG = getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, this.toString() + "::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_license);
        ButterKnife.bind(this);
    }

    @Override
    public void onResume() {
        Log.v(TAG, this.toString() + "::onResume");
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
        Log.v(TAG, this.toString() + "::onPause");
        super.onPause();
        connection.pause();

    }

    @Override
    protected void onStop() {
        Log.v(TAG, this.toString() + "::onStop");
         super.onStop();

    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, this.toString() + "::onDestroy");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, this.toString() + "::onBackPressed");
        super.onBackPressed();
        connection.shutdown();
        finish();
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

    @Override
    public void loadLicense(APDUInterface apduInterface) {
        progressDialog.dismiss();
        new LoadFullLicenceTask(this, apduInterface).execute();
    }

    private class LoadFullLicenceTask extends LoadLicenceTask {
        public LoadFullLicenceTask(AbstractLicenseActivity activity, APDUInterface apduInterface) {
            super(activity, apduInterface);
        }

        @Override
        protected DrivingLicence doInBackground(Void... params) {
            try {
                final ReadDataScript script = new ReadDataScript(apduInterface, ReadDataScript.AID_MDL);
                long t0 = System.currentTimeMillis();

                publishProgress(PROGRESS_CONNECTING);
                byte[] EFSOd = script.readFile(0x0001D, new PPC(PROGRESS_EFSOD, 512, 1, 2));
                Log.d("EFSOd", ByteUtils.bytesToHex(EFSOd));
//                byte[] EFCOM = script.readFile(0x0001E);
//                Log.d("EFCOM", ByteUtils.bytesToHex(EFCOM));
                publishProgress(PROGRESS_DG1, 2);
                byte[] DG1 = script.readFile(0x0001);
                Log.d("DG1", ByteUtils.bytesToHex(DG1));
                publishProgress(PROGRESS_DG6, 3);
                byte[] DG6 = script.readFile(0x0006, new PPC(PROGRESS_DG6, 20000, 3, 95));
                Log.d("DG6", ByteUtils.bytesToHex(DG6));
                publishProgress(PROGRESS_DG11, 96);
                byte[] DG11 = script.readFile(0x000B);
                Log.d("DG11", ByteUtils.bytesToHex(DG11));
                publishProgress(PROGRESS_DG13, 97);
                byte[] DG13 = script.readFile(0x000D);
                Log.d("DG13", ByteUtils.bytesToHex(DG13));
                byte[] DG15 = script.readFile(0x000F);
                Log.d("DG15", ByteUtils.bytesToHex(DG15));
                byte[] DG16 = script.readFile(0x0010);
                Log.d("DG16", ByteUtils.bytesToHex(DG16));
                byte[] rand = new byte[8];
                new Random().nextBytes(rand);
                Log.d("Random", ByteUtils.bytesToHex(rand));
                byte[] responseToAuth = script.internalAuthenticate(rand);
                Log.d("AA response", ByteUtils.bytesToHex(responseToAuth));

                long t1 = System.currentTimeMillis();

                final DrivingLicence licence = new DrivingLicence(DG1, DG6, DG11, DG13, DG15, DG16, EFSOd, rand, responseToAuth);
                activity.setLicence(licence);
                long t2 = System.currentTimeMillis();

                Log.d(TAG, "Card data fetched in "+(t1-t0)+"ms");
                Log.d(TAG, "License built in "+(t2-t1)+"ms");

                publishProgress(PROGRESS_DONE, 100);

                final Bitmap photobm = BitmapFactory.decodeByteArray(licence.getPhoto(), 0, licence.getPhoto().length);

                runOnUiThread(new Runnable() {
                    public void run() {

                        surname.setText(licence.getName());
                        firstname.setText(licence.getFirstNames());
                        dob.setText(licence.getDateBirth());
                        bithplace.setText(licence.getPlaceOfBirth());
                        validFrom.setText(licence.getDateOfIssue());
                        validTo.setText(licence.getDateOfExpiry());
                        licenseNumber.setText(licence.getDocumentNumber());
                        headshot.setImageBitmap(photobm);
                        //qrCodeContents.setText(licence.getMrz());
                        //bsn.setText(licence.getBSN());
                        //restrictions.setText(licence.getRestrictionAsString());

                        Log.d("response", licence.getIssuerDn());

                        if (licence.getIssuerDn().equals("CN=rdw-poc-CSCA-UL")) {
                            view_title.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_license_ul));
                            licenseFlag.setVisibility(View.VISIBLE);
                        }

                        if (licence.getIssuerDn().equals("CN=rdw-poc-CSCA-RDW")) {
                            view_title.setImageBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.title_license_nl));
                            licenseFlag.setVisibility(View.VISIBLE);
                        }

                        view_title.setVisibility(View.VISIBLE);

                        CategoryAdapter adapter = new CategoryAdapter(activity, licence.getCategories().toArray(new Category[licence.getCategories().size()]));
                        for (int i = 0; i < licence.getCategories().size(); i++) {
                            View item = adapter.getView(i, null, null);
                            categoryList.addView(item);
                        }

                        if (licence.getPassiveAuth()) {
//                        passiveAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.check));
                            passiveAuth.setText(R.string.license_pass);
                        } else {
//                        passiveAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.close));
                            passiveAuth.setText(R.string.license_fail);
                            passiveAuth.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReadLicenseActivity.this);
                                    builder.setTitle(R.string.license_authProblem);
                                    builder.setMessage(licence.getPassiveAuthIssue());
                                    builder.setPositiveButton("OK", null);
                                    builder.create().show();
                                }
                            });
                        }

						//activeAuth.setText(R.string.license_featureNotImplemented);
                        if (licence.getActiveAuth()) {
//                        activeAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.check));
                            activeAuth.setText(R.string.license_pass);
                        } else {
//                        activeAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.close));
                            activeAuth.setText(R.string.license_fail);
                            activeAuth.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReadLicenseActivity.this);
                                    builder.setTitle(R.string.license_authProblem);
                                    builder.setMessage(licence.getActiveAuthIssue());
                                    builder.setPositiveButton("OK", null);
                                    builder.create().show();
                                }
                            });
                        }
                        onlineCheck.setText(R.string.license_featureNotImplemented);
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
