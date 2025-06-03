package ai.picovoice.porcupine.open

import android.content.Context
import android.content.res.AssetManager
import org.tensorflow.lite.Tensor
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object DataUtils {


    fun processInputData(
        spec: FloatArray,
        inputFeatureSlices: Int = 3,
        frameSize: Int = 40,
        stride: Int = 3
    ): List<FloatArray> {
        val totalFrames = spec.size / frameSize
        val chunks = mutableListOf<FloatArray>()
        var lastIndex = inputFeatureSlices
        while (lastIndex <= totalFrames) {
            val startFrame = lastIndex - inputFeatureSlices
            val startIndex = startFrame * frameSize
            val endIndex = lastIndex * frameSize

            if (endIndex <= spec.size) {
                val chunk = spec.sliceArray(startIndex until endIndex)
                chunks.add(chunk)
            }
            lastIndex += stride
        }
        return chunks
    }

    /**
     * 量化输入
     */
    fun quantizeInputData(inputTensor: Tensor, data: FloatArray): ByteBuffer {
        val inputScale = inputTensor.quantizationParams().scale
        val inputZeroPoint = inputTensor.quantizationParams().zeroPoint

        val quantizedData = ByteBuffer.allocateDirect(data.size)
        quantizedData.order(ByteOrder.nativeOrder())

        for (value in data) {
            val quantizedValue = ((value / inputScale) + inputZeroPoint).toInt().toByte()
            quantizedData.put(quantizedValue)
        }
        return quantizedData
    }

    /**
     * 反量化输出
     */
    fun dequantizeOutputData(tensor: Tensor, output: ByteBuffer): FloatArray {
        val scale = tensor.quantizationParams().scale
        val zero = tensor.quantizationParams().zeroPoint
        output.rewind()
        val result = FloatArray(tensor.numElements()) {
            val quantized = output.get().toUByte().toInt()
            val dequantized = (quantized - zero) * scale
            dequantized
        }
        return result
    }

    fun readWavFileFromAssets(context: Context, fileName: String): Pair<Int, ShortArray?>? {
        val assetManager: AssetManager = context.assets
        var sampleRate = 0
        var audioData: ShortArray? = null

        try {
            assetManager.open(fileName).use { inputStream ->
                val buffer = ByteArray(44)
                inputStream.read(buffer, 0, 44)
                sampleRate = ByteBuffer.wrap(buffer, 24, 4).order(ByteOrder.LITTLE_ENDIAN).int
                val audioBuffer = ByteArray(inputStream.available())
                inputStream.read(audioBuffer)

                audioData = ShortArray(audioBuffer.size / 2)
                ByteBuffer.wrap(audioBuffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                    .get(audioData)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return if (audioData != null) Pair(sampleRate, audioData) else null
    }

    fun chunkedShortArray(array: ShortArray, chunkSize: Int = 1280): List<ShortArray> {
        val result = mutableListOf<ShortArray>()
        var startIndex = 0
        while (startIndex < array.size) {
            val endIndex = Math.min(startIndex + chunkSize, array.size)
            val chunk = array.copyOfRange(startIndex, endIndex)
            result.add(chunk)
            startIndex += chunkSize
        }
        return result
    }
}