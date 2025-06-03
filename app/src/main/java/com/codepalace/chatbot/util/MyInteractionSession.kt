package com.codepalace.chatbot.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.widget.Toast
import com.codepalace.chatbot.R
import com.codepalace.chatbot.vm.HomeViewModel
import com.common.basesdk.base.Utils

class MyInteractionSession(context: Context) : VoiceInteractionSession(context) {

    override fun onPrepareShow(args: Bundle?, showFlags: Int) {
        super.onPrepareShow(args, showFlags)
        setUiEnabled(false)
    }

    override fun onHandleAssist(state: AssistState) {
        super.onHandleAssist(state)
        try {
            startAssistantActivity(getGPTAssistantActivity())
        } catch (e: Exception) {
            Toast.makeText(
                context,
                R.string.voice_gpt_not_found,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onHandleScreenshot(screenshot: Bitmap?) {
        super.onHandleScreenshot(screenshot)
    }

    private fun getGPTAssistantActivity(): Intent {
        return Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName("com.openai.chatgpt", "com.openai.voice.assistant.AssistantActivity")
        }
    }
}