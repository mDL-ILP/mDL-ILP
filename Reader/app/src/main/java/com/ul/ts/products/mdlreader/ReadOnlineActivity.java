package com.ul.ts.products.mdlreader;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ul.ts.products.mdllibrary.connection.InterchangeProfileOnline;
import com.ul.ts.products.mdlreader.connection.RemoteConnectionException;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.Category;
import com.ul.ts.products.mdlreader.data.DrivingLicence;
import com.ul.ts.products.mdlreader.data.ReadDataScript;
import com.ul.ts.products.mdlreader.utils.ByteUtils;
import com.ul.ts.products.mdlreader.utils.Bytes;
import com.ul.ts.products.mdlreader.webapi.EF;
import com.ul.ts.products.mdlreader.webapi.PartialDrivingLicense;
import com.ul.ts.products.mdlreader.webapi.WebAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadOnlineActivity extends AbstractLicenseActivity {
    @BindView(R.id.license_firstname) TextView firstname;
    @BindView(R.id.license_surname) TextView surname;
    @BindView(R.id.license_dob) TextView dob;
    @BindView(R.id.license_PlaceOfBirth) TextView bithplace;
    @BindView(R.id.license_DateOfIssue) TextView validFrom;
    @BindView(R.id.license_DateOfExpiry) TextView validTo;
    @BindView(R.id.license_DocumentNumber) TextView licenseNumber;
    @BindView(R.id.license_DaysSinceUpdate) TextView daysSinceUpdate;
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


    private final ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, this.toString() + "::onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_license);
        ButterKnife.bind(this);

    }

    @Override
    public void onPause() {
        Log.v(TAG, this.toString() + "::onPause");
        super.onPause();
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
    public void loadLicense(APDUInterface APDUInterfaceNotUsed) {
        progressDialog.dismiss();
        new LoadOnlineLicenceTask(this, APDUInterfaceNotUsed).execute();
    }


    private class LoadOnlineLicenceTask extends LoadLicenceTask {
        public LoadOnlineLicenceTask(AbstractLicenseActivity activity, APDUInterface apduInterface) {
            super(activity, apduInterface);
        }

        @Override
        protected DrivingLicence doInBackground(Void... params) {
            try {
                long t0 = System.currentTimeMillis();


                // Fetch license data here from online server

                //connection.getURL();

                String url = ((InterchangeProfileOnline) deviceEngagement.getTransferInterchangeProfile()).URL;

                Callable<PartialDrivingLicense> registerTask = new WebAPI.GetLicenseTask(url);
                Future<PartialDrivingLicense> f = service.submit(registerTask);

                PartialDrivingLicense partialDrivingLicense = null;
                try {
                    partialDrivingLicense = f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }


                //Object storedLicense = PartialDrivingLicense.fromJson(getResources().openRawResource(R.raw.test_license));



                PartialDrivingLicense dl = partialDrivingLicense;


                byte[] EFSOd = dl.getEfMap().get("EF.SOd").getValue();//downloadedLicense.ef.get(1).value;
                Log.d("EFSOd", ByteUtils.bytesToHex(EFSOd));

                byte[] DG1 = dl.getEfMap().get("EF.DG1").getValue();
                Log.d("DG1", ByteUtils.bytesToHex(DG1));

                byte[] DG6 = dl.getEfMap().get("EF.DG6").getValue();
                Log.d("DG6", ByteUtils.bytesToHex(DG6));

                byte[] DG10 = dl.getEfMap().get("EF.DG10").getValue();
                Log.d("DG10", ByteUtils.bytesToHex(DG10));

                byte[] DG11 = dl.getEfMap().get("EF.DG11").getValue();
                Log.d("DG11", ByteUtils.bytesToHex(DG11));

                long t1 = System.currentTimeMillis();

                final DrivingLicence licence = new DrivingLicence(DG1, DG6, DG10, DG11, EFSOd);
                activity.setLicence(licence);
                long t2 = System.currentTimeMillis();

                Log.d(TAG, "Card data fetched in " + (t1 - t0) + "ms");
                Log.d(TAG, "License built in " + (t2 - t1) + "ms");

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
                        daysSinceUpdate.setText(licence.getDaysSinceUpdate());
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ReadOnlineActivity.this);
                                    builder.setTitle(R.string.license_authProblem);
                                    builder.setMessage(licence.getPassiveAuthIssue());
                                    builder.setPositiveButton("OK", null);
                                    builder.create().show();
                                }
                            });
                        }

                        // Todo: Fix the passiveauth for the online case!!!
                        passiveAuth.setText(R.string.license_pass);


                        activeAuth.setText(R.string.license_featureNotImplemented);
                        onlineCheck.setText(R.string.license_pass);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error occurred:", e);
                final Exception _e = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fail(_e.getMessage());
                    }
                });

                return null;
            }

            return licence;
        }

    }
}
