package com.codepalace.chatbot.service

import ai.picovoice.porcupine.OpenWakeWord
import ai.picovoice.porcupine.OpenWakeWordManagerCallback
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.util.Log
import com.codepalace.chatbot.util.OpenWakeWordHandler
import com.codepalace.chatbot.util.PorcupineHandler
import com.codepalace.chatbot.vm.HomeViewModel.Companion.WAKE_WORD_KEY
import com.codepalace.chatbot.vm.HomeViewModel.Companion.WAKE_WORD_MICRO_TYPE
import com.codepalace.chatbot.vm.HomeViewModel.Companion.WAKE_WORD_READY
import com.common.basesdk.base.Utils

/**
 * Top-level service, which is providing support for hotwording.The current VoiceInteractionService that has been selected by the user is kept always
 * running by the system, to allow it to do things like listen for hotwords in the background to instigate voice interactions.
 * Because this service is always running, it should be kept as lightweight as possible. Heavy-weight operations (including showing UI) should be
 * implemented in the associated [MyVoiceInteractionSessionService] when an actual voice interaction is taking place, and that service should
 * run in a separate process from this one.
 */

class MyVoiceInteractionService : VoiceInteractionService() {

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onReady() {
        super.onReady()
        if (Utils.getBoolean(this, WAKE_WORD_READY)) {
            startPorcupine()
        }
    }

    private fun startPorcupine() {
        if (Utils.getBoolean(this, WAKE_WORD_MICRO_TYPE, true)) {
            OpenWakeWordHandler.createWakeWord(applicationContext,
                object : OpenWakeWordManagerCallback {
                    override fun invoke(keyword: OpenWakeWord.BuiltInKeyword) {
                        Log.d(TAG, "onReady: ${keyword.name}")
                        showSession(null, VoiceInteractionSession.SHOW_WITH_ASSIST)
                    }
                })
        } else {
            PorcupineHandler.createPorcupine(applicationContext) { keywordIndex: Int ->
                Log.d(TAG, "onReady: $keywordIndex")
                showSession(null, VoiceInteractionSession.SHOW_WITH_ASSIST)
            }
        }

//        OpenWakeWordHandler.testWakeWord(applicationContext,
//            object : OpenWakeWordManagerCallback {
//                override fun invoke(keyword: OpenWakeWord.BuiltInKeyword) {
//                    Log.d(TAG, "onReady: ${keyword.name}")
//                    showSession(null, VoiceInteractionSession.SHOW_WITH_ASSIST)
//                }
//            })
    }

    override fun onShutdown() {
        PorcupineHandler.release()
        OpenWakeWordHandler.release()
        super.onShutdown()
    }

    private lateinit var receiver: BroadcastReceiver

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val defaultValue = Utils.getBoolean(context, WAKE_WORD_READY)
                val newState =
                    intent?.getBooleanExtra(WAKE_WORD_STATE, defaultValue) ?: defaultValue
                if (newState != defaultValue) {
                    Utils.putBoolean(context, WAKE_WORD_READY, newState)
                }
                val beforeKey = Utils.getString(context, WAKE_WORD_KEY)
                val newKey =
                    intent?.getStringExtra(WAKE_WORD_KEY) ?: beforeKey
                if (newKey != beforeKey) {
                    Utils.putString(context, WAKE_WORD_KEY, newKey)
                }
                val beforeType = Utils.getBoolean(context, WAKE_WORD_MICRO_TYPE, true)
                val newType =
                    intent?.getBooleanExtra(WAKE_WORD_ENGINE_TYPE, beforeType) ?: beforeType
                var shouldRestart = false
                if (newType != beforeType) {
                    shouldRestart = true
                    Utils.putBoolean(context, WAKE_WORD_MICRO_TYPE, newType)
                }

                if (shouldRestart && newState) {
                    OpenWakeWordHandler.release()
                    PorcupineHandler.release()
                    startPorcupine()
                    return
                }
                if (newState) {
                    startPorcupine()
                } else {
                    OpenWakeWordHandler.release()
                    PorcupineHandler.release()
                }
            }
        }
        val filter = IntentFilter(UPDATE_WAKE_WORD_RECEIVER)
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

private const val TAG = "MyVoiceInteractionService"
const val UPDATE_WAKE_WORD_RECEIVER = "com.dylan.chatgpt.UPDATE_VOICE_SERVICE"
const val WAKE_WORD_STATE = "wake_word_state"
const val WAKE_WORD_ENGINE_TYPE = "wake_word_engine_type"