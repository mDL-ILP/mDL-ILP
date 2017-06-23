package com.ul.ts.products.mdlholder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.widget.ImageView;
import android.widget.Toast;

import com.ul.ts.products.mdlholder.cardsim.MDLSim;
import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.TransferInterface;
import com.ul.ts.products.mdlholder.connection.descriptor.AgeTransferInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.FullLicenseTransferInfo;
import com.ul.ts.products.mdlholder.connection.descriptor.Engagement;
import com.ul.ts.products.mdlholder.connection.descriptor.TransferInfo;
import com.ul.ts.products.mdlholder.connection.hce.MdlApduService;
import com.ul.ts.products.mdlholder.connection.hce.ReceivingHandler;
import com.ul.ts.products.mdlholder.utils.ConnectionPreference;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransferLicenseActivity extends AbstractTransferActivity {
    @BindView(R.id.img_qr) ImageView qrImg;
    private AtomicBoolean qrLoaded = new AtomicBoolean(false);
    private TransferInterface mTransfer;
    private Intent apduService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_license);
        ButterKnife.bind(this);

        // If there's an age check then restrict access to the Simulator.
        final int age = this.getIntent().getExtras().getInt("age");
        final boolean fullAccess = age == 0;

        SecureRandom random = new SecureRandom();
        String password = new BigInteger(130, random).toString(32);

        MDLSim mdlSim = new MDLSim(this, fullAccess, password);

        TransferInfo transferInfo;
        if (fullAccess) {
            transferInfo = new FullLicenseTransferInfo(password);
        } else {
            transferInfo = new AgeTransferInfo(password, age);
        }

        try {
            mTransfer = ConnectionPreference.getTransfer(this, transferInfo, mdlSim);
        } catch (RuntimeException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTransfer.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTransfer.pause();
    }

    @Override
    public void onBackPressed() {
        if (apduService != null) {
            stopService(apduService);
        }
        super.onBackPressed();
        mTransfer.stopServer();
        finish();
    }

    /**
     * Set the Qr image in the interface, and signal waitingInterface once successful.
     */
    public void setupEngagement(final Engagement engagement, final InterfaceAsyncTask waitingInterface) {
        if (qrLoaded.compareAndSet(false, true)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            qrImg.setImageBitmap(engagement.getQr());
                            waitingInterface.dismiss();
                        }
                    });
                }
            }).start();
        }

        if (engagement.engageNFC()) {
            this.apduService = new Intent(this, MdlApduService.class);
            apduService.putExtra("type", "engagement");
            apduService.putExtra("engagementData", engagement.getContents());
            startService(apduService);
        }
    }

}
