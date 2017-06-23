package com.ul.ts.products.mdlholder;

import android.support.v7.app.AppCompatActivity;

import com.ul.ts.products.mdlholder.connection.InterfaceAsyncTask;
import com.ul.ts.products.mdlholder.connection.descriptor.Engagement;

public abstract class AbstractTransferActivity extends AppCompatActivity {
    public abstract void setupEngagement(final Engagement qr, final InterfaceAsyncTask mCondition);
}

