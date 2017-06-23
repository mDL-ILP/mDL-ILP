package com.ul.ts.products.mdlholder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.webkit.WebView;
import android.widget.EditText;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ul.ts.products.mdlholder.utils.StorageUtils;
import com.ul.ts.products.mdlholder.webapi.WebAPI;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getName();

    private ExecutorService service = Executors.newSingleThreadExecutor();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);

        final Preference update = findPreference("updateLicenceButton");
        updateDownloadSummary(update);
        update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final WebAPI.DownloadLicenseDataTask task = new WebAPI.DownloadLicenseDataTask(getActivity());
                task.execute();

                // NOTE(JS): Handle the license download completing and update UI
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.get();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDownloadSummary(update);
                                }
                            });
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    }
                }).start();

                return true;
            }
        });

        final Preference pin = findPreference("changePinButton");
        pin.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), PinEntryActivity.class);
                intent.putExtra(AppLock.EXTRA_TYPE, AppLock.CHANGE_PIN);
                startActivity(intent);
                return true;
            }
        });

        final Preference transfer = findPreference("transferLicenseButton");
        transfer.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Ask user to input Transfer id

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.settings_transfer_title);

                final EditText input = new EditText(getContext());
                input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(5)});

                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Future<Boolean> permitFuture = service.submit(new WebAPI.PermitTransferTask(input.getText().toString()));

                        try {
                            Boolean permit = permitFuture.get();

                            if (permit != null && permit) {
                                // Successfully transferred, delete data
                                deleteAllData(getContext());
                            }

                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(getClass().getName(), e.getMessage(), e);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();

                return true;
            }
        });

        final Preference delete = findPreference("deleteLicenseButton");
        delete.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.settings_delete_confirm_title);
                builder.setMessage(R.string.settings_delete_confirm_body);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Future<Boolean> revokeFuture = service.submit(new WebAPI.RevokeTask());

                        try {
                            Boolean revoke = revokeFuture.get();

                            if (revoke != null && revoke) {
                                deleteAllData(getContext());
                                getActivity().finish();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.settings_delete_remote_failure_title);
                                builder.setMessage(R.string.settings_delete_remote_failure_body);
                                builder.setPositiveButton("OK", null);
                                builder.create().show();
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(getClass().getName(), e.getMessage(), e);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();

                return true;
            }
        });


        final Preference licenses = findPreference("showLicensesButton");
        licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final WebView view = (WebView) LayoutInflater.from(getContext()).inflate(R.layout.dialog_licenses, null);
                view.loadUrl("file:///android_res/raw/licenses.html");
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.settings_licenses_title))
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();

                return true;
            }
        });
    }

    public static void deleteAllData(Context context) {
        StorageUtils.setBooleanPref(context, context.getString(R.string.license_data_deleted_key), true);
        StorageUtils.removeObject(context, context.getString(R.string.data_key));
        StorageUtils.removeObject(context, context.getString(R.string.aaprivatekey_key));
        StorageUtils.removeObject(context, context.getString(R.string.tls_client_cert_key));
        StorageUtils.removePref(context, context.getString(R.string.perso_complete_key));
        StorageUtils.removePref(context, context.getString(R.string.license_last_updated_key));
        StorageUtils.removePref(context, context.getString(R.string.license_is_18_key));
        StorageUtils.removePref(context, context.getString(R.string.license_is_21_key));
        StorageUtils.removePref(context, context.getString(R.string.license_valid_to_key));

        try {
            KeyStore keystore = KeyStore.getInstance("AndroidKeyStore");
            keystore.load(null);
            keystore.deleteEntry(context.getString(R.string.key_key));
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        LockManager.getInstance().getAppLock().disableAndRemoveConfiguration();
        LockManager.getInstance().getAppLock().enable();

        try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private void updateDownloadSummary(Preference update) {
        update.setSummary(getString(R.string.settings_update_summary));
        String date = StorageUtils.getStringPref(getActivity(), getString(R.string.license_last_updated_key));
        if (!date.equals("")) {
            update.setSummary(update.getSummary()+" "+getString(R.string.settings_update_summary_extra)+" "+date);
        }
    }
}
