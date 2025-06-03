package ai.picovoice.porcupine

import ai.picovoice.android.voiceprocessor.VoiceProcessor
import ai.picovoice.android.voiceprocessor.VoiceProcessorErrorListener
import ai.picovoice.android.voiceprocessor.VoiceProcessorException
import ai.picovoice.android.voiceprocessor.VoiceProcessorFrameListener
import ai.picovoice.porcupine.open.ModelBean
import android.content.Context
import android.util.Log

class OpenWakeWordManager private constructor(
    private val wakeWord: OpenWakeWord,
    callback: OpenWakeWordManagerCallback,
    errorCallback: PorcupineManagerErrorCallback?
) {
    private val voiceProcessor: VoiceProcessor = VoiceProcessor.getInstance()
    private var lastCallbackTime = 0L
    private val vpFrameListener: VoiceProcessorFrameListener =
        VoiceProcessorFrameListener { frame ->
            try {
                val keyword = wakeWord.process(frame)
                if (keyword != OpenWakeWord.BuiltInKeyword.DEFAULT) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastCallbackTime > 800){
                        lastCallbackTime = currentTime
                        callback.invoke(keyword)
                    }
                }
            } catch (e: PorcupineException) {
                errorCallback?.invoke(e) ?: Log.e("PorcupineManager", e.toString())
            }
        }
    private val vpErrorListener: VoiceProcessorErrorListener =
        VoiceProcessorErrorListener { error ->
            errorCallback?.invoke(PorcupineException(error)) ?: Log.e(
                "PorcupineManager",
                error.toString()
            )
        }
    private var isListening = false

    fun delete() {
        wakeWord.delete()
    }

    @Throws(PorcupineException::class)
    fun start() {
        if (isListening) {
            return
        }
        voiceProcessor.addFrameListener(vpFrameListener)
        voiceProcessor.addErrorListener(vpErrorListener)
        try {
            voiceProcessor.start(wakeWord.frameLength, wakeWord.sampleRate)
        } catch (e: VoiceProcessorException) {
            throw PorcupineException(e)
        }
        isListening = true
    }

    @Throws(PorcupineException::class)
    fun stop() {
        if (!isListening) {
            return
        }
        voiceProcessor.removeErrorListener(vpErrorListener)
        voiceProcessor.removeFrameListener(vpFrameListener)
        if (voiceProcessor.numFrameListeners == 0) {
            try {
                voiceProcessor.stop()
            } catch (e: VoiceProcessorException) {
                throw PorcupineException(e)
            }
        }
        isListening = false
    }

    class Builder {
        private var initKeyWords: List<ModelBean> = listOf()
        fun setKeyWords(initKeyWords: List<ModelBean>): Builder {
            this.initKeyWords = initKeyWords
            return this
        }

        fun build(
            context: Context,
            callback: OpenWakeWordManagerCallback
        ): OpenWakeWordManager {
            val wakeWord = OpenWakeWord(initKeyWords, context)
            return OpenWakeWordManager(wakeWord, callback, null)
        }
    }
}


interface OpenWakeWordManagerCallback {
    operator fun invoke(keyword: OpenWakeWord.BuiltInKeyword)
}


