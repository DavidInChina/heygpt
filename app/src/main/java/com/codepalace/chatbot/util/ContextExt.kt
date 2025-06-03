package com.codepalace.chatbot.util

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings


fun Context.isSetAsDefaultAssistant(): Boolean {
    val setting = Settings.Secure.getString(contentResolver, "assistant")
    return if (setting != null) {
        ComponentName.unflattenFromString(setting)?.packageName == this.packageName
    } else false
}

fun Context.isRunning(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
    for (task in tasks) {
        if (packageName.equals(task.baseActivity!!.packageName, ignoreCase = true)) return true
    }
    return false
}

fun Context.launchViewIntent(url: String) {
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        try {
            startActivity(this)
        } catch (_: ActivityNotFoundException) {
        } catch (_: Exception) {
        }
    }
}
