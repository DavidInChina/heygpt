package ai.picovoice.porcupine

import ai.picovoice.porcupine.open.ModelBean
import ai.picovoice.porcupine.open.TFWakeWordModel
import android.content.Context
import android.util.Log

class OpenWakeWord(
    initKeyWords: List<ModelBean>,
    context: Context
) {
    private var models: List<TFWakeWordModel> = listOf()

    init {
        models = initKeyWords.map { model ->
            TFWakeWordModel(context, model.fileName, model.sensitivity, model.key)
        }
    }

    fun delete() {
        models = listOf()
    }

    fun process(pcm: ShortArray?): BuiltInKeyword {
        var result = BuiltInKeyword.DEFAULT
        if (models.isEmpty()) {
            Log.e("OpenWakeWord","No models inited.")
            return  result
        }
        if (pcm == null) {
            Log.e("OpenWakeWord","Passed null frame to audio process.")
            return  result
        }

        if (pcm.size != frameLength) {
           Log.e("OpenWakeWord",
                String.format(
                    "Porcupine process requires frames of length %d. " +
                            "Received frame of size %d.", frameLength, pcm.size
                )
            )
            return  result
        }

        models.forEach { model ->
            if (model.predictVoiceStream(pcm)) {
                result = model.key
                return@forEach
            }
        }
        return result
    }

    val frameLength: Int
        get() = 1280
    val sampleRate: Int
        get() = 16000

    enum class BuiltInKeyword {
        HEY_GPT,
        DEFAULT
    }
}
