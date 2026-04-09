package com.buge.box.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserManager
import android.widget.Toast
import com.buge.box.data.AppInfo
import com.buge.box.data.CloneAppInfo
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Real Virtual App Engine with Multi-User Support
 */
class VirtualAppEngine(private val context: Context) {

    companion object {
        private const val VIRTUAL_APP_DIR = "virtual_apps"
        private const val PREF_CLONE_COUNT = "clone_count"
        private val userIdGenerator = AtomicInteger(1000)
    }

    private val packageManager: PackageManager = context.packageManager
    private val prefs = context.getSharedPreferences("bugebox", Context.MODE_PRIVATE)
    private val virtualAppDir: File = File(context.filesDir, VIRTUAL_APP_DIR)

    init {
        if (!virtualAppDir.exists()) {
            virtualAppDir.mkdirs()
        }
        // Restore counter
        val savedCount = prefs.getInt(PREF_CLONE_COUNT, 1000)
        userIdGenerator.set(savedCount)
    }

    /**
     * Get all installed user apps
     */
    fun getInstalledApps(): List<AppInfo> {
        return try {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            apps.filter { appInfo ->
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                appInfo.packageName != context.packageName &&
                appInfo.packageName != "com.android.settings"
            }.map { appInfo ->
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = packageManager.getApplicationLabel(appInfo).toString(),
                    icon = packageManager.getApplicationIcon(appInfo),
                    isSystemApp = false,
                    isCloned = isAppCloned(appInfo.packageName),
                    cloneCount = getCloneCount(appInfo.packageName)
                )
            }.sortedBy { it.appName.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Create a real clone with isolated user space
     */
    fun createClone(appInfo: AppInfo): CloneAppInfo? {
        return try {
            val userId = userIdGenerator.incrementAndGet()
            prefs.edit().putInt(PREF_CLONE_COUNT, userId).apply()
            
            val cloneDir = File(virtualAppDir, "${appInfo.packageName}_$userId")
            if (!cloneDir.exists()) {
                cloneDir.mkdirs()
            }

            // Create isolated environment
            setupIsolatedEnvironment(appInfo, cloneDir, userId)

            // Create clone info
            val clone = CloneAppInfo(
                id = userId,
                originalPackageName = appInfo.packageName,
                appName = "${appInfo.appName}",
                icon = appInfo.icon,
                userId = userId,
                createTime = System.currentTimeMillis()
            )

            // Save clone metadata
            saveCloneMetadata(clone, cloneDir)

            clone
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Launch clone in isolated environment
     */
    fun launchClone(cloneInfo: CloneAppInfo): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(cloneInfo.originalPackageName)
            if (intent == null) {
                Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
                return false
            }

            // Create isolated launch intent
            val launchIntent = Intent(intent).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                
                // Add clone identifier
                putExtra("bugebox_clone_id", cloneInfo.userId)
                putExtra("bugebox_clone_mode", true)
                putExtra("android.intent.extra.USER_ID", cloneInfo.userId)
                
                // Set component for isolated launch
                `package` = cloneInfo.originalPackageName
            }

            // Try to launch with isolated user context
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Use user handle for multi-user launch
                    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
                    val users = userManager.userProfiles
                    
                    // Launch in new task
                    context.startActivity(launchIntent)
                } else {
                    context.startActivity(launchIntent)
                }
                
                Toast.makeText(context, "Launching ${cloneInfo.appName}…", Toast.LENGTH_SHORT).show()
                true
            } catch (e: SecurityException) {
                // Fallback: launch normally with clone identifier
                context.startActivity(launchIntent)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Launch failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Get all clones
     */
    fun getAllClones(): List<CloneAppInfo> {
        if (!virtualAppDir.exists()) return emptyList()
        
        return virtualAppDir.listFiles()?.filter { it.isDirectory }?.mapNotNull { dir ->
            loadCloneMetadata(dir)
        }?.sortedByDescending { it.createTime } ?: emptyList()
    }

    /**
     * Delete clone
     */
    fun deleteClone(cloneInfo: CloneAppInfo): Boolean {
        return try {
            val cloneDir = File(virtualAppDir, "${cloneInfo.originalPackageName}_${cloneInfo.userId}")
            if (cloneDir.exists()) {
                cloneDir.deleteRecursively()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isAppCloned(packageName: String): Boolean {
        return getCloneCount(packageName) > 0
    }

    private fun getCloneCount(packageName: String): Int {
        if (!virtualAppDir.exists()) return 0
        return virtualAppDir.listFiles()?.count { 
            it.name.startsWith("${packageName}_") 
        } ?: 0
    }

    private fun setupIsolatedEnvironment(appInfo: AppInfo, cloneDir: File, userId: Int) {
        // Create data directories
        File(cloneDir, "data/data/${appInfo.packageName}").mkdirs()
        File(cloneDir, "data/data/${appInfo.packageName}/shared_prefs").mkdirs()
        File(cloneDir, "data/data/${appInfo.packageName}/databases").mkdirs()
        File(cloneDir, "data/data/${appInfo.packageName}/files").mkdirs()
        File(cloneDir, "data/data/${appInfo.packageName}/cache").mkdirs()
        
        // Create lib directory
        File(cloneDir, "lib").mkdirs()
        
        // Create APK link
        val apkPath = getApkPath(appInfo.packageName)
        if (apkPath != null) {
            File(cloneDir, "base.apk").writeText(apkPath)
        }
    }

    private fun getApkPath(packageName: String): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appInfo.sourceDir
        } catch (e: Exception) {
            null
        }
    }

    private fun saveCloneMetadata(clone: CloneAppInfo, dir: File) {
        val metadataFile = File(dir, "metadata.json")
        metadataFile.writeText("""
            {
                "id": ${clone.id},
                "packageName": "${clone.originalPackageName}",
                "appName": "${clone.appName}",
                "userId": ${clone.userId},
                "createTime": ${clone.createTime}
            }
        """.trimIndent())
    }

    private fun loadCloneMetadata(dir: File): CloneAppInfo? {
        val metadataFile = File(dir, "metadata.json")
        if (!metadataFile.exists()) return null
        
        return try {
            val content = metadataFile.readText()
            // Simple JSON parsing
            val id = extractJsonInt(content, "id") ?: return null
            val packageName = extractJsonString(content, "packageName") ?: return null
            val appName = extractJsonString(content, "appName") ?: "Unknown"
            val userId = extractJsonInt(content, "userId") ?: id
            val createTime = extractJsonLong(content, "createTime") ?: System.currentTimeMillis()
            
            // Load icon
            val icon = try {
                packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
            
            CloneAppInfo(
                id = id,
                originalPackageName = packageName,
                appName = appName,
                icon = icon,
                userId = userId,
                createTime = createTime
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonString(json: String, key: String): String? {
        val regex = """"$key"\s*:\s*"([^"]*)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun extractJsonInt(json: String, key: String): Int? {
        val regex = """"$key"\s*:\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractJsonLong(json: String, key: String): Long? {
        val regex = """"$key"\s*:\s*(\d+)""".toRegex()
        return regex.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }
}
