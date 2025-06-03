package ai.picovoice.porcupine.open

import ai.picovoice.porcupine.OpenWakeWord
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.util.Log
import com.dylan.micro.audio.AudioProcessor
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFWakeWordModel(
    context: Context,
    fileName: String,
    private val sensitivity: Float,
    val key: OpenWakeWord.BuiltInKeyword
) {
    private lateinit var inputTensor: Tensor
    private lateinit var outputTensor: Tensor
    private lateinit var tfWakeWordModel: Interpreter

    private val bufferQueue = FixedSizeQueue<ShortArray>(10)

    init {
        try {
            val interpreter = Interpreter(
                loadModelFile(context, fileName),
            )
            interpreter.allocateTensors()
            inputTensor = interpreter.getInputTensor(0)
            outputTensor = interpreter.getOutputTensor(0)
            tfWakeWordModel = interpreter
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val assetFileDescriptor: AssetFileDescriptor = context.assets.openFd(modelPath)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictVoiceStream(inputData: ShortArray): Boolean {
        bufferQueue.add(inputData)
        val trulyInput = bufferQueue.getAll().flatMap { it.toList() }.toShortArray()
        val currentTime = System.currentTimeMillis()
        val clip = AudioProcessor().generateFeaturesForClip(trulyInput, 16000, 0)
        val scale = 0.0390625f
        val features = clip.map { it * scale }
            .toFloatArray()
        val chunks = DataUtils.processInputData(features)
        val wakeWordProbabilities = chunks.map { input ->
            val outputBuffer = ByteBuffer.allocateDirect(outputTensor.numElements())
            outputBuffer.order(ByteOrder.nativeOrder())
            tfWakeWordModel.run(DataUtils.quantizeInputData(inputTensor, input), outputBuffer)
            val chunkResult = DataUtils.dequantizeOutputData(outputTensor, outputBuffer)[0]
            chunkResult
        }
        val result = hasConsecutiveAboveThreshold(wakeWordProbabilities,sensitivity)
        if (result) {
            Log.e("TF", "cost time:${System.currentTimeMillis() - currentTime}")
            Log.e("TF", "Current input size: ${inputData.size}")
            Log.e("TF", "Cached input size: ${trulyInput.size}")
            Log.e("TF", "Current input: ${inputData.joinToString()}")
            Log.e("TF", "Cached input: ${trulyInput.joinToString()}")
            Log.e("TF", "Prediction spectrogram: ${features.joinToString()}")
            Log.e("TF", "clip size: ${clip.size}")
            Log.e("TF", "chunks size: ${chunks.size}")
            Log.e("TF", "predict result:$wakeWordProbabilities")
            bufferQueue.clear()
        }
        return result
    }

    private fun hasConsecutiveAboveThreshold(
        probabilities: List<Float>,
        threshold: Float,
        consecutiveCount: Int = 16
    ): Boolean {
        var count = 0
        for (value in probabilities) {
            if (value > threshold) {
                count++
                if (count >= consecutiveCount) return true
            } else {
                count = 0
            }
        }
        return false
    }
}
