//
// Created by 烦啦烦啦 on 2024/6/1.
//

#include <jni.h>
#include <vector>
#include "lib/frontend.h"
#include "lib/frontend_util.h"

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_dylan_micro_audio_AudioProcessor_generateFeaturesForClip(JNIEnv *env, jobject,
                                                              jshortArray audioData,
                                                              jint sampleRate, jint desiredLength) {
    jsize length = env->GetArrayLength(audioData);
    std::vector<int16_t> audioVec(length);
    env->GetShortArrayRegion(audioData, 0, length, audioVec.data());

    struct FrontendConfig config;
    struct FrontendState state;
    config.window.size_ms = 30;
    config.window.step_size_ms = 10;
    config.filterbank.num_channels = 40;
    config.filterbank.lower_band_limit = 125.0;
    config.filterbank.upper_band_limit = 7500.0;
    config.noise_reduction.smoothing_bits = 10;
    config.noise_reduction.even_smoothing = 0.025;
    config.noise_reduction.odd_smoothing = 0.06;
    config.noise_reduction.min_signal_remaining = 0.05;
    config.pcan_gain_control.enable_pcan = 1;
    config.pcan_gain_control.strength = 0.95;
    config.pcan_gain_control.offset = 80.0;
    config.pcan_gain_control.gain_bits = 21;
    config.log_scale.enable_log = 1;
    config.log_scale.scale_shift = 6;

    FrontendPopulateState(&config, &state, sampleRate);

    std::vector<float> spectrogram;
    size_t num_samples_read = 0;

    for (int i = 0; i < length; i += config.window.step_size_ms * sampleRate / 1000) {
        if (i + config.window.size_ms * sampleRate / 1000 > length) break;

        int16_t *frame = &audioVec[i];
        size_t frame_size = config.window.size_ms * sampleRate / 1000;

        struct FrontendOutput output = FrontendProcessSamples(&state, frame, frame_size,
                                                              &num_samples_read);
        spectrogram.insert(spectrogram.end(), output.values, output.values + output.size);
    }

    FrontendFreeStateContents(&state);

    if (desiredLength > 0 && spectrogram.size() > desiredLength) {
        spectrogram.resize(desiredLength);
    }

    jfloatArray result = env->NewFloatArray(spectrogram.size());
    env->SetFloatArrayRegion(result, 0, spectrogram.size(), spectrogram.data());
    return result;
}
