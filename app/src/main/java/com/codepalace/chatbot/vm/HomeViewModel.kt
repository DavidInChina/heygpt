package com.codepalace.chatbot.vm

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.codepalace.chatbot.R
import com.codepalace.chatbot.service.UPDATE_WAKE_WORD_RECEIVER
import com.codepalace.chatbot.service.WAKE_WORD_ENGINE_TYPE
import com.codepalace.chatbot.service.WAKE_WORD_STATE
import com.codepalace.chatbot.util.isSetAsDefaultAssistant
import com.codepalace.chatbot.util.launchViewIntent
import com.common.basesdk.base.Utils

class HomeViewModel(
    private val requestPermissionLauncher: ActivityResultLauncher<String>,
    private val applicationContext: Application,
) : ViewModel() {


    var chatGptInstalled = mutableStateOf(false)
        private set

    var chatGptSettingReady = mutableStateOf(false)


    var isDefaultAssistant = mutableStateOf(false)
        private set


    var audioPermissionReady = mutableStateOf(
        false
    )

    var wakeWordReady = mutableStateOf(false)


    var isSelfWakeWordReady = mutableStateOf(true)

    var showWakeWordDialog = mutableStateOf(false)

    var accessKey = mutableStateOf("")

    init {
        refreshState()
    }

    fun refreshState() {
        chatGptInstalled.value = isPackageInstalled(CHATGPT_PKG)
        chatGptSettingReady.value = Utils.getBoolean(applicationContext, CHATGPT_SETTINGS_READY)
        isDefaultAssistant.value = applicationContext.isSetAsDefaultAssistant()

        audioPermissionReady.value = ContextCompat.checkSelfPermission(
            applicationContext, Manifest.permission.RECORD_AUDIO
        ) == 0
        wakeWordReady.value = Utils.getBoolean(applicationContext, WAKE_WORD_READY)
        isSelfWakeWordReady.value = Utils.getBoolean(applicationContext, WAKE_WORD_MICRO_TYPE, true)

        accessKey.value = Utils.getString(applicationContext, WAKE_WORD_KEY)
    }

    fun openAssistantActivity(context: Context) {
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(CHATGPT_PKG, "com.openai.voice.assistant.AssistantActivity")
        }
        try {
            context.startActivity(intent)
            Utils.putBoolean(context, CHATGPT_SETTINGS_READY, true)
        } catch (e: Exception) {
            val intent2 = Intent().apply {
                component =
                    ComponentName(CHATGPT_PKG, "com.openai.voice.assistant.AssistantActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent2)
                Utils.putBoolean(context, CHATGPT_SETTINGS_READY, true)
            } catch (e: Exception) {
                openChatGPTActivity(context)
                Utils.putBoolean(context, CHATGPT_SETTINGS_READY, false)
                Toast.makeText(
                    applicationContext, R.string.voice_gpt_not_found, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun isPackageInstalled(pkgName: String): Boolean {
        return try {
            applicationContext.packageManager.getPackageInfo(pkgName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun checkGPT(context: Context) {
        if (chatGptInstalled.value) {
            openChatGPTActivity(context)
        } else {
            downloadGPT(context)
        }
    }

    private fun openChatGPTActivity(context: Context) {
        val intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(CHATGPT_PKG, "com.openai.chatgpt.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {

        }
    }

    private fun downloadGPT(context: Context) {
        val appUrl = "https://play.google.com/store/apps/details?id=com.openai.chatgpt"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(Uri.parse(appUrl))
        intent.setPackage("com.android.vending")
        context.startActivity(intent)
    }

    fun setAppAsVoiceAssistant(context: Context) {
        val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun enableWakeWord(context: Context, enabled: Boolean, showedInput: Boolean = false) {
        if (enabled && !showedInput && !isSelfWakeWordReady.value) {
            showWakeWordDialog.value = true
            return
        }
        wakeWordReady.value = enabled
        Utils.putBoolean(applicationContext, WAKE_WORD_READY, enabled)

        val intent = Intent(UPDATE_WAKE_WORD_RECEIVER)
        intent.putExtra(WAKE_WORD_STATE, enabled)
        intent.putExtra(WAKE_WORD_KEY, Utils.getString(applicationContext, WAKE_WORD_KEY))
        intent.putExtra(WAKE_WORD_ENGINE_TYPE, isSelfWakeWordReady.value)
        context.sendBroadcast(intent)
        if (enabled) {
        }
    }

    fun setWakeWordMicroEngine(context: Context, isSelfEngine: Boolean) {
        if (!isSelfEngine) {
            enableWakeWord(context, false)
        }
        isSelfWakeWordReady.value = isSelfEngine
        Utils.putBoolean(applicationContext, WAKE_WORD_MICRO_TYPE, isSelfEngine)

        val intent = Intent(UPDATE_WAKE_WORD_RECEIVER)
        intent.putExtra(WAKE_WORD_ENGINE_TYPE, isSelfEngine)
        intent.putExtra(WAKE_WORD_STATE, wakeWordReady.value)
        intent.putExtra(WAKE_WORD_KEY, Utils.getString(applicationContext, WAKE_WORD_KEY))
        context.sendBroadcast(intent)
    }

    fun inputWakeWordKey(context: Context) {
        Utils.putString(applicationContext, WAKE_WORD_KEY, accessKey.value)
        enableWakeWord(context, enabled = true, showedInput = true)
    }

    fun toggleWakeWord(context: Context) {
        if (audioPermissionReady.value) {
            enableWakeWord(context, !wakeWordReady.value)
        } else {
            requestAudioRecord()
        }
    }

    private fun requestAudioRecord() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun doShare(context: Context) {
//        BaseSdk.doShare(context, BuildConfig.APPLICATION_ID)
    }

    fun privacyPolicy(context: Context) {
        context.launchViewIntent("")
    }

    fun emailMe(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("youremail@gmail.com"))
        }
        context.startActivity(intent)
    }

    fun watchVideo(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/2wAx2LcSJZM"))
        context.startActivity(intent)
    }

    fun getAccessKey(context: Context) {
        context.launchViewIntent("https://console.picovoice.ai/signup")
    }

    companion object {
        private const val CHATGPT_PKG = "com.openai.chatgpt"
        const val CHATGPT_SETTINGS_READY = "chatgpt_settings_ready"
        const val WAKE_WORD_READY = "wake_word_ready"
        const val WAKE_WORD_KEY = "wake_word_key"
        const val WAKE_WORD_MICRO_TYPE = "wake_word_micro_type"
    }
}