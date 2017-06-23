package com.ul.ts.products.mdlreader;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PictureZoomActivity extends Activity {

    @BindView(R.id.license_imageZoom)
    ImageView licenseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_zoom);
        ButterKnife.bind(this);

        final byte[] picture = getIntent().getByteArrayExtra("picture");

        licenseView.setImageBitmap(BitmapFactory.decodeByteArray(picture, 0, picture.length));
    }

    @OnClick(R.id.license_imageZoom)
    void licensePressed() {
        onBackPressed();
    }

}
