package com.codepalace.chatbot.util

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.Porcupine.Builder
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import ai.picovoice.porcupine.PorcupineException
import android.content.Context
import android.util.Log
import com.codepalace.chatbot.vm.HomeViewModel.Companion.WAKE_WORD_KEY
import com.common.basesdk.base.Utils
import com.dylan.wakeword.BuildConfig


object PorcupineHandler {

    private var porcupineManager: PorcupineManager? = null

    fun createPorcupine(
        context: Context,
        callback: PorcupineManagerCallback
    ) {
        try {
            if (porcupineManager == null) {
                val key = if (BuildConfig.DEBUG) {
                    DEBUG_ACCESS_KEY
                } else {
                    Utils.getString(context.applicationContext, WAKE_WORD_KEY)
                }
                porcupineManager = PorcupineManager.Builder()
                    .setAccessKey(key)
                    .setKeywords(
                        arrayOf(
                            Porcupine.BuiltInKeyword.HEY_GOOGLE,
                            Porcupine.BuiltInKeyword.JARVIS,
                            Porcupine.BuiltInKeyword.OK_GOOGLE,
                        )
                    )
                    .setSensitivities(floatArrayOf(0.7f, 0.7f, 0.7f))
                    .build(context, callback)
                porcupineManager?.start()
            }
        } catch (e: PorcupineException) {
            Log.e(TAG, e.toString())
        }
    }

    fun release() {
        try {
            porcupineManager?.apply {
                stop()
                delete()
            }
        } catch (e: PorcupineException) {
            Log.e(TAG, e.toString())
        } finally {
            porcupineManager = null
        }
    }

    private const val DEBUG_ACCESS_KEY = "your porcupine key"
    private const val TAG = "PorcupineBuilder"
}

