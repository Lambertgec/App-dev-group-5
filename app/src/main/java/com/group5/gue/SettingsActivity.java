package com.group5.gue;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.group5.gue.blocking.AppBlockingManager;
import com.group5.gue.data.PermissionHandler;

/**
 * SettingsActivity provides the user interface for application settings.
 * It hosts the SettingsFragment.
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * Initializes the activity and sets up the fragment container.
     * 
     * @param savedInstanceState Bundles with existing data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        
        // Load the settings fragment if it's the first time the activity is created
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        
        // Enable the 'Up' button in the action bar for navigation back to parent
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Fragment that displays the preference hierarchy.
     * Manages app blocking preferences and permissions.
     */
    public static class SettingsFragment extends PreferenceFragmentCompat {
        
        /**
         * Called during onCreate(Bundle) to supply the preferences for this fragment.
         * 
         * @param savedInstanceState bundle with existing data.
         * @param rootKey If non-null, this preference fragment should be rooted at the preference with this key.
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Set the custom shared preferences name used by the blocking manager
            getPreferenceManager().setSharedPreferencesName(AppBlockingManager.PREFS_NAME);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Listener for the app blocking toggle switch
            findPreference(AppBlockingManager.KEY_BLOCKING_ENABLED).setOnPreferenceChangeListener((preference, newValue) -> {
                AppBlockingManager blockingManager = new AppBlockingManager(getActivity());
                if (newValue.equals(true)) {
                    // If enabled, ensure necessary permissions and start the blocking service
                    new PermissionHandler(getActivity()).requestAppBlocking();
//                    start blocking service
                    blockingManager.startBlockingService();
                } else {
                    // If disabled, stop the blocking service
                    blockingManager.stopBlockingService();
                }
                return true;
            });
    }   }
}