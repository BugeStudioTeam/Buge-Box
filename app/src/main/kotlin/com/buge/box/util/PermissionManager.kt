package com.buge.box.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
        
        val ALL_PERMISSIONS = mutableListOf<String>().apply {
            // Storage
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            
            // Camera & Audio
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
            
            // Location
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            
            // Phone & Contacts
            add(Manifest.permission.READ_PHONE_STATE)
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.WRITE_CONTACTS)
            add(Manifest.permission.CALL_PHONE)
            
            // SMS
            add(Manifest.permission.SEND_SMS)
            add(Manifest.permission.RECEIVE_SMS)
            add(Manifest.permission.READ_SMS)
            
            // Calendar
            add(Manifest.permission.READ_CALENDAR)
            add(Manifest.permission.WRITE_CALENDAR)
            
            // Sensors
            add(Manifest.permission.BODY_SENSORS)
            
            // Notification (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            
            // Bluetooth (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }.toList()
    }

    private val sharedPreferences by lazy {
        activity.getSharedPreferences("permissions", Context.MODE_PRIVATE)
    }

    fun requestAllPermissions() {
        val permissionsToRequest = ALL_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            activity, 
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissionStatus(): Map<String, Boolean> {
        return ALL_PERMISSIONS.associateWith { checkPermission(it) }
    }

    fun getGrantedCount(): Int {
        return getPermissionStatus().count { it.value }
    }

    fun getTotalCount(): Int {
        return ALL_PERMISSIONS.size
    }

    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun isFirstTimeRequest(permission: String): Boolean {
        return sharedPreferences.getBoolean(permission, true)
    }

    fun markPermissionRequested(permission: String) {
        sharedPreferences.edit().putBoolean(permission, false).apply()
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onResult: (allGranted: Boolean, grantedList: List<String>, deniedList: List<String>) -> Unit
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = mutableListOf<String>()
            val denied = mutableListOf<String>()
            
            permissions.forEachIndexed { index, permission ->
                if (grantResults.getOrNull(index) == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission)
                } else {
                    denied.add(permission)
                }
            }
            
            onResult(denied.isEmpty(), granted, denied)
        }
    }
}
