package com.buge.box

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.buge.box.util.PermissionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        
        private lateinit var permissionManager: PermissionManager

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            permissionManager = PermissionManager(requireActivity())
            
            findPreference<Preference>("grant_permissions")?.setOnPreferenceClickListener {
                requestAllPermissions()
                true
            }
            
            updatePermissionStatus()
            
            findPreference<Preference>("about")?.setOnPreferenceClickListener {
                showAboutDialog()
                true
            }
        }

        private fun requestAllPermissions() {
            permissionManager.requestAllPermissions()
            updatePermissionStatus()
        }

        private fun updatePermissionStatus() {
            val granted = permissionManager.getGrantedCount()
            val total = permissionManager.getTotalCount()
            
            findPreference<Preference>("permission_status")?.summary = 
                getString(R.string.permissions_granted, granted, total)
        }

        private fun showAboutDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.about_title))
                .setMessage(getString(R.string.about_message))
                .setPositiveButton(getString(R.string.ok), null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
