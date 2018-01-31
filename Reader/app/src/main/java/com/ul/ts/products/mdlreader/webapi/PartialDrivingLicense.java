package com.ul.ts.products.mdlreader.webapi;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ul.ts.products.mdlreader.utils.Bytes;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartialDrivingLicense {

    private List<EF> ef;

    public List<EF> getEf() {
        return ef;
    }

    public void setEf(List<EF> ef) {
        this.ef = ef;
    }

    public Map<String, EF> getEfMap() {
        HashMap<String, EF> map = new HashMap<String, EF>();
        for (EF entry: ef) {
            map.put(entry.getName(), entry);
        }
        return map;
    }


    @NonNull
    @JsonIgnore
    protected Map<Byte, byte[]> getFileContent() {
        Map<Byte, byte[]> fileContent;
        fileContent = new HashMap<>();

        for (EF file : getEf()) {
            byte[] id = Bytes.bytes(file.getId());

            byte fileID = id[id.length-1];
            fileContent.put(fileID, file.getValue());
        }
        return fileContent;
    }

    public static PartialDrivingLicense fromJson(InputStream i) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(i, PartialDrivingLicense.class);
    }
}
