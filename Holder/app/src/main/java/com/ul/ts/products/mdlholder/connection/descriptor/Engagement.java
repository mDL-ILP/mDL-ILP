package com.ul.ts.products.mdlholder.connection.descriptor;

import android.graphics.Bitmap;

public interface Engagement {
    public Bitmap getQr();
    public Bitmap getQr(int size);
    public byte[] getContents();

    public boolean engageNFC();
}
