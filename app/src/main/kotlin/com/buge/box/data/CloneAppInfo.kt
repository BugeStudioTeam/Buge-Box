package com.buge.box.data

import android.graphics.drawable.Drawable

/**
 * 克隆应用信息
 */
data class CloneAppInfo(
    val id: Int,
    val originalPackageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val userId: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)
