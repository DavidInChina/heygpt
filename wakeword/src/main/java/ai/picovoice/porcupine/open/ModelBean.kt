package ai.picovoice.porcupine.open

import ai.picovoice.porcupine.OpenWakeWord

class ModelBean(
    val fileName: String,
    val sensitivity: Float,
    val key: OpenWakeWord.BuiltInKeyword
)