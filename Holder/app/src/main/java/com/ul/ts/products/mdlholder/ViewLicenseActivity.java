package com.ul.ts.products.mdlholder;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.data.Category;
import com.ul.ts.products.mdlholder.data.DrivingLicence;
import com.ul.ts.products.mdlholder.utils.LicenseUtils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewLicenseActivity extends AppCompatActivity {

    @BindView(R.id.license_firstname) TextView firstname;
    @BindView(R.id.license_surname) TextView surname;
    @BindView(R.id.license_dob) TextView dob;
    @BindView(R.id.license_PlaceOfBirth) TextView bithplace;
    @BindView(R.id.license_DateOfIssue) TextView validFrom;
    @BindView(R.id.license_DateOfExpiry) TextView validTo;
    @BindView(R.id.license_IssuingAuthority) TextView issuedBy;
    @BindView(R.id.license_DocumentNumber) TextView licenseNumber;
    @BindView(R.id.license_DaysSinceUpdate) TextView daysSinceUpdate;
    //@BindView(R.id.license_RestrictionsSummary) TextView restrictions;
    //@BindView(R.id.license_Bsn) TextView bsn;
    @BindView(R.id.license_image) ImageView headshot;
    @BindView(R.id.license_aa) TextView activeAuth;
    @BindView(R.id.license_pa) TextView passiveAuth;
    @BindView(R.id.categoryList) LinearLayout categoryList;
    @BindView(R.id.view_title) ImageView view_title;
    @BindView(R.id.license_image_flag) ImageView licenseFlag;

    private DrivingLicence licence;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_license);
        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        context = this;

        try {
            licence = LicenseUtils.getLicense(this);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to get license information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateUI();
    }

    private void populateUI() {
        final Bitmap photobm = BitmapFactory.decodeByteArray(licence.getPhoto(), 0, licence.getPhoto().length);

        runOnUiThread(new Runnable() {
            public void run() {

            surname.setText(licence.getName());
            firstname.setText(licence.getFirstNames());
            dob.setText(licence.getDateBirth());
            bithplace.setText(licence.getPlaceOfBirth());
            validFrom.setText(licence.getDateOfIssue());
            validTo.setText(licence.getDateOfExpiry());
            issuedBy.setText(licence.getIssuingAuthority());
            licenseNumber.setText(licence.getDocumentNumber());
            daysSinceUpdate.setText(licence.getDaysSinceUpdate());
            headshot.setImageBitmap(photobm);

            //qrCodeContents.setText(licence.getMrz());
            //bsn.setText(licence.getBSN());
            //restrictions.setText(licence.getRestrictionAsString());


                if (licence.getIssuerDn().equals("CN=rdw-poc-CSCA-UL")) {
                    view_title.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.title_license_ul));
                    licenseFlag.setVisibility(View.VISIBLE);
                }
                if (licence.getIssuerDn().equals("CN=rdw-poc-CSCA-RDW")) {
                    view_title.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.title_license_nl));
                    licenseFlag.setVisibility(View.VISIBLE);
                }

            CategoryAdapter adapter = new CategoryAdapter(context, licence.getCategories().toArray(new Category[licence.getCategories().size()]));
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewLicenseActivity.this);
                        builder.setTitle(R.string.license_authProblem);
                        builder.setMessage(licence.getPassiveAuthIssue());
                        builder.setPositiveButton("OK", null);
                        builder.create().show();
                    }
                });
            }


                //activeAuth.setText(R.string.license_pass);
            if (licence.getActiveAuth()) {
                //activeAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.check));
                activeAuth.setText(R.string.license_pass);
            } else {
                //activeAuth.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.close));
                activeAuth.setText(R.string.license_fail);
                activeAuth.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ViewLicenseActivity.this);
                        builder.setTitle(R.string.license_authProblem);
                        builder.setMessage(licence.getActiveAuthIssue());
                        builder.setPositiveButton("OK", null);
                        builder.create().show();
                    }
                });
            }
            }
        });
    }

    @OnClick(R.id.license_image)
    void licensePressed() {

        final Intent intent = new Intent(this, PictureZoomActivity.class);
        final Bundle extras = new Bundle();
        extras.putByteArray("picture", licence.getPhoto());
        intent.putExtras(extras);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, headshot, "zoom");
        startActivity(intent, options.toBundle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // the only options item is the back icon from "setDisplayHomeAsUpEnabled(true)"
        onBackPressed();
        return true;
    }
}
