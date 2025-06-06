package com.codepalace.chatbot.service

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log
import com.codepalace.chatbot.util.MyInteractionSession

class MyVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(p0: Bundle?): VoiceInteractionSession {
        Log.d(TAG, "onNewSession: ")
        return MyInteractionSession(this)
    }
}

private const val TAG = "MyVoiceInteractionSession"