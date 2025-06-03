package com.dylan.micro.audio

/**
 * 从音频中获取特征值
 */
class AudioProcessor {

    external fun generateFeaturesForClip(
        audioData: ShortArray,
        sampleRate: Int,
        desiredLength: Int
    ): FloatArray

    companion object {
        init {
            System.loadLibrary("audio")
        }
    }
}