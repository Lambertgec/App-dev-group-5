package com.group5.gue;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.group5.gue.blocking.AppBlockingManager;
import com.group5.gue.data.PermissionHandler;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(AppBlockingManager.PREFS_NAME);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            findPreference(AppBlockingManager.KEY_BLOCKING_ENABLED).setOnPreferenceChangeListener((preference, newValue) -> {
                AppBlockingManager blockingManager = new AppBlockingManager(getActivity());
                if (newValue.equals(true)) {
//                    ensure permissions
                    new PermissionHandler(getActivity()).requestAppBlocking();
//                    start blocking service
                    blockingManager.startBlockingService();
                } else {
                    blockingManager.stopBlockingService();
                }
                return true;
            });
    }   }
}