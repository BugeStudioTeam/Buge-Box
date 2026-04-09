package com.buge.box.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.buge.box.core.VirtualAppEngine
import com.buge.box.data.AppInfo
import com.buge.box.data.CloneAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = VirtualAppEngine(application)
    
    private val _installedApps = MutableLiveData<List<AppInfo>>()
    val installedApps: LiveData<List<AppInfo>> = _installedApps
    
    private val _clonedApps = MutableLiveData<List<CloneAppInfo>>()
    val clonedApps: LiveData<List<CloneAppInfo>> = _clonedApps
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    init {
        loadClonedApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                engine.getInstalledApps()
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    fun loadClonedApps() {
        viewModelScope.launch {
            val clones = withContext(Dispatchers.IO) {
                engine.getAllClones()
            }
            _clonedApps.value = clones
        }
    }

    fun cloneApp(appInfo: AppInfo) {
        viewModelScope.launch {
            _isLoading.value = true
            val clone = withContext(Dispatchers.IO) {
                engine.createClone(appInfo)
            }
            
            if (clone != null) {
                val currentClones = _clonedApps.value ?: emptyList()
                _clonedApps.value = currentClones + clone
                _operationResult.value = "Clone created: ${clone.appName}"
            } else {
                _operationResult.value = "Failed to create clone"
            }
            _isLoading.value = false
        }
    }

    fun launchClone(cloneInfo: CloneAppInfo) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                engine.launchClone(cloneInfo)
            }
            
            _operationResult.value = if (success) {
                "Launching ${cloneInfo.appName}..."
            } else {
                "Failed to launch ${cloneInfo.appName}"
            }
        }
    }

    fun deleteClone(cloneInfo: CloneAppInfo) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                engine.deleteClone(cloneInfo)
            }
            
            if (success) {
                val currentClones = _clonedApps.value ?: emptyList()
                _clonedApps.value = currentClones.filter { it.id != cloneInfo.id }
                _operationResult.value = "Clone deleted"
            } else {
                _operationResult.value = "Failed to delete clone"
            }
        }
    }
}
