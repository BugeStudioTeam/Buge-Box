package com.buge.box

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.buge.box.adapter.AppListDialogAdapter
import com.buge.box.adapter.CloneAppAdapter
import com.buge.box.core.VirtualAppEngine
import com.buge.box.data.AppInfo
import com.buge.box.data.CloneAppInfo
import com.buge.box.databinding.ActivityMainBinding
import com.buge.box.databinding.DialogSelectAppBinding
import com.buge.box.util.PermissionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var engine: VirtualAppEngine
    private lateinit var permissionManager: PermissionManager
    private lateinit var cloneAdapter: CloneAppAdapter
    
    private var installedApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        engine = VirtualAppEngine(this)
        permissionManager = PermissionManager(this)
        
        setupBottomNavigation()
        setupFab()
        loadClonedApps()
        checkPermissions()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHome()
                    true
                }
                R.id.nav_settings -> {
                    showSettings()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showCloneDialog()
        }
    }

    private fun showHome() {
        // Home is default view
        binding.fragmentContainer.visibility = android.view.View.VISIBLE
        supportFragmentManager.popBackStack()
    }

    private fun showSettings() {
        startActivity(android.content.Intent(this, SettingsActivity::class.java))
    }

    private fun showCloneDialog() {
        val dialogBinding = DialogSelectAppBinding.inflate(LayoutInflater.from(this))
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Setup RecyclerView
        val appAdapter = AppListDialogAdapter { appInfo ->
            dialog.dismiss()
            startCloning(appInfo)
        }
        
        dialogBinding.appList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appAdapter
        }

        // Load apps
        dialogBinding.progressBar.visibility = android.view.View.VISIBLE
        
        CoroutineScope(Dispatchers.IO).launch {
            val apps = engine.getInstalledApps()
            installedApps = apps
            
            withContext(Dispatchers.Main) {
                dialogBinding.progressBar.visibility = android.view.View.GONE
                appAdapter.submitList(apps)
            }
        }

        // Search functionality
        dialogBinding.searchInput.doAfterTextChanged { text ->
            val query = text?.toString()?.lowercase() ?: ""
            val filtered = installedApps.filter {
                it.appName.lowercase().contains(query) ||
                it.packageName.lowercase().contains(query)
            }
            appAdapter.submitList(filtered)
        }

        dialog.show()
    }

    private fun startCloning(appInfo: AppInfo) {
        // Show progress
        val progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.cloning_in_progress))
            .setMessage(appInfo.appName)
            .setCancelable(false)
            .create()
        progressDialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            val clone = engine.createClone(appInfo)
            
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                
                if (clone != null) {
                    Snackbar.make(binding.root, getString(R.string.clone_success), Snackbar.LENGTH_SHORT).show()
                    loadClonedApps()
                } else {
                    Snackbar.make(binding.root, getString(R.string.clone_failed), Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loadClonedApps() {
        val clones = engine.getAllClones()
        
        cloneAdapter = CloneAppAdapter(
            onLaunch = { clone -> launchClone(clone) },
            onDelete = { clone -> deleteClone(clone) }
        )
        
        // Setup RecyclerView in fragment_container
        // For now, use a simple approach
        if (clones.isEmpty()) {
            // Show empty state
        } else {
            cloneAdapter.submitList(clones)
        }
    }

    private fun launchClone(clone: CloneAppInfo) {
        val success = engine.launchClone(clone)
        if (!success) {
            Snackbar.make(binding.root, getString(R.string.launch_failed), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteClone(clone: CloneAppInfo) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_confirm, clone.appName))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                if (engine.deleteClone(clone)) {
                    Snackbar.make(binding.root, getString(R.string.delete_success), Snackbar.LENGTH_SHORT).show()
                    loadClonedApps()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun checkPermissions() {
        val granted = permissionManager.getGrantedCount()
        val total = permissionManager.getTotalCount()
        
        if (granted < total / 2) {
            Snackbar.make(
                binding.root,
                "Grant permissions for full functionality",
                Snackbar.LENGTH_LONG
            ).setAction("Grant") {
                permissionManager.requestAllPermissions()
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadClonedApps()
    }
}
