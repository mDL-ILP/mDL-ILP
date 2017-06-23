package com.ul.ts.products.mdlholder.webapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ul.ts.products.mdlholder.R;
import com.ul.ts.products.mdlholder.data.DrivingLicence;
import com.ul.ts.products.mdlholder.utils.Bytes;
import com.ul.ts.products.mdlholder.utils.LicenseUtils;
import com.ul.ts.products.mdlholder.utils.StorageUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobileDrivingLicense {
    private final String TAG = getClass().getName();

    private byte[] aaprivateKey;
    private List<EffectiveFile> ef;

    public void setAaprivateKey(byte[] aaprivateKey) {
        this.aaprivateKey = aaprivateKey;
    }

    public void setEf(List<EffectiveFile> ef) {
        this.ef = ef;
    }

    public byte[] getAaprivateKey() {
        return aaprivateKey;
    }

    public List<EffectiveFile> getEf() {
        return ef;
    }

    @NonNull
    @JsonIgnore
    protected Map<Byte, byte[]> getFileContent() {
        Map<Byte, byte[]> fileContent;
        fileContent = new HashMap<>();

        for (EffectiveFile file : getEf()) {
            byte[] id = Bytes.bytes(file.getID());

            byte fileID = id[id.length-1];
            fileContent.put(fileID, file.getValue());
        }
        return fileContent;
    }

    public static MobileDrivingLicense fromJson(InputStream i) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(i, MobileDrivingLicense.class);
    }

    public void store(Context context) {
        // Save the license data
        StorageUtils.saveObject(context, context.getString(R.string.data_key), getFileContent());

        Log.d(TAG, "loading aa private key:");
        if (getAaprivateKey() != null) {
            StorageUtils.saveObject(context, context.getString(R.string.aaprivatekey_key), getAaprivateKey());
            Log.d(TAG, "loaded aa private key.");

        }
        //StorageUtils.saveObject(context, "mdlpoc.aaprivate.key", null);

        try {
            DrivingLicence licence = LicenseUtils.getLicense(context);

            if (licence.is18()) {
                StorageUtils.setBooleanPref(context, context.getString(R.string.license_is_18_key), true);
            }
            if (!licence.is18()) {
                StorageUtils.setBooleanPref(context, context.getString(R.string.license_is_18_key), false);
            }
            if (licence.is21()) {
                StorageUtils.setBooleanPref(context, context.getString(R.string.license_is_21_key), true);
            }
            if (!licence.is21()) {
                StorageUtils.setBooleanPref(context, context.getString(R.string.license_is_21_key), false);
            }

            StorageUtils.setStringPref(context, context.getString(R.string.license_valid_to_key), licence.getDateOfExpiry());

        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        // Save last updated date
        DateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StorageUtils.setStringPref(context, context.getString(R.string.license_last_updated_key), dt.format(new Date()));

        StorageUtils.setBooleanPref(context, context.getString(R.string.perso_complete_key), true);
    }
}
