package com.ul.ts.products.mdlholder.utils.QR;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ul.ts.products.mdllibrary.connection.Utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class QRUtils {
    public static final int COLOR_DARK = Color.rgb(0x00, 0x00, 0x00);
    public static final int COLOR_LIGHT = Color.rgb(0xff, 0xff, 0xff);

    public static Bitmap getQR(final byte[] content) {
        return getQR(content, 256);
    }

    public static Bitmap getQR(final byte[] content, final int size) {
        final QRCodeWriter writer = new QRCodeWriter();
        try {
            final Map<EncodeHintType, Object> encodingHints = new HashMap<>();
            encodingHints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            encodingHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            final BitMatrix encoding = writer.encode(Utils.makeQR(content), BarcodeFormat.QR_CODE, size, size, encodingHints);
            final Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    bitmap.setPixel(i, j, encoding.get(i, j) ? COLOR_DARK : COLOR_LIGHT);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            Log.e("QRUtils", "Failed to get QR code", e);
        }
        return null;
    }
}