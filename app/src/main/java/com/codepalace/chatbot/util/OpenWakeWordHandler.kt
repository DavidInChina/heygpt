package com.codepalace.chatbot.util

import ai.picovoice.porcupine.OpenWakeWord
import ai.picovoice.porcupine.OpenWakeWordManager
import ai.picovoice.porcupine.OpenWakeWordManagerCallback
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.open.DataUtils
import ai.picovoice.porcupine.open.ModelBean
import android.content.Context
import android.util.Log


object OpenWakeWordHandler {

    private var openWakeWordManager: OpenWakeWordManager? = null

    fun createWakeWord(
        context: Context,
        callback: OpenWakeWordManagerCallback
    ) {
        try {
            if (openWakeWordManager == null) {
                openWakeWordManager = OpenWakeWordManager.Builder()
                    .setKeyWords(
                        listOf(
                            ModelBean(
                                "hey_gpt.tflite",
                                0.96f,
                                OpenWakeWord.BuiltInKeyword.HEY_GPT
                            )
                        )
                    )
                    .build(context, callback)
                openWakeWordManager?.start()
            }
        } catch (e: PorcupineException) {
            Log.e(TAG, e.toString())
        }
    }

    fun testWakeWord(
        context: Context,
        callback: OpenWakeWordManagerCallback
    ) {
        val wakeWord = OpenWakeWord(
            listOf(
                ModelBean(
                    "hey_mycroft.tflite",
                    0.6f,
                    OpenWakeWord.BuiltInKeyword.HEY_GPT
                )
            ), context
        )
        DataUtils.readWavFileFromAssets(context, "hey_mycroft.wav")?.second?.let { samples ->
            DataUtils.chunkedShortArray(samples).forEach { chunk ->
                val result = wakeWord.process(chunk)
                if (result != OpenWakeWord.BuiltInKeyword.DEFAULT) {
                    callback.invoke(result)
                    return@let
                }
            }
        }
    }

    fun release() {
        try {
            openWakeWordManager?.apply {
                stop()
                delete()
            }
        } catch (e: PorcupineException) {
            Log.e(TAG, e.toString())
        } finally {
            openWakeWordManager = null
        }
    }

    private const val TAG = "OpenWakeWordBuilder"
}

