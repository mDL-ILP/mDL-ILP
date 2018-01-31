package com.ul.ts.products.mdlreader;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.ul.ts.products.mdllibrary.connection.InterchangeProfileOnline;
import com.ul.ts.products.mdlreader.data.APDUInterface;
import com.ul.ts.products.mdlreader.data.DrivingLicence;
import com.ul.ts.products.mdlreader.utils.ByteUtils;
import com.ul.ts.products.mdlreader.webapi.PartialDrivingLicense;
import com.ul.ts.products.mdlreader.webapi.WebAPI;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReadOnlineAgeActivity extends AbstractLicenseActivity {

    private final String TAG = getClass().getName();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    @BindView(R.id.license_image) ImageView headshot;
    @BindView(R.id.ageView) Button ageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read_age);
        ButterKnife.bind(this);
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
    public void loadLicense(APDUInterface APDUInterfaceNotUsed) {
        progressDialog.dismiss();
        new LoadAgeTask(this, APDUInterfaceNotUsed).execute();
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

                byte[] DG6 = dl.getEfMap().get("EF.DG6").getValue();
                Log.d("DG6", ByteUtils.bytesToHex(DG6));

                byte[] DG10 = dl.getEfMap().get("EF.DG10").getValue();
                Log.d("DG10", ByteUtils.bytesToHex(DG10));

                byte[] DG15 = dl.getEfMap().get("EF.DG15").getValue();
                Log.d("DG15", ByteUtils.bytesToHex(DG15));

                byte[] DG16 = dl.getEfMap().get("EF.DG16").getValue();
                Log.d("DG16", ByteUtils.bytesToHex(DG16));

                licence = new DrivingLicence(DG6, DG15, DG16, EFSOd);
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
