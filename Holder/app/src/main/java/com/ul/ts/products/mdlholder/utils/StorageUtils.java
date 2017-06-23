package com.ul.ts.products.mdlholder.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ul.ts.products.mdlholder.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class StorageUtils {

    public static String getStringPref(Context context, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(key, "");
    }

    public static void setStringPref(Context context, String key, String value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static boolean getBooleanPref(Context context, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, false);
    }

    public static void setBooleanPref(Context context, String key, boolean value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void removePref(Context context, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.apply();
    }

    public static void saveObject(Context context, String key, Object object) {
        try (ObjectOutputStream os = new ObjectOutputStream(context.getApplicationContext().openFileOutput(key, Context.MODE_PRIVATE))) {
            os.writeObject(object);
        } catch (IOException e) {
            Log.e("StorageUtils", "Error saving object", e);
        }
    }

    public static Object loadObject(Context context, String key) {
        try (ObjectInputStream os = new ObjectInputStream(context.getApplicationContext().openFileInput(key))) {
            return os.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("StorageUtils", "Error loading object", e);
        }

        return null;
    }

    public static void removeObject(Context context, String key) {
        context.getApplicationContext().deleteFile(key);
    }

    public static boolean objectExists(Context context, String key){
        File file = context.getFileStreamPath(key);
        return file.exists();
    }
}
